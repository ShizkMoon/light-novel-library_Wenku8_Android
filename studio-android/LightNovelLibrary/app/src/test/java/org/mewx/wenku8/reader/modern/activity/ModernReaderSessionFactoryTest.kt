package org.mewx.wenku8.reader.modern.activity

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mewx.wenku8.reader.modern.model.ReaderBlock
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ModernReaderSessionFactoryTest {
    @Test
    fun createsSessionFromInjectedMeasurerAndLayoutForDisplaySettings() {
        val capturedSettings = mutableListOf<ModernReaderDisplaySettings>()
        val factory = ModernReaderSessionFactory(
            createTextMeasurer = { settings ->
                capturedSettings += settings
                textMeasurer
            },
            createLayoutSpec = { settings ->
                capturedSettings += settings
                layout
            },
        )
        val firstPage = ModernReaderSession(
            document = document,
            textMeasurer = textMeasurer,
            layout = layout,
        ).currentPage

        val session = factory.create(
            document = document,
            displaySettings = displaySettings,
            initialCursor = firstPage.end,
        )

        assertEquals(listOf(displaySettings, displaySettings), capturedSettings)
        assertEquals(firstPage.end, session.currentPage.start)
        assertEquals(1, session.pageIndex)
    }

    private companion object {
        val displaySettings = ModernReaderDisplaySettings(
            fontSizeSp = 22,
            lineHeightSp = 34,
            nightMode = true,
        )
        val document = ReaderDocument(
            title = "正文标题",
            blocks = listOf(ReaderBlock.Paragraph("甲乙丙丁戊己庚辛壬癸")),
        )
        val textMeasurer = ReaderTextMeasurer { text -> text.length * 10f }
        val layout = ReaderLayoutSpec(
            contentWidthPx = 60,
            contentHeightPx = 20,
            fontHeightPx = 20,
            lineSpacingPx = 4,
            paragraphSpacingPx = 8,
        )
    }
}
