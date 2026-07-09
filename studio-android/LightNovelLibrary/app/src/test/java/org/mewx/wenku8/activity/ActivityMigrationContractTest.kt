package org.mewx.wenku8.activity

import kotlin.Metadata
import org.junit.Assert.assertNotNull
import org.junit.Test

class ActivityMigrationContractTest {
    @Test
    fun baseMaterialAndAboutActivitiesAreKotlinOwned() {
        assertKotlinClass(MainActivity::class.java)
        assertKotlinClass(BaseMaterialActivity::class.java)
        assertKotlinClass(AboutActivity::class.java)
        assertKotlinClass(SearchActivity::class.java)
        assertKotlinClass(SearchResultActivity::class.java)
        assertKotlinClass(MenuBackgroundSelectorActivity::class.java)
        assertKotlinClass(UserLoginActivity::class.java)
        assertKotlinClass(NovelReviewNewPostActivity::class.java)
        assertKotlinClass(ViewImageDetailActivity::class.java)
        assertKotlinClass(NovelReviewListActivity::class.java)
        assertKotlinClass(NovelReviewReplyListActivity::class.java)
        assertKotlinClass(UserInfoActivity::class.java)
        assertKotlinClass(VerticalReaderActivity::class.java)
        assertKotlinClass(NovelInfoActivity::class.java)
    }

    private fun assertKotlinClass(clazz: Class<*>) {
        assertNotNull("${clazz.simpleName} should be compiled from Kotlin", clazz.getAnnotation(Metadata::class.java))
    }
}
