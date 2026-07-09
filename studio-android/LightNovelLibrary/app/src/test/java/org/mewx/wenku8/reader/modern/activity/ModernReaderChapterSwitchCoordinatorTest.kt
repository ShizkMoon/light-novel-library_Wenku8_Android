package org.mewx.wenku8.reader.modern.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.global.api.ChapterInfo
import org.mewx.wenku8.global.api.VolumeList
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ModernReaderChapterSwitchCoordinatorTest {
    private val coordinator = ModernReaderChapterSwitchCoordinator()

    @Test
    fun returnsNullWhenCurrentArgumentsAreMissing() {
        val update = coordinator.prepare(
            currentArgs = null,
            chapter = ReaderCatalogChapter(cid = 201, title = "Next"),
            displaySettings = ModernReaderDisplaySettings(),
        )

        assertNull(update)
    }

    @Test
    fun returnsNullWhenSelectingCurrentChapter() {
        val update = coordinator.prepare(
            currentArgs = launchArgs(cid = 101),
            chapter = ReaderCatalogChapter(cid = 101, title = "Current"),
            displaySettings = ModernReaderDisplaySettings(),
        )

        assertNull(update)
    }

    @Test
    fun preparesActivitySideEffectsAndNextLoadingStateForChapterSwitch() {
        val first = volume(1, "First Volume", listOf(chapter(101, "Start")))
        val second = volume(2, "Second Volume", listOf(chapter(201, "Next")))
        val displaySettings = ModernReaderDisplaySettings(
            fontSizeSp = 23,
            lineHeightSp = 35,
            nightMode = true,
        )

        val update = coordinator.prepare(
            currentArgs = launchArgs(
                cid = 101,
                forceJump = true,
                volume = first,
                volumes = listOf(first, second),
            ),
            chapter = ReaderCatalogChapter(
                cid = 201,
                title = "Next",
                volumeId = 2,
            ),
            displaySettings = displaySettings,
        )!!

        assertTrue(update.shouldSaveCurrentProgress)
        assertTrue(update.shouldCancelCurrentLoad)
        assertTrue(update.shouldClearCurrentReaderState)
        assertEquals(201, update.args.cid)
        assertFalse(update.args.forceJump)
        assertEquals(2, update.context.vid)
        assertEquals(201, update.context.cid)
        assertEquals(201, update.catalog.currentChapter?.cid)
        assertEquals("Next", update.chapterTitle)
        assertEquals("Next", update.fallbackTitle)
        assertEquals(displaySettings, update.loadingState.displaySettings)
        assertTrue(update.loadingState.isLoading)
        assertEquals("加载中", update.loadingState.progressText)
    }

    private fun launchArgs(
        aid: Int = 7,
        cid: Int,
        from: String = "fav",
        forceJump: Boolean = false,
        volume: VolumeList? = null,
        volumes: List<VolumeList> = volume?.let { listOf(it) }.orEmpty(),
    ): ReaderLaunchArguments =
        ReaderLaunchArguments(
            aid = aid,
            cid = cid,
            from = from,
            forceJump = forceJump,
            volume = volume,
            volumes = volumes,
        )

    private fun volume(
        vid: Int,
        name: String,
        chapters: List<ChapterInfo>,
    ): VolumeList =
        VolumeList().apply {
            this.vid = vid
            volumeName = name
            chapterList = ArrayList(chapters)
        }

    private fun chapter(cid: Int, name: String): ChapterInfo =
        ChapterInfo().apply {
            this.cid = cid
            chapterName = name
        }
}
