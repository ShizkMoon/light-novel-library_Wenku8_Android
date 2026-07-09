package org.mewx.wenku8.reader.modern.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReaderSettingsSheet(
    displaySettings: ModernReaderDisplaySettings,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit,
    onDecreaseLineHeight: () -> Unit,
    onIncreaseLineHeight: () -> Unit,
    onDecreaseParagraphSpacing: () -> Unit,
    onIncreaseParagraphSpacing: () -> Unit,
    onNightModeChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val model = remember(displaySettings) { ReaderSettingsUiModel.from(displaySettings) }
    val supportingTextColor = if (displaySettings.nightMode) {
        Color(0xFFB7C0CB)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (displaySettings.nightMode) Color(0xFF22252B) else MaterialTheme.colorScheme.surface,
        contentColor = if (displaySettings.nightMode) Color(0xFFE6EAF0) else MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(
                text = "阅读设置",
                style = MaterialTheme.typography.titleLarge,
            )
            ReaderSettingsStepper(
                label = "字号",
                value = model.fontSizeValue,
                supportingTextColor = supportingTextColor,
                onDecrease = onDecreaseFontSize,
                onIncrease = onIncreaseFontSize,
            )
            ReaderSettingsStepper(
                label = "行高",
                value = model.lineHeightValue,
                supportingTextColor = supportingTextColor,
                onDecrease = onDecreaseLineHeight,
                onIncrease = onIncreaseLineHeight,
            )
            ReaderSettingsStepper(
                label = "段间距",
                value = model.paragraphSpacingValue,
                supportingTextColor = supportingTextColor,
                onDecrease = onDecreaseParagraphSpacing,
                onIncrease = onIncreaseParagraphSpacing,
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(text = "夜间模式", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = model.nightModeDescription,
                        color = supportingTextColor,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = displaySettings.nightMode,
                    onCheckedChange = onNightModeChange,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ReaderSettingsStepper(
    label: String,
    value: String,
    supportingTextColor: Color,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = value,
                color = supportingTextColor,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDecrease) { Text("减小") }
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(onClick = onIncrease) { Text("增大") }
        }
    }
}
