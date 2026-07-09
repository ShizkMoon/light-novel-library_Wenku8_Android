package org.mewx.wenku8.reader.modern.ui

import org.mewx.wenku8.reader.modern.model.ReaderLine

data class ReaderImageLineUiModel(
    val source: String,
    val cachedPath: String?,
    val placeholderText: String,
) {
    companion object {
        fun from(
            line: ReaderLine,
            cachedPathForSource: (String) -> String?,
        ): ReaderImageLineUiModel {
            val source = line.source ?: line.text
            return ReaderImageLineUiModel(
                source = source,
                cachedPath = cachedPathForSource(source),
                placeholderText = "插图 $source",
            )
        }
    }
}
