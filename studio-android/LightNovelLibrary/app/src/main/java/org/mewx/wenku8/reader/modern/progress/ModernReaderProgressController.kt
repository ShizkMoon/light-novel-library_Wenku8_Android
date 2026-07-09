package org.mewx.wenku8.reader.modern.progress

import org.mewx.wenku8.reader.modern.model.ReaderCursor

data class ModernReaderProgressRecord(
    val aid: Int,
    val vid: Int,
    val cid: Int,
    val cursor: ReaderCursor,
)

interface ModernReaderProgressStore {
    fun load(aid: Int): ModernReaderProgressRecord?

    fun save(record: ModernReaderProgressRecord)
}

class ModernReaderProgressController(
    private val store: ModernReaderProgressStore,
) {
    fun initialCursor(
        aid: Int,
        vid: Int,
        cid: Int,
        shouldRestore: Boolean,
    ): ReaderCursor {
        if (!shouldRestore) return ReaderCursor.START

        val record = store.load(aid) ?: return ReaderCursor.START
        return if (record.vid == vid && record.cid == cid) record.cursor else ReaderCursor.START
    }

    fun saveCurrentCursor(
        aid: Int,
        vid: Int,
        cid: Int,
        cursor: ReaderCursor,
    ) {
        store.save(
            ModernReaderProgressRecord(
                aid = aid,
                vid = vid,
                cid = cid,
                cursor = cursor,
            ),
        )
    }
}
