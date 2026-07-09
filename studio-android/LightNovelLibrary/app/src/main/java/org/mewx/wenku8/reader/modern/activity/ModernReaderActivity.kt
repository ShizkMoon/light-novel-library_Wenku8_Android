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
import org.mewx.wenku8.reader.modern.layout.ModernReaderLayoutSpecFactory
import org.mewx.wenku8.reader.modern.layout.ModernReaderWindowMetrics
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchArguments
import org.mewx.wenku8.reader.modern.launch.ReaderLaunchContext
import org.mewx.wenku8.reader.modern.model.ReaderDocument
import org.mewx.wenku8.reader.modern.model.ReaderLayoutSpec
import org.mewx.wenku8.reader.modern.paging.ModernReaderSession
import org.mewx.wenku8.reader.modern.paging.ReaderTextMeasurer
import org.mewx.wenku8.reader.modern.progress.GlobalConfigReaderProgressStore
import org.mewx.wenku8.reader.modern.progress.ModernReaderProgressController
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettings
import org.mewx.wenku8.reader.modern.settings.ModernReaderDisplaySettingsController
import org.mewx.wenku8.reader.modern.settings.SharedPreferencesModernReaderDisplaySettingsStore
import org.mewx.wenku8.reader.modern.ui.ModernReaderScreen
import org.mewx.wenku8.reader.modern.ui.ModernReaderTheme
import org.mewx.wenku8.reader.modern.ui.ModernReaderUiState
import java.util.concurrent.Executors
import java.util.concurrent.Future

class ModernReaderActivity : ComponentActivity() {
    private val readerExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var loadFuture: Future<*>? = null
    private var uiState: ModernReaderUiState? by mutableStateOf(null)
    private var displaySettings: ModernReaderDisplaySettings by mutableStateOf(ModernReaderDisplaySettings())
    private lateinit var displaySettingsController: ModernReaderDisplaySettingsController
    private var readerSession: ModernReaderSession? = null
    private var readerDocument: ReaderDocument? = null
    private var readerContext: ReaderLaunchContext? = null
    private var readerArgs: ReaderLaunchArguments? = null
    private val progressController = ModernReaderProgressController(GlobalConfigReaderProgressStore())
    private val cachedImageResolver = ModernReaderCachedImageResolver(
        fileNameForUrl = GlobalConfig::generateImageFileNameByURL,
        existingPathForFileName = GlobalConfig::getExistingNovelContentImagePath,
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
                    cachedImagePathForSource = cachedImageResolver::cachedPathFor,
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
        val request = ModernReaderChapterLoadModel.request(args, fallbackTitle)
        val repository = ModernReaderContentRepository(AndroidModernReaderRawContentSource())
        val activeDisplaySettings = displaySettings

        loadFuture = readerExecutor.submit {
            val result = repository.load(request)
            val textMeasurer = createTextMeasurer(activeDisplaySettings)
            val layout = createLayoutSpec(activeDisplaySettings)
            val initialCursor = progressController.initialCursor(
                aid = args.aid,
                vid = args.volumeId(),
                cid = args.cid,
                shouldRestore = args.forceJump,
            )
            val outcome = ModernReaderChapterLoadModel.outcome(
                args = args,
                fallbackTitle = fallbackTitle,
                chapterTitle = chapterTitle,
                result = result,
                textMeasurer = textMeasurer,
                layout = layout,
                displaySettings = activeDisplaySettings,
                catalog = catalog,
                initialCursor = initialCursor,
            )

            mainHandler.post {
                if (!isFinishing && !isDestroyed) {
                    readerDocument = outcome.document
                    readerSession = outcome.session
                    uiState = outcome.state
                }
            }
        }
    }

    private fun showPreviousPage() {
        val session = readerSession ?: return
        session.previousPage()
        updateReadingStateFromSession(session)
    }

    private fun showNextPage() {
        val session = readerSession ?: return
        session.nextPage()
        updateReadingStateFromSession(session)
    }

    private fun showPage(pageIndex: Int) {
        val session = readerSession ?: return
        session.goToPage(pageIndex)
        updateReadingStateFromSession(session)
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

    private fun updateReadingStateFromSession(session: ModernReaderSession) {
        val currentState = uiState ?: return
        uiState = ModernReaderStateFactory.reading(
            aid = currentState.aid,
            cid = currentState.cid,
            title = currentState.title,
            chapterTitle = currentState.chapterTitle,
            page = session.currentPage,
            pageIndex = session.pageIndex,
            pageCount = session.pageCount,
            catalog = currentState.catalog,
            displaySettings = displaySettings,
        )
        saveCurrentProgress()
    }

    private fun applyDisplaySettings(nextSettings: ModernReaderDisplaySettings) {
        displaySettings = nextSettings
        val currentState = uiState
        if (currentState != null) {
            uiState = currentState.copy(
                displaySettings = nextSettings,
                isNightMode = nextSettings.nightMode,
            )
        }

        val document = readerDocument ?: return
        val currentSession = readerSession ?: return
        val nextSession = ModernReaderSession(
            document = document,
            textMeasurer = createTextMeasurer(nextSettings),
            layout = createLayoutSpec(nextSettings),
            initialCursor = currentSession.currentPage.start,
        )
        readerSession = nextSession
        updateReadingStateFromSession(nextSession)
    }

    private fun saveCurrentProgress() {
        val session = readerSession ?: return
        val context = readerContext ?: return
        progressController.saveCurrentCursor(
            aid = context.aid,
            vid = context.vid,
            cid = context.cid,
            cursor = session.currentPage.start,
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
