package org.mewx.wenku8.activity

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.Wenku8Error

class NovelImageCacheServiceTest {
    @Test
    fun skipsDownloadWhenImageAlreadyExistsInEitherStorage() {
        var downloadCount = 0
        val service = service(
            existingPaths = setOf("backup/imgs${File.separator}cached.png"),
            downloader = {
                downloadCount++
                byteArrayOf(1)
            },
        )

        val result = service.cacheImage("https://img.example.test/cached.png", forceUpdate = false)

        assertEquals(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, result)
        assertEquals(0, downloadCount)
        assertTrue(service.savedFiles.isEmpty())
    }

    @Test
    fun returnsNetworkErrorWhenDownloadFails() {
        val service = service(downloader = { null })

        val result = service.cacheImage("https://img.example.test/missing.png", forceUpdate = false)

        assertEquals(Wenku8Error.ErrorCode.NETWORK_ERROR, result)
        assertTrue(service.savedFiles.isEmpty())
    }

    @Test
    fun savesToBackupStorageWhenPrimarySaveFails() {
        val service = service(
            saveResults = listOf(false, true),
            downloader = { byteArrayOf(1, 2, 3) },
        )

        val result = service.cacheImage("https://img.example.test/new.png", forceUpdate = false)

        assertEquals(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, result)
        assertEquals(
            listOf(
                SavedFile("primary/imgs${File.separator}", "cached.png", byteArrayOf(1, 2, 3), true),
                SavedFile("backup/imgs${File.separator}", "cached.png", byteArrayOf(1, 2, 3), true),
            ),
            service.savedFiles,
        )
    }

    private fun service(
        existingPaths: Set<String> = emptySet(),
        saveResults: List<Boolean> = listOf(true),
        downloader: (String) -> ByteArray? = { byteArrayOf(1) },
    ): RecordingImageCacheStore =
        RecordingImageCacheStore(existingPaths, saveResults).also { store ->
            store.service = NovelImageCacheService(
                firstSaveRoot = "primary/",
                secondSaveRoot = "backup/",
                imageFolderName = "imgs",
                fileNameForUrl = { "cached.png" },
                fileExists = store::fileExists,
                download = downloader,
                saveFile = store::saveFile,
            )
        }

    private data class SavedFile(
        val directoryPath: String,
        val fileName: String,
        val bytes: ByteArray,
        val forceUpdate: Boolean,
    ) {
        override fun equals(other: Any?): Boolean =
            other is SavedFile &&
                directoryPath == other.directoryPath &&
                fileName == other.fileName &&
                bytes.contentEquals(other.bytes) &&
                forceUpdate == other.forceUpdate

        override fun hashCode(): Int =
            31 * (31 * (31 * directoryPath.hashCode() + fileName.hashCode()) + bytes.contentHashCode()) +
                forceUpdate.hashCode()
    }

    private class RecordingImageCacheStore(
        private val existingPaths: Set<String>,
        private val saveResults: List<Boolean>,
    ) {
        lateinit var service: NovelImageCacheService
        val savedFiles = mutableListOf<SavedFile>()
        private var saveCount = 0

        fun cacheImage(url: String, forceUpdate: Boolean): Wenku8Error.ErrorCode =
            service.cacheImage(url, forceUpdate)

        fun fileExists(path: String): Boolean = path in existingPaths

        fun saveFile(
            directoryPath: String,
            fileName: String,
            bytes: ByteArray,
            forceUpdate: Boolean,
        ): Boolean {
            savedFiles += SavedFile(directoryPath, fileName, bytes, forceUpdate)
            return saveResults.getOrElse(saveCount++) { true }
        }
    }
}
