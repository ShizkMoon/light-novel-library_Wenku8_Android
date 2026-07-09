package org.mewx.wenku8.util

import android.annotation.TargetApi
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileNotFoundException
import org.mewx.wenku8.MyApp

/**
 * Utility class for migrating save files from pre-API-30 storage to the Android R+ world.
 */
class SaveFileMigration private constructor() {
    companion object {
        private val TAG = SaveFileMigration::class.java.simpleName
        private const val SIGNAL_FILE_NAME = ".migration_completed"

        private var savedInternalPath: String? = null
        private var savedExternalPath: String? = null
        private var overrideExternalPathUrl: Uri? = null

        @JvmStatic
        fun markMigrationCompleted() {
            LightCache.saveFile(getInternalSavePath(), SIGNAL_FILE_NAME, "".toByteArray(), false)
        }

        @JvmStatic
        fun revertMigrationStatus() {
            LightCache.deleteFile(getInternalSavePath(), SIGNAL_FILE_NAME)
        }

        /**
         * Checks if the external storage contains the wenku8 directory.
         *
         * @return true if eligible; otherwise false
         */
        @TargetApi(Build.VERSION_CODES.TIRAMISU)
        @JvmStatic
        fun migrationEligible(): Boolean =
            LightCache.testFileExist(
                Environment.getExternalStorageDirectory().toString() + File.separator + "wenku8" + File.separator,
                true
            )

        @JvmStatic
        fun migrationCompleted(): Boolean {
            val signalFileExists = LightCache.testFileExist(getInternalSavePath() + SIGNAL_FILE_NAME, true)
            Log.d(TAG, "migrationCompleted: $signalFileExists")
            return signalFileExists
        }

        @TargetApi(Build.VERSION_CODES.TIRAMISU)
        @JvmStatic
        fun generateMigrationPlan(): List<Uri> {
            val overrideUri = overrideExternalPathUrl
            if (overrideUri != null) {
                val directory = DocumentFile.fromTreeUri(MyApp.getContext(), overrideUri)
                return if (directory != null) {
                    LightCache.listAllFilesInDirectory(directory)
                } else {
                    emptyList()
                }
            }

            return LightCache.listAllFilesInDirectory(File(getExternalStoragePath()))
        }

        /**
         * Given an external file path, copy the file to the internal storage.
         *
         * @param externalFilePath the file Uri in external storage
         * @return the internal absolute file path to the copied file
         */
        @Throws(FileNotFoundException::class)
        @JvmStatic
        fun migrateFile(externalFilePath: Uri): String {
            val externalPath = externalFilePath.path.orEmpty()
            val internalFilePath = externalPath.replace(getExternalStoragePath(), getInternalSavePath())
            val overrideUri = overrideExternalPathUrl

            if (overrideUri != null) {
                val inputStream = MyApp.getContext().contentResolver.openInputStream(externalFilePath)
                    ?: throw FileNotFoundException(externalFilePath.toString())
                LightCache.copyFile(inputStream, internalFilePath, true)
            } else {
                LightCache.copyFile(externalPath, internalFilePath, true)
            }
            return internalFilePath
        }

        @JvmStatic
        fun getInternalSavePath(): String {
            val cached = savedInternalPath
            if (cached != null) {
                return cached
            }

            val path = MyApp.getContext().filesDir.toString() + File.separator
            savedInternalPath = path
            return path
        }

        @JvmStatic
        fun overrideExternalPath(uri: Uri?) {
            overrideExternalPathUrl = uri
        }

        @JvmStatic
        fun getExternalStoragePath(): String {
            val overrideUri = overrideExternalPathUrl
            if (overrideUri != null) {
                return overrideUri.path.orEmpty()
            }

            val cached = savedExternalPath
            if (cached != null) {
                return cached
            }

            val path = Environment.getExternalStorageDirectory().toString() + File.separator + "wenku8" + File.separator
            savedExternalPath = path
            return path
        }
    }
}
