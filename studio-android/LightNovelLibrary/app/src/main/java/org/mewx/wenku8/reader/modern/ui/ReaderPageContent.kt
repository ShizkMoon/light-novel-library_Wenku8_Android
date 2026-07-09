package org.mewx.wenku8.reader.modern.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
) {
    page.lines.forEach { line ->
        if (line.type == ReaderLineType.IMAGE) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 12.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "插图 ${line.source.orEmpty()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
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
