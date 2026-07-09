package org.mewx.wenku8.reader.modern.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.model.ReaderPage
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

data class ModernReaderUiState(
    val title: String,
    val chapterTitle: String,
    val aid: Int,
    val cid: Int,
    val page: ReaderPage? = null,
    val pageIndex: Int = 0,
    val pageCount: Int = 1,
    val catalog: ModernReaderCatalog = ModernReaderCatalog.from(volume = null, currentCid = cid),
    val progressText: String = "",
    val displaySettings: ModernReaderDisplaySettings = ModernReaderDisplaySettings(),
    val isNightMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val canGoPrevious: Boolean
        get() = page?.hasMoreBefore == true || catalog.previousChapter != null

    val canGoNext: Boolean
        get() = page?.hasMoreAfter == true || catalog.nextChapter != null

    val previousNavigationLabel: String
        get() = if (page?.hasMoreBefore == true || catalog.previousChapter == null) {
            "上一页"
        } else {
            "上一章"
        }

    val nextNavigationLabel: String
        get() = if (page?.hasMoreAfter == true || catalog.nextChapter == null) {
            "下一页"
        } else {
            "下一章"
        }
}

@Composable
fun ModernReaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.lightColorScheme(
            primary = Color(0xFF2F6FDB),
            onPrimary = Color.White,
            surface = Color(0xFFFFFBF2),
            onSurface = Color(0xFF28231D),
            surfaceVariant = Color(0xFFEDE3D2),
            onSurfaceVariant = Color(0xFF5F5548),
        ),
        content = content,
    )
}

@Composable
fun ModernReaderScreen(
    state: ModernReaderUiState,
    modifier: Modifier = Modifier,
    onPreviousPage: () -> Unit = {},
    onNextPage: () -> Unit = {},
    onDecreaseFontSize: () -> Unit = {},
    onIncreaseFontSize: () -> Unit = {},
    onDecreaseLineHeight: () -> Unit = {},
    onIncreaseLineHeight: () -> Unit = {},
    onNightModeChange: (Boolean) -> Unit = {},
    onSelectPage: (Int) -> Unit = {},
    onSelectChapter: (ReaderCatalogChapter) -> Unit = {},
    cachedImagePathForSource: (String) -> String? = { null },
    onRequestImageCache: (ReaderImageCacheRequest) -> Unit = {},
    onOpenImage: (String) -> Unit = {},
) {
    var overlayState by remember { mutableStateOf(ReaderOverlayState()) }
    val chrome = ReaderChromeUiModel.from(state)
    val pageBackground = if (state.displaySettings.nightMode) Color(0xFF17191D) else MaterialTheme.colorScheme.surface
    val pageText = if (state.displaySettings.nightMode) Color(0xFFD9DEE6) else MaterialTheme.colorScheme.onSurface
    fun performNavigation(action: ReaderNavigationAction) {
        when (action.target) {
            ReaderNavigationTarget.PREVIOUS_PAGE -> onPreviousPage()
            ReaderNavigationTarget.NEXT_PAGE -> onNextPage()
            ReaderNavigationTarget.PREVIOUS_CHAPTER,
            ReaderNavigationTarget.NEXT_CHAPTER -> action.chapter?.let(onSelectChapter)
            ReaderNavigationTarget.NONE -> Unit
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(
                    state.page?.start,
                    state.page?.end,
                    state.catalog.previousChapter?.cid,
                    state.catalog.nextChapter?.cid,
                ) {
                    detectTapGestures { offset ->
                        when (
                            ReaderInteractionModel.tapAction(
                                contentWidthPx = size.width,
                                tapX = offset.x,
                                canGoPrevious = chrome.canGoPrevious,
                                canGoNext = chrome.canGoNext,
                            )
                        ) {
                            ReaderTapAction.PREVIOUS -> performNavigation(ReaderNavigationModel.previousAction(state))
                            ReaderTapAction.NEXT -> performNavigation(ReaderNavigationModel.nextAction(state))
                            ReaderTapAction.TOGGLE_CONTROLS -> overlayState = overlayState.toggleControls()
                        }
                    }
                },
        )

        ReaderPageContent(
            state = state,
            textColor = pageText,
            displaySettings = state.displaySettings,
            cachedImagePathForSource = cachedImagePathForSource,
            onRequestImageCache = onRequestImageCache,
            onOpenImage = onOpenImage,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp, vertical = 20.dp),
        )

        if (overlayState.controlsVisible) {
            ReaderTopBar(
                chrome = chrome,
                onOpenCatalog = { overlayState = overlayState.openCatalog() },
                onOpenSettings = { overlayState = overlayState.openSettings() },
                modifier = Modifier.align(Alignment.TopCenter),
            )
            if (chrome.hasPage) {
                ReaderBottomBar(
                    chrome = chrome,
                    onPreviousPage = { performNavigation(ReaderNavigationModel.previousAction(state)) },
                    onNextPage = { performNavigation(ReaderNavigationModel.nextAction(state)) },
                    onSelectPage = onSelectPage,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }

        if (overlayState.settingsVisible) {
            ReaderSettingsSheet(
                displaySettings = state.displaySettings,
                onDecreaseFontSize = onDecreaseFontSize,
                onIncreaseFontSize = onIncreaseFontSize,
                onDecreaseLineHeight = onDecreaseLineHeight,
                onIncreaseLineHeight = onIncreaseLineHeight,
                onNightModeChange = onNightModeChange,
                onDismiss = { overlayState = overlayState.dismissSettings() },
            )
        }

        if (overlayState.catalogVisible) {
            ReaderCatalogSheet(
                title = state.title,
                chapterTitle = state.chapterTitle,
                catalog = state.catalog,
                isNightMode = state.displaySettings.nightMode,
                onSelectChapter = { chapter ->
                    val selection = overlayState.selectCatalogChapter(chapter)
                    overlayState = selection.state
                    onSelectChapter(selection.chapter)
                },
                onDismiss = { overlayState = overlayState.dismissCatalog() },
            )
        }
    }
}
