package org.mewx.wenku8.reader.modern.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderInteractionModelTest {
    @Test
    fun tapActionUsesLeftThirdForPreviousNavigation() {
        assertEquals(
            ReaderTapAction.PREVIOUS,
            ReaderInteractionModel.tapAction(
                contentWidthPx = 900,
                tapX = 120f,
                canGoPrevious = true,
                canGoNext = true,
            ),
        )
    }

    @Test
    fun tapActionUsesRightThirdForNextNavigation() {
        assertEquals(
            ReaderTapAction.NEXT,
            ReaderInteractionModel.tapAction(
                contentWidthPx = 900,
                tapX = 780f,
                canGoPrevious = true,
                canGoNext = true,
            ),
        )
    }

    @Test
    fun tapActionTogglesControlsInMiddleThird() {
        assertEquals(
            ReaderTapAction.TOGGLE_CONTROLS,
            ReaderInteractionModel.tapAction(
                contentWidthPx = 900,
                tapX = 450f,
                canGoPrevious = true,
                canGoNext = true,
            ),
        )
    }

    @Test
    fun tapActionTogglesControlsWhenEdgeNavigationIsUnavailable() {
        assertEquals(
            ReaderTapAction.TOGGLE_CONTROLS,
            ReaderInteractionModel.tapAction(
                contentWidthPx = 900,
                tapX = 120f,
                canGoPrevious = false,
                canGoNext = true,
            ),
        )
        assertEquals(
            ReaderTapAction.TOGGLE_CONTROLS,
            ReaderInteractionModel.tapAction(
                contentWidthPx = 900,
                tapX = 780f,
                canGoPrevious = true,
                canGoNext = false,
            ),
        )
    }

    @Test
    fun tapActionTogglesControlsForInvalidWidth() {
        assertEquals(
            ReaderTapAction.TOGGLE_CONTROLS,
            ReaderInteractionModel.tapAction(
                contentWidthPx = 0,
                tapX = 0f,
                canGoPrevious = true,
                canGoNext = true,
            ),
        )
    }
}
