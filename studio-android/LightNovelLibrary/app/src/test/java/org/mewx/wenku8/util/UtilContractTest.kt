package org.mewx.wenku8.util

import java.nio.charset.StandardCharsets
import kotlin.Metadata
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class UtilContractTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun utilityClassesAreKotlinOwned() {
        assertKotlinClass(LightCache::class.java)
        assertKotlinClass(LightTool::class.java)
        assertKotlinClass(GoogleServicesHelper::class.java)
        assertKotlinClass(ProgressDialogHelper::class.java)
        assertKotlinClass(SaveFileMigration::class.java)
    }

    @Test
    fun lightToolKeepsNumericParsingContract() {
        assertTrue(LightTool.isInteger("42"))
        assertTrue(LightTool.isInteger("-7"))
        assertFalse(LightTool.isInteger("4.2"))
        assertFalse(LightTool.isInteger("abc"))

        assertTrue(LightTool.isDouble("4.2"))
        assertFalse(LightTool.isDouble("42"))
        assertFalse(LightTool.isDouble("abc"))

        assertTrue(LightTool.isNumber("42"))
        assertTrue(LightTool.isNumber("4.2"))
        assertFalse(LightTool.isNumber("abc"))
    }

    @Test
    fun lightCacheKeepsBasicFilePersistenceContract() {
        val root = temporaryFolder.newFolder("cache-root")
        val payload = "hello cache".toByteArray(StandardCharsets.UTF_8)
        val fullPath = root.resolve("nested/book.txt").absolutePath

        assertFalse(LightCache.testFileExist(fullPath))
        assertNull(LightCache.loadFile(fullPath))

        assertTrue(LightCache.saveFile(fullPath, payload, false))
        assertTrue(LightCache.testFileExist(fullPath))
        assertArrayEquals(payload, LightCache.loadFile(fullPath))

        assertTrue(LightCache.deleteFile(fullPath))
        assertFalse(LightCache.testFileExist(fullPath))
    }

    private fun assertKotlinClass(clazz: Class<*>) {
        assertNotNull("${clazz.simpleName} should be compiled from Kotlin", clazz.getAnnotation(Metadata::class.java))
    }
}
