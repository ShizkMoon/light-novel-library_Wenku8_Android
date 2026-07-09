package org.mewx.wenku8.reader.modern.catalog

import org.mewx.wenku8.global.api.VolumeList

data class ReaderCatalogChapter(
    val cid: Int,
    val title: String,
    val isCurrent: Boolean = false,
    val volumeId: Int = 0,
)

data class ReaderCatalogSection(
    val volumeId: Int,
    val title: String,
    val chapters: List<ReaderCatalogChapter>,
)

data class ModernReaderCatalog(
    val volumeId: Int,
    val volumeTitle: String,
    val chapters: List<ReaderCatalogChapter>,
    val sections: List<ReaderCatalogSection> = listOf(
        ReaderCatalogSection(
            volumeId = volumeId,
            title = volumeTitle,
            chapters = chapters,
        ),
    ),
) {
    private val allChapters: List<ReaderCatalogChapter>
        get() = sections.flatMap { it.chapters }

    val currentChapter: ReaderCatalogChapter?
        get() = allChapters.firstOrNull { it.isCurrent }

    val previousChapter: ReaderCatalogChapter?
        get() = adjacentChapter(offset = -1)

    val nextChapter: ReaderCatalogChapter?
        get() = adjacentChapter(offset = 1)

    val currentChapterItemIndex: Int
        get() {
            var index = 0
            sections.forEach { section ->
                index += 1
                section.chapters.forEach { chapter ->
                    if (chapter.isCurrent) return index
                    index += 1
                }
            }
            return 0
        }

    private fun adjacentChapter(offset: Int): ReaderCatalogChapter? {
        val currentIndex = allChapters.indexOfFirst { it.isCurrent }
        if (currentIndex < 0) return null
        return allChapters.getOrNull(currentIndex + offset)
    }

    companion object {
        fun from(
            volume: VolumeList?,
            currentCid: Int,
        ): ModernReaderCatalog =
            from(
                volumes = volume?.let { listOf(it) },
                currentCid = currentCid,
            )

        fun from(
            volumes: List<VolumeList>?,
            currentCid: Int,
        ): ModernReaderCatalog {
            val sections = volumes.orEmpty().mapNotNull { volume ->
                val chapters = volume.chapterList.orEmpty().map { chapter ->
                    ReaderCatalogChapter(
                        cid = chapter.cid,
                        title = chapter.chapterName.orEmpty().ifBlank { "章节 ${chapter.cid}" },
                        isCurrent = chapter.cid == currentCid,
                        volumeId = volume.vid,
                    )
                }

                if (chapters.isEmpty()) {
                    null
                } else {
                    ReaderCatalogSection(
                        volumeId = volume.vid,
                        title = volume.volumeName.orEmpty(),
                        chapters = chapters,
                    )
                }
            }

            if (sections.isEmpty()) {
                val fallbackChapter = ReaderCatalogChapter(
                    cid = currentCid,
                    title = "章节 $currentCid",
                    isCurrent = true,
                )
                return ModernReaderCatalog(
                    volumeId = 0,
                    volumeTitle = "",
                    chapters = listOf(fallbackChapter),
                    sections = listOf(
                        ReaderCatalogSection(
                            volumeId = 0,
                            title = "",
                            chapters = listOf(fallbackChapter),
                        ),
                    ),
                )
            }

            val currentSection = sections.firstOrNull { section ->
                section.chapters.any { it.isCurrent }
            } ?: sections.first()

            return ModernReaderCatalog(
                volumeId = currentSection.volumeId,
                volumeTitle = currentSection.title,
                chapters = currentSection.chapters,
                sections = sections,
            )
        }
    }
}
