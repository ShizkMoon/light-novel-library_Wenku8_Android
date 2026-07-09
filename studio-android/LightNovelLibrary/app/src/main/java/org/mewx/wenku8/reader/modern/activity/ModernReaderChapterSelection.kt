package org.mewx.wenku8.reader.modern.activity

import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchContext
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings
import org.mewx.wenku8.reader.modern.ui.ModernReaderUiState

data class ModernReaderChapterSelection(
    val args: ReaderLaunchArguments,
    val context: ReaderLaunchContext,
    val catalog: ModernReaderCatalog,
    val chapterTitle: String,
    val fallbackTitle: String,
    val loadingState: ModernReaderUiState,
) {
    companion object {
        fun prepare(
            currentArgs: ReaderLaunchArguments,
            chapter: ReaderCatalogChapter,
            displaySettings: ModernReaderDisplaySettings,
        ): ModernReaderChapterSelection? {
            if (chapter.cid == currentArgs.cid) return null

            val nextArgs = currentArgs.forChapter(chapter)
            val catalog = nextArgs.catalog()
            val chapterTitle = catalog.currentChapter?.title.orEmpty()
            val fallbackTitle = chapterTitle.ifBlank { "章节 ${nextArgs.cid}" }

            return ModernReaderChapterSelection(
                args = nextArgs,
                context = ReaderLaunchContext.from(nextArgs),
                catalog = catalog,
                chapterTitle = chapterTitle,
                fallbackTitle = fallbackTitle,
                loadingState = ModernReaderStateFactory.loading(
                    aid = nextArgs.aid,
                    cid = nextArgs.cid,
                    title = fallbackTitle,
                    chapterTitle = chapterTitle,
                    displaySettings = displaySettings,
                    catalog = catalog,
                ),
            )
        }
    }
}
