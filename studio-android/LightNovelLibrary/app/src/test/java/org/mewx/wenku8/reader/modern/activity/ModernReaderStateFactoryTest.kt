package org.mewx.wenku8.reader.modern.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadFailure
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadResult
import org.mewx.wenku8.reader.modern.model.ReaderBlock
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.model.ReaderLine
import org.mewx.wenku8.reader.modern.model.ReaderLineType
import org.mewx.wenku8.reader.modern.model.ReaderPage
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ModernReaderStateFactoryTest {
    @Test
    fun successStateRendersFirstPageFromLoadedDocument() {
        val document = ReaderDocument(
            title = "正文标题",
            blocks = listOf(ReaderBlock.Paragraph("测试正文内容")),
        )
        val capturedSessions = mutableListOf<CapturedSessionRequest>()

        val state = ModernReaderStateFactory.fromLoadResult(
            aid = 7,
            cid = 9,
            fallbackTitle = "备用标题",
            chapterTitle = "第一章",
            result = ModernReaderLoadResult.Success(document),
            createSession = { nextDocument, settings, cursor ->
                capturedSessions += CapturedSessionRequest(nextDocument, settings, cursor)
                sessionFor(nextDocument, cursor)
            },
        )

        assertEquals(
            listOf(
                CapturedSessionRequest(
                    document = document,
                    settings = ModernReaderDisplaySettings(),
                    cursor = ReaderCursor.START,
                ),
            ),
            capturedSessions,
        )
        assertEquals("正文标题", state.title)
        assertEquals("第一章", state.chapterTitle)
        assertEquals(7, state.aid)
        assertEquals(9, state.cid)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals("第 1 / 1 页", state.progressText)
        assertNotNull(state.page)
    }

    @Test
    fun failureStateUsesReaderFriendlyMessage() {
        val state = ModernReaderStateFactory.fromLoadResult(
            aid = 7,
            cid = 9,
            fallbackTitle = "备用标题",
            chapterTitle = "第一章",
            result = ModernReaderLoadResult.Failure(ModernReaderLoadFailure.LOCAL_CONTENT_MISSING),
            createSession = { _, _, _ -> error("failure state must not create a session") },
        )

        assertEquals("备用标题", state.title)
        assertTrue(state.errorMessage!!.contains("本地章节"))
        assertNull(state.page)
        assertFalse(state.isLoading)
    }

    @Test
    fun readingStateUsesOneBasedPageIndexInProgressText() {
        val page = ReaderPage(
            start = ReaderCursor(blockIndex = 0, charIndex = 0),
            end = ReaderCursor(blockIndex = 0, charIndex = 4),
            lines = listOf(ReaderLine(type = ReaderLineType.TEXT, text = "测试")),
            hasMoreBefore = true,
            hasMoreAfter = true,
        )

        val state = ModernReaderStateFactory.reading(
            aid = 7,
            cid = 9,
            title = "正文标题",
            chapterTitle = "第一章",
            page = page,
            pageIndex = 2,
            pageCount = 8,
        )

        assertEquals(page, state.page)
        assertEquals(2, state.pageIndex)
        assertEquals(8, state.pageCount)
        assertEquals("第 3 / 8 页", state.progressText)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun readingStateCarriesDisplaySettings() {
        val page = ReaderPage(
            start = ReaderCursor(blockIndex = 0, charIndex = 0),
            end = ReaderCursor(blockIndex = 0, charIndex = 4),
            lines = listOf(ReaderLine(type = ReaderLineType.TEXT, text = "测试")),
            hasMoreBefore = true,
            hasMoreAfter = true,
        )
        val displaySettings = ModernReaderDisplaySettings(
            fontSizeSp = 22,
            lineHeightSp = 34,
            nightMode = true,
        )

        val state = ModernReaderStateFactory.reading(
            aid = 7,
            cid = 9,
            title = "正文标题",
            chapterTitle = "第一章",
            page = page,
            pageIndex = 0,
            pageCount = 1,
            displaySettings = displaySettings,
        )

        assertEquals(displaySettings, state.displaySettings)
        assertTrue(state.isNightMode)
    }

    @Test
    fun readingStateCarriesCatalog() {
        val page = ReaderPage(
            start = ReaderCursor(blockIndex = 0, charIndex = 0),
            end = ReaderCursor(blockIndex = 0, charIndex = 4),
            lines = listOf(ReaderLine(type = ReaderLineType.TEXT, text = "测试")),
            hasMoreBefore = false,
            hasMoreAfter = false,
        )
        val catalog = ModernReaderCatalog(
            volumeId = 12,
            volumeTitle = "第一卷",
            chapters = listOf(
                ReaderCatalogChapter(cid = 101, title = "序章"),
                ReaderCatalogChapter(cid = 102, title = "第一章", isCurrent = true),
            ),
        )

        val state = ModernReaderStateFactory.reading(
            aid = 7,
            cid = 102,
            title = "正文标题",
            chapterTitle = "第一章",
            page = page,
            pageIndex = 0,
            pageCount = 1,
            catalog = catalog,
        )

        assertEquals(catalog, state.catalog)
        assertEquals(102, state.catalog.currentChapter?.cid)
    }

    @Test
    fun readingStateCanNavigateAcrossChapterBoundaries() {
        val page = ReaderPage(
            start = ReaderCursor(blockIndex = 0, charIndex = 0),
            end = ReaderCursor(blockIndex = 0, charIndex = 4),
            lines = listOf(ReaderLine(type = ReaderLineType.TEXT, text = "测试")),
            hasMoreBefore = false,
            hasMoreAfter = false,
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

        val state = ModernReaderStateFactory.reading(
            aid = 7,
            cid = 102,
            title = "正文标题",
            chapterTitle = "第一章",
            page = page,
            pageIndex = 0,
            pageCount = 1,
            catalog = catalog,
        )

        assertTrue(state.canGoPrevious)
        assertTrue(state.canGoNext)
        assertEquals("上一章", state.previousNavigationLabel)
        assertEquals("下一章", state.nextNavigationLabel)
    }

    @Test
    fun readingStatePrefersPageNavigationLabelsInsideChapter() {
        val page = ReaderPage(
            start = ReaderCursor(blockIndex = 0, charIndex = 0),
            end = ReaderCursor(blockIndex = 0, charIndex = 4),
            lines = listOf(ReaderLine(type = ReaderLineType.TEXT, text = "测试")),
            hasMoreBefore = true,
            hasMoreAfter = true,
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

        val state = ModernReaderStateFactory.reading(
            aid = 7,
            cid = 102,
            title = "正文标题",
            chapterTitle = "第一章",
            page = page,
            pageIndex = 1,
            pageCount = 3,
            catalog = catalog,
        )

        assertTrue(state.canGoPrevious)
        assertTrue(state.canGoNext)
        assertEquals("上一页", state.previousNavigationLabel)
        assertEquals("下一页", state.nextNavigationLabel)
    }

    private data class CapturedSessionRequest(
        val document: ReaderDocument,
        val settings: ModernReaderDisplaySettings,
        val cursor: ReaderCursor,
    )

    private fun sessionFor(
        document: ReaderDocument,
        cursor: ReaderCursor,
    ): ModernReaderSession =
        ModernReaderSession(
            document = document,
            textMeasurer = ReaderTextMeasurer { text -> text.length * 10f },
            layout = ReaderLayoutSpec(
                contentWidthPx = 120,
                contentHeightPx = 160,
                fontHeightPx = 20,
                lineSpacingPx = 4,
                paragraphSpacingPx = 8,
            ),
            initialCursor = cursor,
        )
}
