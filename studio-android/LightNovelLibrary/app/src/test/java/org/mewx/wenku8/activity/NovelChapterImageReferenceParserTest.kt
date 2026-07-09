package org.mewx.wenku8.activity

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mewx.wenku8.global.api.OldNovelContentParser.NovelContent
import org.mewx.wenku8.global.api.OldNovelContentParser.NovelContentType

class NovelChapterImageReferenceParserTest {
    @Test
    fun returnsOnlyImageReferencesFromParsedChapterContent() {
        val parser = NovelChapterImageReferenceParser(
            parseImageContent = {
                listOf(
                    content(NovelContentType.TEXT, "chapter text"),
                    content(NovelContentType.IMAGE, "https://img.example.test/first.png"),
                    content(NovelContentType.IMAGE, "https://img.example.test/second.png"),
                )
            },
        )

        val references = parser.imageReferences("<chapter />")

        assertEquals(
            listOf(
                "https://img.example.test/first.png",
                "https://img.example.test/second.png",
            ),
            references,
        )
    }

    @Test
    fun delegatesRawXmlToLegacyChapterImageParser() {
        var parsedXml = ""
        val parser = NovelChapterImageReferenceParser(
            parseImageContent = { xml ->
                parsedXml = xml
                listOf(content(NovelContentType.IMAGE, "https://img.example.test/cover.png"))
            },
        )

        val references = parser.imageReferences("<chapter>xml</chapter>")

        assertEquals("<chapter>xml</chapter>", parsedXml)
        assertEquals(listOf("https://img.example.test/cover.png"), references)
    }

    private fun content(type: NovelContentType, value: String): NovelContent =
        NovelContent().apply {
            this.type = type
            content = value
        }
}
