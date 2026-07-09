package org.mewx.wenku8.reader.modern.progress

import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.reader.modern.model.ReaderCursor

class GlobalConfigReaderProgressStore : ModernReaderProgressStore {
    override fun load(aid: Int): ModernReaderProgressRecord? {
        val record = GlobalConfig.getReadSavesRecordV1(aid) ?: return null
        return ModernReaderProgressRecord(
            aid = record.aid,
            vid = record.vid,
            cid = record.cid,
            cursor = ReaderCursor(
                blockIndex = record.lineId,
                charIndex = record.wordId,
            ),
        )
    }

    override fun save(record: ModernReaderProgressRecord) {
        GlobalConfig.addReadSavesRecordV1(
            record.aid,
            record.vid,
            record.cid,
            record.cursor.blockIndex,
            record.cursor.charIndex,
        )
    }
}
