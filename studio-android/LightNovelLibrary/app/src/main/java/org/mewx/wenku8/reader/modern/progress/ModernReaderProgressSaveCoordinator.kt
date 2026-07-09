package org.mewx.wenku8.reader.modern.progress

import org.mewx.wenku8.reader.modern.launch.ReaderLaunchContext
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession

class ModernReaderProgressSaveCoordinator(
    private val progressController: ModernReaderProgressController,
) {
    fun saveCurrentProgress(
        context: ReaderLaunchContext?,
        session: ModernReaderSession?,
        cursor: ReaderCursor? = null,
    ): ReaderCursor? {
        val currentContext = context ?: return null
        val currentCursor = cursor ?: session?.currentPage?.start ?: return null
        progressController.saveCurrentCursor(
            aid = currentContext.aid,
            vid = currentContext.vid,
            cid = currentContext.cid,
            cursor = currentCursor,
        )
        return currentCursor
    }
}
