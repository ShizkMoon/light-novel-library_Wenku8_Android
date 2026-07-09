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
    val primaryAction: ReaderErrorAction? = null,
    val secondaryAction: ReaderErrorAction? = null,
    val page: ReaderPage?,
) {
    val primaryActionLabel: String?
        get() = primaryAction?.label

    val secondaryActionLabel: String?
        get() = secondaryAction?.label

    companion object {
        fun from(state: ModernReaderUiState): ReaderPageContentUiModel =
            when {
                state.isLoading -> message(
                    title = "正在加载章节",
                    message = state.chapterTitle.ifEmpty { state.title },
                )
                state.readerError != null -> message(
                    title = state.readerError.title,
                    message = state.readerError.message,
                    primaryAction = state.readerError.primaryAction,
                    secondaryAction = state.readerError.secondaryAction,
                )
                state.errorMessage != null -> message(
                    title = "章节加载失败",
                    message = state.errorMessage,
                    primaryAction = ReaderErrorAction.RETRY,
                    secondaryAction = ReaderErrorAction.CLOSE,
                )
                state.page == null -> message(
                    title = "暂无内容",
                    message = state.chapterTitle.ifEmpty { state.title },
                )
                else -> ReaderPageContentUiModel(
                    mode = ReaderPageContentMode.PAGE,
                    messageTitle = null,
                    message = null,
                    primaryAction = null,
                    secondaryAction = null,
                    page = state.page,
                )
            }

        private fun message(
            title: String,
            message: String,
            primaryAction: ReaderErrorAction? = null,
            secondaryAction: ReaderErrorAction? = null,
        ): ReaderPageContentUiModel =
            ReaderPageContentUiModel(
                mode = ReaderPageContentMode.MESSAGE,
                messageTitle = title,
                message = message,
                primaryAction = primaryAction,
                secondaryAction = secondaryAction,
                page = null,
            )
    }
}
