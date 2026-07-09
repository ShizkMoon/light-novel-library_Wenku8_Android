package org.mewx.wenku8.reader.modern.activity

import org.junit.Assert.assertEquals
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
import org.mewx.wenku8.reader.modern.ui.ModernReaderUiState

class ModernReaderReadingSessionCoordinatorTest {
    private val coordinator = ModernReaderReadingSessionCoordinator()

    @Test
    fun nextPageReturnsUpdatedStateAndCursorToPersist() {
        val session = sessionFor("甲乙丙丁戊己庚辛壬癸")
        val firstEnd = session.currentPage.end
        val currentState = stateFor(session)

        val update = coordinator.nextPage(
            session = session,
            currentState = currentState,
            displaySettings = nightSettings,
        )!!

        assertEquals(firstEnd, update.cursor)
        assertEquals(1, update.state.pageIndex)
        assertEquals(session.pageCount, update.state.pageCount)
        assertEquals("第 2 / ${session.pageCount} 页", update.state.progressText)
        assertEquals(firstEnd, update.state.page?.start)
        assertEquals(currentState.title, update.state.title)
        assertEquals(currentState.chapterTitle, update.state.chapterTitle)
        assertEquals(currentState.aid, update.state.aid)
        assertEquals(currentState.cid, update.state.cid)
        assertEquals(currentState.catalog, update.state.catalog)
        assertEquals(nightSettings, update.state.displaySettings)
    }

    @Test
    fun previousPageReturnsUpdatedStateAndCursorToPersist() {
        val session = sessionFor("甲乙丙丁戊己庚辛壬癸")
        session.nextPage()
        val currentState = stateFor(session)

        val update = coordinator.previousPage(
            session = session,
            currentState = currentState,
            displaySettings = daySettings,
        )!!

        assertEquals(ReaderCursor.START, update.cursor)
        assertEquals(0, update.state.pageIndex)
        assertEquals(ReaderCursor.START, update.state.page?.start)
        assertEquals("第 1 / ${session.pageCount} 页", update.state.progressText)
        assertEquals(daySettings, update.state.displaySettings)
    }

    @Test
    fun selectPageUsesSessionClampingAndReturnsCursorToPersist() {
        val session = sessionFor("甲乙丙丁戊己庚辛壬癸子丑寅卯辰巳午未")
        val currentState = stateFor(session)

        val update = coordinator.selectPage(
            pageIndex = 99,
            session = session,
            currentState = currentState,
            displaySettings = daySettings,
        )!!

        assertEquals(session.currentPage.start, update.cursor)
        assertEquals(session.pageCount - 1, update.state.pageIndex)
        assertEquals(session.currentPage, update.state.page)
    }

    @Test
    fun currentPageReturnsStateWithoutMovingSession() {
        val session = sessionFor("甲乙丙丁戊己庚辛壬癸")
        session.nextPage()
        val pageBeforeUpdate = session.currentPage
        val indexBeforeUpdate = session.pageIndex
        val currentState = stateFor(session)

        val update = coordinator.currentPage(
            session = session,
            currentState = currentState,
            displaySettings = nightSettings,
        )!!

        assertEquals(pageBeforeUpdate, session.currentPage)
        assertEquals(indexBeforeUpdate, session.pageIndex)
        assertEquals(pageBeforeUpdate.start, update.cursor)
        assertEquals(pageBeforeUpdate, update.state.page)
        assertEquals(indexBeforeUpdate, update.state.pageIndex)
        assertEquals(nightSettings, update.state.displaySettings)
    }

    @Test
    fun returnsNullWhenSessionOrCurrentStateIsMissing() {
        val session = sessionFor("甲乙丙丁")
        val currentState = stateFor(session)

        assertNull(coordinator.currentPage(null, currentState, daySettings))
        assertNull(coordinator.nextPage(null, currentState, daySettings))
        assertNull(coordinator.previousPage(session, null, daySettings))
        assertNull(coordinator.selectPage(0, null, null, daySettings))
    }

    private fun stateFor(session: ModernReaderSession): ModernReaderUiState =
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

    private fun sessionFor(text: String): ModernReaderSession =
        ModernReaderSession(
            document = ReaderDocument(
                title = "正文标题",
                blocks = listOf(ReaderBlock.Paragraph(text)),
            ),
            textMeasurer = ReaderTextMeasurer { measured -> measured.length * 10f },
            layout = ReaderLayoutSpec(
                contentWidthPx = 60,
                contentHeightPx = 20,
                fontHeightPx = 20,
                lineSpacingPx = 4,
                paragraphSpacingPx = 8,
            ),
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
