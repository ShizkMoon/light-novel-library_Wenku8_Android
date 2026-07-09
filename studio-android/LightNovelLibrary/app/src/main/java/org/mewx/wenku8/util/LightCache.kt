package org.mewx.wenku8.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.LinkedList
import java.util.Queue
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.OldNovelContentParser
import org.mewx.wenku8.global.api.VolumeList

/**
 * Straightforward file operations for the legacy cache layer.
 */
class LightCache private constructor() {
    companion object {
        private val TAG = LightCache::class.java.simpleName

        /**
         * Test whether file exists.
         *
         * @param path the full file path
         * @return true if file exists and is not empty; otherwise false
         */
        @JvmStatic
        fun testFileExist(path: String): Boolean = testFileExist(path, false)

        @JvmStatic
        fun testFileExist(path: String, allowEmptyFile: Boolean): Boolean {
            val file = File(path)
            if (file.exists()) {
                if (!allowEmptyFile && file.length() == 0L) {
                    deleteFile(path)
                } else {
                    return true
                }
            }
            return false
        }

        /**
         * Load file content.
         *
         * @param path full file path
         * @return null if the file does not exist; otherwise file bytes, which can be empty
         */
        @JvmStatic
        fun loadFile(path: String): ByteArray? {
            val file = File(path)
            if (file.exists() && file.isFile) {
                try {
                    return loadStream(FileInputStream(file))
                } catch (exception: FileNotFoundException) {
                    exception.printStackTrace()
                }
            }
            return null
        }

        @JvmStatic
        fun loadStream(inputStream: InputStream): ByteArray? {
            return try {
                val fileSize = inputStream.available()
                DataInputStream(inputStream).use { stream ->
                    val bytes = ByteArray(fileSize)
                    if (stream.read(bytes, 0, fileSize) == -1) {
                        null
                    } else {
                        bytes
                    }
                }
            } catch (exception: IOException) {
                exception.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun saveFile(path: String, fileName: String, bs: ByteArray, forceUpdate: Boolean): Boolean {
            val fullPath = path + if (path[path.length - 1] != File.separatorChar) {
                File.separator
            } else {
                ""
            } + fileName
            return saveFile(fullPath, bs, forceUpdate)
        }

        @JvmStatic
        fun saveFile(filepath: String, bs: ByteArray, forceUpdate: Boolean): Boolean {
            val file = File(filepath)
            val parentFile = file.parentFile
            if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
                Log.d(TAG, "Failed to create dir: $filepath")
            }

            Log.d(TAG, "Path: $filepath")
            if (!file.exists() || forceUpdate) {
                if (file.exists() && !file.isFile) {
                    Log.d(TAG, "Failed to write, which may caused by file is not a file")
                    return false
                }

                try {
                    if (!file.createNewFile()) {
                        Log.d(TAG, "File existed or failed to create file: $filepath")
                    }

                    DataOutputStream(FileOutputStream(file)).use { stream ->
                        stream.write(bs)
                    }
                    Log.d(TAG, "Write successfully")
                } catch (exception: IOException) {
                    exception.printStackTrace()
                    return false
                }
            }
            return true
        }

        @JvmStatic
        fun deleteFile(path: String, fileName: String): Boolean {
            val fullPath = path + if (path[path.length - 1] != File.separatorChar) {
                File.separator
            } else {
                ""
            } + fileName
            return deleteFile(fullPath)
        }

        @JvmStatic
        fun deleteFile(filepath: String): Boolean {
            Log.d(TAG, "Deleting: $filepath")
            return File(filepath).delete()
        }

        /**
         * Copy file from one place to another, creating target parents when needed.
         *
         * @param from full source path
         * @param to full target path
         * @param forceWrite true to override
         */
        @JvmStatic
        fun copyFile(from: String, to: String, forceWrite: Boolean) {
            val fromFile = File(from)
            if (!fromFile.exists() || !fromFile.isFile || !fromFile.canRead()) {
                return
            }

            try {
                FileInputStream(fromFile).use { inputStream ->
                    copyFile(inputStream, to, forceWrite)
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        @JvmStatic
        fun copyFile(from: InputStream, to: String, forceWrite: Boolean) {
            val toFile = File(to)
            if (toFile.exists() && !forceWrite) {
                return
            }

            val parentFile = toFile.parentFile
            if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
                Log.d(TAG, "Failed to create parent dirs for target file: $to")
            }
            if (toFile.exists() && forceWrite && !toFile.delete()) {
                Log.d(TAG, "Failed to create or delete target file: $to")
            }

            try {
                from.use { inputStream ->
                    FileOutputStream(toFile).use { outputStream ->
                        inputStream.copyTo(outputStream, 1024)
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        // Copied from https://stackoverflow.com/a/36714242/4206925
        @JvmStatic
        fun getFilePath(context: Context, uri: Uri): String? {
            var targetUri = uri
            var selection: String? = null
            var selectionArgs: Array<String>? = null

            if (DocumentsContract.isDocumentUri(context.applicationContext, targetUri)) {
                if (isExternalStorageDocument(targetUri)) {
                    val docId = DocumentsContract.getDocumentId(targetUri)
                    val split = docId.split(":")
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else if (isDownloadsDocument(targetUri)) {
                    val id = DocumentsContract.getDocumentId(targetUri)
                    targetUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        id.toLong()
                    )
                } else if (isMediaDocument(targetUri)) {
                    val docId = DocumentsContract.getDocumentId(targetUri)
                    val split = docId.split(":")
                    when (split[0]) {
                        "image" -> targetUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> targetUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> targetUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    selection = "_id=?"
                    selectionArgs = arrayOf(split[1])
                }
            }

            if ("content".equals(targetUri.scheme, ignoreCase = true)) {
                if (isGooglePhotosUri(targetUri)) {
                    return targetUri.lastPathSegment
                }

                val projection = arrayOf(MediaStore.Images.Media.DATA)
                try {
                    context.contentResolver
                        .query(targetUri, projection, selection, selectionArgs, null)
                        ?.use { cursor ->
                            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                            if (cursor.moveToFirst()) {
                                return cursor.getString(columnIndex)
                            }
                        }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            } else if ("file".equals(targetUri.scheme, ignoreCase = true)) {
                return targetUri.path
            }
            return null
        }

        @JvmStatic
        fun isExternalStorageDocument(uri: Uri): Boolean =
            "com.android.externalstorage.documents" == uri.authority

        @JvmStatic
        fun isDownloadsDocument(uri: Uri): Boolean =
            "com.android.providers.downloads.documents" == uri.authority

        @JvmStatic
        fun isMediaDocument(uri: Uri): Boolean =
            "com.android.providers.media.documents" == uri.authority

        @JvmStatic
        fun isGooglePhotosUri(uri: Uri): Boolean =
            "com.google.android.apps.photos.content" == uri.authority

        /**
         * Lists all files in the given directory recursively.
         *
         * @param fullDirectoryPath the directory to look up
         * @return the list of absolute paths for all files inside
         */
        @JvmStatic
        fun listAllFilesInDirectory(fullDirectoryPath: File): List<Uri> {
            val paths = ArrayList<Uri>()
            val directoryQueue: Queue<File> = LinkedList()
            if (fullDirectoryPath.isDirectory) {
                directoryQueue.add(fullDirectoryPath)
            }

            while (directoryQueue.isNotEmpty()) {
                val currentDir = directoryQueue.remove()
                val fileList = currentDir.listFiles() ?: continue
                for (file in fileList) {
                    if (file.isDirectory) {
                        directoryQueue.add(file)
                    } else if (file.isFile) {
                        paths.add(Uri.fromFile(file))
                    }
                }
            }
            return paths
        }

        @JvmStatic
        fun listAllFilesInDirectory(fullDirectoryPath: DocumentFile): List<Uri> {
            val paths = ArrayList<Uri>()
            val directoryQueue: Queue<DocumentFile> = LinkedList()
            if (fullDirectoryPath.isDirectory) {
                directoryQueue.add(fullDirectoryPath)
            }

            while (directoryQueue.isNotEmpty()) {
                val currentDir = directoryQueue.remove()
                for (file in currentDir.listFiles()) {
                    if (file.isDirectory) {
                        directoryQueue.add(file)
                    } else if (file.isFile) {
                        paths.add(file.uri)
                    }
                }
            }
            return paths
        }

        @JvmStatic
        fun cleanLocalCache(volumeList: VolumeList) {
            for (chapterInfo in volumeList.chapterList ?: return) {
                val xml = GlobalConfig.loadFullFileFromSaveFolder("novel", "${chapterInfo.cid}.xml")
                if (xml.isEmpty()) {
                    return
                }

                val novelContent = OldNovelContentParser.NovelContentParser_onlyImage(xml)
                for (content in novelContent) {
                    if (content.type == OldNovelContentParser.NovelContentType.IMAGE) {
                        val imgFileName = GlobalConfig.generateImageFileNameByURL(content.content)
                        deleteFile(
                            GlobalConfig.getFirstFullSaveFilePath() +
                                GlobalConfig.imgsSaveFolderName + File.separator + imgFileName
                        )
                        deleteFile(
                            GlobalConfig.getSecondFullSaveFilePath() +
                                GlobalConfig.imgsSaveFolderName + File.separator + imgFileName
                        )
                    }
                }
                deleteFile(GlobalConfig.getFirstFullSaveFilePath(), "novel" + File.separator + "${chapterInfo.cid}.xml")
                deleteFile(GlobalConfig.getSecondFullSaveFilePath(), "novel" + File.separator + "${chapterInfo.cid}.xml")
            }
            volumeList.inLocal = false
        }
    }
}
