package org.mewx.wenku8.reader.modern.settings

data class ModernReaderDisplaySettings(
    val fontSizeSp: Int = DEFAULT_FONT_SIZE_SP,
    val lineHeightSp: Int = DEFAULT_LINE_HEIGHT_SP,
    val nightMode: Boolean = false,
) {
    fun normalized(): ModernReaderDisplaySettings {
        val normalizedFontSize = fontSizeSp.coerceIn(MIN_FONT_SIZE_SP, MAX_FONT_SIZE_SP)
        val normalizedLineHeight = lineHeightSp
            .coerceIn(MIN_LINE_HEIGHT_SP, MAX_LINE_HEIGHT_SP)
            .coerceAtLeast(minimumLineHeightFor(normalizedFontSize))
        return copy(
            fontSizeSp = normalizedFontSize,
            lineHeightSp = normalizedLineHeight,
        )
    }

    companion object {
        const val DEFAULT_FONT_SIZE_SP = 20
        const val MIN_FONT_SIZE_SP = 16
        const val MAX_FONT_SIZE_SP = 30
        const val DEFAULT_LINE_HEIGHT_SP = 32
        const val MIN_LINE_HEIGHT_SP = 24
        const val MAX_LINE_HEIGHT_SP = 52
        private const val MIN_LINE_HEIGHT_EXTRA_SP = 8

        fun minimumLineHeightFor(fontSizeSp: Int): Int =
            (fontSizeSp + MIN_LINE_HEIGHT_EXTRA_SP).coerceAtLeast(MIN_LINE_HEIGHT_SP)
    }
}

interface ModernReaderDisplaySettingsStore {
    fun load(): ModernReaderDisplaySettings

    fun save(settings: ModernReaderDisplaySettings)
}

class ModernReaderDisplaySettingsController(
    private val store: ModernReaderDisplaySettingsStore,
) {
    var current: ModernReaderDisplaySettings = store.load().normalized()
        private set

    fun increaseFontSize(): ModernReaderDisplaySettings =
        update { it.copy(fontSizeSp = it.fontSizeSp + FONT_SIZE_STEP_SP) }

    fun decreaseFontSize(): ModernReaderDisplaySettings =
        update { it.copy(fontSizeSp = it.fontSizeSp - FONT_SIZE_STEP_SP) }

    fun increaseLineHeight(): ModernReaderDisplaySettings =
        update { it.copy(lineHeightSp = it.lineHeightSp + LINE_HEIGHT_STEP_SP) }

    fun decreaseLineHeight(): ModernReaderDisplaySettings =
        update { it.copy(lineHeightSp = it.lineHeightSp - LINE_HEIGHT_STEP_SP) }

    fun setNightMode(enabled: Boolean): ModernReaderDisplaySettings =
        update { it.copy(nightMode = enabled) }

    private fun update(
        transform: (ModernReaderDisplaySettings) -> ModernReaderDisplaySettings,
    ): ModernReaderDisplaySettings {
        current = transform(current).normalized()
        store.save(current)
        return current
    }

    private companion object {
        const val FONT_SIZE_STEP_SP = 1
        const val LINE_HEIGHT_STEP_SP = 2
    }
}
