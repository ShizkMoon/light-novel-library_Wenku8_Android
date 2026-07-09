package org.mewx.wenku8.reader.modern.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModernReaderDisplaySettingsControllerTest {
    @Test
    fun loadNormalizesOutOfRangeStoredSettings() {
        val store = FakeDisplaySettingsStore(
            ModernReaderDisplaySettings(
                fontSizeSp = 99,
                lineHeightSp = 12,
                nightMode = true,
            ),
        )

        val controller = ModernReaderDisplaySettingsController(store)

        assertEquals(ModernReaderDisplaySettings.MAX_FONT_SIZE_SP, controller.current.fontSizeSp)
        assertEquals(
            ModernReaderDisplaySettings.minimumLineHeightFor(ModernReaderDisplaySettings.MAX_FONT_SIZE_SP),
            controller.current.lineHeightSp,
        )
        assertTrue(controller.current.nightMode)
    }

    @Test
    fun fontSizeChangesAreClampedAndSaved() {
        val store = FakeDisplaySettingsStore(ModernReaderDisplaySettings(fontSizeSp = 29))
        val controller = ModernReaderDisplaySettingsController(store)

        assertEquals(ModernReaderDisplaySettings.MAX_FONT_SIZE_SP, controller.increaseFontSize().fontSizeSp)
        assertEquals(ModernReaderDisplaySettings.MAX_FONT_SIZE_SP, controller.increaseFontSize().fontSizeSp)
        assertEquals(
            ModernReaderDisplaySettings(
                fontSizeSp = 30,
                lineHeightSp = ModernReaderDisplaySettings.minimumLineHeightFor(30),
            ),
            store.saved.last(),
        )
    }

    @Test
    fun lineHeightChangesAreClampedAndSaved() {
        val store = FakeDisplaySettingsStore(ModernReaderDisplaySettings(lineHeightSp = 29))
        val controller = ModernReaderDisplaySettingsController(store)

        val minimumReadableLineHeight = ModernReaderDisplaySettings.minimumLineHeightFor(
            controller.current.fontSizeSp,
        )

        assertEquals(minimumReadableLineHeight, controller.decreaseLineHeight().lineHeightSp)
        assertEquals(minimumReadableLineHeight, controller.decreaseLineHeight().lineHeightSp)
        assertEquals(ModernReaderDisplaySettings(lineHeightSp = minimumReadableLineHeight), store.saved.last())
    }

    @Test
    fun nightModeChangeIsSaved() {
        val store = FakeDisplaySettingsStore(ModernReaderDisplaySettings())
        val controller = ModernReaderDisplaySettingsController(store)

        assertEquals(true, controller.setNightMode(true).nightMode)

        assertEquals(ModernReaderDisplaySettings(nightMode = true), store.saved.last())
    }

    private class FakeDisplaySettingsStore(
        private val initial: ModernReaderDisplaySettings,
    ) : ModernReaderDisplaySettingsStore {
        val saved = mutableListOf<ModernReaderDisplaySettings>()

        override fun load(): ModernReaderDisplaySettings = initial

        override fun save(settings: ModernReaderDisplaySettings) {
            saved += settings
        }
    }
}
