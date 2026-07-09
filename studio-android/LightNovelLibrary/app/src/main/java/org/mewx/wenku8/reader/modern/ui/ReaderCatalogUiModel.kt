package org.mewx.wenku8.reader.modern.ui

import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter

data class ReaderCatalogUiModel(
    val fallbackTitle: String,
    val supportingMessage: String?,
    val summaryText: String,
    val initialFirstVisibleItemIndex: Int,
    val sections: List<ReaderCatalogSectionUiModel>,
) {
    fun selectChapter(chapter: ReaderCatalogChapterUiModel): ReaderCatalogChapter? =
        chapter.source.takeIf { chapter.isSelectable }

    companion object {
        fun from(
            title: String,
            chapterTitle: String,
            catalog: ModernReaderCatalog,
        ): ReaderCatalogUiModel {
            val fallbackTitle = chapterTitle.ifBlank { title }
            val sections = catalog.sections.map { section ->
                ReaderCatalogSectionUiModel(
                    volumeId = section.volumeId,
                    title = section.title.ifBlank { fallbackTitle },
                    chapters = section.chapters.map { chapter ->
                        ReaderCatalogChapterUiModel(
                            title = chapter.title,
                            stateLabel = if (chapter.isCurrent) "当前" else null,
                            isSelectable = !chapter.isCurrent,
                            source = chapter,
                        )
                    },
                )
            }

            return ReaderCatalogUiModel(
                fallbackTitle = fallbackTitle,
                supportingMessage = if (catalog.hasKnownCatalog) {
                    null
                } else {
                    "目录数据暂不可用，仅显示当前章节。"
                },
                summaryText = if (catalog.hasKnownCatalog) {
                    "${catalog.sections.size} 卷 / ${catalog.sections.sumOf { it.chapters.size }} 章"
                } else {
                    "当前章节"
                },
                initialFirstVisibleItemIndex = (catalog.currentChapterItemIndex - 1).coerceAtLeast(0),
                sections = sections,
            )
        }
    }
}

data class ReaderCatalogSectionUiModel(
    val volumeId: Int,
    val title: String,
    val chapters: List<ReaderCatalogChapterUiModel>,
)

data class ReaderCatalogChapterUiModel(
    val title: String,
    val stateLabel: String?,
    val isSelectable: Boolean,
    val source: ReaderCatalogChapter,
)
