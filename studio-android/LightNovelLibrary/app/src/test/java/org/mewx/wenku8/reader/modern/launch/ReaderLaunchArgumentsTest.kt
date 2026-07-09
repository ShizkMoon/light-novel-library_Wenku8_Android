package org.mewx.wenku8.reader.modern.launch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mewx.wenku8.global.api.ChapterInfo
import org.mewx.wenku8.global.api.VolumeList
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.data.ReaderContentSourceMode

class ReaderLaunchArgumentsTest {
    @Test
    fun sourceModeOnlyTreatsFavAsLocal() {
        assertEquals(ReaderContentSourceMode.LOCAL, launchArgs(from = "fav").sourceMode())
        assertEquals(ReaderContentSourceMode.CLOUD, launchArgs(from = "cloud").sourceMode())
        assertEquals(ReaderContentSourceMode.CLOUD, launchArgs(from = "").sourceMode())
    }

    @Test
    fun volumeIdUsesVolumeContainingCurrentChapter() {
        val first = volume(1, "第一卷", listOf(chapter(101, "序章")))
        val second = volume(2, "第二卷", listOf(chapter(201, "新章")))

        val args = launchArgs(
            cid = 201,
            volume = first,
            volumes = listOf(first, second),
        )

        assertEquals(2, args.volumeId())
    }

    @Test
    fun forChapterMovesVolumeAndClearsForceJump() {
        val first = volume(1, "第一卷", listOf(chapter(101, "序章")))
        val second = volume(2, "第二卷", listOf(chapter(201, "新章")))
        val args = launchArgs(
            cid = 101,
            forceJump = true,
            volume = first,
            volumes = listOf(first, second),
        )

        val next = args.forChapter(
            ReaderCatalogChapter(
                cid = 201,
                title = "新章",
                volumeId = 2,
            ),
        )

        assertEquals(201, next.cid)
        assertFalse(next.forceJump)
        assertEquals(2, next.volumeId())
        assertEquals("新章", next.catalog().currentChapter?.title)
    }

    private fun launchArgs(
        aid: Int = 7,
        cid: Int = 101,
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
