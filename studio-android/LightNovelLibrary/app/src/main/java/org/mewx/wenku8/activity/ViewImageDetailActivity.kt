@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import java.io.File
import java.io.IOException
import org.mewx.wenku8.R
import org.mewx.wenku8.util.GoogleServicesHelper
import org.mewx.wenku8.util.LightCache

/**
 * View large image activity.
 */
class ViewImageDetailActivity : BaseMaterialActivity() {
    private lateinit var path: String
    private lateinit var fileName: String
    private lateinit var imageView: SubsamplingScaleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_view_image_detail, StatusBarColor.DARK)
        installFadeBackCallback()

        GoogleServicesHelper.initFirebase(this)

        path = intent.getStringExtra("path")!!
        fileName = if (path.contains("/")) path.split("/").last() else "default.jpg"
        Log.d(TAG, "onCreate: path = $path")
        Log.d(TAG, "onCreate: fileName = $fileName")
        supportActionBar?.title = fileName

        imageView = findViewById(R.id.image_scalable)
        imageView.setImage(ImageSource.uri(path))
        imageView.maxScale = 4.0f

        var chromeShown = true
        imageView.setOnClickListener {
            chromeShown = if (chromeShown) {
                hideNavigationBar()
                findViewById<View>(R.id.toolbar_actionbar).visibility = View.INVISIBLE
                findViewById<View>(R.id.image_detail_bot).visibility = View.INVISIBLE
                setStatusBarAlpha(0.0f)
                setNavigationBarAlpha(0.0f)
                false
            } else {
                showNavigationBar()
                findViewById<View>(R.id.toolbar_actionbar).visibility = View.VISIBLE
                findViewById<View>(R.id.image_detail_bot).visibility = View.VISIBLE
                setStatusBarAlpha(0.9f)
                setNavigationBarAlpha(0.8f)
                true
            }
        }

        findViewById<View>(R.id.btn_rotate).setOnClickListener {
            imageView.orientation = when (imageView.orientation) {
                SubsamplingScaleImageView.ORIENTATION_0 -> SubsamplingScaleImageView.ORIENTATION_90
                SubsamplingScaleImageView.ORIENTATION_90 -> SubsamplingScaleImageView.ORIENTATION_180
                SubsamplingScaleImageView.ORIENTATION_180 -> SubsamplingScaleImageView.ORIENTATION_270
                SubsamplingScaleImageView.ORIENTATION_270 -> SubsamplingScaleImageView.ORIENTATION_0
                else -> SubsamplingScaleImageView.ORIENTATION_0
            }
        }
        findViewById<View>(R.id.btn_rotate).setOnLongClickListener {
            Toast.makeText(this, resources.getString(R.string.reader_rotate), Toast.LENGTH_SHORT).show()
            true
        }
        findViewById<View>(R.id.btn_download).setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_REQUEST
                )
            } else {
                performSaveImage()
            }
        }
        findViewById<View>(R.id.btn_download).setOnLongClickListener {
            Toast.makeText(this, resources.getString(R.string.reader_download), Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        showNavigationBar()
    }

    private fun hideNavigationBar() {
        val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = flags

        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                decorView.systemUiVisibility = flags
            }
        }
    }

    private fun showNavigationBar() {
        val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = flags

        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                decorView.systemUiVisibility = flags
            }
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            performSaveImage()
        }
    }

    private fun performSaveImage() {
        if (saveImageToGallery(path, fileName)) {
            Toast.makeText(this, "已保存： DCIM/wenku8/$fileName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToGallery(sourcePath: String, fileName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageWithMediaStore(sourcePath, fileName)
        } else {
            saveImageWithLegacyStorage(sourcePath, fileName)
        }
    }

    private fun saveImageWithMediaStore(sourcePath: String, fileName: String): Boolean {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + File.separator + "wenku8")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }

        val resolver: ContentResolver = contentResolver
        val imageUri: Uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return false

        return try {
            resolver.openOutputStream(imageUri).use { outputStream ->
                val data = LightCache.loadFile(sourcePath) ?: return false
                outputStream?.write(data) ?: return false
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun saveImageWithLegacyStorage(sourcePath: String, fileName: String): Boolean {
        val dcimDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "wenku8")
        if (!dcimDir.exists() && !dcimDir.mkdirs()) {
            return false
        }

        val destFile = File(dcimDir, fileName)
        LightCache.copyFile(sourcePath, destFile.absolutePath, true)

        return if (destFile.exists()) {
            MediaScannerConnection.scanFile(this, arrayOf(destFile.absolutePath), null, null)
            true
        } else {
            false
        }
    }

    companion object {
        private val TAG = ViewImageDetailActivity::class.java.simpleName
        private const val WRITE_EXTERNAL_STORAGE_REQUEST = 100
    }
}
