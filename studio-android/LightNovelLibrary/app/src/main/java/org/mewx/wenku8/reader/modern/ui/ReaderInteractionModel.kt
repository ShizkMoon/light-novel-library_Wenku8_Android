package org.mewx.wenku8.reader.modern.ui

enum class ReaderTapAction {
    PREVIOUS,
    NEXT,
    TOGGLE_CONTROLS,
}

object ReaderInteractionModel {
    fun tapAction(
        contentWidthPx: Int,
        tapX: Float,
        canGoPrevious: Boolean,
        canGoNext: Boolean,
    ): ReaderTapAction {
        if (contentWidthPx <= 0) return ReaderTapAction.TOGGLE_CONTROLS

        val leftEdge = contentWidthPx / 3f
        val rightEdge = contentWidthPx * 2f / 3f
        return when {
            canGoPrevious && tapX < leftEdge -> ReaderTapAction.PREVIOUS
            canGoNext && tapX > rightEdge -> ReaderTapAction.NEXT
            else -> ReaderTapAction.TOGGLE_CONTROLS
        }
    }
}
