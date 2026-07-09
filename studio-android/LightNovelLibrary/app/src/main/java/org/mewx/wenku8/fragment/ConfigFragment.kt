package org.mewx.wenku8.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import org.mewx.wenku8.R
import org.mewx.wenku8.activity.AboutActivity
import org.mewx.wenku8.activity.MainActivity
import org.mewx.wenku8.activity.MenuBackgroundSelectorActivity
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.api.Wenku8Error
import org.mewx.wenku8.async.CheckAppNewVersion
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.OldNovelContentParser
import org.mewx.wenku8.util.LightCache
import org.mewx.wenku8.util.LightTool
import org.mewx.wenku8.util.ProgressDialogHelper
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

class ConfigFragment : Fragment(R.layout.fragment_config) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val cleanupExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val cleanupCancelled = AtomicBoolean(false)
    private var cleanupFuture: Future<*>? = null
    private var progressDialog: ProgressDialogHelper? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindNotice(view)
        bindLanguage(view)
        bindCacheCleanup(view)
        bindNavigation(view)
        bindEinkMode(view)
        bindUpdateCheck(view)
        bindAbout(view)
    }

    private fun bindNotice(root: View) {
        val noticeLayout = root.findViewById<View>(R.id.notice_layout)
        val noticeText = root.findViewById<TextView>(R.id.notice)
        val notice = Wenku8API.NoticeString

        if (notice.isEmpty()) {
            noticeLayout.visibility = View.GONE
            return
        }

        @Suppress("DEPRECATION")
        var sequence: CharSequence = Html.fromHtml(notice.trim())
        var end = sequence.length - 1
        while (end >= 0 && Character.isWhitespace(sequence[end])) {
            end--
        }
        sequence = sequence.subSequence(0, end + 1)

        val builder = SpannableStringBuilder(sequence)
        val urls = builder.getSpans(0, sequence.length, URLSpan::class.java)
        urls.forEach { span ->
            val start = builder.getSpanStart(span)
            val spanEnd = builder.getSpanEnd(span)
            val flags = builder.getSpanFlags(span)
            val clickable = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GlobalConfig.blogPageUrl)))
                }
            }
            builder.setSpan(clickable, start, spanEnd, flags)
            builder.removeSpan(span)
        }

        noticeText.text = builder
        noticeText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun bindLanguage(root: View) {
        root.findViewById<View>(R.id.btn_choose_language).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.config_choose_language)
                .setItems(R.array.choose_language_option) { _, which ->
                    val selected = if (which == 0) Wenku8API.AppLanguage.SC else Wenku8API.AppLanguage.TC
                    if (selected == GlobalConfig.getCurrentLang()) {
                        Toast.makeText(requireContext(), "Already in.", Toast.LENGTH_SHORT).show()
                        return@setItems
                    }

                    GlobalConfig.setCurrentLang(selected)
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.hold)
                    requireActivity().finish()
                }
                .show()
        }
    }

    private fun bindCacheCleanup(root: View) {
        root.findViewById<View>(R.id.btn_clear_cache).setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.config_clear_cache)
                .setItems(R.array.wipe_cache_option) { _, which ->
                    runCacheCleanup(slow = which == 1)
                }
                .show()
        }
    }

    private fun bindNavigation(root: View) {
        root.findViewById<View>(R.id.btn_navigation_drawer_wallpaper).setOnClickListener {
            startActivity(Intent(requireContext(), MenuBackgroundSelectorActivity::class.java))
        }
    }

    private fun bindEinkMode(root: View) {
        val switchEinkMode = root.findViewById<MaterialSwitch>(R.id.switch_eink_mode)
        val row = root.findViewById<View>(R.id.eink_mode_config)

        switchEinkMode.isChecked = GlobalConfig.isEinkModeEnabled()
        switchEinkMode.setOnCheckedChangeListener { _, checked ->
            GlobalConfig.setToAllSetting(GlobalConfig.SettingItems.eink_mode, if (checked) "1" else "0")
        }
        row.setOnClickListener {
            switchEinkMode.isChecked = !switchEinkMode.isChecked
        }
    }

    private fun bindUpdateCheck(root: View) {
        root.findViewById<View>(R.id.btn_check_update).setOnClickListener {
            @Suppress("DEPRECATION")
            CheckAppNewVersion(requireActivity(), true).execute()
        }
    }

    private fun bindAbout(root: View) {
        root.findViewById<View>(R.id.btn_about).setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
    }

    private fun runCacheCleanup(slow: Boolean) {
        val runningCleanup = cleanupFuture
        if (runningCleanup != null && !runningCleanup.isDone) {
            Toast.makeText(requireContext(), R.string.system_loading_please_wait, Toast.LENGTH_SHORT).show()
            return
        }

        cleanupCancelled.set(false)
        val message = if (slow) {
            getString(R.string.dialog_content_wipe_cache_slow)
        } else {
            getString(R.string.dialog_content_wipe_cache_fast)
        }

        progressDialog = ProgressDialogHelper.show(
            requireContext(),
            message,
            true,
            slow
        ) {
            cleanupCancelled.set(true)
        }

        cleanupFuture = cleanupExecutor.submit {
            val result = if (slow) {
                cleanCacheSlow()
            } else {
                cleanCacheFast()
            }

            mainHandler.post {
                progressDialog?.dismiss()
                progressDialog = null

                if (!isAdded) {
                    return@post
                }

                if (result == Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                    Toast.makeText(requireContext(), "OK", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), result.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cleanCacheFast(): Wenku8Error.ErrorCode {
        deleteUnownedCoverImages()
        deleteGeneratedImageCache()
        return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
    }

    private fun cleanCacheSlow(): Wenku8Error.ErrorCode {
        deleteUnownedCoverImages()
        deleteGeneratedImageCache()

        if (cleanupCancelled.get()) {
            return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
        }

        val referencedPictures = collectReferencedPictureNames()
        if (cleanupCancelled.get()) {
            return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
        }

        val imageDir = existingDirectory(
            GlobalConfig.getFirstFullSaveFilePath() + "imgs",
            GlobalConfig.getSecondFullSaveFilePath() + "imgs"
        )
        imageDir.listFiles()?.forEach { file ->
            if (cleanupCancelled.get()) {
                return Wenku8Error.ErrorCode.USER_CANCELLED_TASK
            }
            if (!referencedPictures.contains(file.name) && !file.delete()) {
                Log.d(TAG, "Failed to delete file: ${file.absolutePath}")
            }
        }

        return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
    }

    private fun deleteUnownedCoverImages() {
        val coverDir = existingDirectory(
            GlobalConfig.getDefaultStoragePath() + "imgs",
            GlobalConfig.getBackupStoragePath() + "imgs"
        )
        coverDir.listFiles()?.forEach { file ->
            val id = file.name.substringBeforeLast('.', file.name)
            if (LightTool.isInteger(id) &&
                !GlobalConfig.testInLocalBookshelf(id.toInt()) &&
                !file.delete()
            ) {
                Log.d(TAG, "Failed to delete file: ${file.absolutePath}")
            }
        }
    }

    private fun deleteGeneratedImageCache() {
        val cacheDir = existingDirectory(
            GlobalConfig.getDefaultStoragePath() + "cache",
            GlobalConfig.getBackupStoragePath() + "cache"
        )
        cacheDir.listFiles()?.forEach { file ->
            if (!file.delete()) {
                Log.d(TAG, "Failed to delete file: ${file.absolutePath}")
            }
        }
    }

    private fun collectReferencedPictureNames(): MutableList<String> {
        val referencedPictures = mutableListOf<String>()
        val novelDir = existingDirectory(
            GlobalConfig.getFirstFullSaveFilePath() + "novel",
            GlobalConfig.getSecondFullSaveFilePath() + "novel"
        )

        novelDir.listFiles()?.forEach { file ->
            if (cleanupCancelled.get()) {
                return referencedPictures
            }

            val content = LightCache.loadFile(file.absolutePath) ?: return@forEach
            val imageList = OldNovelContentParser.NovelContentParser_onlyImage(String(content, Charsets.UTF_8))
            imageList.forEach { novelContent ->
                referencedPictures.add(GlobalConfig.generateImageFileNameByURL(novelContent.content))
            }
        }

        return referencedPictures
    }

    private fun existingDirectory(primaryPath: String, fallbackPath: String): File {
        val primary = File(primaryPath)
        return if (primary.exists()) primary else File(fallbackPath)
    }

    override fun onDestroyView() {
        cleanupCancelled.set(true)
        cleanupFuture?.cancel(true)
        progressDialog?.dismiss()
        progressDialog = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        cleanupExecutor.shutdownNow()
        super.onDestroy()
    }

    companion object {
        private val TAG = ConfigFragment::class.java.simpleName
    }
}
