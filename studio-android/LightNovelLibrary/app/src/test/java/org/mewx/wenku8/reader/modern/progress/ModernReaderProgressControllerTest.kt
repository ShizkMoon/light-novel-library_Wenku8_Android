package org.mewx.wenku8.reader.modern.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mewx.wenku8.reader.modern.model.ReaderCursor

class ModernReaderProgressControllerTest {
    private val savedCursor = ReaderCursor(blockIndex = 3, charIndex = 14)

    @Test
    fun initialCursorReturnsStartWhenRestoreWasNotRequested() {
        val controller = ModernReaderProgressController(
            store = FakeProgressStore(
                ModernReaderProgressRecord(
                    aid = 1,
                    vid = 2,
                    cid = 3,
                    cursor = savedCursor,
                ),
            ),
        )

        val cursor = controller.initialCursor(aid = 1, vid = 2, cid = 3, shouldRestore = false)

        assertEquals(ReaderCursor.START, cursor)
    }

    @Test
    fun initialCursorReturnsSavedCursorWhenAidVolumeAndChapterMatch() {
        val controller = ModernReaderProgressController(
            store = FakeProgressStore(
                ModernReaderProgressRecord(
                    aid = 1,
                    vid = 2,
                    cid = 3,
                    cursor = savedCursor,
                ),
            ),
        )

        val cursor = controller.initialCursor(aid = 1, vid = 2, cid = 3, shouldRestore = true)

        assertEquals(savedCursor, cursor)
    }

    @Test
    fun initialCursorIgnoresSavedCursorForDifferentVolumeOrChapter() {
        val controller = ModernReaderProgressController(
            store = FakeProgressStore(
                ModernReaderProgressRecord(
                    aid = 1,
                    vid = 2,
                    cid = 3,
                    cursor = savedCursor,
                ),
            ),
        )

        assertEquals(ReaderCursor.START, controller.initialCursor(aid = 1, vid = 9, cid = 3, shouldRestore = true))
        assertEquals(ReaderCursor.START, controller.initialCursor(aid = 1, vid = 2, cid = 9, shouldRestore = true))
    }

    @Test
    fun saveCurrentCursorWritesLegacyCompatibleRecord() {
        val store = FakeProgressStore()
        val controller = ModernReaderProgressController(store = store)

        controller.saveCurrentCursor(aid = 1, vid = 2, cid = 3, cursor = savedCursor)

        assertEquals(
            ModernReaderProgressRecord(
                aid = 1,
                vid = 2,
                cid = 3,
                cursor = savedCursor,
            ),
            store.savedRecord,
        )
    }

    private class FakeProgressStore(
        private val loadedRecord: ModernReaderProgressRecord? = null,
    ) : ModernReaderProgressStore {
        var savedRecord: ModernReaderProgressRecord? = null

        override fun load(aid: Int): ModernReaderProgressRecord? = loadedRecord

        override fun save(record: ModernReaderProgressRecord) {
            savedRecord = record
        }
    }
}
