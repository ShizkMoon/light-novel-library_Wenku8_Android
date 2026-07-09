package org.mewx.wenku8.reader.modern.activity

import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.data.ModernReaderContentRequest
import org.mewx.wenku8.reader.modern.data.ModernReaderLoadResult
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings

class ModernReaderChapterLoadCoordinator(
    private val loadContent: (ModernReaderContentRequest) -> ModernReaderLoadResult,
    private val createSession: (
        ReaderDocument,
        ModernReaderDisplaySettings,
        ReaderCursor,
    ) -> ModernReaderSession,
    private val initialCursorFor: (ReaderLaunchArguments) -> ReaderCursor,
    private val runInBackground: (() -> Unit) -> Future<*>,
    private val postToMain: (() -> Unit) -> Unit,
) {
    private val loadGeneration = AtomicInteger(0)

    fun loadChapter(
        args: ReaderLaunchArguments,
        fallbackTitle: String,
        chapterTitle: String,
        catalog: ModernReaderCatalog,
        displaySettings: ModernReaderDisplaySettings,
        isActive: () -> Boolean,
        onLoaded: (ModernReaderChapterLoadOutcome) -> Unit,
    ): Future<*> {
        val generation = loadGeneration.incrementAndGet()
        return runInBackground {
            val result = loadContent(
                ModernReaderChapterLoadModel.request(
                    args = args,
                    fallbackTitle = fallbackTitle,
                ),
            )
            val outcome = ModernReaderChapterLoadModel.outcome(
                args = args,
                fallbackTitle = fallbackTitle,
                chapterTitle = chapterTitle,
                result = result,
                displaySettings = displaySettings,
                catalog = catalog,
                initialCursor = initialCursorFor(args),
                createSession = createSession,
            )
            postToMain {
                if (isActive() && generation == loadGeneration.get()) {
                    onLoaded(outcome)
                }
            }
        }
    }
}
