package org.mewx.wenku8.reader

import kotlin.Metadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mewx.wenku8.reader.loader.WenkuReaderLoader
import org.mewx.wenku8.reader.loader.WenkuReaderLoaderXML
import org.mewx.wenku8.reader.setting.WenkuReaderSettingV1

class ReaderCoreMigrationContractTest {
    @Test
    fun readerCoreClassesAreKotlinOwned() {
        assertKotlinClass(WenkuReaderLoader::class.java)
        assertKotlinClass(WenkuReaderLoaderXML::class.java)
        assertKotlinClass(WenkuReaderSettingV1::class.java)
    }

    @Test
    fun legacyReaderSettingKeepsJavaFieldShape() {
        val fieldNames = listOf(
            "fontColorLight",
            "fontColorDark",
            "bgColorLight",
            "bgColorDark",
            "widgetHeight",
            "widgetTextSize",
        )

        fieldNames.forEach { fieldName ->
            assertNotNull(WenkuReaderSettingV1::class.java.getField(fieldName))
        }
    }

    @Test
    fun loaderElementTypeKeepsLegacyEnumNames() {
        assertEquals(
            listOf("TEXT", "IMAGE_INDEPENDENT", "IMAGE_DEPENDENT"),
            WenkuReaderLoader.ElementType.entries.map { it.name },
        )
    }

    private fun assertKotlinClass(clazz: Class<*>) {
        assertNotNull("${clazz.simpleName} should be compiled from Kotlin", clazz.getAnnotation(Metadata::class.java))
    }
}
