package org.mewx.wenku8.reader.modern.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderLine
import org.mewx.wenku8.reader.modern.model.ReaderLineType
import org.mewx.wenku8.reader.modern.model.ReaderPage

class ReaderNavigationModelTest {
    @Test
    fun previousActionUsesPageWhenMoreContentBefore() {
        val action = ReaderNavigationModel.previousAction(
            state(page = page(hasMoreBefore = true, hasMoreAfter = false)),
        )

        assertEquals(ReaderNavigationTarget.PREVIOUS_PAGE, action.target)
        assertNull(action.chapter)
    }

    @Test
    fun previousActionUsesPreviousChapterAtChapterStart() {
        val action = ReaderNavigationModel.previousAction(
            state(page = page(hasMoreBefore = false, hasMoreAfter = false)),
        )

        assertEquals(ReaderNavigationTarget.PREVIOUS_CHAPTER, action.target)
        assertEquals(101, action.chapter?.cid)
    }

    @Test
    fun previousActionIsNoneWithoutPageOrPreviousChapter() {
        val action = ReaderNavigationModel.previousAction(
            state(page = null, chapters = listOf(currentChapter)),
        )

        assertEquals(ReaderNavigationTarget.NONE, action.target)
        assertNull(action.chapter)
    }

    @Test
    fun nextActionUsesPageWhenMoreContentAfter() {
        val action = ReaderNavigationModel.nextAction(
            state(page = page(hasMoreBefore = false, hasMoreAfter = true)),
        )

        assertEquals(ReaderNavigationTarget.NEXT_PAGE, action.target)
        assertNull(action.chapter)
    }

    @Test
    fun nextActionUsesNextChapterAtChapterEnd() {
        val action = ReaderNavigationModel.nextAction(
            state(page = page(hasMoreBefore = false, hasMoreAfter = false)),
        )

        assertEquals(ReaderNavigationTarget.NEXT_CHAPTER, action.target)
        assertEquals(103, action.chapter?.cid)
    }

    @Test
    fun nextActionIsNoneWithoutPageOrNextChapter() {
        val action = ReaderNavigationModel.nextAction(
            state(page = null, chapters = listOf(currentChapter)),
        )

        assertEquals(ReaderNavigationTarget.NONE, action.target)
        assertNull(action.chapter)
    }

    private val previousChapter = ReaderCatalogChapter(cid = 101, title = "Previous")
    private val currentChapter = ReaderCatalogChapter(cid = 102, title = "Current", isCurrent = true)
    private val nextChapter = ReaderCatalogChapter(cid = 103, title = "Next")

    private fun state(
        page: ReaderPage?,
        chapters: List<ReaderCatalogChapter> = listOf(previousChapter, currentChapter, nextChapter),
    ): ModernReaderUiState =
        ModernReaderUiState(
            title = "Title",
            chapterTitle = "Current",
            aid = 7,
            cid = 102,
            page = page,
            catalog = ModernReaderCatalog(
                volumeId = 12,
                volumeTitle = "Volume",
                chapters = chapters,
            ),
        )

    private fun page(
        hasMoreBefore: Boolean,
        hasMoreAfter: Boolean,
    ): ReaderPage =
        ReaderPage(
            start = ReaderCursor(blockIndex = 0, charIndex = 0),
            end = ReaderCursor(blockIndex = 0, charIndex = 4),
            lines = listOf(ReaderLine(type = ReaderLineType.TEXT, text = "Test")),
            hasMoreBefore = hasMoreBefore,
            hasMoreAfter = hasMoreAfter,
        )
}
