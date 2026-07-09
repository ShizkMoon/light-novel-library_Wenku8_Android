package org.mewx.wenku8.reader.modern.activity

import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ModernReaderSessionFactory(
    private val createTextMeasurer: (ModernReaderDisplaySettings) -> ReaderTextMeasurer,
    private val createLayoutSpec: (ModernReaderDisplaySettings) -> ReaderLayoutSpec,
) {
    fun create(
        document: ReaderDocument,
        displaySettings: ModernReaderDisplaySettings,
        initialCursor: ReaderCursor,
    ): ModernReaderSession =
        ModernReaderSession(
            document = document,
            textMeasurer = createTextMeasurer(displaySettings),
            layout = createLayoutSpec(displaySettings),
            initialCursor = initialCursor,
        )
}
