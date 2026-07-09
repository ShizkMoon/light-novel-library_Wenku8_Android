package org.mewx.wenku8.activity

import org.mewx.wenku8.api.Wenku8Error

class NovelImageCacheService(
    private val firstSaveRoot: String,
    private val secondSaveRoot: String,
    private val imageFolderName: String,
    private val fileNameForUrl: (String) -> String,
    private val fileExists: (String) -> Boolean,
    private val download: (String) -> ByteArray?,
    private val saveFile: (String, String, ByteArray, Boolean) -> Boolean,
) {
    fun cacheImage(url: String, forceUpdate: Boolean): Wenku8Error.ErrorCode {
        val paths = NovelImageCachePlanner.paths(
            url = url,
            firstSaveRoot = firstSaveRoot,
            secondSaveRoot = secondSaveRoot,
            imageFolderName = imageFolderName,
            fileNameForUrl = fileNameForUrl,
        )
        if (!NovelImageCachePlanner.shouldDownload(
                firstExists = fileExists(paths.firstFilePath),
                secondExists = fileExists(paths.secondFilePath),
                forceUpdate = forceUpdate,
            )
        ) {
            return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        }

        val fileContent = download(url)
            ?: return Wenku8Error.ErrorCode.NETWORK_ERROR
        if (!saveFile(paths.firstDirectoryPath, paths.fileName, fileContent, true)) {
            saveFile(paths.secondDirectoryPath, paths.fileName, fileContent, true)
        }
        return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
    }
}
