package org.mewx.wenku8.activity

import java.util.ArrayList
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.global.api.ChapterInfo
import org.mewx.wenku8.global.api.VolumeList

class NovelVolumeCacheMarkerTest {
    @Test
    fun marksVolumeLocalWhenEveryChapterIsCached() {
        val volume = volume(chapter(101), chapter(102))

        NovelVolumeCacheMarker.markInLocalVolumes(listOf(volume)) { cid ->
            cid == 101 || cid == 102
        }

        assertTrue(volume.inLocal)
    }

    @Test
    fun leavesVolumeRemoteWhenAnyChapterIsMissing() {
        val volume = volume(chapter(101), chapter(102))

        NovelVolumeCacheMarker.markInLocalVolumes(listOf(volume)) { cid ->
            cid == 101
        }

        assertFalse(volume.inLocal)
    }

    @Test
    fun clearsStaleLocalFlagWhenAnyChapterIsMissing() {
        val volume = volume(chapter(101), chapter(102)).apply {
            inLocal = true
        }

        NovelVolumeCacheMarker.markInLocalVolumes(listOf(volume)) { cid ->
            cid == 101
        }

        assertFalse(volume.inLocal)
    }

    @Test
    fun emptyVolumeIsNotMarkedLocal() {
        val volume = VolumeList()

        NovelVolumeCacheMarker.markInLocalVolumes(listOf(volume)) { true }

        assertFalse(volume.inLocal)
    }

    private fun volume(vararg chapters: ChapterInfo): VolumeList =
        VolumeList().apply {
            chapterList = ArrayList(chapters.toList())
        }

    private fun chapter(cid: Int): ChapterInfo =
        ChapterInfo().apply {
            this.cid = cid
        }
}
