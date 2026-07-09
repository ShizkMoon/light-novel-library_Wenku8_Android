package org.mewx.wenku8.reader.modern.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogSection

class ReaderCatalogUiModelTest {
    @Test
    fun fromKnownCatalogDoesNotShowUnavailableMessage() {
        val model = ReaderCatalogUiModel.from(
            title = "书名",
            chapterTitle = "第一章",
            catalog = ModernReaderCatalog(
                volumeId = 1,
                volumeTitle = "第一卷",
                chapters = listOf(ReaderCatalogChapter(cid = 11, title = "第一章", isCurrent = true)),
            ),
        )

        assertEquals("第一章", model.fallbackTitle)
        assertNull(model.supportingMessage)
        assertEquals("1 卷 / 1 章", model.summaryText)
    }

    @Test
    fun fromFallbackCatalogExplainsThatOnlyCurrentChapterIsAvailable() {
        val model = ReaderCatalogUiModel.from(
            title = "书名",
            chapterTitle = "",
            catalog = ModernReaderCatalog.from(volume = null, currentCid = 55),
        )

        assertEquals("书名", model.fallbackTitle)
        assertEquals("目录数据暂不可用，仅显示当前章节。", model.supportingMessage)
        assertEquals("当前章节", model.summaryText)
    }

    @Test
    fun fromCatalogBuildsSectionAndChapterRowsForRendering() {
        val currentChapter = ReaderCatalogChapter(cid = 102, title = "第一章", isCurrent = true, volumeId = 1)
        val nextChapter = ReaderCatalogChapter(cid = 103, title = "第二章", volumeId = 1)
        val catalog = ModernReaderCatalog(
            volumeId = 1,
            volumeTitle = "第一卷",
            chapters = listOf(currentChapter, nextChapter),
            sections = listOf(
                ReaderCatalogSection(
                    volumeId = 1,
                    title = "第一卷",
                    chapters = listOf(currentChapter, nextChapter),
                ),
            ),
        )

        val model = ReaderCatalogUiModel.from(
            title = "书名",
            chapterTitle = "第一章",
            catalog = catalog,
        )

        assertEquals(1, model.sections.size)
        assertEquals("第一卷", model.sections[0].title)
        assertEquals(1, model.sections[0].volumeId)
        assertEquals(2, model.sections[0].chapters.size)
        assertEquals("第一章", model.sections[0].chapters[0].title)
        assertEquals("当前", model.sections[0].chapters[0].stateLabel)
        assertFalse(model.sections[0].chapters[0].isSelectable)
        assertEquals(currentChapter, model.sections[0].chapters[0].source)
        assertEquals("第二章", model.sections[0].chapters[1].title)
        assertNull(model.sections[0].chapters[1].stateLabel)
        assertTrue(model.sections[0].chapters[1].isSelectable)
        assertEquals(nextChapter, model.sections[0].chapters[1].source)
    }

    @Test
    fun blankSectionTitleFallsBackToCurrentChapterOrBookTitle() {
        val catalog = ModernReaderCatalog.from(volume = null, currentCid = 55)

        val model = ReaderCatalogUiModel.from(
            title = "书名",
            chapterTitle = "",
            catalog = catalog,
        )

        assertEquals("书名", model.sections.single().title)
        assertEquals("章节 55", model.sections.single().chapters.single().title)
        assertFalse(model.sections.single().chapters.single().isSelectable)
    }

    @Test
    fun selectChapterReturnsSourceForSelectableRow() {
        val currentChapter = ReaderCatalogChapter(cid = 102, title = "第一章", isCurrent = true, volumeId = 1)
        val nextChapter = ReaderCatalogChapter(cid = 103, title = "第二章", volumeId = 1)
        val catalog = ModernReaderCatalog(
            volumeId = 1,
            volumeTitle = "第一卷",
            chapters = listOf(currentChapter, nextChapter),
            sections = listOf(
                ReaderCatalogSection(
                    volumeId = 1,
                    title = "第一卷",
                    chapters = listOf(currentChapter, nextChapter),
                ),
            ),
        )

        val model = ReaderCatalogUiModel.from(
            title = "书名",
            chapterTitle = "第一章",
            catalog = catalog,
        )

        assertEquals(nextChapter, model.selectChapter(model.sections.single().chapters[1]))
    }

    @Test
    fun selectChapterReturnsNullForCurrentRow() {
        val catalog = ModernReaderCatalog.from(volume = null, currentCid = 55)
        val model = ReaderCatalogUiModel.from(
            title = "书名",
            chapterTitle = "",
            catalog = catalog,
        )

        assertNull(model.selectChapter(model.sections.single().chapters.single()))
    }
}
