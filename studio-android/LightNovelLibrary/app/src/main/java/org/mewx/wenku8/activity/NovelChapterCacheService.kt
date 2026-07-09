package org.mewx.wenku8.activity

import org.mewx.wenku8.api.Wenku8Error

data class NovelChapterCacheResult(
    val errorCode: Wenku8Error.ErrorCode,
    val xml: String = "",
)

class NovelChapterCacheService<Request, Language>(
    private val requestForChapter: (Int, Int, Language) -> Request,
    private val loadChapterXml: (Int) -> String,
    private val downloadChapterXml: (Request) -> ByteArray?,
    private val saveChapterXml: (Int, String) -> Unit,
) {
    fun cacheChapter(
        aid: Int,
        chapterCid: Int,
        language: Language,
        forceUpdate: Boolean,
    ): NovelChapterCacheResult {
        var xml = loadChapterXml(chapterCid)
        if (xml.isEmpty() || forceUpdate) {
            val request = requestForChapter(aid, chapterCid, language)
            val bytes = downloadChapterXml(request)
                ?: return NovelChapterCacheResult(Wenku8Error.ErrorCode.NETWORK_ERROR)
            xml = String(bytes, Charsets.UTF_8)
            if (xml.trim().isEmpty()) {
                return NovelChapterCacheResult(Wenku8Error.ErrorCode.SERVER_RETURN_NOTHING)
            }
            saveChapterXml(chapterCid, xml)
        }
        return NovelChapterCacheResult(Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED, xml)
    }
}
