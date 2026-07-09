package org.mewx.wenku8.reader.modern.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderLine
import org.mewx.wenku8.reader.modern.model.ReaderLineType
import org.mewx.wenku8.reader.modern.model.ReaderPage

class ReaderChromeUiModelTest {
    @Test
    fun fromStateUsesChapterNavigationAtChapterEdges() {
        val state = state(
            page = page(hasMoreBefore = false, hasMoreAfter = false),
        )

        val model = ReaderChromeUiModel.from(state)

        assertTrue(model.canGoPrevious)
        assertTrue(model.canGoNext)
        assertEquals("上一章", model.previousNavigationLabel)
        assertEquals("下一章", model.nextNavigationLabel)
        assertEquals("1/1", model.progressText)
    }

    @Test
    fun fromStatePrefersPageNavigationInsideChapter() {
        val state = state(
            page = page(hasMoreBefore = true, hasMoreAfter = true),
            pageIndex = 1,
            pageCount = 3,
            progressText = "2/3",
        )

        val model = ReaderChromeUiModel.from(state)

        assertTrue(model.canGoPrevious)
        assertTrue(model.canGoNext)
        assertEquals("上一页", model.previousNavigationLabel)
        assertEquals("下一页", model.nextNavigationLabel)
        assertEquals(1, model.pageIndex)
        assertEquals(3, model.pageCount)
        assertEquals("2/3", model.progressText)
    }

    @Test
    fun fromStateDisablesNavigationWithoutPageOrAdjacentChapters() {
        val state = ModernReaderUiState(
            title = "正文标题",
            chapterTitle = "",
            aid = 7,
            cid = 102,
            page = null,
            catalog = ModernReaderCatalog(
                volumeId = 12,
                volumeTitle = "第一卷",
                chapters = listOf(ReaderCatalogChapter(cid = 102, title = "第一章", isCurrent = true)),
            ),
        )

        val model = ReaderChromeUiModel.from(state)

        assertFalse(model.hasPage)
        assertFalse(model.canGoPrevious)
        assertFalse(model.canGoNext)
    }

    private fun state(
        page: ReaderPage,
        pageIndex: Int = 0,
        pageCount: Int = 1,
        progressText: String = "1/1",
    ): ModernReaderUiState =
        ModernReaderUiState(
            title = "正文标题",
            chapterTitle = "第一章",
            aid = 7,
            cid = 102,
            page = page,
            pageIndex = pageIndex,
            pageCount = pageCount,
            progressText = progressText,
            catalog = ModernReaderCatalog(
                volumeId = 12,
                volumeTitle = "第一卷",
                chapters = listOf(
                    ReaderCatalogChapter(cid = 101, title = "序章"),
                    ReaderCatalogChapter(cid = 102, title = "第一章", isCurrent = true),
                    ReaderCatalogChapter(cid = 103, title = "第二章"),
                ),
            ),
        )

    private fun page(
        hasMoreBefore: Boolean,
        hasMoreAfter: Boolean,
    ): ReaderPage =
        ReaderPage(
            start = ReaderCursor(blockIndex = 0, charIndex = 0),
            end = ReaderCursor(blockIndex = 0, charIndex = 4),
            lines = listOf(ReaderLine(type = ReaderLineType.TEXT, text = "测试")),
            hasMoreBefore = hasMoreBefore,
            hasMoreAfter = hasMoreAfter,
        )
}
