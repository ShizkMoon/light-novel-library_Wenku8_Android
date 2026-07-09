package org.mewx.wenku8.reader.modern.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter

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
    }
}
