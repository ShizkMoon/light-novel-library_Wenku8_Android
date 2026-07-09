package org.mewx.wenku8.reader.modern.ui

import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

data class ReaderSettingsUiModel(
    val fontSizeValue: String,
    val lineHeightValue: String,
    val paragraphSpacingValue: String,
    val nightModeDescription: String,
) {
    companion object {
        fun from(settings: ModernReaderDisplaySettings): ReaderSettingsUiModel =
            ReaderSettingsUiModel(
                fontSizeValue = "${settings.fontSizeSp} sp",
                lineHeightValue = "${settings.lineHeightSp} sp",
                paragraphSpacingValue = "${settings.paragraphSpacingSp} sp",
                nightModeDescription = if (settings.nightMode) {
                    "低亮度深色阅读背景"
                } else {
                    "暖白纸张阅读背景"
                },
            )
    }
}
