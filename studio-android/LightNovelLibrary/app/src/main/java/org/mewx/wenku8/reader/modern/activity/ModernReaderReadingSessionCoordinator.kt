package org.mewx.wenku8.reader.modern.activity

import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings
import org.mewx.wenku8.reader.modern.ui.ModernReaderUiState

data class ModernReaderReadingSessionUpdate(
    val state: ModernReaderUiState,
    val cursor: ReaderCursor,
)

class ModernReaderReadingSessionCoordinator {
    fun currentPage(
        session: ModernReaderSession?,
        currentState: ModernReaderUiState?,
        displaySettings: ModernReaderDisplaySettings,
    ): ModernReaderReadingSessionUpdate? {
        if (session == null || currentState == null) return null

        return updateFrom(session, currentState, displaySettings)
    }

    fun previousPage(
        session: ModernReaderSession?,
        currentState: ModernReaderUiState?,
        displaySettings: ModernReaderDisplaySettings,
    ): ModernReaderReadingSessionUpdate? {
        if (session == null || currentState == null) return null

        session.previousPage()
        return updateFrom(session, currentState, displaySettings)
    }

    fun nextPage(
        session: ModernReaderSession?,
        currentState: ModernReaderUiState?,
        displaySettings: ModernReaderDisplaySettings,
    ): ModernReaderReadingSessionUpdate? {
        if (session == null || currentState == null) return null

        session.nextPage()
        return updateFrom(session, currentState, displaySettings)
    }

    fun selectPage(
        pageIndex: Int,
        session: ModernReaderSession?,
        currentState: ModernReaderUiState?,
        displaySettings: ModernReaderDisplaySettings,
    ): ModernReaderReadingSessionUpdate? {
        if (session == null || currentState == null) return null

        session.goToPage(pageIndex)
        return updateFrom(session, currentState, displaySettings)
    }

    private fun updateFrom(
        session: ModernReaderSession,
        currentState: ModernReaderUiState,
        displaySettings: ModernReaderDisplaySettings,
    ): ModernReaderReadingSessionUpdate {
        val page = session.currentPage
        return ModernReaderReadingSessionUpdate(
            state = ModernReaderStateFactory.reading(
                aid = currentState.aid,
                cid = currentState.cid,
                title = currentState.title,
                chapterTitle = currentState.chapterTitle,
                page = page,
                pageIndex = session.pageIndex,
                pageCount = session.pageCount,
                catalog = currentState.catalog,
                displaySettings = displaySettings,
            ),
            cursor = page.start,
        )
    }
}
