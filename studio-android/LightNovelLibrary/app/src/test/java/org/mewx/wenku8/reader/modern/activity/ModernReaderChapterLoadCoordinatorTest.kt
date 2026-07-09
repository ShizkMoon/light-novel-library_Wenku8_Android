package org.mewx.wenku8.reader.modern.activity

import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.data.ModernReaderContentRequest
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadFailure
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadResult
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.model.ReaderBlock
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ModernReaderChapterLoadCoordinatorTest {
    @Test
    fun loadsChapterInBackgroundAndPostsOutcomeToMain() {
        val queue = WorkQueue()
        val loadedOutcomes = mutableListOf<ModernReaderChapterLoadOutcome>()
        val capturedRequests = mutableListOf<ModernReaderContentRequest>()
        val capturedSettings = mutableListOf<ModernReaderDisplaySettings>()
        val document = ReaderDocument(
            title = "Loaded",
            blocks = listOf(ReaderBlock.Paragraph("abcdefghijk")),
        )
        val coordinator = coordinator(
            queue = queue,
            loadContent = { request ->
                capturedRequests += request
                ModernReaderLoadResult.Success(document)
            },
            createTextMeasurer = { settings ->
                capturedSettings += settings
                fixedTextMeasurer
            },
            createLayoutSpec = { settings ->
                capturedSettings += settings
                layout
            },
            initialCursorFor = { ReaderCursor(blockIndex = 0, charIndex = 4) },
        )

        val future = coordinator.loadChapter(
            args = launchArgs(aid = 7, cid = 101),
            fallbackTitle = "Fallback",
            chapterTitle = "Chapter",
            catalog = ModernReaderCatalog.from(volume = null, currentCid = 101),
            displaySettings = ModernReaderDisplaySettings(nightMode = true),
            isActive = { true },
            onLoaded = { loadedOutcomes += it },
        )
        queue.runBackground()
        queue.runMain()

        assertSame(queue.future, future)
        assertEquals(1, capturedRequests.size)
        assertEquals(7, capturedRequests.single().aid)
        assertEquals(101, capturedRequests.single().cid)
        assertEquals("Fallback", capturedRequests.single().chapterTitle)
        assertEquals(listOf(ModernReaderDisplaySettings(nightMode = true), ModernReaderDisplaySettings(nightMode = true)), capturedSettings)
        assertEquals(1, loadedOutcomes.size)
        assertEquals(document, loadedOutcomes.single().document)
        assertEquals(ReaderCursor(blockIndex = 0, charIndex = 4), loadedOutcomes.single().state.page?.start)
        assertEquals(true, loadedOutcomes.single().state.isNightMode)
    }

    @Test
    fun skipsMainThreadCallbackWhenOwnerIsInactive() {
        val queue = WorkQueue()
        val loadedOutcomes = mutableListOf<ModernReaderChapterLoadOutcome>()
        val coordinator = coordinator(
            queue = queue,
            loadContent = {
                ModernReaderLoadResult.Failure(ModernReaderLoadFailure.NETWORK_ERROR)
            },
        )

        coordinator.loadChapter(
            args = launchArgs(aid = 7, cid = 101),
            fallbackTitle = "Fallback",
            chapterTitle = "Chapter",
            catalog = ModernReaderCatalog.from(volume = null, currentCid = 101),
            displaySettings = ModernReaderDisplaySettings(),
            isActive = { false },
            onLoaded = { loadedOutcomes += it },
        )
        queue.runBackground()
        queue.runMain()

        assertTrue(loadedOutcomes.isEmpty())
    }

    private fun coordinator(
        queue: WorkQueue,
        loadContent: (ModernReaderContentRequest) -> ModernReaderLoadResult = {
            ModernReaderLoadResult.Failure(ModernReaderLoadFailure.NETWORK_ERROR)
        },
        createTextMeasurer: (ModernReaderDisplaySettings) -> ReaderTextMeasurer = { fixedTextMeasurer },
        createLayoutSpec: (ModernReaderDisplaySettings) -> ReaderLayoutSpec = { layout },
        initialCursorFor: (ReaderLaunchArguments) -> ReaderCursor = { ReaderCursor.START },
    ): ModernReaderChapterLoadCoordinator =
        ModernReaderChapterLoadCoordinator(
            loadContent = loadContent,
            createTextMeasurer = createTextMeasurer,
            createLayoutSpec = createLayoutSpec,
            initialCursorFor = initialCursorFor,
            runInBackground = queue::enqueueBackground,
            postToMain = queue::enqueueMain,
        )

    private class WorkQueue {
        val future = FakeFuture()
        private val background = mutableListOf<() -> Unit>()
        private val main = mutableListOf<() -> Unit>()

        fun enqueueBackground(work: () -> Unit): Future<*> {
            background += work
            return future
        }

        fun enqueueMain(work: () -> Unit) {
            main += work
        }

        fun runBackground() {
            background.removeAt(0).invoke()
        }

        fun runMain() {
            main.removeAt(0).invoke()
        }
    }

    private class FakeFuture : Future<Unit> {
        override fun cancel(mayInterruptIfRunning: Boolean): Boolean = false
        override fun isCancelled(): Boolean = false
        override fun isDone(): Boolean = false
        override fun get(): Unit = Unit
        override fun get(timeout: Long, unit: TimeUnit): Unit = Unit
    }

    private fun launchArgs(aid: Int, cid: Int): ReaderLaunchArguments =
        ReaderLaunchArguments(
            aid = aid,
            cid = cid,
            from = "fav",
            forceJump = false,
            volume = null,
            volumes = emptyList(),
        )

    private companion object {
        val fixedTextMeasurer = ReaderTextMeasurer { text -> text.length * 10f }
        val layout = ReaderLayoutSpec(
            contentWidthPx = 80,
            contentHeightPx = 120,
            fontHeightPx = 20,
            lineSpacingPx = 4,
            paragraphSpacingPx = 8,
        )
    }
}
