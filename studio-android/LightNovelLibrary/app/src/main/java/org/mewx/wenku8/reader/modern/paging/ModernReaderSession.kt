package org.mewx.wenku8.reader.modern.paging

import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.model.ReaderPage

class ModernReaderSession(
    document: ReaderDocument,
    textMeasurer: ReaderTextMeasurer,
    private val layout: ReaderLayoutSpec,
    initialCursor: ReaderCursor = ReaderCursor.START,
) {
    private val paginator = ModernReaderPaginator(document, textMeasurer)

    var currentPage: ReaderPage
        private set

    var pageIndex: Int
        private set

    val pageCount: Int

    init {
        pageCount = countPages()
        currentPage = paginator.pageFrom(initialCursor, layout)
        pageIndex = countPagesBefore(currentPage.start).coerceAtMost(pageCount - 1)
    }

    fun nextPage(): ReaderPage {
        if (!currentPage.hasMoreAfter) return currentPage

        currentPage = paginator.pageFrom(currentPage.end, layout)
        pageIndex += 1
        return currentPage
    }

    fun previousPage(): ReaderPage {
        if (!currentPage.hasMoreBefore) return currentPage

        currentPage = paginator.pageBefore(currentPage.start, layout)
        pageIndex = (pageIndex - 1).coerceAtLeast(0)
        return currentPage
    }

    fun goToPage(targetPageIndex: Int): ReaderPage {
        val target = targetPageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
        var index = 0
        var page = paginator.pageFrom(ReaderCursor.START, layout)

        while (index < target && page.hasMoreAfter) {
            val nextPage = paginator.pageFrom(page.end, layout)
            if (nextPage.end <= page.end) break

            page = nextPage
            index += 1
        }

        currentPage = page
        pageIndex = index
        return currentPage
    }

    private fun countPages(): Int {
        var count = 0
        var cursor = ReaderCursor.START

        while (true) {
            val page = paginator.pageFrom(cursor, layout)
            count += 1

            if (!page.hasMoreAfter || page.end <= cursor) break
            cursor = page.end
        }

        return count
    }

    private fun countPagesBefore(target: ReaderCursor): Int {
        var index = 0
        var cursor = ReaderCursor.START

        while (cursor < target) {
            val page = paginator.pageFrom(cursor, layout)
            if (page.end <= cursor) break

            cursor = page.end
            if (cursor <= target) {
                index += 1
            }
        }

        return index
    }
}
