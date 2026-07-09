package org.mewx.wenku8.reader.modern.activity

import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchContext
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings
import org.mewx.wenku8.reader.modern.ui.ModernReaderUiState

data class ModernReaderChapterSwitchUpdate(
    val args: ReaderLaunchArguments,
    val context: ReaderLaunchContext,
    val catalog: ModernReaderCatalog,
    val chapterTitle: String,
    val fallbackTitle: String,
    val loadingState: ModernReaderUiState,
    val shouldSaveCurrentProgress: Boolean = true,
    val shouldCancelCurrentLoad: Boolean = true,
    val shouldClearCurrentReaderState: Boolean = true,
)

class ModernReaderChapterSwitchCoordinator {
    fun prepare(
        currentArgs: ReaderLaunchArguments?,
        chapter: ReaderCatalogChapter,
        displaySettings: ModernReaderDisplaySettings,
    ): ModernReaderChapterSwitchUpdate? {
        val args = currentArgs ?: return null
        val selection = ModernReaderChapterSelection.prepare(
            currentArgs = args,
            chapter = chapter,
            displaySettings = displaySettings,
        ) ?: return null

        return ModernReaderChapterSwitchUpdate(
            args = selection.args,
            context = selection.context,
            catalog = selection.catalog,
            chapterTitle = selection.chapterTitle,
            fallbackTitle = selection.fallbackTitle,
            loadingState = selection.loadingState,
        )
    }
}
