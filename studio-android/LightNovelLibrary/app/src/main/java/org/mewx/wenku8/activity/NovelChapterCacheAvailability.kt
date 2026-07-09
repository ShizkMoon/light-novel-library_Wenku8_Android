package org.mewx.wenku8.activity

import java.io.File

data class NovelChapterCachePaths(
    val primary: String,
    val backup: String,
)

object NovelChapterCacheAvailability {
    fun paths(
        defaultStoragePath: String,
        backupStoragePath: String,
        saveFolderName: String,
        cid: Int,
    ): NovelChapterCachePaths {
        val relativePath = saveFolderName + File.separator + "novel" + File.separator + "$cid.xml"
        return NovelChapterCachePaths(
            primary = defaultStoragePath + relativePath,
            backup = backupStoragePath + relativePath,
        )
    }

    fun isCached(
        defaultStoragePath: String,
        backupStoragePath: String,
        saveFolderName: String,
        cid: Int,
        fileExists: (String) -> Boolean,
    ): Boolean {
        val paths = paths(
            defaultStoragePath = defaultStoragePath,
            backupStoragePath = backupStoragePath,
            saveFolderName = saveFolderName,
            cid = cid,
        )
        return fileExists(paths.primary) || fileExists(paths.backup)
    }
}
