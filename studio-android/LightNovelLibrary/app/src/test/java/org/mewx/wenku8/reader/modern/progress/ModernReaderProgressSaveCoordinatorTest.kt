package org.mewx.wenku8.reader.modern.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchContext
import org.mewx.wenku8.reader.modern.model.ReaderBlock
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer

class ModernReaderProgressSaveCoordinatorTest {
    @Test
    fun savesExplicitCursorForCurrentLaunchContext() {
        val store = FakeProgressStore()
        val coordinator = coordinator(store)
        val cursor = ReaderCursor(blockIndex = 2, charIndex = 7)

        val savedCursor = coordinator.saveCurrentProgress(
            context = launchContext,
            session = null,
            cursor = cursor,
        )

        assertEquals(cursor, savedCursor)
        assertEquals(
            ModernReaderProgressRecord(
                aid = 10,
                vid = 20,
                cid = 30,
                cursor = cursor,
            ),
            store.savedRecords.single(),
        )
    }

    @Test
    fun fallsBackToCurrentSessionPageStartWhenCursorIsMissing() {
        val store = FakeProgressStore()
        val coordinator = coordinator(store)
        val session = sessionFor("甲乙丙丁戊己庚辛壬癸子丑寅卯辰巳午未")
        session.nextPage()
        val pageStart = session.currentPage.start
        assertTrue(session.currentPage.hasMoreAfter)

        val savedCursor = coordinator.saveCurrentProgress(
            context = launchContext,
            session = session,
            cursor = null,
        )

        assertEquals(pageStart, savedCursor)
        assertEquals(pageStart, store.savedRecords.single().cursor)
    }

    @Test
    fun clearsSavedProgressWhenCurrentSessionIsOnFinalPage() {
        val store = FakeProgressStore()
        val coordinator = coordinator(store)

        val savedCursor = coordinator.saveCurrentProgress(
            context = launchContext,
            session = sessionFor("甲乙丙丁"),
            cursor = null,
        )

        assertNull(savedCursor)
        assertEquals(emptyList<ModernReaderProgressRecord>(), store.savedRecords)
        assertEquals(10, store.clearedAid)
    }

    @Test
    fun doesNotSaveWithoutLaunchContext() {
        val store = FakeProgressStore()
        val coordinator = coordinator(store)

        val savedCursor = coordinator.saveCurrentProgress(
            context = null,
            session = sessionFor("甲乙丙丁"),
            cursor = ReaderCursor(blockIndex = 1, charIndex = 3),
        )

        assertNull(savedCursor)
        assertEquals(emptyList<ModernReaderProgressRecord>(), store.savedRecords)
    }

    @Test
    fun doesNotSaveWithoutCursorOrSession() {
        val store = FakeProgressStore()
        val coordinator = coordinator(store)

        val savedCursor = coordinator.saveCurrentProgress(
            context = launchContext,
            session = null,
            cursor = null,
        )

        assertNull(savedCursor)
        assertEquals(emptyList<ModernReaderProgressRecord>(), store.savedRecords)
    }

    private fun coordinator(store: FakeProgressStore): ModernReaderProgressSaveCoordinator =
        ModernReaderProgressSaveCoordinator(
            progressController = ModernReaderProgressController(store),
        )

    private fun sessionFor(text: String): ModernReaderSession =
        ModernReaderSession(
            document = ReaderDocument(
                title = "正文标题",
                blocks = listOf(ReaderBlock.Paragraph(text)),
            ),
            textMeasurer = ReaderTextMeasurer { measured -> measured.length * 10f },
            layout = ReaderLayoutSpec(
                contentWidthPx = 60,
                contentHeightPx = 20,
                fontHeightPx = 20,
                lineSpacingPx = 4,
                paragraphSpacingPx = 8,
            ),
        )

    private class FakeProgressStore : ModernReaderProgressStore {
        val savedRecords = mutableListOf<ModernReaderProgressRecord>()
        var clearedAid: Int? = null

        override fun load(aid: Int): ModernReaderProgressRecord? = null

        override fun save(record: ModernReaderProgressRecord) {
            savedRecords += record
        }

        override fun clear(aid: Int) {
            clearedAid = aid
        }
    }

    private companion object {
        val launchContext = ReaderLaunchContext(
            aid = 10,
            vid = 20,
            cid = 30,
        )
    }
}
