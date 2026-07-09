package org.mewx.wenku8.reader.modern.ui

import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter

enum class ReaderNavigationTarget {
    PREVIOUS_PAGE,
    NEXT_PAGE,
    PREVIOUS_CHAPTER,
    NEXT_CHAPTER,
    NONE,
}

data class ReaderNavigationAction(
    val target: ReaderNavigationTarget,
    val chapter: ReaderCatalogChapter? = null,
)

object ReaderNavigationModel {
    fun previousAction(state: ModernReaderUiState): ReaderNavigationAction =
        when {
            state.page?.hasMoreBefore == true -> ReaderNavigationAction(ReaderNavigationTarget.PREVIOUS_PAGE)
            state.catalog.previousChapter != null -> ReaderNavigationAction(
                target = ReaderNavigationTarget.PREVIOUS_CHAPTER,
                chapter = state.catalog.previousChapter,
            )
            else -> ReaderNavigationAction(ReaderNavigationTarget.NONE)
        }

    fun nextAction(state: ModernReaderUiState): ReaderNavigationAction =
        when {
            state.page?.hasMoreAfter == true -> ReaderNavigationAction(ReaderNavigationTarget.NEXT_PAGE)
            state.catalog.nextChapter != null -> ReaderNavigationAction(
                target = ReaderNavigationTarget.NEXT_CHAPTER,
                chapter = state.catalog.nextChapter,
            )
            else -> ReaderNavigationAction(ReaderNavigationTarget.NONE)
        }
}
