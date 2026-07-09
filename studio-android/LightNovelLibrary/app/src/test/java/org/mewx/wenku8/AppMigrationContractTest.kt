package org.mewx.wenku8

import kotlin.Metadata
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppMigrationContractTest {
    @Test
    fun applicationClassIsKotlinOwned() {
        assertNotNull(
            "MyApp should be compiled from Kotlin",
            MyApp::class.java.getAnnotation(Metadata::class.java)
        )
    }
}
