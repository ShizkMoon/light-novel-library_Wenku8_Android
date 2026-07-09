package org.mewx.wenku8.reader.modern.activity

import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.data.ModernReaderContentRequest
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadResult
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
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
        displaySettings: ModernReaderDisplaySettings,
        catalog: ModernReaderCatalog,
        initialCursor: ReaderCursor,
        createSession: (
            ReaderDocument,
            ModernReaderDisplaySettings,
            ReaderCursor,
        ) -> ModernReaderSession,
    ): ModernReaderChapterLoadOutcome =
        when (result) {
            is ModernReaderLoadResult.Success -> {
                val session = createSession(result.document, displaySettings, initialCursor)
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
                    state = ModernReaderStateFactory.failure(
                        aid = args.aid,
                        cid = args.cid,
                        fallbackTitle = fallbackTitle,
                        chapterTitle = chapterTitle,
                        failure = result.reason,
                        displaySettings = displaySettings,
                        catalog = catalog,
                    ),
                )
            }
        }
}
