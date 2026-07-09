package org.mewx.wenku8.reader.modern.layout

import kotlin.math.roundToInt
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

data class ModernReaderWindowMetrics(
    val widthPx: Int,
    val heightPx: Int,
    val density: Float,
    val scaledDensity: Float,
)

object ModernReaderLayoutSpecFactory {
    fun create(
        metrics: ModernReaderWindowMetrics,
        displaySettings: ModernReaderDisplaySettings = ModernReaderDisplaySettings(),
    ): ReaderLayoutSpec {
        val normalizedSettings = displaySettings.normalized()
        return ReaderLayoutSpec(
            contentWidthPx = (metrics.widthPx - metrics.dp(96)).coerceAtLeast(metrics.dp(240)),
            contentHeightPx = (metrics.heightPx - metrics.dp(244)).coerceAtLeast(metrics.dp(360)),
            fontHeightPx = metrics.sp(normalizedSettings.lineHeightSp),
            lineSpacingPx = metrics.dp(10),
            paragraphSpacingPx = metrics.sp(normalizedSettings.paragraphSpacingSp),
        )
    }

    private fun ModernReaderWindowMetrics.dp(value: Int): Int =
        (value * density).roundToInt()

    private fun ModernReaderWindowMetrics.sp(value: Int): Int =
        (value * scaledDensity).roundToInt()
}
