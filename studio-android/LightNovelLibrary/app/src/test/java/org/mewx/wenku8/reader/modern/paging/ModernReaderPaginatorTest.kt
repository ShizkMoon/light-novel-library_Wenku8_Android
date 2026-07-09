package org.mewx.wenku8.reader.modern.paging

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mewx.wenku8.reader.modern.model.ReaderBlock
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.model.ReaderLineType

class ModernReaderPaginatorTest {
    private val fixedMeasurer = ReaderTextMeasurer { text -> text.length * 10f }
    private val layout = ReaderLayoutSpec(
        contentWidthPx = 60,
        contentHeightPx = 72,
        fontHeightPx = 20,
        lineSpacingPx = 4,
        paragraphSpacingPx = 8,
        paragraphIndentChars = 2,
    )

    @Test
    fun pageFromWrapsCjkParagraphWithFullWidthIndent() {
        val document = ReaderDocument(
            title = "测试卷",
            blocks = listOf(ReaderBlock.Paragraph("甲乙丙丁戊己庚辛")),
        )

        val page = ModernReaderPaginator(document, fixedMeasurer).pageFrom(ReaderCursor.START, layout)

        assertEquals(listOf("　　甲乙丙丁", "戊己庚辛"), page.lines.map { it.text })
        assertEquals(ReaderCursor(blockIndex = 1, charIndex = 0), page.end)
        assertFalse(page.hasMoreBefore)
        assertFalse(page.hasMoreAfter)
    }

    @Test
    fun pageBeforeFindsThePageEndingAtCursor() {
        val document = ReaderDocument(
            title = "测试卷",
            blocks = listOf(
                ReaderBlock.Paragraph("甲乙丙丁戊己庚辛"),
                ReaderBlock.Paragraph("子丑寅卯辰巳午未"),
            ),
        )
        val paginator = ModernReaderPaginator(document, fixedMeasurer)
        val firstPage = paginator.pageFrom(ReaderCursor.START, layout)
        val secondPage = paginator.pageFrom(firstPage.end, layout)

        val previous = paginator.pageBefore(secondPage.end, layout)

        assertEquals(secondPage.start, previous.start)
        assertEquals(secondPage.end, previous.end)
        assertEquals(secondPage.lines.map { it.text }, previous.lines.map { it.text })
        assertEquals(true, previous.hasMoreBefore)
    }

    @Test
    fun imageBlockOccupiesASinglePlaceholderPage() {
        val document = ReaderDocument(
            title = "插图章",
            blocks = listOf(
                ReaderBlock.Image("https://example.invalid/1.jpg"),
                ReaderBlock.Paragraph("甲乙丙丁"),
            ),
        )

        val page = ModernReaderPaginator(document, fixedMeasurer).pageFrom(ReaderCursor.START, layout)

        assertEquals(1, page.lines.size)
        assertEquals(ReaderLineType.IMAGE, page.lines.single().type)
        assertEquals("https://example.invalid/1.jpg", page.lines.single().source)
        assertEquals(ReaderCursor(blockIndex = 1, charIndex = 0), page.end)
        assertEquals(true, page.hasMoreAfter)
    }
}
