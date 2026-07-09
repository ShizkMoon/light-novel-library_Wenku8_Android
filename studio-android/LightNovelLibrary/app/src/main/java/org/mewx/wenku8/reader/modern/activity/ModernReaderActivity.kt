package org.mewx.wenku8.reader.modern.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextPaint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.mewx.wenku8.R
import org.mewx.wenku8.activity.ViewImageDetailActivity
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.data.AndroidModernReaderRawContentSource
import org.mewx.wenku8.reader.modern.data.ModernReaderContentRepository
import org.mewx.wenku8.reader.modern.image.ModernReaderCachedImageResolver
import org.mewx.wenku8.reader.modern.image.ModernReaderImageCacheCoordinator
import org.mewx.wenku8.reader.modern.layout.ModernReaderLayoutSpecFactory
import org.mewx.wenku8.reader.modern.layout.ModernReaderWindowMetrics
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchContext
import org.mewx.wenku8.reader.modern.model.ReaderCursor
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer
import org.mewx.wenku8.reader.modern.progress.GlobalConfigReaderProgressStore
import org.mewx.wenku8.reader.modern.progress.ModernReaderProgressController
import org.mewx.wenku8.reader.modern.progress.ModernReaderProgressSaveCoordinator
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettingsController
import org.mewx.wenku8.reader.modern.settings.SharedPreferencesModernReaderDisplaySettingsStore
import org.mewx.wenku8.reader.modern.ui.ModernReaderScreen
import org.mewx.wenku8.reader.modern.ui.ModernReaderTheme
import org.mewx.wenku8.reader.modern.ui.ModernReaderUiState
import org.mewx.wenku8.reader.modern.ui.ReaderImageCacheRequest
import org.mewx.wenku8.util.LightCache
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ModernReaderActivity : ComponentActivity() {
    private val readerExecutor = Executors.newSingleThreadExecutor()
    private val imageCacheExecutor = Executors.newFixedThreadPool(2)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var loadFuture: Future<*>? = null
    private var uiState: ModernReaderUiState? by mutableStateOf(null)
    private var resolvedImagePaths: Map<String, String> by mutableStateOf(emptyMap())
    private var displaySettings: ModernReaderDisplaySettings by mutableStateOf(ModernReaderDisplaySettings())
    private lateinit var displaySettingsController: ModernReaderDisplaySettingsController
    private var readerSession: ModernReaderSession? = null
    private var readerDocument: ReaderDocument? = null
    private var readerContext: ReaderLaunchContext? = null
    private var readerArgs: ReaderLaunchArguments? = null
    private val progressController = ModernReaderProgressController(GlobalConfigReaderProgressStore())
    private val progressSaveCoordinator = ModernReaderProgressSaveCoordinator(progressController)
    private val contentRepository = ModernReaderContentRepository(AndroidModernReaderRawContentSource())
    private val readingSessionCoordinator = ModernReaderReadingSessionCoordinator()
    private val displaySettingsCoordinator = ModernReaderDisplaySettingsCoordinator(
        createSession = ::createSession,
    )
    private val chapterLoadCoordinator = ModernReaderChapterLoadCoordinator(
        loadContent = contentRepository::load,
        createTextMeasurer = ::createTextMeasurer,
        createLayoutSpec = ::createLayoutSpec,
        initialCursorFor = { args ->
            progressController.initialCursor(
                aid = args.aid,
                vid = args.volumeId(),
                cid = args.cid,
                shouldRestore = args.forceJump,
            )
        },
        runInBackground = { work -> readerExecutor.submit(work) },
        postToMain = { work -> mainHandler.post(work) },
    )
    private val cachedImageResolver = ModernReaderCachedImageResolver(
        fileNameForUrl = GlobalConfig::generateImageFileNameByURL,
        existingPathForFileName = GlobalConfig::getExistingNovelContentImagePath,
        saveImage = GlobalConfig::saveNovelContentImage,
        deleteCachedPath = LightCache::deleteFile,
    )
    private val imageCacheCoordinator = ModernReaderImageCacheCoordinator(
        cachedPathForSource = cachedImageResolver::cachedPathFor,
        cachePathAfterSaving = cachedImageResolver::cachePathAfterSaving,
        refreshCachePathAfterDeleting = cachedImageResolver::cachePathAfterRefreshing,
        runInBackground = { work -> imageCacheExecutor.submit(work) },
        postToMain = { work -> mainHandler.post(work) },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        displaySettingsController = ModernReaderDisplaySettingsController(
            SharedPreferencesModernReaderDisplaySettingsStore(this),
        )
        displaySettings = displaySettingsController.current

        val args = ReaderLaunchArguments.from(intent.extras)
        val launch = ModernReaderInitialLaunch.prepare(args, displaySettings)
        readerArgs = launch.args
        readerContext = launch.context
        val loadingState = launch.loadingState
        uiState = loadingState

        setContent {
            ModernReaderTheme {
                ModernReaderScreen(
                    state = uiState ?: loadingState,
                    onPreviousPage = ::showPreviousPage,
                    onNextPage = ::showNextPage,
                    onDecreaseFontSize = { applyDisplaySettings(displaySettingsController.decreaseFontSize()) },
                    onIncreaseFontSize = { applyDisplaySettings(displaySettingsController.increaseFontSize()) },
                    onDecreaseLineHeight = { applyDisplaySettings(displaySettingsController.decreaseLineHeight()) },
                    onIncreaseLineHeight = { applyDisplaySettings(displaySettingsController.increaseLineHeight()) },
                    onNightModeChange = { enabled ->
                        applyDisplaySettings(displaySettingsController.setNightMode(enabled))
                    },
                    onSelectPage = ::showPage,
                    onSelectChapter = ::showChapter,
                    cachedImagePathForSource = ::cachedImagePathForSource,
                    onRequestImageCache = ::requestImageCache,
                    onOpenImage = ::openImageDetail,
                )
            }
        }

        loadChapter(
            args = launch.args,
            fallbackTitle = launch.fallbackTitle,
            chapterTitle = launch.chapterTitle,
            catalog = launch.catalog,
        )
    }

    override fun onDestroy() {
        saveCurrentProgress()
        loadFuture?.cancel(true)
        readerExecutor.shutdownNow()
        imageCacheExecutor.shutdownNow()
        super.onDestroy()
    }

    override fun onPause() {
        saveCurrentProgress()
        super.onPause()
    }

    private fun loadChapter(
        args: ReaderLaunchArguments,
        fallbackTitle: String,
        chapterTitle: String,
        catalog: ModernReaderCatalog,
    ) {
        loadFuture = chapterLoadCoordinator.loadChapter(
            args = args,
            fallbackTitle = fallbackTitle,
            chapterTitle = chapterTitle,
            catalog = catalog,
            displaySettings = displaySettings,
            isActive = { !isFinishing && !isDestroyed },
            onLoaded = ::applyChapterLoadOutcome,
        )
    }

    private fun applyChapterLoadOutcome(outcome: ModernReaderChapterLoadOutcome) {
        readerDocument = outcome.document
        readerSession = outcome.session
        uiState = outcome.state
    }

    private fun showPreviousPage() {
        applyReadingSessionUpdate(
            readingSessionCoordinator.previousPage(
                session = readerSession,
                currentState = uiState,
                displaySettings = displaySettings,
            ),
        )
    }

    private fun showNextPage() {
        applyReadingSessionUpdate(
            readingSessionCoordinator.nextPage(
                session = readerSession,
                currentState = uiState,
                displaySettings = displaySettings,
            ),
        )
    }

    private fun showPage(pageIndex: Int) {
        applyReadingSessionUpdate(
            readingSessionCoordinator.selectPage(
                pageIndex = pageIndex,
                session = readerSession,
                currentState = uiState,
                displaySettings = displaySettings,
            ),
        )
    }

    private fun showChapter(chapter: ReaderCatalogChapter) {
        val currentArgs = readerArgs ?: return
        val selection = ModernReaderChapterSelection.prepare(
            currentArgs = currentArgs,
            chapter = chapter,
            displaySettings = displaySettings,
        ) ?: return

        saveCurrentProgress()
        loadFuture?.cancel(true)

        val nextArgs = selection.args
        readerArgs = nextArgs
        readerContext = selection.context
        readerSession = null
        readerDocument = null

        uiState = selection.loadingState
        loadChapter(
            args = nextArgs,
            fallbackTitle = selection.fallbackTitle,
            chapterTitle = selection.chapterTitle,
            catalog = selection.catalog,
        )
    }

    @Suppress("DEPRECATION")
    private fun openImageDetail(path: String) {
        val intent = Intent(this, ViewImageDetailActivity::class.java)
        intent.putExtra("path", path)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.hold)
    }

    private fun cachedImagePathForSource(source: String): String? =
        imageCacheCoordinator.cachedPathForSource(
            source = source,
            resolvedImagePaths = resolvedImagePaths,
        )

    private fun requestImageCache(request: ReaderImageCacheRequest) =
        imageCacheCoordinator.requestImageCache(
            source = request.source,
            resolvedImagePaths = resolvedImagePaths,
            refreshExisting = request.refreshExisting,
            isActive = { !isFinishing && !isDestroyed },
            onImageCached = { imageSource, path ->
                resolvedImagePaths = resolvedImagePaths + (imageSource to path)
            },
        )

    private fun applyReadingSessionUpdate(update: ModernReaderReadingSessionUpdate?) {
        if (update == null) return

        uiState = update.state
        saveCurrentProgress(update.cursor)
    }

    private fun applyDisplaySettings(nextSettings: ModernReaderDisplaySettings) {
        displaySettings = nextSettings
        val update = displaySettingsCoordinator.apply(
            nextSettings = nextSettings,
            document = readerDocument,
            session = readerSession,
            currentState = uiState,
        )
        uiState = update.state
        update.rebuiltSession?.let { readerSession = it }
        update.cursorToPersist?.let(::saveCurrentProgress)
    }

    private fun saveCurrentProgress(cursor: ReaderCursor? = null) {
        progressSaveCoordinator.saveCurrentProgress(
            context = readerContext,
            session = readerSession,
            cursor = cursor,
        )
    }

    private fun createTextMeasurer(
        displaySettings: ModernReaderDisplaySettings,
    ): ReaderTextMeasurer {
        val textPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            textSize = displaySettings.fontSizeSp * readerScaledDensity()
            typeface = Typeface.SERIF
        }
        return ReaderTextMeasurer { text -> textPaint.measureText(text) }
    }

    private fun createSession(
        document: ReaderDocument,
        displaySettings: ModernReaderDisplaySettings,
        initialCursor: ReaderCursor,
    ): ModernReaderSession =
        ModernReaderSession(
            document = document,
            textMeasurer = createTextMeasurer(displaySettings),
            layout = createLayoutSpec(displaySettings),
            initialCursor = initialCursor,
        )

    private fun createLayoutSpec(
        displaySettings: ModernReaderDisplaySettings,
    ): ReaderLayoutSpec =
        ModernReaderLayoutSpecFactory.create(
            ModernReaderWindowMetrics(
                widthPx = resources.displayMetrics.widthPixels,
                heightPx = resources.displayMetrics.heightPixels,
                density = resources.displayMetrics.density,
                scaledDensity = readerScaledDensity(),
            ),
            displaySettings = displaySettings,
        )

    private fun readerScaledDensity(): Float =
        resources.displayMetrics.density * resources.configuration.fontScale
}
