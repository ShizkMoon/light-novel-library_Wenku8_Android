package org.mewx.wenku8.reader.modern.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
internal fun ReaderTopBar(
    chrome: ReaderChromeUiModel,
    onOpenCatalog: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color.Black.copy(alpha = 0.72f),
        contentColor = Color.White,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = chrome.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (chrome.chapterTitle.isNotEmpty()) {
                    Text(
                        text = chrome.chapterTitle,
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onOpenCatalog) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "目录",
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "设置",
                )
            }
        }
    }
}

@Composable
internal fun ReaderBottomBar(
    chrome: ReaderChromeUiModel,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onSelectPage: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sliderMax = (chrome.pageCount - 1).coerceAtLeast(1).toFloat()
    var sliderValue by remember(chrome.pageIndex, chrome.pageCount) {
        mutableStateOf(chrome.pageIndex.coerceIn(0, (chrome.pageCount - 1).coerceAtLeast(0)).toFloat())
    }

    Surface(
        color = Color.Black.copy(alpha = 0.78f),
        contentColor = Color.White,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextButton(
                    enabled = chrome.canGoPrevious,
                    onClick = onPreviousPage,
                ) { Text(chrome.previousNavigationLabel) }
                Text(text = chrome.progressText, color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                TextButton(
                    enabled = chrome.canGoNext,
                    onClick = onNextPage,
                ) { Text(chrome.nextNavigationLabel) }
            }
            Slider(
                value = sliderValue.coerceIn(0f, sliderMax),
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onSelectPage(sliderValue.roundToInt()) },
                enabled = chrome.pageCount > 1,
                valueRange = 0f..sliderMax,
            )
        }
    }
}
