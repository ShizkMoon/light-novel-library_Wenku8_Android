package org.mewx.wenku8.reader.modern.model

data class ReaderDocument(
    val title: String = "",
    val blocks: List<ReaderBlock>,
)

sealed interface ReaderBlock {
    data class Paragraph(val text: String) : ReaderBlock
    data class Image(val source: String) : ReaderBlock
}

data class ReaderCursor(
    val blockIndex: Int,
    val charIndex: Int,
) : Comparable<ReaderCursor> {
    override fun compareTo(other: ReaderCursor): Int {
        val blockCompare = blockIndex.compareTo(other.blockIndex)
        return if (blockCompare != 0) blockCompare else charIndex.compareTo(other.charIndex)
    }

    companion object {
        val START = ReaderCursor(blockIndex = 0, charIndex = 0)
    }
}

data class ReaderLayoutSpec(
    val contentWidthPx: Int,
    val contentHeightPx: Int,
    val fontHeightPx: Int,
    val lineSpacingPx: Int,
    val paragraphSpacingPx: Int,
    val paragraphIndentChars: Int = 2,
)

enum class ReaderLineType {
    TEXT,
    IMAGE,
}

data class ReaderLine(
    val type: ReaderLineType,
    val text: String,
    val source: String? = null,
)

data class ReaderPage(
    val start: ReaderCursor,
    val end: ReaderCursor,
    val lines: List<ReaderLine>,
    val hasMoreBefore: Boolean,
    val hasMoreAfter: Boolean,
)
