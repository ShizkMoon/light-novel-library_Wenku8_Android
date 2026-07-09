package org.mewx.wenku8.reader.modern.data

import org.mewx.wenku8.global.api.OldNovelContentParser
import org.mewx.wenku8.reader.modern.model.ReaderBlock

object LegacyReaderContentMapper {
    fun toReaderBlocks(legacyContent: List<OldNovelContentParser.NovelContent>): List<ReaderBlock> =
        legacyContent.mapNotNull { item ->
            when (item.type) {
                OldNovelContentParser.NovelContentType.TEXT -> {
                    val text = item.content.trim()
                    if (text.isEmpty()) null else ReaderBlock.Paragraph(text)
                }
                OldNovelContentParser.NovelContentType.IMAGE -> {
                    val source = item.content.trim()
                    if (source.isEmpty()) null else ReaderBlock.Image(source)
                }
            }
        }
}
