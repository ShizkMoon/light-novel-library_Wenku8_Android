package org.mewx.wenku8.reader.modern.ui

import org.mewx.wenku8.reader.modern.model.ReaderPage

enum class ReaderPageContentMode {
    MESSAGE,
    PAGE,
}

data class ReaderPageContentUiModel(
    val mode: ReaderPageContentMode,
    val messageTitle: String?,
    val message: String?,
    val page: ReaderPage?,
) {
    companion object {
        fun from(state: ModernReaderUiState): ReaderPageContentUiModel =
            when {
                state.isLoading -> message(
                    title = "正在加载章节",
                    message = state.chapterTitle.ifEmpty { state.title },
                )
                state.errorMessage != null -> message(
                    title = "章节加载失败",
                    message = state.errorMessage,
                )
                state.page == null -> message(
                    title = "暂无内容",
                    message = state.chapterTitle.ifEmpty { state.title },
                )
                else -> ReaderPageContentUiModel(
                    mode = ReaderPageContentMode.PAGE,
                    messageTitle = null,
                    message = null,
                    page = state.page,
                )
            }

        private fun message(
            title: String,
            message: String,
        ): ReaderPageContentUiModel =
            ReaderPageContentUiModel(
                mode = ReaderPageContentMode.MESSAGE,
                messageTitle = title,
                message = message,
                page = null,
            )
    }
}
