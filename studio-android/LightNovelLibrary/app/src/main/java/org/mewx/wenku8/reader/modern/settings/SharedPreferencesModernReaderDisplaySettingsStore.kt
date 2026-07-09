package org.mewx.wenku8.reader.modern.settings

import android.content.Context

class SharedPreferencesModernReaderDisplaySettingsStore(
    context: Context,
) : ModernReaderDisplaySettingsStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun load(): ModernReaderDisplaySettings =
        ModernReaderDisplaySettings(
            fontSizeSp = preferences.getInt(
                KEY_FONT_SIZE_SP,
                ModernReaderDisplaySettings.DEFAULT_FONT_SIZE_SP,
            ),
            lineHeightSp = preferences.getInt(
                KEY_LINE_HEIGHT_SP,
                ModernReaderDisplaySettings.DEFAULT_LINE_HEIGHT_SP,
            ),
            paragraphSpacingSp = preferences.getInt(
                KEY_PARAGRAPH_SPACING_SP,
                ModernReaderDisplaySettings.DEFAULT_PARAGRAPH_SPACING_SP,
            ),
            nightMode = preferences.getBoolean(KEY_NIGHT_MODE, false),
        ).normalized()

    override fun save(settings: ModernReaderDisplaySettings) {
        val normalized = settings.normalized()
        preferences.edit()
            .putInt(KEY_FONT_SIZE_SP, normalized.fontSizeSp)
            .putInt(KEY_LINE_HEIGHT_SP, normalized.lineHeightSp)
            .putInt(KEY_PARAGRAPH_SPACING_SP, normalized.paragraphSpacingSp)
            .putBoolean(KEY_NIGHT_MODE, normalized.nightMode)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "modern_reader_display_settings"
        const val KEY_FONT_SIZE_SP = "font_size_sp"
        const val KEY_LINE_HEIGHT_SP = "line_height_sp"
        const val KEY_PARAGRAPH_SPACING_SP = "paragraph_spacing_sp"
        const val KEY_NIGHT_MODE = "night_mode"
    }
}
