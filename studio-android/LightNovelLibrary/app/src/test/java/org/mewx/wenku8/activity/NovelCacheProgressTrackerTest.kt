package org.mewx.wenku8.activity

import java.util.ArrayList
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mewx.wenku8.global.api.ChapterInfo
import org.mewx.wenku8.global.api.VolumeList

class NovelCacheProgressTrackerTest {
    @Test
    fun startsWithChapterCountAcrossVolumes() {
        val tracker = NovelCacheProgressTracker()

        val event = tracker.startChapterTotal(
            listOf(
                volume(chapter(101), chapter(102)),
                volume(chapter(201)),
                VolumeList(),
            ),
        )

        assertEquals(NovelCacheProgressEvent.MaxChanged(3), event)
        assertEquals(3, tracker.maxProgress)
        assertEquals(0, tracker.currentProgress)
    }

    @Test
    fun startsWithChapterCountForSelectedVolumesOnly() {
        val tracker = NovelCacheProgressTracker()

        val event = tracker.startSelectedChapterTotal(
            volumes = listOf(
                volume(chapter(101), chapter(102)),
                volume(chapter(201)),
                volume(chapter(301), chapter(302), chapter(303)),
            ),
            selectedIndices = listOf(0, 2),
        )

        assertEquals(NovelCacheProgressEvent.MaxChanged(5), event)
        assertEquals(5, tracker.maxProgress)
        assertEquals(0, tracker.currentProgress)
    }

    @Test
    fun discoveredImageIncreasesMaxWithoutAdvancingProgress() {
        val tracker = NovelCacheProgressTracker()
        tracker.startChapterTotal(listOf(volume(chapter(101))))

        val event = tracker.addImageWork()

        assertEquals(NovelCacheProgressEvent.MaxChanged(2), event)
        assertEquals(2, tracker.maxProgress)
        assertEquals(0, tracker.currentProgress)
    }

    @Test
    fun completedWorkAdvancesProgressMonotonically() {
        val tracker = NovelCacheProgressTracker()
        tracker.startChapterTotal(listOf(volume(chapter(101))))

        val first = tracker.completeWork()
        val second = tracker.completeWork()

        assertEquals(NovelCacheProgressEvent.ProgressChanged(1), first)
        assertEquals(NovelCacheProgressEvent.ProgressChanged(2), second)
        assertEquals(2, tracker.currentProgress)
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
