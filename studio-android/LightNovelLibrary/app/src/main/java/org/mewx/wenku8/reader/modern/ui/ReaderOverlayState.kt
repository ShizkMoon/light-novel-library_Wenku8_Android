package org.mewx.wenku8.reader.modern.ui

import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter

data class ReaderOverlayState(
    val controlsVisible: Boolean = true,
    val settingsVisible: Boolean = false,
    val catalogVisible: Boolean = false,
) {
    fun toggleControls(): ReaderOverlayState =
        copy(controlsVisible = !controlsVisible)

    fun openSettings(): ReaderOverlayState =
        copy(settingsVisible = true, catalogVisible = false)

    fun dismissSettings(): ReaderOverlayState =
        copy(settingsVisible = false)

    fun openCatalog(): ReaderOverlayState =
        copy(settingsVisible = false, catalogVisible = true)

    fun dismissCatalog(): ReaderOverlayState =
        copy(catalogVisible = false)

    fun selectCatalogChapter(chapter: ReaderCatalogChapter): ReaderCatalogSelection =
        ReaderCatalogSelection(
            state = dismissCatalog(),
            chapter = chapter,
        )
}

data class ReaderCatalogSelection(
    val state: ReaderOverlayState,
    val chapter: ReaderCatalogChapter,
)
