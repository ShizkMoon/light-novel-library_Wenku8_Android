package org.mewx.wenku8.reader.activity

import kotlin.Metadata
import org.junit.Assert.assertNotNull
import org.junit.Test

class ReaderActivityMigrationContractTest {
    @Test
    fun legacyHorizontalReaderActivityIsKotlinOwned() {
        assertNotNull(
            "Wenku8ReaderActivityV1 should be compiled from Kotlin",
            Wenku8ReaderActivityV1::class.java.getAnnotation(Metadata::class.java),
        )
    }
}
