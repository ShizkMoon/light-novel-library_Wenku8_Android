package org.mewx.wenku8.reader.modern.ui

enum class ReaderErrorAction(val label: String) {
    RETRY("重试"),
    CLOSE("返回"),
}

data class ReaderErrorUiModel(
    val title: String,
    val message: String,
    val primaryAction: ReaderErrorAction? = null,
    val secondaryAction: ReaderErrorAction? = null,
) {
    val primaryActionLabel: String?
        get() = primaryAction?.label

    val secondaryActionLabel: String?
        get() = secondaryAction?.label

    companion object {
        fun loadFailure(message: String): ReaderErrorUiModel =
            ReaderErrorUiModel(
                title = "章节加载失败",
                message = message,
                primaryAction = ReaderErrorAction.RETRY,
                secondaryAction = ReaderErrorAction.CLOSE,
            )

        fun missingArguments(): ReaderErrorUiModel =
            ReaderErrorUiModel(
                title = "无法打开章节",
                message = "阅读器缺少小说或章节参数，请返回后重新打开章节。",
                secondaryAction = ReaderErrorAction.CLOSE,
            )
    }
}
