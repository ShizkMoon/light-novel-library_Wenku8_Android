package org.mewx.wenku8.activity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.Wenku8Error

class NovelChapterCacheServiceTest {
    @Test
    fun usesCachedXmlWhenPresentAndNotForced() {
        val service = service(
            cachedXml = "<chapter>cached</chapter>",
            downloader = { error("network should not be used") },
        )

        val result = service.cacheChapter(aid = 1, chapterCid = 101, language = 0, forceUpdate = false)

        assertEquals(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, result.errorCode)
        assertEquals("<chapter>cached</chapter>", result.xml)
        assertTrue(service.savedChapters.isEmpty())
    }

    @Test
    fun downloadsAndSavesWhenLocalXmlIsMissing() {
        val service = service(
            cachedXml = "",
            downloader = { request ->
                assertEquals(ChapterRequest(1, 101, 0), request)
                "<chapter>remote</chapter>".toByteArray()
            },
        )

        val result = service.cacheChapter(aid = 1, chapterCid = 101, language = 0, forceUpdate = false)

        assertEquals(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, result.errorCode)
        assertEquals("<chapter>remote</chapter>", result.xml)
        assertEquals(listOf(SavedChapter(101, "<chapter>remote</chapter>")), service.savedChapters)
    }

    @Test
    fun forcedUpdateDownloadsEvenWhenLocalXmlExists() {
        var downloadCount = 0
        val service = service(
            cachedXml = "<chapter>cached</chapter>",
            downloader = {
                downloadCount++
                "<chapter>fresh</chapter>".toByteArray()
            },
        )

        val result = service.cacheChapter(aid = 1, chapterCid = 101, language = 0, forceUpdate = true)

        assertEquals(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, result.errorCode)
        assertEquals("<chapter>fresh</chapter>", result.xml)
        assertEquals(1, downloadCount)
        assertEquals(listOf(SavedChapter(101, "<chapter>fresh</chapter>")), service.savedChapters)
    }

    @Test
    fun returnsNetworkErrorWhenDownloadFails() {
        val service = service(cachedXml = "", downloader = { null })

        val result = service.cacheChapter(aid = 1, chapterCid = 101, language = 0, forceUpdate = false)

        assertEquals(Wenku8Error.ErrorCode.NETWORK_ERROR, result.errorCode)
        assertEquals("", result.xml)
        assertTrue(service.savedChapters.isEmpty())
    }

    @Test
    fun returnsServerEmptyWhenRemoteXmlIsBlank() {
        val service = service(cachedXml = "", downloader = { "   ".toByteArray() })

        val result = service.cacheChapter(aid = 1, chapterCid = 101, language = 0, forceUpdate = false)

        assertEquals(Wenku8Error.ErrorCode.SERVER_RETURN_NOTHING, result.errorCode)
        assertEquals("", result.xml)
        assertTrue(service.savedChapters.isEmpty())
    }

    private fun service(
        cachedXml: String,
        downloader: (ChapterRequest) -> ByteArray?,
    ): RecordingChapterCacheService =
        RecordingChapterCacheService(cachedXml).also { recording ->
            recording.service = NovelChapterCacheService(
                requestForChapter = { aid, chapterCid, language -> ChapterRequest(aid, chapterCid, language) },
                loadChapterXml = recording::loadChapter,
                downloadChapterXml = downloader,
                saveChapterXml = recording::saveChapter,
            )
        }

    private data class ChapterRequest(
        val aid: Int,
        val chapterCid: Int,
        val language: Int,
    )

    private data class SavedChapter(
        val chapterCid: Int,
        val xml: String,
    )

    private class RecordingChapterCacheService(
        private val cachedXml: String,
    ) {
        lateinit var service: NovelChapterCacheService<ChapterRequest, Int>
        val savedChapters = mutableListOf<SavedChapter>()

        fun cacheChapter(
            aid: Int,
            chapterCid: Int,
            language: Int,
            forceUpdate: Boolean,
        ): NovelChapterCacheResult =
            service.cacheChapter(aid, chapterCid, language, forceUpdate)

        fun loadChapter(chapterCid: Int): String {
            assertEquals(101, chapterCid)
            return cachedXml
        }

        fun saveChapter(chapterCid: Int, xml: String) {
            savedChapters += SavedChapter(chapterCid, xml)
        }
    }
}
