package org.mewx.wenku8.reader.modern.ui

import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog

data class ReaderCatalogUiModel(
    val fallbackTitle: String,
    val supportingMessage: String?,
) {
    companion object {
        fun from(
            title: String,
            chapterTitle: String,
            catalog: ModernReaderCatalog,
        ): ReaderCatalogUiModel =
            ReaderCatalogUiModel(
                fallbackTitle = chapterTitle.ifBlank { title },
                supportingMessage = if (catalog.hasKnownCatalog) {
                    null
                } else {
                    "目录数据暂不可用，仅显示当前章节。"
                },
            )
    }
}
