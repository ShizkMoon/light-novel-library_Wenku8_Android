package org.mewx.wenku8.reader.modern.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadFailure
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadResult
import org.mewx.wenku8.reader.modern.data.ReaderContentSourceMode
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.model.ReaderBlock
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ModernReaderChapterLoadModelTest {
    @Test
    fun requestUsesLaunchArgumentsAndFallbackTitle() {
        val localRequest = ModernReaderChapterLoadModel.request(
            args = launchArgs(aid = 7, cid = 101, from = "fav"),
            fallbackTitle = "Fallback",
        )
        val cloudRequest = ModernReaderChapterLoadModel.request(
            args = launchArgs(aid = 8, cid = 202, from = "cloud"),
            fallbackTitle = "Cloud Fallback",
        )

        assertEquals(7, localRequest.aid)
        assertEquals(101, localRequest.cid)
        assertEquals("Fallback", localRequest.chapterTitle)
        assertEquals(ReaderContentSourceMode.LOCAL, localRequest.sourceMode)
        assertEquals(8, cloudRequest.aid)
        assertEquals(202, cloudRequest.cid)
        assertEquals("Cloud Fallback", cloudRequest.chapterTitle)
        assertEquals(ReaderContentSourceMode.CLOUD, cloudRequest.sourceMode)
    }

    @Test
    fun outcomeCreatesSessionAndStateFromSuccessResult() {
        val initialCursor = ReaderCursor(blockIndex = 0, charIndex = 4)
        val document = ReaderDocument(
            title = "Loaded Title",
            blocks = listOf(ReaderBlock.Paragraph("abcdefghijk")),
        )

        val outcome = ModernReaderChapterLoadModel.outcome(
            args = launchArgs(aid = 7, cid = 101),
            fallbackTitle = "Fallback",
            chapterTitle = "Chapter",
            result = ModernReaderLoadResult.Success(document),
            textMeasurer = fixedTextMeasurer,
            layout = layout,
            displaySettings = ModernReaderDisplaySettings(nightMode = true),
            catalog = ModernReaderCatalog.from(volume = null, currentCid = 101),
            initialCursor = initialCursor,
        )

        assertEquals(document, outcome.document)
        assertNotNull(outcome.session)
        assertEquals(initialCursor, outcome.session?.currentPage?.start)
        assertEquals("Loaded Title", outcome.state.title)
        assertEquals("Chapter", outcome.state.chapterTitle)
        assertEquals(initialCursor, outcome.state.page?.start)
        assertEquals(true, outcome.state.isNightMode)
    }

    @Test
    fun outcomeKeepsFailureWithoutSessionOrDocument() {
        val outcome = ModernReaderChapterLoadModel.outcome(
            args = launchArgs(aid = 7, cid = 101),
            fallbackTitle = "Fallback",
            chapterTitle = "Chapter",
            result = ModernReaderLoadResult.Failure(ModernReaderLoadFailure.NETWORK_ERROR),
            textMeasurer = fixedTextMeasurer,
            layout = layout,
            displaySettings = ModernReaderDisplaySettings(),
            catalog = ModernReaderCatalog.from(volume = null, currentCid = 101),
            initialCursor = ReaderCursor.START,
        )

        assertNull(outcome.document)
        assertNull(outcome.session)
        assertEquals("Fallback", outcome.state.title)
        assertNotNull(outcome.state.errorMessage)
    }

    private val fixedTextMeasurer = ReaderTextMeasurer { text -> text.length * 10f }

    private val layout = ReaderLayoutSpec(
        contentWidthPx = 80,
        contentHeightPx = 120,
        fontHeightPx = 20,
        lineSpacingPx = 4,
        paragraphSpacingPx = 8,
    )

    private fun launchArgs(
        aid: Int,
        cid: Int,
        from: String = "fav",
    ): ReaderLaunchArguments =
        ReaderLaunchArguments(
            aid = aid,
            cid = cid,
            from = from,
            forceJump = false,
            volume = null,
            volumes = emptyList(),
        )
}
