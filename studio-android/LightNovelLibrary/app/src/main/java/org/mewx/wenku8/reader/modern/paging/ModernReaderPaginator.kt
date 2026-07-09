package org.mewx.wenku8.reader.modern.paging

import org.mewx.wenku8.reader.modern.model.ReaderBlock
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.model.ReaderLine
import org.mewx.wenku8.reader.modern.model.ReaderLineType
import org.mewx.wenku8.reader.modern.model.ReaderPage

fun interface ReaderTextMeasurer {
    fun width(text: String): Float
}

class ModernReaderPaginator(
    private val document: ReaderDocument,
    private val textMeasurer: ReaderTextMeasurer,
) {
    fun pageFrom(cursor: ReaderCursor, layout: ReaderLayoutSpec): ReaderPage {
        val start = normalize(cursor)
        val lines = mutableListOf<ReaderLine>()
        var current = start
        var usedHeight = 0
        var previousBlockIndex: Int? = null

        while (current.blockIndex < document.blocks.size) {
            val block = document.blocks[current.blockIndex]
            when (block) {
                is ReaderBlock.Image -> {
                    if (current.charIndex > 0) {
                        current = ReaderCursor(current.blockIndex + 1, 0)
                        continue
                    }
                    if (lines.isNotEmpty()) break
                    if (!canAddLine(usedHeight, 0, layout.fontHeightPx, layout)) break
                    lines += ReaderLine(
                        type = ReaderLineType.IMAGE,
                        text = block.source,
                        source = block.source,
                    )
                    current = ReaderCursor(current.blockIndex + 1, 0)
                    usedHeight = layout.contentHeightPx
                    break
                }

                is ReaderBlock.Paragraph -> {
                    if (block.text.isEmpty()) {
                        current = ReaderCursor(current.blockIndex + 1, 0)
                        continue
                    }

                    val spacingBefore = when {
                        lines.isEmpty() -> 0
                        previousBlockIndex == current.blockIndex -> layout.lineSpacingPx
                        else -> layout.paragraphSpacingPx
                    }
                    if (!canAddLine(usedHeight, spacingBefore, layout.fontHeightPx, layout)) {
                        break
                    }

                    val prefix = if (current.charIndex == 0) {
                        "\u3000".repeat(layout.paragraphIndentChars)
                    } else {
                        ""
                    }
                    val line = buildLine(block.text, current.charIndex, prefix, layout)
                    if (line.consumedChars == 0) break

                    usedHeight += spacingBefore + layout.fontHeightPx
                    previousBlockIndex = current.blockIndex
                    lines += ReaderLine(type = ReaderLineType.TEXT, text = line.text)

                    val nextCharIndex = current.charIndex + line.consumedChars
                    current = if (nextCharIndex >= block.text.length) {
                        ReaderCursor(current.blockIndex + 1, 0)
                    } else {
                        ReaderCursor(current.blockIndex, nextCharIndex)
                    }
                }
            }
        }

        val end = normalize(current)
        return ReaderPage(
            start = start,
            end = end,
            lines = lines,
            hasMoreBefore = start > ReaderCursor.START,
            hasMoreAfter = end < documentEnd(),
        )
    }

    fun pageBefore(cursor: ReaderCursor, layout: ReaderLayoutSpec): ReaderPage {
        val target = normalize(cursor)
        var current = ReaderCursor.START
        var previous = pageFrom(current, layout)

        while (previous.end < target && previous.end > current) {
            current = previous.end
            previous = pageFrom(current, layout)
        }

        return previous
    }

    private fun buildLine(
        paragraph: String,
        startIndex: Int,
        prefix: String,
        layout: ReaderLayoutSpec,
    ): BuiltLine {
        val builder = StringBuilder(prefix)
        var index = startIndex

        while (index < paragraph.length) {
            val candidate = builder.toString() + paragraph[index]
            if (textMeasurer.width(candidate) > layout.contentWidthPx && builder.isNotEmpty()) {
                break
            }
            builder.append(paragraph[index])
            index++
        }

        return BuiltLine(
            text = builder.toString(),
            consumedChars = index - startIndex,
        )
    }

    private fun canAddLine(
        usedHeight: Int,
        spacingBefore: Int,
        lineHeight: Int,
        layout: ReaderLayoutSpec,
    ): Boolean = usedHeight + spacingBefore + lineHeight <= layout.contentHeightPx

    private fun normalize(cursor: ReaderCursor): ReaderCursor {
        val blockIndex = cursor.blockIndex.coerceIn(0, document.blocks.size)
        if (blockIndex == document.blocks.size) return ReaderCursor(blockIndex, 0)

        val block = document.blocks[blockIndex]
        val maxCharIndex = when (block) {
            is ReaderBlock.Image -> 0
            is ReaderBlock.Paragraph -> block.text.length
        }
        return ReaderCursor(blockIndex, cursor.charIndex.coerceIn(0, maxCharIndex))
    }

    private fun documentEnd(): ReaderCursor = ReaderCursor(document.blocks.size, 0)

    private data class BuiltLine(
        val text: String,
        val consumedChars: Int,
    )
}
