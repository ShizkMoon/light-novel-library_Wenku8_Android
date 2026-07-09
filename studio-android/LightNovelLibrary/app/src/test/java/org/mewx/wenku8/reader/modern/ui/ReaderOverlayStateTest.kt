package org.mewx.wenku8.reader.modern.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter

class ReaderOverlayStateTest {
    @Test
    fun startsWithControlsVisibleAndSheetsHidden() {
        val state = ReaderOverlayState()

        assertTrue(state.controlsVisible)
        assertFalse(state.settingsVisible)
        assertFalse(state.catalogVisible)
    }

    @Test
    fun toggleControlsOnlyChangesControlVisibility() {
        val state = ReaderOverlayState(
            controlsVisible = true,
            settingsVisible = true,
            catalogVisible = false,
        )

        val next = state.toggleControls()

        assertFalse(next.controlsVisible)
        assertTrue(next.settingsVisible)
        assertFalse(next.catalogVisible)
    }

    @Test
    fun openingSettingsClosesCatalog() {
        val state = ReaderOverlayState(
            controlsVisible = true,
            settingsVisible = false,
            catalogVisible = true,
        )

        val next = state.openSettings()

        assertTrue(next.controlsVisible)
        assertTrue(next.settingsVisible)
        assertFalse(next.catalogVisible)
    }

    @Test
    fun openingCatalogClosesSettings() {
        val state = ReaderOverlayState(
            controlsVisible = true,
            settingsVisible = true,
            catalogVisible = false,
        )

        val next = state.openCatalog()

        assertTrue(next.controlsVisible)
        assertFalse(next.settingsVisible)
        assertTrue(next.catalogVisible)
    }

    @Test
    fun dismissesSheetsIndependently() {
        val state = ReaderOverlayState(
            controlsVisible = true,
            settingsVisible = true,
            catalogVisible = true,
        )

        assertFalse(state.dismissSettings().settingsVisible)
        assertTrue(state.dismissSettings().catalogVisible)
        assertTrue(state.dismissCatalog().settingsVisible)
        assertFalse(state.dismissCatalog().catalogVisible)
    }

    @Test
    fun selectingCatalogChapterClosesCatalogAndCarriesSelection() {
        val chapter = ReaderCatalogChapter(cid = 103, title = "下一章")
        val state = ReaderOverlayState(
            controlsVisible = true,
            settingsVisible = false,
            catalogVisible = true,
        )

        val selection = state.selectCatalogChapter(chapter)

        assertFalse(selection.state.catalogVisible)
        assertEquals(chapter, selection.chapter)
    }
}
