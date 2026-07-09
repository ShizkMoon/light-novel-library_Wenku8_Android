package org.mewx.wenku8.reader.modern.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ReaderSettingsUiModelTest {
    @Test
    fun fromSettingsFormatsStepperValues() {
        val model = ReaderSettingsUiModel.from(
            ModernReaderDisplaySettings(
                fontSizeSp = 21,
                lineHeightSp = 34,
            ),
        )

        assertEquals("21 sp", model.fontSizeValue)
        assertEquals("34 sp", model.lineHeightValue)
    }

    @Test
    fun fromSettingsDescribesCurrentThemeMode() {
        assertEquals(
            "暖白纸张阅读背景",
            ReaderSettingsUiModel.from(ModernReaderDisplaySettings(nightMode = false)).nightModeDescription,
        )
        assertEquals(
            "低亮度深色阅读背景",
            ReaderSettingsUiModel.from(ModernReaderDisplaySettings(nightMode = true)).nightModeDescription,
        )
    }
}
