package org.mewx.wenku8.reader.view

import kotlin.Metadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mewx.wenku8.reader.loader.WenkuReaderLoader

class ReaderViewMigrationContractTest {
    @Test
    fun readerViewFoundationsAreKotlinOwned() {
        assertKotlinClass(LineInfo::class.java)
        assertKotlinClass(TextMeasurer::class.java)
        assertKotlinClass(WenkuReaderPaginator::class.java)
        assertKotlinClass(WenkuReaderPageView::class.java)
    }

    @Test
    fun lineInfoKeepsJavaRecordStyleAccessors() {
        val lineInfo = LineInfo(WenkuReaderLoader.ElementType.TEXT, "hello")

        assertEquals(WenkuReaderLoader.ElementType.TEXT, lineInfo.type())
        assertEquals("hello", lineInfo.text())
    }

    private fun assertKotlinClass(clazz: Class<*>) {
        assertNotNull("${clazz.simpleName} should be compiled from Kotlin", clazz.getAnnotation(Metadata::class.java))
    }
}
