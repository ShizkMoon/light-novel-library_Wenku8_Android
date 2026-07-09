package org.mewx.wenku8.reader.modern.paging

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.reader.modern.model.ReaderBlock
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec

class ModernReaderSessionTest {
    private val measurer = ReaderTextMeasurer { text -> text.length * 10f }
    private val layout = ReaderLayoutSpec(
        contentWidthPx = 60,
        contentHeightPx = 20,
        fontHeightPx = 20,
        lineSpacingPx = 4,
        paragraphSpacingPx = 8,
    )

    @Test
    fun nextPageAdvancesFromCurrentPageEnd() {
        val session = ModernReaderSession(
            document = ReaderDocument(
                title = "测试卷",
                blocks = listOf(ReaderBlock.Paragraph("甲乙丙丁戊己庚辛壬癸")),
            ),
            textMeasurer = measurer,
            layout = layout,
        )
        val firstEnd = session.currentPage.end

        val next = session.nextPage()

        assertEquals(firstEnd, next.start)
        assertEquals(1, session.pageIndex)
        assertTrue(next.hasMoreBefore)
    }

    @Test
    fun previousPageReturnsToThePageBeforeCurrentStart() {
        val session = ModernReaderSession(
            document = ReaderDocument(
                title = "测试卷",
                blocks = listOf(ReaderBlock.Paragraph("甲乙丙丁戊己庚辛壬癸")),
            ),
            textMeasurer = measurer,
            layout = layout,
        )
        val firstStart = session.currentPage.start
        session.nextPage()

        val previous = session.previousPage()

        assertEquals(firstStart, previous.start)
        assertEquals(0, session.pageIndex)
        assertFalse(previous.hasMoreBefore)
    }

    @Test
    fun previousPageAtDocumentStartKeepsCurrentPage() {
        val session = ModernReaderSession(
            document = ReaderDocument(
                title = "测试卷",
                blocks = listOf(ReaderBlock.Paragraph("甲乙丙丁")),
            ),
            textMeasurer = measurer,
            layout = layout,
        )

        val previous = session.previousPage()

        assertEquals(ReaderCursor.START, previous.start)
        assertEquals(0, session.pageIndex)
    }

    @Test
    fun initialCursorStartsFromSavedPageAndCountsPagesBeforeIt() {
        val document = ReaderDocument(
            title = "测试卷",
            blocks = listOf(ReaderBlock.Paragraph("甲乙丙丁戊己庚辛壬癸")),
        )
        val firstPage = ModernReaderPaginator(document, measurer).pageFrom(ReaderCursor.START, layout)

        val session = ModernReaderSession(
            document = document,
            textMeasurer = measurer,
            layout = layout,
            initialCursor = firstPage.end,
        )

        assertEquals(firstPage.end, session.currentPage.start)
        assertEquals(1, session.pageIndex)
    }

    @Test
    fun pageCountReflectsAllPagesInDocument() {
        val session = ModernReaderSession(
            document = ReaderDocument(
                title = "测试卷",
                blocks = listOf(ReaderBlock.Paragraph("甲乙丙丁戊己庚辛壬癸子丑寅卯辰巳午未")),
            ),
            textMeasurer = measurer,
            layout = layout,
        )

        assertEquals(4, session.pageCount)
        assertEquals(0, session.pageIndex)
    }

    @Test
    fun goToPageNavigatesToRequestedPage() {
        val session = ModernReaderSession(
            document = ReaderDocument(
                title = "测试卷",
                blocks = listOf(ReaderBlock.Paragraph("甲乙丙丁戊己庚辛壬癸子丑寅卯辰巳午未")),
            ),
            textMeasurer = measurer,
            layout = layout,
        )

        val page = session.goToPage(2)

        assertEquals(2, session.pageIndex)
        assertEquals("子丑寅卯辰巳", page.lines.single().text)
        assertTrue(page.hasMoreBefore)
        assertTrue(page.hasMoreAfter)
    }

    @Test
    fun goToPageClampsOutsideRange() {
        val session = ModernReaderSession(
            document = ReaderDocument(
                title = "测试卷",
                blocks = listOf(ReaderBlock.Paragraph("甲乙丙丁戊己庚辛壬癸子丑寅卯辰巳午未")),
            ),
            textMeasurer = measurer,
            layout = layout,
        )

        val first = session.goToPage(-1)
        assertEquals(0, session.pageIndex)
        assertEquals(ReaderCursor.START, first.start)

        val last = session.goToPage(99)
        assertEquals(3, session.pageIndex)
        assertEquals("午未", last.lines.single().text)
        assertTrue(last.hasMoreBefore)
        assertFalse(last.hasMoreAfter)
    }
}
