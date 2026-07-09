package org.mewx.wenku8.reader.modern.data

import org.mewx.wenku8.global.api.OldNovelContentParser
import org.mewx.wenku8.reader.modern.model.ReaderDocument

enum class ReaderContentSourceMode {
    LOCAL,
    CLOUD,
}

data class ModernReaderContentRequest(
    val aid: Int,
    val cid: Int,
    val chapterTitle: String,
    val sourceMode: ReaderContentSourceMode,
)

enum class ModernReaderLoadFailure {
    LOCAL_CONTENT_MISSING,
    NETWORK_ERROR,
    EMPTY_CONTENT,
}

sealed interface ModernReaderLoadResult {
    data class Success(val document: ReaderDocument) : ModernReaderLoadResult

    data class Failure(val reason: ModernReaderLoadFailure) : ModernReaderLoadResult
}

interface ModernReaderRawContentSource {
    fun loadLocalChapterXml(cid: Int): String

    fun loadCloudChapterXml(aid: Int, cid: Int): String?
}

class ModernReaderContentRepository(
    private val source: ModernReaderRawContentSource,
) {
    fun load(request: ModernReaderContentRequest): ModernReaderLoadResult {
        val rawXml = when (request.sourceMode) {
            ReaderContentSourceMode.LOCAL -> {
                val localXml = source.loadLocalChapterXml(request.cid)
                if (localXml.isBlank()) {
                    return ModernReaderLoadResult.Failure(
                        ModernReaderLoadFailure.LOCAL_CONTENT_MISSING,
                    )
                }
                localXml
            }
            ReaderContentSourceMode.CLOUD -> {
                source.loadCloudChapterXml(request.aid, request.cid)
                    ?: return ModernReaderLoadResult.Failure(ModernReaderLoadFailure.NETWORK_ERROR)
            }
        }

        val legacyContent = OldNovelContentParser.parseNovelContent(rawXml) { }
        val blocks = LegacyReaderContentMapper.toReaderBlocks(legacyContent)
        if (blocks.isEmpty()) {
            return ModernReaderLoadResult.Failure(ModernReaderLoadFailure.EMPTY_CONTENT)
        }

        return ModernReaderLoadResult.Success(
            ReaderDocument(
                title = request.chapterTitle,
                blocks = blocks,
            ),
        )
    }
}
