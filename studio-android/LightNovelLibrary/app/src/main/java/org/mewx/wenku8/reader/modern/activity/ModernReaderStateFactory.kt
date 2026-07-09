package org.mewx.wenku8.reader.modern.activity

import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadFailure
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadResult
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.model.ReaderPage
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings
import org.mewx.wenku8.reader.modern.ui.ModernReaderUiState

object ModernReaderStateFactory {
    fun loading(
        aid: Int,
        cid: Int,
        title: String,
        chapterTitle: String,
        displaySettings: ModernReaderDisplaySettings = ModernReaderDisplaySettings(),
        catalog: ModernReaderCatalog = ModernReaderCatalog.from(volume = null, currentCid = cid),
    ): ModernReaderUiState =
        ModernReaderUiState(
            title = title,
            chapterTitle = chapterTitle,
            aid = aid,
            cid = cid,
            catalog = catalog,
            progressText = "加载中",
            displaySettings = displaySettings,
            isNightMode = displaySettings.nightMode,
            isLoading = true,
        )

    fun fromLoadResult(
        aid: Int,
        cid: Int,
        fallbackTitle: String,
        chapterTitle: String,
        result: ModernReaderLoadResult,
        textMeasurer: ReaderTextMeasurer,
        layout: ReaderLayoutSpec,
        displaySettings: ModernReaderDisplaySettings = ModernReaderDisplaySettings(),
        catalog: ModernReaderCatalog = ModernReaderCatalog.from(volume = null, currentCid = cid),
    ): ModernReaderUiState =
        when (result) {
            is ModernReaderLoadResult.Success -> {
                val session = ModernReaderSession(
                    document = result.document,
                    textMeasurer = textMeasurer,
                    layout = layout,
                )
                reading(
                    aid = aid,
                    cid = cid,
                    title = result.document.title.ifBlank { fallbackTitle },
                    chapterTitle = chapterTitle,
                    page = session.currentPage,
                    pageIndex = session.pageIndex,
                    pageCount = session.pageCount,
                    catalog = catalog,
                    displaySettings = displaySettings,
                )
            }
            is ModernReaderLoadResult.Failure -> {
                ModernReaderUiState(
                    title = fallbackTitle,
                    chapterTitle = chapterTitle,
                    aid = aid,
                    cid = cid,
                    catalog = catalog,
                    errorMessage = result.reason.toReaderMessage(),
                    displaySettings = displaySettings,
                    isNightMode = displaySettings.nightMode,
                )
            }
        }

    fun reading(
        aid: Int,
        cid: Int,
        title: String,
        chapterTitle: String,
        page: ReaderPage,
        pageIndex: Int,
        pageCount: Int = 1,
        catalog: ModernReaderCatalog = ModernReaderCatalog.from(volume = null, currentCid = cid),
        displaySettings: ModernReaderDisplaySettings = ModernReaderDisplaySettings(),
    ): ModernReaderUiState {
        val safePageIndex = pageIndex.coerceAtLeast(0)
        val safePageCount = pageCount.coerceAtLeast(safePageIndex + 1)
        return ModernReaderUiState(
            title = title,
            chapterTitle = chapterTitle,
            aid = aid,
            cid = cid,
            page = page,
            pageIndex = safePageIndex,
            pageCount = safePageCount,
            catalog = catalog,
            progressText = "第 ${safePageIndex + 1} / $safePageCount 页",
            displaySettings = displaySettings,
            isNightMode = displaySettings.nightMode,
        )
    }

    private fun ModernReaderLoadFailure.toReaderMessage(): String =
        when (this) {
            ModernReaderLoadFailure.LOCAL_CONTENT_MISSING -> "本地章节文件不存在或为空。"
            ModernReaderLoadFailure.NETWORK_ERROR -> "公开版 API 不可用，或当前网络请求失败。"
            ModernReaderLoadFailure.EMPTY_CONTENT -> "章节内容为空，旧版 XML 解析器没有得到可显示内容。"
        }
}
