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

class ModernReaderChapterSelectionTest {
    @Test
    fun prepareReturnsNullWhenSelectingCurrentChapter() {
        val selection = ModernReaderChapterSelection.prepare(
            currentArgs = launchArgs(cid = 101),
            chapter = ReaderCatalogChapter(cid = 101, title = "Current"),
            displaySettings = ModernReaderDisplaySettings(),
        )

        assertNull(selection)
    }

    @Test
    fun prepareBuildsNextChapterLoadingState() {
        val first = volume(1, "First Volume", listOf(chapter(101, "Start")))
        val second = volume(2, "Second Volume", listOf(chapter(201, "Next")))
        val displaySettings = ModernReaderDisplaySettings(
            fontSizeSp = 22,
            lineHeightSp = 34,
            nightMode = true,
        )

        val selection = ModernReaderChapterSelection.prepare(
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

        assertEquals(201, selection.args.cid)
        assertFalse(selection.args.forceJump)
        assertEquals(2, selection.context.vid)
        assertEquals(201, selection.context.cid)
        assertEquals(201, selection.catalog.currentChapter?.cid)
        assertEquals("Next", selection.chapterTitle)
        assertEquals("Next", selection.fallbackTitle)
        assertEquals(displaySettings, selection.loadingState.displaySettings)
        assertTrue(selection.loadingState.isLoading)
        assertEquals("加载中", selection.loadingState.progressText)
    }

    @Test
    fun prepareUsesCidFallbackTitleWhenCatalogChapterNameIsMissing() {
        val selection = ModernReaderChapterSelection.prepare(
            currentArgs = launchArgs(cid = 101),
            chapter = ReaderCatalogChapter(cid = 202, title = ""),
            displaySettings = ModernReaderDisplaySettings(),
        )!!

        assertEquals("章节 202", selection.fallbackTitle)
        assertEquals("章节 202", selection.loadingState.title)
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
