@file:Suppress("DEPRECATION")

package org.mewx.wenku8.activity

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import java.io.File
import java.io.FileNotFoundException
import org.mewx.wenku8.R
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.util.GoogleServicesHelper
import org.mewx.wenku8.util.LightCache

/**
 * Lets the user select a menu background.
 */
class MenuBackgroundSelectorActivity : BaseMaterialActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_menu_background_selector)

        GoogleServicesHelper.initFirebase(this)

        for ((id, settingValue) in VIEW_ID_TO_SETTING_ITEM_MAP) {
            findViewById<View>(id).setOnClickListener {
                GlobalConfig.setToAllSetting(GlobalConfig.SettingItems.menu_bg_id, settingValue)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bg_selector, menu)
        menu.getItem(0).icon?.mutate()?.setColorFilter(
            resources.getColor(R.color.default_white),
            PorterDuff.Mode.SRC_ATOP
        )
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressed()
        } else if (menuItem.itemId == R.id.action_find) {
            val intent = Intent().apply {
                action = Intent.ACTION_OPEN_DOCUMENT
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_PICK_IMAGE)
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || requestCode != REQUEST_PICK_IMAGE) {
            return
        }

        val mediaUri = data?.data ?: return
        if (mediaUri.path == null) {
            return
        }

        val copiedFilePath = GlobalConfig.getDefaultStoragePath() +
            GlobalConfig.customFolderName + File.separator + "menu_bg"
        try {
            val inputStream = applicationContext.contentResolver.openInputStream(mediaUri)
                ?: throw FileNotFoundException(mediaUri.toString())
            LightCache.copyFile(inputStream, copiedFilePath, true)
            runSaveCustomMenuBackground(copiedFilePath.replace("file://", ""))
        } catch (exception: FileNotFoundException) {
            exception.printStackTrace()
            Toast.makeText(this, "Exception: $exception", Toast.LENGTH_SHORT).show()
        }
    }

    private fun runSaveCustomMenuBackground(path: String) {
        try {
            BitmapFactory.decodeFile(path)
        } catch (error: OutOfMemoryError) {
            try {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2
                }
                val bitmap = BitmapFactory.decodeFile(path, options)
                if (bitmap == null) {
                    throw Exception("PictureDecodeFailedException")
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                Toast.makeText(this, "Exception: $exception", Toast.LENGTH_SHORT).show()
                return
            }
        }

        GlobalConfig.setToAllSetting(GlobalConfig.SettingItems.menu_bg_id, "0")
        GlobalConfig.setToAllSetting(GlobalConfig.SettingItems.menu_bg_path, path)
        finish()
    }

    companion object {
        private const val REQUEST_PICK_IMAGE = 0

        private val VIEW_ID_TO_SETTING_ITEM_MAP = mapOf(
            R.id.bg01 to "1",
            R.id.bg02 to "2",
            R.id.bg03 to "3",
            R.id.bg04 to "4",
            R.id.bg05 to "5"
        )
    }
}
