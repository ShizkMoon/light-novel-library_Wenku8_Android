package org.mewx.wenku8.activity

import org.mewx.wenku8.global.api.OldNovelContentParser
import org.mewx.wenku8.global.api.OldNovelContentParser.NovelContentType

class NovelChapterImageReferenceParser(
    private val parseImageContent: (String) -> List<OldNovelContentParser.NovelContent> =
        OldNovelContentParser::NovelContentParser_onlyImage,
) {
    fun imageReferences(xml: String): List<String> =
        parseImageContent(xml)
            .filter { content -> content.type == NovelContentType.IMAGE }
            .map { content -> content.content }
}
