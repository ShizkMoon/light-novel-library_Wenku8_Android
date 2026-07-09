package org.mewx.wenku8.activity

import java.io.File

data class NovelImageCachePaths(
    val fileName: String,
    val firstFilePath: String,
    val secondFilePath: String,
    val firstDirectoryPath: String,
    val secondDirectoryPath: String,
)

object NovelImageCachePlanner {
    fun paths(
        url: String,
        firstSaveRoot: String,
        secondSaveRoot: String,
        imageFolderName: String,
        fileNameForUrl: (String) -> String,
    ): NovelImageCachePaths {
        val fileName = fileNameForUrl(url)
        val firstDirectoryPath = firstSaveRoot + imageFolderName + File.separator
        val secondDirectoryPath = secondSaveRoot + imageFolderName + File.separator
        return NovelImageCachePaths(
            fileName = fileName,
            firstFilePath = firstDirectoryPath + fileName,
            secondFilePath = secondDirectoryPath + fileName,
            firstDirectoryPath = firstDirectoryPath,
            secondDirectoryPath = secondDirectoryPath,
        )
    }

    fun shouldDownload(
        firstExists: Boolean,
        secondExists: Boolean,
        forceUpdate: Boolean,
    ): Boolean = (!firstExists && !secondExists) || forceUpdate
}
