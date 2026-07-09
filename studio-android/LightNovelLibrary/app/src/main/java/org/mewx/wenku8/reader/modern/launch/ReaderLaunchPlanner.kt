package org.mewx.wenku8.reader.modern.launch

import org.mewx.wenku8.activity.VerticalReaderActivity
import org.mewx.wenku8.reader.activity.Wenku8ReaderActivityV1
import org.mewx.wenku8.reader.modern.activity.ModernReaderActivity

enum class ReaderEngine {
    MODERN,
    LEGACY_V1,
    LEGACY_VERTICAL,
}

data class ReaderLaunchPlan(
    val engine: ReaderEngine,
    val targetActivityClass: Class<*>,
    val resolvedSource: String,
    val forceJump: Boolean,
)

object ReaderLaunchPlanner {
    private const val SOURCE_LOCAL = "fav"
    private const val SOURCE_CLOUD = "cloud"

    @JvmStatic
    fun defaultEngine(): ReaderEngine = ReaderEngine.MODERN

    @JvmStatic
    fun engineForDialogIndex(index: Int): ReaderEngine =
        when (index) {
            1 -> ReaderEngine.LEGACY_V1
            2 -> ReaderEngine.LEGACY_VERTICAL
            else -> ReaderEngine.MODERN
        }

    @JvmStatic
    fun targetActivityClass(engine: ReaderEngine): Class<*> =
        when (engine) {
            ReaderEngine.MODERN -> ModernReaderActivity::class.java
            ReaderEngine.LEGACY_V1 -> Wenku8ReaderActivityV1::class.java
            ReaderEngine.LEGACY_VERTICAL -> VerticalReaderActivity::class.java
        }

    @JvmStatic
    fun defaultPlan(
        requestedSource: String?,
        localContentAvailable: Boolean,
        forceJump: Boolean,
    ): ReaderLaunchPlan =
        planForEngine(
            engine = defaultEngine(),
            requestedSource = requestedSource,
            localContentAvailable = localContentAvailable,
            forceJump = forceJump,
        )

    @JvmStatic
    fun dialogPlan(
        dialogIndex: Int,
        requestedSource: String?,
        localContentAvailable: Boolean,
        forceJump: Boolean,
    ): ReaderLaunchPlan =
        planForEngine(
            engine = engineForDialogIndex(dialogIndex),
            requestedSource = requestedSource,
            localContentAvailable = localContentAvailable,
            forceJump = forceJump,
        )

    @JvmStatic
    fun planForEngine(
        engine: ReaderEngine,
        requestedSource: String?,
        localContentAvailable: Boolean,
        forceJump: Boolean,
    ): ReaderLaunchPlan =
        ReaderLaunchPlan(
            engine = engine,
            targetActivityClass = targetActivityClass(engine),
            resolvedSource = resolveSource(
                requestedSource = requestedSource,
                localContentAvailable = localContentAvailable,
            ),
            forceJump = forceJump,
        )

    @JvmStatic
    fun resolveSource(
        requestedSource: String?,
        localContentAvailable: Boolean,
    ): String {
        if (requestedSource == SOURCE_LOCAL && !localContentAvailable) {
            return SOURCE_CLOUD
        }
        return requestedSource.orEmpty()
    }
}
