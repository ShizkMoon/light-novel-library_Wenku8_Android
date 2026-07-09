package org.mewx.wenku8.reader.modern.ui

data class ReaderChromeUiModel(
    val title: String,
    val chapterTitle: String,
    val progressText: String,
    val pageIndex: Int,
    val pageCount: Int,
    val hasPage: Boolean,
    val canGoPrevious: Boolean,
    val canGoNext: Boolean,
    val previousNavigationLabel: String,
    val nextNavigationLabel: String,
) {
    companion object {
        fun from(state: ModernReaderUiState): ReaderChromeUiModel =
            ReaderChromeUiModel(
                title = state.title,
                chapterTitle = state.chapterTitle,
                progressText = state.progressText,
                pageIndex = state.pageIndex,
                pageCount = state.pageCount,
                hasPage = state.page != null,
                canGoPrevious = state.canGoPrevious,
                canGoNext = state.canGoNext,
                previousNavigationLabel = state.previousNavigationLabel,
                nextNavigationLabel = state.nextNavigationLabel,
            )
    }
}
