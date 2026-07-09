package org.mewx.wenku8.async

import kotlin.Metadata
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mewx.wenku8.component.PagerSlidingTabStrip
import org.mewx.wenku8.component.ScrollViewNoFling

class AsyncAndComponentMigrationContractTest {
    @Test
    fun asyncTasksAndComponentsAreKotlinOwned() {
        assertKotlinClass(CheckAppNewVersion::class.java)
        assertKotlinClass(UpdateNotificationMessage::class.java)
        assertKotlinClass(PagerSlidingTabStrip::class.java)
        assertKotlinClass(ScrollViewNoFling::class.java)
    }

    private fun assertKotlinClass(clazz: Class<*>) {
        assertNotNull("${clazz.simpleName} should be compiled from Kotlin", clazz.getAnnotation(Metadata::class.java))
    }
}
