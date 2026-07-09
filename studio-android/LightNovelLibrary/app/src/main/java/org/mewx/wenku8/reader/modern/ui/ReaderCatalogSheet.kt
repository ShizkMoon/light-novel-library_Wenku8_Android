package org.mewx.wenku8.reader.modern.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderCatalogSheet(
    title: String,
    chapterTitle: String,
    catalog: ModernReaderCatalog,
    isNightMode: Boolean,
    onSelectChapter: (ReaderCatalogChapter) -> Unit,
    onDismiss: () -> Unit,
) {
    val catalogUi = ReaderCatalogUiModel.from(
        title = title,
        chapterTitle = chapterTitle,
        catalog = catalog,
    )
    val supportingTextColor = if (isNightMode) {
        Color(0xFFB7C0CB)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (isNightMode) Color(0xFF22252B) else MaterialTheme.colorScheme.surface,
        contentColor = if (isNightMode) Color(0xFFE6EAF0) else MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "目录",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "关闭目录",
                    )
                }
            }
            catalogUi.supportingMessage?.let { message ->
                Text(
                    text = message,
                    color = supportingTextColor,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
            Text(
                text = catalogUi.summaryText,
                color = supportingTextColor,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            ReaderChapterList(
                catalog = catalogUi,
                supportingTextColor = supportingTextColor,
                onSelectChapter = onSelectChapter,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ReaderChapterList(
    catalog: ReaderCatalogUiModel,
    supportingTextColor: Color,
    onSelectChapter: (ReaderCatalogChapter) -> Unit,
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = catalog.initialFirstVisibleItemIndex,
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp),
        ) {
            catalog.sections.forEach { section ->
                item(key = "volume-${section.volumeId}") {
                    Text(
                        text = section.title,
                        color = supportingTextColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                items(
                    items = section.chapters,
                    key = { chapter -> "${section.volumeId}:${chapter.source.cid}" },
                ) { chapter ->
                    ReaderChapterRow(
                        chapter = chapter,
                        onClick = { onSelectChapter(chapter.source) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderChapterRow(
    chapter: ReaderCatalogChapterUiModel,
    onClick: () -> Unit,
) {
    val rowBackground = if (chapter.isSelectable) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBackground)
            .clickable(enabled = chapter.isSelectable, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (chapter.isSelectable) FontWeight.Normal else FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        chapter.stateLabel?.let { stateLabel ->
            Text(
                text = stateLabel,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
