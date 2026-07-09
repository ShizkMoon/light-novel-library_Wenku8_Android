package org.mewx.wenku8.reader.modern.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Test
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.model.ReaderBlock
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ModernReaderDisplaySettingsCoordinatorTest {
    @Test
    fun updatesVisibleStateWhenReaderContentIsNotReady() {
        val currentState = stateFor(sessionFor(document))
        val coordinator = coordinator()

        val update = coordinator.apply(
            nextSettings = nightSettings,
            document = null,
            session = null,
            currentState = currentState,
        )

        assertEquals(nightSettings, update.state?.displaySettings)
        assertEquals(true, update.state?.isNightMode)
        assertEquals(currentState.title, update.state?.title)
        assertEquals(currentState.chapterTitle, update.state?.chapterTitle)
        assertNull(update.rebuiltSession)
        assertNull(update.cursorToPersist)
    }

    @Test
    fun rebuildsSessionFromCurrentPageStartWhenContentIsReady() {
        val session = sessionFor(document)
        session.nextPage()
        val currentPageStart = session.currentPage.start
        val currentState = stateFor(session)
        val captured = mutableListOf<CapturedSessionRequest>()
        val coordinator = coordinator(
            createSession = { nextDocument, settings, cursor ->
                captured += CapturedSessionRequest(nextDocument, settings, cursor)
                sessionFor(
                    document = nextDocument,
                    initialCursor = cursor,
                    layout = ReaderLayoutSpec(
                        contentWidthPx = 80,
                        contentHeightPx = 24,
                        fontHeightPx = 24,
                        lineSpacingPx = 4,
                        paragraphSpacingPx = 8,
                    ),
                )
            },
        )

        val update = coordinator.apply(
            nextSettings = nightSettings,
            document = document,
            session = session,
            currentState = currentState,
        )

        assertEquals(listOf(CapturedSessionRequest(document, nightSettings, currentPageStart)), captured)
        assertNotSame(session, update.rebuiltSession)
        assertEquals(currentPageStart, update.rebuiltSession?.currentPage?.start)
        assertEquals(currentPageStart, update.cursorToPersist)
        assertEquals(nightSettings, update.state?.displaySettings)
        assertEquals(true, update.state?.isNightMode)
        assertEquals(currentPageStart, update.state?.page?.start)
        assertEquals(currentState.title, update.state?.title)
        assertEquals(currentState.catalog, update.state?.catalog)
    }

    @Test
    fun doesNotRebuildSessionWhenCurrentStateIsMissing() {
        val session = sessionFor(document)
        val coordinator = coordinator()

        val update = coordinator.apply(
            nextSettings = nightSettings,
            document = document,
            session = session,
            currentState = null,
        )

        assertNull(update.state)
        assertNull(update.rebuiltSession)
        assertNull(update.cursorToPersist)
    }

    private fun coordinator(
        createSession: (
            ReaderDocument,
            ModernReaderDisplaySettings,
            ReaderCursor,
        ) -> ModernReaderSession = { nextDocument, _, cursor ->
            sessionFor(document = nextDocument, initialCursor = cursor)
        },
    ): ModernReaderDisplaySettingsCoordinator =
        ModernReaderDisplaySettingsCoordinator(createSession = createSession)

    private fun stateFor(session: ModernReaderSession) =
        ModernReaderStateFactory.reading(
            aid = 7,
            cid = 102,
            title = "正文标题",
            chapterTitle = "第一章",
            page = session.currentPage,
            pageIndex = session.pageIndex,
            pageCount = session.pageCount,
            catalog = catalog,
            displaySettings = daySettings,
        )

    private fun sessionFor(
        document: ReaderDocument,
        initialCursor: ReaderCursor = ReaderCursor.START,
        layout: ReaderLayoutSpec = defaultLayout,
    ): ModernReaderSession =
        ModernReaderSession(
            document = document,
            textMeasurer = ReaderTextMeasurer { measured -> measured.length * 10f },
            layout = layout,
            initialCursor = initialCursor,
        )

    private data class CapturedSessionRequest(
        val document: ReaderDocument,
        val settings: ModernReaderDisplaySettings,
        val cursor: ReaderCursor,
    )

    private companion object {
        val daySettings = ModernReaderDisplaySettings(
            fontSizeSp = 20,
            lineHeightSp = 30,
            nightMode = false,
        )
        val nightSettings = ModernReaderDisplaySettings(
            fontSizeSp = 22,
            lineHeightSp = 32,
            nightMode = true,
        )
        val document = ReaderDocument(
            title = "正文标题",
            blocks = listOf(ReaderBlock.Paragraph("甲乙丙丁戊己庚辛壬癸子丑寅卯辰巳午未")),
        )
        val defaultLayout = ReaderLayoutSpec(
            contentWidthPx = 60,
            contentHeightPx = 20,
            fontHeightPx = 20,
            lineSpacingPx = 4,
            paragraphSpacingPx = 8,
        )
        val catalog = ModernReaderCatalog(
            volumeId = 12,
            volumeTitle = "第一卷",
            chapters = listOf(
                ReaderCatalogChapter(cid = 101, title = "序章"),
                ReaderCatalogChapter(cid = 102, title = "第一章", isCurrent = true),
                ReaderCatalogChapter(cid = 103, title = "第二章"),
            ),
        )
    }
}
