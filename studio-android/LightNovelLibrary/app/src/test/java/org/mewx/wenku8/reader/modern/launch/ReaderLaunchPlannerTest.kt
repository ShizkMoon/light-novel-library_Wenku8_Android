package org.mewx.wenku8.reader.modern.launch

import org.junit.Assert.assertEquals
import org.junit.Test
import org.mewx.wenku8.activity.VerticalReaderActivity
import org.mewx.wenku8.reader.activity.Wenku8ReaderActivityV1
import org.mewx.wenku8.reader.modern.activity.ModernReaderActivity

class ReaderLaunchPlannerTest {
    @Test
    fun defaultEngineUsesModernReader() {
        assertEquals(ReaderEngine.MODERN, ReaderLaunchPlanner.defaultEngine())
        assertEquals(
            ModernReaderActivity::class.java,
            ReaderLaunchPlanner.targetActivityClass(ReaderLaunchPlanner.defaultEngine()),
        )
    }

    @Test
    fun dialogSelectionKeepsLegacyEnginesExplicit() {
        assertEquals(ReaderEngine.MODERN, ReaderLaunchPlanner.engineForDialogIndex(0))
        assertEquals(ReaderEngine.LEGACY_V1, ReaderLaunchPlanner.engineForDialogIndex(1))
        assertEquals(ReaderEngine.LEGACY_VERTICAL, ReaderLaunchPlanner.engineForDialogIndex(2))
        assertEquals(ReaderEngine.MODERN, ReaderLaunchPlanner.engineForDialogIndex(99))

        assertEquals(Wenku8ReaderActivityV1::class.java, ReaderLaunchPlanner.targetActivityClass(ReaderEngine.LEGACY_V1))
        assertEquals(VerticalReaderActivity::class.java, ReaderLaunchPlanner.targetActivityClass(ReaderEngine.LEGACY_VERTICAL))
    }

    @Test
    fun localSourceFallsBackToCloudWhenChapterCacheIsMissing() {
        assertEquals("fav", ReaderLaunchPlanner.resolveSource(requestedSource = "fav", localContentAvailable = true))
        assertEquals("cloud", ReaderLaunchPlanner.resolveSource(requestedSource = "fav", localContentAvailable = false))
        assertEquals("cloud", ReaderLaunchPlanner.resolveSource(requestedSource = "cloud", localContentAvailable = false))
        assertEquals("", ReaderLaunchPlanner.resolveSource(requestedSource = null, localContentAvailable = false))
    }

    @Test
    fun defaultPlanTargetsModernReaderAndCarriesResolvedSource() {
        val plan = ReaderLaunchPlanner.defaultPlan(
            requestedSource = "fav",
            localContentAvailable = false,
            forceJump = true,
        )

        assertEquals(ReaderEngine.MODERN, plan.engine)
        assertEquals(ModernReaderActivity::class.java, plan.targetActivityClass)
        assertEquals("cloud", plan.resolvedSource)
        assertEquals(true, plan.forceJump)
    }

    @Test
    fun dialogPlanKeepsExplicitLegacySelectionInKotlinLayer() {
        val plan = ReaderLaunchPlanner.dialogPlan(
            dialogIndex = 1,
            requestedSource = "fav",
            localContentAvailable = true,
            forceJump = false,
        )

        assertEquals(ReaderEngine.LEGACY_V1, plan.engine)
        assertEquals(Wenku8ReaderActivityV1::class.java, plan.targetActivityClass)
        assertEquals("fav", plan.resolvedSource)
        assertEquals(false, plan.forceJump)
    }
}
