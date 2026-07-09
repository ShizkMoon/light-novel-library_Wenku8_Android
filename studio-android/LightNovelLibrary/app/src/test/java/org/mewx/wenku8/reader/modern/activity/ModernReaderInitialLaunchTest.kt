package org.mewx.wenku8.reader.modern.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.global.api.ChapterInfo
import org.mewx.wenku8.global.api.VolumeList
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ModernReaderInitialLaunchTest {
    @Test
    fun prepareUsesCatalogTitleForInitialLoadingState() {
        val volume = volume(
            vid = 12,
            name = "Volume",
            chapters = listOf(chapter(cid = 101, name = "Chapter One")),
        )
        val displaySettings = ModernReaderDisplaySettings(
            fontSizeSp = 22,
            lineHeightSp = 34,
            nightMode = true,
        )

        val launch = ModernReaderInitialLaunch.prepare(
            args = launchArgs(cid = 101, volume = volume),
            displaySettings = displaySettings,
        )

        assertEquals(101, launch.args.cid)
        assertEquals(12, launch.context.vid)
        assertEquals(101, launch.context.cid)
        assertEquals(101, launch.catalog.currentChapter?.cid)
        assertEquals("Chapter One", launch.chapterTitle)
        assertEquals("Chapter One", launch.fallbackTitle)
        assertEquals(displaySettings, launch.loadingState.displaySettings)
        assertEquals("Chapter One", launch.loadingState.title)
        assertTrue(launch.loadingState.isLoading)
        assertEquals("加载中", launch.loadingState.progressText)
    }

    @Test
    fun prepareUsesCidFallbackWhenCatalogHasNoTitle() {
        val launch = ModernReaderInitialLaunch.prepare(
            args = launchArgs(cid = 202),
            displaySettings = ModernReaderDisplaySettings(),
        )

        assertEquals("章节 202", launch.fallbackTitle)
        assertEquals("章节 202", launch.loadingState.title)
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
