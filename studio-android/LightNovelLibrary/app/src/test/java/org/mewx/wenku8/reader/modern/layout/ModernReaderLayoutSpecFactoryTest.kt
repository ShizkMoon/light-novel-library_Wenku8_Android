package org.mewx.wenku8.reader.modern.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ModernReaderLayoutSpecFactoryTest {
    @Test
    fun createsReadableSpecFromPhoneMetrics() {
        val spec = ModernReaderLayoutSpecFactory.create(
            ModernReaderWindowMetrics(
                widthPx = 1080,
                heightPx = 2400,
                density = 3f,
                scaledDensity = 3f,
            ),
        )

        assertEquals(792, spec.contentWidthPx)
        assertEquals(1668, spec.contentHeightPx)
        assertEquals(96, spec.fontHeightPx)
        assertEquals(30, spec.lineSpacingPx)
        assertEquals(54, spec.paragraphSpacingPx)
    }

    @Test
    fun keepsMinimumReadingAreaForSmallWindows() {
        val spec = ModernReaderLayoutSpecFactory.create(
            ModernReaderWindowMetrics(
                widthPx = 360,
                heightPx = 480,
                density = 1f,
                scaledDensity = 1.25f,
            ),
        )

        assertEquals(264, spec.contentWidthPx)
        assertEquals(360, spec.contentHeightPx)
        assertEquals(40, spec.fontHeightPx)
        assertTrue(spec.lineSpacingPx > 0)
    }

    @Test
    fun displaySettingsControlMeasuredLineHeight() {
        val spec = ModernReaderLayoutSpecFactory.create(
            metrics = ModernReaderWindowMetrics(
                widthPx = 1080,
                heightPx = 2400,
                density = 3f,
                scaledDensity = 3f,
            ),
            displaySettings = ModernReaderDisplaySettings(
                fontSizeSp = 24,
                lineHeightSp = 40,
            ),
        )

        assertEquals(120, spec.fontHeightPx)
    }
}
