package org.mewx.wenku8.global

import java.lang.reflect.Modifier
import kotlin.Metadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GlobalConfigContractTest {

    @Test
    fun globalConfigIsKotlinAndKeepsPureUtilityContract() {
        assertKotlinClass(GlobalConfig::class.java)
        assertKotlinClass(GlobalConfig.ReadSaves::class.java)
        assertKotlinClass(GlobalConfig.ReadSavesV1::class.java)
        assertPublicField(GlobalConfig.ReadSaves::class.java, "cid")
        assertPublicField(GlobalConfig.ReadSaves::class.java, "pos")
        assertPublicField(GlobalConfig.ReadSaves::class.java, "height")
        assertPublicField(GlobalConfig.ReadSavesV1::class.java, "aid")
        assertPublicField(GlobalConfig.ReadSavesV1::class.java, "vid")
        assertPublicField(GlobalConfig.ReadSavesV1::class.java, "cid")
        assertPublicField(GlobalConfig.ReadSavesV1::class.java, "lineId")
        assertPublicField(GlobalConfig.ReadSavesV1::class.java, "wordId")

        assertEquals("saves", GlobalConfig.saveFolderName)
        assertEquals("imgs", GlobalConfig.imgsSaveFolderName)
        assertEquals("custom", GlobalConfig.customFolderName)
        assertEquals(18, GlobalConfig.getShowTextSize())
        assertEquals(48, GlobalConfig.getShowTextPaddingTop())
        assertEquals(32, GlobalConfig.getShowTextPaddingLeft())
        assertEquals(32, GlobalConfig.getShowTextPaddingRight())
        assertEquals(2, GlobalConfig.getTextLoadWay())
        assertTrue(GlobalConfig.doCacheImage())
        assertEquals(
            "pictures113054175950471.jpg",
            GlobalConfig.generateImageFileNameByURL(
                "<!--image-->http://pic.wenku8.cn/pictures/1/1305/41759/50471.jpg<!--image-->"
            )
        )
    }

    @Test
    fun shelfAndLatestModeSwitchesRemainStaticMutableState() {
        GlobalConfig.LeaveBookshelf()
        assertFalse(GlobalConfig.testInBookshelf())

        GlobalConfig.EnterBookshelf()
        assertTrue(GlobalConfig.testInBookshelf())

        GlobalConfig.LeaveBookshelf()
        assertFalse(GlobalConfig.testInBookshelf())

        GlobalConfig.LeaveLatest()
        assertFalse(GlobalConfig.testInLatest())

        GlobalConfig.EnterLatest()
        assertTrue(GlobalConfig.testInLatest())

        GlobalConfig.LeaveLatest()
        assertFalse(GlobalConfig.testInLatest())
    }

    @Test
    fun maxSearchHistoryKeepsPositiveOnlyContract() {
        val original = GlobalConfig.getMaxSearchHistory()

        try {
            GlobalConfig.setMaxSearchHistory(3)
            assertEquals(3, GlobalConfig.getMaxSearchHistory())

            GlobalConfig.setMaxSearchHistory(0)
            assertEquals(3, GlobalConfig.getMaxSearchHistory())

            GlobalConfig.setMaxSearchHistory(-1)
            assertEquals(3, GlobalConfig.getMaxSearchHistory())
        } finally {
            GlobalConfig.setMaxSearchHistory(original)
        }
    }

    private fun assertKotlinClass(clazz: Class<*>) {
        assertNotNull("${clazz.simpleName} should be compiled from Kotlin", clazz.getAnnotation(Metadata::class.java))
    }

    private fun assertPublicField(clazz: Class<*>, fieldName: String) {
        val field = clazz.getField(fieldName)

        assertTrue("$fieldName should remain public for Java callers", Modifier.isPublic(field.modifiers))
    }
}
