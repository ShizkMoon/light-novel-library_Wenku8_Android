package org.mewx.wenku8.reader.modern.data

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mewx.wenku8.global.api.OldNovelContentParser
import org.mewx.wenku8.reader.modern.model.ReaderBlock

class LegacyReaderContentMapperTest {
    @Test
    fun mapsLegacyTextAndImageContentIntoModernBlocks() {
        val legacyContent = OldNovelContentParser.parseNovelContent(
            "第一段\r\n" +
                "<!--image-->https://example.invalid/cover.jpg<!--image-->\r\n" +
                "第二段",
        ) { }

        val blocks = LegacyReaderContentMapper.toReaderBlocks(legacyContent)

        assertEquals(
            listOf(
                ReaderBlock.Paragraph("第一段"),
                ReaderBlock.Image("https://example.invalid/cover.jpg"),
                ReaderBlock.Paragraph("第二段"),
            ),
            blocks,
        )
    }
}
