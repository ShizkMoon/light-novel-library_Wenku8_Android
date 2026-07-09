@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.nostra13.universalimageloader.core.ImageLoader
import java.io.File
import java.io.FileNotFoundException
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import org.mewx.wenku8.R
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.async.CheckAppNewVersion
import org.mewx.wenku8.async.UpdateNotificationMessage
import org.mewx.wenku8.fragment.NavigationDrawerFragment
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.network.LightUserSession
import org.mewx.wenku8.util.GoogleServicesHelper
import org.mewx.wenku8.util.LightCache
import org.mewx.wenku8.util.ProgressDialogHelper
import org.mewx.wenku8.util.SaveFileMigration
import org.mewx.wenku8.util.SaveMigrationDirectorySelection
import org.mewx.wenku8.util.SaveMigrationStartAction
import org.mewx.wenku8.util.SaveMigrationStartDecision

class MainActivity : BaseMaterialActivity() {
    enum class FragmentMenuOption {
        RKLIST,
        LATEST,
        FAV,
        CONFIG,
    }

    var currentFragment: FragmentMenuOption = FragmentMenuOption.LATEST

    private var navigationDrawerFragment: NavigationDrawerFragment? = null
    private var firebaseAnalytics: FirebaseAnalytics? = null

    private fun initialApp() {
        val locale = when (GlobalConfig.getCurrentLang()) {
            Wenku8API.AppLanguage.TC -> Locale.TRADITIONAL_CHINESE
            Wenku8API.AppLanguage.SC -> Locale.SIMPLIFIED_CHINESE
        }
        val config = Configuration().apply {
            this.locale = locale
        }
        Locale.setDefault(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        if (SaveFileMigration.migrationCompleted()) {
            Log.i(TAG, "Save file migration has completed.")
        } else if (missingPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL,
            )
        }

        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            missingPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL,
            )
        }

        if (Build.VERSION.SDK_INT >= EXTERNAL_SAVE_MIGRATION_API) {
            if (!LightCache.testFileExist(SaveFileMigration.getExternalStoragePath())) {
                GlobalConfig.setExternalStoragePathAvailable(false)
            }
        } else {
            LightCache.saveFile(GlobalConfig.getDefaultStoragePath() + "imgs", ".nomedia", "".toByteArray(), false)
            LightCache.saveFile(
                GlobalConfig.getDefaultStoragePath() + GlobalConfig.customFolderName,
                ".nomedia",
                "".toByteArray(),
                false,
            )
            LightCache.saveFile(GlobalConfig.getBackupStoragePath() + "imgs", ".nomedia", "".toByteArray(), false)
            LightCache.saveFile(
                GlobalConfig.getBackupStoragePath() + GlobalConfig.customFolderName,
                ".nomedia",
                "".toByteArray(),
                false,
            )
            GlobalConfig.setExternalStoragePathAvailable(
                LightCache.testFileExist(
                    SaveFileMigration.getExternalStoragePath() + "imgs" + File.separator + ".nomedia",
                    true,
                ),
            )
        }

        LightUserSession.aiui = LightUserSession.AsyncInitUserInfo(
            applicationContext,
            Runnable {
                LightCache.deleteFile(GlobalConfig.getFirstFullUserAccountSaveFilePath())
                LightCache.deleteFile(GlobalConfig.getSecondFullUserAccountSaveFilePath())
                LightCache.deleteFile(GlobalConfig.getFirstUserAvatarSaveFilePath())
                LightCache.deleteFile(GlobalConfig.getSecondUserAvatarSaveFilePath())
                Toast.makeText(
                    applicationContext,
                    applicationContext.resources.getString(R.string.system_log_info_outofdate),
                    Toast.LENGTH_SHORT,
                ).show()
            },
            Runnable { GlobalConfig.loadUserInfoSet() },
        )
        LightUserSession.aiui?.execute()
        GlobalConfig.loadAllSetting()

        Wenku8API.NoticeString = GlobalConfig.loadSavedNotice()
    }

    private fun startOldSaveMigration() {
        val decision = SaveMigrationStartDecision.from(
            sdkInt = Build.VERSION.SDK_INT,
            migrationApi = EXTERNAL_SAVE_MIGRATION_API,
            tiramisuApi = Build.VERSION_CODES.TIRAMISU,
            migrationCompleted = SaveFileMigration.migrationCompleted(),
            missingReadExternalStorage = missingPermission(Manifest.permission.READ_EXTERNAL_STORAGE),
            migrationEligible = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                SaveFileMigration.migrationEligible()
            } else {
                false
            },
        )

        when (decision.action) {
            SaveMigrationStartAction.SKIP -> return
            SaveMigrationStartAction.PROMPT_FOR_DIRECTORY -> {
                Log.d(TAG, "startOldSaveMigration: Eligible")
                MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.system_save_need_to_migrate)
                    .setPositiveButton(R.string.dialog_positive_upgrade) { _, _ ->
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                            addCategory(Intent.CATEGORY_DEFAULT)
                        }
                        startActivityForResult(Intent.createChooser(intent, "Choose directory"), REQUEST_READ_EXTERNAL_SAVES)
                    }
                    .setNeutralButton(R.string.dialog_negative_pass_for_now, null)
                    .setNegativeButton(R.string.dialog_negative_never) { _, _ ->
                        SaveFileMigration.markMigrationCompleted()
                    }
                    .setCancelable(false)
                    .show()
            }
            SaveMigrationStartAction.RUN_MIGRATION -> runExternalSaveMigration()
        }
    }

    private fun runExternalSaveMigration() {
        val progressDialog = ProgressDialogHelper.show(
            this,
            getString(R.string.system_save_upgrading),
            false,
            false,
            null,
        )

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            try {
                val filesToCopy = SaveFileMigration.generateMigrationPlan()

                val saveMigrationFilesTotalParams = Bundle().apply {
                    putString("count", filesToCopy.size.toString())
                }
                GoogleServicesHelper.logEvent(
                    firebaseAnalytics,
                    "save_migration_files_total",
                    saveMigrationFilesTotalParams,
                )

                if (filesToCopy.isEmpty()) {
                    Log.d(TAG, "Empty list of files to copy")
                    handler.post(progressDialog::dismiss)
                    SaveFileMigration.markMigrationCompleted()
                    return@execute
                }

                handler.post { progressDialog.setMaxProgress(filesToCopy.size) }

                var progress = 0
                var failedFiles = 0
                for (filePath in filesToCopy) {
                    try {
                        val targetFilePath = SaveFileMigration.migrateFile(filePath)
                        if (!LightCache.testFileExist(targetFilePath, true)) {
                            Log.d(TAG, "Failed migrating: $targetFilePath (from $filePath)")
                            failedFiles++
                        }
                    } catch (exception: FileNotFoundException) {
                        failedFiles++
                        exception.printStackTrace()
                    }
                    progress++

                    val finalProgress = progress
                    handler.post { progressDialog.setProgress(finalProgress) }
                }

                try {
                    Thread.sleep(2000)
                } catch (exception: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw RuntimeException(exception)
                }

                val finalFailedFiles = failedFiles
                val saveMigrationFilesFailedParams = Bundle().apply {
                    putString("failed", finalFailedFiles.toString())
                }
                GoogleServicesHelper.logEvent(
                    firebaseAnalytics,
                    "save_migration_files_failed",
                    saveMigrationFilesFailedParams,
                )

                handler.post {
                    SaveFileMigration.markMigrationCompleted()
                    progressDialog.dismiss()

                    MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.system_save_migrated, filesToCopy.size, finalFailedFiles))
                        .setPositiveButton(R.string.dialog_positive_sure) { _, _ -> reloadApp() }
                        .setCancelable(false)
                        .show()
                }
            } finally {
                executor.shutdown()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_main, HomeIndicatorStyle.HAMBURGER)
        initialApp()

        firebaseAnalytics = GoogleServicesHelper.initFirebase(this)

        val imageLoader = ImageLoader.getInstance()
        if (imageLoader == null || !imageLoader.isInited) {
            GlobalConfig.initImageLoader(this)
        }

        startOldSaveMigration()

        navigationDrawerFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_drawer) as? NavigationDrawerFragment
        val appToolbar = getToolbar()
        if (appToolbar != null) {
            navigationDrawerFragment?.setup(R.id.fragment_drawer, findViewById(R.id.drawer), appToolbar)
        }

        appToolbar?.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_search) {
                startActivity(Intent(this, SearchActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.hold)
            }
            true
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val drawerFragment = navigationDrawerFragment
                    if (drawerFragment != null && drawerFragment.isDrawerOpen()) {
                        drawerFragment.closeDrawer()
                    } else {
                        exitBy2Click()
                    }
                }
            },
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (navigationDrawerFragment?.isDrawerOpen() != true) {
            when (currentFragment) {
                FragmentMenuOption.LATEST -> {
                    supportActionBar?.title = resources.getString(R.string.main_menu_latest)
                    menuInflater.inflate(R.menu.menu_latest, menu)
                }
                FragmentMenuOption.RKLIST -> supportActionBar?.title = resources.getString(R.string.main_menu_rklist)
                FragmentMenuOption.FAV -> supportActionBar?.title = resources.getString(R.string.main_menu_fav)
                FragmentMenuOption.CONFIG -> supportActionBar?.title = resources.getString(R.string.main_menu_config)
            }
        } else {
            supportActionBar?.title = resources.getString(R.string.app_name)
        }

        return true
    }

    fun changeFragment(targetFragment: Fragment) {
        supportActionBar?.elevation = if (currentFragment == FragmentMenuOption.RKLIST) {
            0f
        } else {
            resources.getDimension(R.dimen.toolbar_elevation)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, targetFragment, "fragment")
            .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }

    override fun onResume() {
        super.onResume()

        if (!NEW_VERSION_CHECKED.getAndSet(true)) {
            CheckAppNewVersion(this).execute()
            UpdateNotificationMessage().execute()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_EXTERNAL,
            REQUEST_READ_EXTERNAL -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_READ_EXTERNAL_SAVES || resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        val wenku8Uri: Uri = data.data ?: return
        val selection = SaveMigrationDirectorySelection.from(
            path = wenku8Uri.path,
            lastPathSegment = wenku8Uri.lastPathSegment,
        )
        if (!selection.isValid) {
            Log.i(TAG, "LastPathSegment: ${selection.lastPathSegment}")
            Log.i(TAG, "Selected path for save migration doesn't look right: $wenku8Uri")

            val saveMigrationParams = Bundle().apply {
                putString("path", selection.path)
                putString("valid_path", selection.validPathAnalyticsValue)
            }
            GoogleServicesHelper.logEvent(firebaseAnalytics, "save_migration_path_selection", saveMigrationParams)

            MaterialAlertDialogBuilder(this)
                .setMessage(getString(R.string.dialog_content_wrong_path, selection.displayPath))
                .setPositiveButton(R.string.dialog_positive_retry) { _, _ -> reloadApp() }
                .setNeutralButton(R.string.dialog_negative_pass_for_now, null)
                .setNegativeButton(R.string.dialog_negative_never) { _, _ ->
                    SaveFileMigration.markMigrationCompleted()
                }
                .setCancelable(false)
                .show()
            return
        }

        val saveMigrationParams = Bundle().apply {
            putString("path", selection.path)
            putString("valid_path", selection.validPathAnalyticsValue)
        }
        GoogleServicesHelper.logEvent(firebaseAnalytics, "save_migration_path_selection", saveMigrationParams)

        contentResolver.takePersistableUriPermission(wenku8Uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

        Log.i(TAG, "Selected the right directory: ${selection.path}")
        SaveFileMigration.overrideExternalPath(wenku8Uri)
        runExternalSaveMigration()
    }

    private fun exitBy2Click() {
        if (!isExit) {
            isExit = true
            Toast.makeText(
                this,
                resources.getString(R.string.press_twice_to_exit),
                Toast.LENGTH_SHORT,
            ).show()
            Timer().schedule(
                object : TimerTask() {
                    override fun run() {
                        isExit = false
                    }
                },
                2000,
            )
        } else {
            finish()
        }
    }

    private fun missingPermission(permissionName: String): Boolean =
        ContextCompat.checkSelfPermission(this, permissionName) != PackageManager.PERMISSION_GRANTED

    private fun reloadApp() {
        val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
        private val EXTERNAL_SAVE_MIGRATION_API = Build.VERSION_CODES.Q

        private const val REQUEST_WRITE_EXTERNAL = 100
        private const val REQUEST_READ_EXTERNAL = 101
        private const val REQUEST_READ_EXTERNAL_SAVES = 103

        private val NEW_VERSION_CHECKED = AtomicBoolean(false)

        private var isExit = false
    }
}
