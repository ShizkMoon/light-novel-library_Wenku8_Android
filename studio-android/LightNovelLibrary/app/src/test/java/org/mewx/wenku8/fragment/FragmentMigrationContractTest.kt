package org.mewx.wenku8.fragment

import kotlin.Metadata
import org.junit.Assert.assertNotNull
import org.junit.Test

class FragmentMigrationContractTest {
    @Test
    fun migratedFragmentsAreKotlinOwned() {
        assertKotlinClass(ConfigFragment::class.java)
        assertKotlinClass(RKListFragment::class.java)
        assertKotlinClass(LatestFragment::class.java)
        assertKotlinClass(NovelItemListFragment::class.java)
        assertKotlinClass(NavigationDrawerFragment::class.java)
        assertKotlinClass(FavFragment::class.java)
    }

    private fun assertKotlinClass(clazz: Class<*>) {
        assertNotNull("${clazz.simpleName} should be compiled from Kotlin", clazz.getAnnotation(Metadata::class.java))
    }
}
