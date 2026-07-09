package org.mewx.wenku8.reader.modern.activity

import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchContext
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings
import org.mewx.wenku8.reader.modern.ui.ModernReaderUiState

data class ModernReaderInitialLaunch(
    val args: ReaderLaunchArguments,
    val context: ReaderLaunchContext,
    val catalog: ModernReaderCatalog,
    val chapterTitle: String,
    val fallbackTitle: String,
    val loadingState: ModernReaderUiState,
) {
    companion object {
        fun prepare(
            args: ReaderLaunchArguments,
            displaySettings: ModernReaderDisplaySettings,
        ): ModernReaderInitialLaunch {
            val catalog = args.catalog()
            val chapterTitle = catalog.currentChapter?.title.orEmpty()
            val fallbackTitle = chapterTitle.ifBlank { "章节 ${args.cid}" }

            return ModernReaderInitialLaunch(
                args = args,
                context = ReaderLaunchContext.from(args),
                catalog = catalog,
                chapterTitle = chapterTitle,
                fallbackTitle = fallbackTitle,
                loadingState = ModernReaderStateFactory.loading(
                    aid = args.aid,
                    cid = args.cid,
                    title = fallbackTitle,
                    chapterTitle = chapterTitle,
                    displaySettings = displaySettings,
                    catalog = catalog,
                ),
            )
        }
    }
}
