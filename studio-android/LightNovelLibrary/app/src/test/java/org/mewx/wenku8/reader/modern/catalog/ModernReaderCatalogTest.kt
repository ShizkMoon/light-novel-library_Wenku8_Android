package org.mewx.wenku8.reader.modern.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.global.api.ChapterInfo
import org.mewx.wenku8.global.api.VolumeList

class ModernReaderCatalogTest {
    @Test
    fun fromVolumeMapsChaptersAndMarksCurrentChapter() {
        val catalog = ModernReaderCatalog.from(
            volume = volume(
                vid = 12,
                name = "第一卷",
                chapters = listOf(
                    chapter(101, "序章"),
                    chapter(102, "第一章"),
                    chapter(103, "第二章"),
                ),
            ),
            currentCid = 102,
        )

        assertEquals("第一卷", catalog.volumeTitle)
        assertEquals(12, catalog.volumeId)
        assertEquals(3, catalog.chapters.size)
        assertEquals(102, catalog.currentChapter?.cid)
        assertFalse(catalog.chapters[0].isCurrent)
        assertTrue(catalog.chapters[1].isCurrent)
        assertFalse(catalog.chapters[2].isCurrent)
    }

    @Test
    fun previousAndNextChapterUseCurrentChapterPosition() {
        val catalog = ModernReaderCatalog.from(
            volume = volume(
                vid = 12,
                name = "第一卷",
                chapters = listOf(
                    chapter(101, "序章"),
                    chapter(102, "第一章"),
                    chapter(103, "第二章"),
                ),
            ),
            currentCid = 102,
        )

        assertEquals(101, catalog.previousChapter?.cid)
        assertEquals(103, catalog.nextChapter?.cid)
    }

    @Test
    fun previousAndNextChapterAreNullAtVolumeEdges() {
        val first = ModernReaderCatalog.from(
            volume = volume(
                vid = 12,
                name = "第一卷",
                chapters = listOf(chapter(101, "序章"), chapter(102, "第一章")),
            ),
            currentCid = 101,
        )
        val last = ModernReaderCatalog.from(
            volume = volume(
                vid = 12,
                name = "第一卷",
                chapters = listOf(chapter(101, "序章"), chapter(102, "第一章")),
            ),
            currentCid = 102,
        )

        assertNull(first.previousChapter)
        assertEquals(102, first.nextChapter?.cid)
        assertEquals(101, last.previousChapter?.cid)
        assertNull(last.nextChapter)
    }

    @Test
    fun fromVolumesKeepsVolumeSectionsAndMarksCurrentChapter() {
        val catalog = ModernReaderCatalog.from(
            volumes = listOf(
                volume(
                    vid = 12,
                    name = "第一卷",
                    chapters = listOf(chapter(101, "序章"), chapter(102, "第一章")),
                ),
                volume(
                    vid = 13,
                    name = "第二卷",
                    chapters = listOf(chapter(201, "第二卷序章"), chapter(202, "第二卷第一章")),
                ),
            ),
            currentCid = 201,
        )

        assertEquals(2, catalog.sections.size)
        assertEquals("第一卷", catalog.sections[0].title)
        assertEquals("第二卷", catalog.sections[1].title)
        assertEquals(13, catalog.volumeId)
        assertEquals("第二卷", catalog.volumeTitle)
        assertEquals(201, catalog.currentChapter?.cid)
        assertTrue(catalog.sections[1].chapters[0].isCurrent)
    }

    @Test
    fun previousAndNextChapterCanCrossVolumeBoundaries() {
        val catalog = ModernReaderCatalog.from(
            volumes = listOf(
                volume(
                    vid = 12,
                    name = "第一卷",
                    chapters = listOf(chapter(101, "序章"), chapter(102, "第一章")),
                ),
                volume(
                    vid = 13,
                    name = "第二卷",
                    chapters = listOf(chapter(201, "第二卷序章"), chapter(202, "第二卷第一章")),
                ),
            ),
            currentCid = 102,
        )

        assertEquals(101, catalog.previousChapter?.cid)
        assertEquals(201, catalog.nextChapter?.cid)
        assertEquals(13, catalog.nextChapter?.volumeId)
    }

    @Test
    fun currentChapterItemIndexIncludesVolumeHeaders() {
        val catalog = ModernReaderCatalog.from(
            volumes = listOf(
                volume(
                    vid = 12,
                    name = "第一卷",
                    chapters = listOf(chapter(101, "序章"), chapter(102, "第一章")),
                ),
                volume(
                    vid = 13,
                    name = "第二卷",
                    chapters = listOf(chapter(201, "第二卷序章"), chapter(202, "第二卷第一章")),
                ),
            ),
            currentCid = 201,
        )

        assertEquals(4, catalog.currentChapterItemIndex)
    }

    @Test
    fun missingVolumeFallsBackToCurrentChapterOnly() {
        val catalog = ModernReaderCatalog.from(volume = null, currentCid = 55)

        assertEquals("", catalog.volumeTitle)
        assertEquals(0, catalog.volumeId)
        assertFalse(catalog.hasKnownCatalog)
        assertEquals(listOf(ReaderCatalogChapter(cid = 55, title = "章节 55", isCurrent = true)), catalog.chapters)
        assertEquals(55, catalog.currentChapter?.cid)
        assertNull(catalog.previousChapter)
        assertNull(catalog.nextChapter)
    }

    @Test
    fun volumeWithChaptersIsMarkedAsKnownCatalog() {
        val catalog = ModernReaderCatalog.from(
            volume = volume(
                vid = 12,
                name = "第一卷",
                chapters = listOf(chapter(101, "序章")),
            ),
            currentCid = 101,
        )

        assertTrue(catalog.hasKnownCatalog)
    }

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
