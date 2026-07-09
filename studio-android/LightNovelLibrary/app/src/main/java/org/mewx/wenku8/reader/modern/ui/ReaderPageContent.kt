package org.mewx.wenku8.reader.modern.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.mewx.wenku8.reader.modern.model.ReaderLineType
import org.mewx.wenku8.reader.modern.model.ReaderPage
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

@Composable
internal fun ReaderPageContent(
    state: ModernReaderUiState,
    textColor: Color,
    displaySettings: ModernReaderDisplaySettings,
    modifier: Modifier = Modifier,
    cachedImagePathForSource: (String) -> String? = { null },
    onRequestImageCache: (ReaderImageCacheRequest) -> Unit = {},
    onOpenImage: (String) -> Unit = {},
) {
    val model = ReaderPageContentUiModel.from(state)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        when (model.mode) {
            ReaderPageContentMode.MESSAGE -> ReaderCenteredMessage(
                title = model.messageTitle.orEmpty(),
                message = model.message.orEmpty(),
                textColor = textColor,
                modifier = Modifier.fillMaxSize(),
            )
            ReaderPageContentMode.PAGE -> {
                val page = model.page ?: return@Column
                ReaderLines(
                    page = page,
                    textColor = textColor,
                    displaySettings = displaySettings,
                    cachedImagePathForSource = cachedImagePathForSource,
                    onRequestImageCache = onRequestImageCache,
                    onOpenImage = onOpenImage,
                )
            }
        }
    }
}

@Composable
private fun ReaderLines(
    page: ReaderPage,
    textColor: Color,
    displaySettings: ModernReaderDisplaySettings,
    cachedImagePathForSource: (String) -> String?,
    onRequestImageCache: (ReaderImageCacheRequest) -> Unit,
    onOpenImage: (String) -> Unit,
) {
    page.lines.forEach { line ->
        if (line.type == ReaderLineType.IMAGE) {
            ReaderImageLine(
                model = ReaderImageLineUiModel.from(line, cachedImagePathForSource),
                onRequestImageCache = onRequestImageCache,
                onOpenImage = onOpenImage,
            )
        } else {
            Text(
                text = line.text,
                color = textColor,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = displaySettings.fontSizeSp.sp,
                    lineHeight = displaySettings.lineHeightSp.sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                softWrap = false,
                modifier = Modifier.padding(vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun ReaderImageLine(
    model: ReaderImageLineUiModel,
    onRequestImageCache: (ReaderImageCacheRequest) -> Unit,
    onOpenImage: (String) -> Unit,
) {
    val cachedPath = model.cachedPath
    val bitmap = cachedPath?.let(BitmapFactory::decodeFile)
    val surfaceModifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)
        .height(180.dp)
    val bitmapAvailable = bitmap != null
    val cacheRequest = model.cacheRequest(bitmapAvailable)
    if (cacheRequest != null) {
        LaunchedEffect(cacheRequest) {
            onRequestImageCache(cacheRequest)
        }
    }

    when (model.displayMode(bitmapAvailable)) {
        ReaderImageLineDisplayMode.IMAGE -> {
            val openImagePath = model.openImagePath(bitmapAvailable) ?: return
            val imageBitmap = bitmap ?: return
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = surfaceModifier.clickable { onOpenImage(openImagePath) },
            ) {
                Image(
                    bitmap = imageBitmap.asImageBitmap(),
                    contentDescription = model.placeholderText,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        ReaderImageLineDisplayMode.PLACEHOLDER,
        ReaderImageLineDisplayMode.BROKEN_CACHE -> {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = surfaceModifier,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = model.placeholderText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderCenteredMessage(
    title: String,
    message: String,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = title,
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = message,
                color = textColor.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
