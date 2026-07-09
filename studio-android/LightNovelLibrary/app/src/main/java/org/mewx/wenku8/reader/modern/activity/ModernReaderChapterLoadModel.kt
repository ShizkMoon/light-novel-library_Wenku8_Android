package org.mewx.wenku8.reader.modern.activity

import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.data.ModernReaderContentRequest
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadResult
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings
import org.mewx.wenku8.reader.modern.ui.ModernReaderUiState

data class ModernReaderChapterLoadOutcome(
    val document: ReaderDocument?,
    val session: ModernReaderSession?,
    val state: ModernReaderUiState,
)

object ModernReaderChapterLoadModel {
    fun request(
        args: ReaderLaunchArguments,
        fallbackTitle: String,
    ): ModernReaderContentRequest =
        ModernReaderContentRequest(
            aid = args.aid,
            cid = args.cid,
            chapterTitle = fallbackTitle,
            sourceMode = args.sourceMode(),
        )

    fun outcome(
        args: ReaderLaunchArguments,
        fallbackTitle: String,
        chapterTitle: String,
        result: ModernReaderLoadResult,
        textMeasurer: ReaderTextMeasurer,
        layout: ReaderLayoutSpec,
        displaySettings: ModernReaderDisplaySettings,
        catalog: ModernReaderCatalog,
        initialCursor: ReaderCursor,
    ): ModernReaderChapterLoadOutcome =
        when (result) {
            is ModernReaderLoadResult.Success -> {
                val session = ModernReaderSession(
                    document = result.document,
                    textMeasurer = textMeasurer,
                    layout = layout,
                    initialCursor = initialCursor,
                )
                ModernReaderChapterLoadOutcome(
                    document = result.document,
                    session = session,
                    state = ModernReaderStateFactory.reading(
                        aid = args.aid,
                        cid = args.cid,
                        title = result.document.title.ifBlank { fallbackTitle },
                        chapterTitle = chapterTitle,
                        page = session.currentPage,
                        pageIndex = session.pageIndex,
                        pageCount = session.pageCount,
                        catalog = catalog,
                        displaySettings = displaySettings,
                    ),
                )
            }
            is ModernReaderLoadResult.Failure -> {
                ModernReaderChapterLoadOutcome(
                    document = null,
                    session = null,
                    state = ModernReaderStateFactory.fromLoadResult(
                        aid = args.aid,
                        cid = args.cid,
                        fallbackTitle = fallbackTitle,
                        chapterTitle = chapterTitle,
                        result = result,
                        textMeasurer = textMeasurer,
                        layout = layout,
                        displaySettings = displaySettings,
                        catalog = catalog,
                    ),
                )
            }
        }
}
