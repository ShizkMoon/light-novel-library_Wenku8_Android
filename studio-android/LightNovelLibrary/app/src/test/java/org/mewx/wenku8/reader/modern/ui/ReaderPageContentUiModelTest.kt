package org.mewx.wenku8.reader.modern.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderLine
import org.mewx.wenku8.reader.modern.model.ReaderLineType
import org.mewx.wenku8.reader.modern.model.ReaderPage

class ReaderPageContentUiModelTest {
    @Test
    fun fromStateShowsLoadingMessage() {
        val model = ReaderPageContentUiModel.from(
            state(
                chapterTitle = "第一章",
                isLoading = true,
            ),
        )

        assertEquals(ReaderPageContentMode.MESSAGE, model.mode)
        assertEquals("正在加载章节", model.messageTitle)
        assertEquals("第一章", model.message)
        assertNull(model.page)
    }

    @Test
    fun fromStateShowsFriendlyErrorMessage() {
        val model = ReaderPageContentUiModel.from(
            state(
                errorMessage = "本地章节不存在",
            ),
        )

        assertEquals(ReaderPageContentMode.MESSAGE, model.mode)
        assertEquals("章节加载失败", model.messageTitle)
        assertEquals("本地章节不存在", model.message)
        assertNull(model.page)
    }

    @Test
    fun fromStateShowsEmptyMessageWhenNoPage() {
        val model = ReaderPageContentUiModel.from(
            state(
                chapterTitle = "",
                title = "正文标题",
            ),
        )

        assertEquals(ReaderPageContentMode.MESSAGE, model.mode)
        assertEquals("暂无内容", model.messageTitle)
        assertEquals("正文标题", model.message)
        assertNull(model.page)
    }

    @Test
    fun fromStateShowsReaderPageWhenPageExists() {
        val page = page()

        val model = ReaderPageContentUiModel.from(
            state(page = page),
        )

        assertEquals(ReaderPageContentMode.PAGE, model.mode)
        assertSame(page, model.page)
        assertNull(model.messageTitle)
        assertNull(model.message)
    }

    private fun state(
        title: String = "正文标题",
        chapterTitle: String = "第一章",
        page: ReaderPage? = null,
        isLoading: Boolean = false,
        errorMessage: String? = null,
    ): ModernReaderUiState =
        ModernReaderUiState(
            title = title,
            chapterTitle = chapterTitle,
            aid = 7,
            cid = 9,
            page = page,
            isLoading = isLoading,
            errorMessage = errorMessage,
        )

    private fun page(): ReaderPage =
        ReaderPage(
            start = ReaderCursor(blockIndex = 0, charIndex = 0),
            end = ReaderCursor(blockIndex = 0, charIndex = 4),
            lines = listOf(ReaderLine(type = ReaderLineType.TEXT, text = "测试")),
            hasMoreBefore = false,
            hasMoreAfter = false,
        )
}
