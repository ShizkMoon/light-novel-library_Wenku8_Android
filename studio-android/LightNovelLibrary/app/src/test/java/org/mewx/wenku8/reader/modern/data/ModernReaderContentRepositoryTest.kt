package org.mewx.wenku8.reader.modern.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.reader.modern.model.ReaderBlock

class ModernReaderContentRepositoryTest {
    @Test
    fun loadLocalChapterBuildsReaderDocumentFromLegacyXml() {
        val repository = ModernReaderContentRepository(
            source = FakeRawContentSource(
                localXml = "第一段\r\n<!--image-->https://example.invalid/1.jpg<!--image-->\r\n第二段",
            ),
        )

        val result = repository.load(
            ModernReaderContentRequest(
                aid = 1,
                cid = 2,
                chapterTitle = "第二章",
                sourceMode = ReaderContentSourceMode.LOCAL,
            ),
        )

        require(result is ModernReaderLoadResult.Success)
        assertEquals("第二章", result.document.title)
        assertEquals(
            listOf(
                ReaderBlock.Paragraph("第一段"),
                ReaderBlock.Image("https://example.invalid/1.jpg"),
                ReaderBlock.Paragraph("第二段"),
            ),
            result.document.blocks,
        )
    }

    @Test
    fun loadCloudChapterReportsNetworkErrorWhenCloudReturnsNull() {
        val repository = ModernReaderContentRepository(source = FakeRawContentSource(cloudXml = null))

        val result = repository.load(
            ModernReaderContentRequest(
                aid = 1,
                cid = 2,
                chapterTitle = "第二章",
                sourceMode = ReaderContentSourceMode.CLOUD,
            ),
        )

        assertEquals(
            ModernReaderLoadResult.Failure(ModernReaderLoadFailure.NETWORK_ERROR),
            result,
        )
    }

    @Test
    fun loadLocalChapterReportsMissingContentWhenLocalXmlIsBlank() {
        val repository = ModernReaderContentRepository(source = FakeRawContentSource(localXml = ""))

        val result = repository.load(
            ModernReaderContentRequest(
                aid = 1,
                cid = 2,
                chapterTitle = "第二章",
                sourceMode = ReaderContentSourceMode.LOCAL,
            ),
        )

        assertEquals(
            ModernReaderLoadResult.Failure(ModernReaderLoadFailure.LOCAL_CONTENT_MISSING),
            result,
        )
    }

    @Test
    fun loadChapterReportsEmptyContentWhenParserProducesNoBlocks() {
        val repository = ModernReaderContentRepository(source = FakeRawContentSource(cloudXml = "    \r\n  "))

        val result = repository.load(
            ModernReaderContentRequest(
                aid = 1,
                cid = 2,
                chapterTitle = "第二章",
                sourceMode = ReaderContentSourceMode.CLOUD,
            ),
        )

        assertTrue(result is ModernReaderLoadResult.Failure)
        assertEquals(
            ModernReaderLoadResult.Failure(ModernReaderLoadFailure.EMPTY_CONTENT),
            result,
        )
    }

    private class FakeRawContentSource(
        private val localXml: String = "",
        private val cloudXml: String? = "",
    ) : ModernReaderRawContentSource {
        override fun loadLocalChapterXml(cid: Int): String = localXml

        override fun loadCloudChapterXml(aid: Int, cid: Int): String? = cloudXml
    }
}
