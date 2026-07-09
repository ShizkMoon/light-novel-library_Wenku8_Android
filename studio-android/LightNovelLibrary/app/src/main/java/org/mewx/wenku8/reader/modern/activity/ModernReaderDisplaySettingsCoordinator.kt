package org.mewx.wenku8.reader.modern.activity

import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings
import org.mewx.wenku8.reader.modern.ui.ModernReaderUiState

data class ModernReaderDisplaySettingsUpdate(
    val state: ModernReaderUiState?,
    val rebuiltSession: ModernReaderSession?,
    val cursorToPersist: ReaderCursor?,
)

class ModernReaderDisplaySettingsCoordinator(
    private val readingSessionCoordinator: ModernReaderReadingSessionCoordinator =
        ModernReaderReadingSessionCoordinator(),
    private val createSession: (
        ReaderDocument,
        ModernReaderDisplaySettings,
        ReaderCursor,
    ) -> ModernReaderSession,
) {
    fun apply(
        nextSettings: ModernReaderDisplaySettings,
        document: ReaderDocument?,
        session: ModernReaderSession?,
        currentState: ModernReaderUiState?,
    ): ModernReaderDisplaySettingsUpdate {
        if (currentState == null) {
            return ModernReaderDisplaySettingsUpdate(
                state = null,
                rebuiltSession = null,
                cursorToPersist = null,
            )
        }

        val stateWithSettings = currentState.copy(
            displaySettings = nextSettings,
            isNightMode = nextSettings.nightMode,
        )
        if (document == null || session == null) {
            return ModernReaderDisplaySettingsUpdate(
                state = stateWithSettings,
                rebuiltSession = null,
                cursorToPersist = null,
            )
        }

        val rebuiltSession = createSession(document, nextSettings, session.currentPage.start)
        val readingUpdate = readingSessionCoordinator.currentPage(
            session = rebuiltSession,
            currentState = stateWithSettings,
            displaySettings = nextSettings,
        )
        return ModernReaderDisplaySettingsUpdate(
            state = readingUpdate?.state ?: stateWithSettings,
            rebuiltSession = rebuiltSession,
            cursorToPersist = readingUpdate?.cursor,
        )
    }
}
