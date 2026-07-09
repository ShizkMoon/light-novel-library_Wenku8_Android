package org.mewx.wenku8.adapter

import java.util.Date
import kotlin.Metadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mewx.wenku8.global.api.NovelItemInfoUpdate
import org.mewx.wenku8.global.api.ReviewList
import org.mewx.wenku8.global.api.ReviewReplyList

class AdapterMigrationContractTest {
    @Test
    fun adaptersAreKotlinOwnedAndKeepItemCounts() {
        assertKotlinClass(NovelItemAdapterUpdate::class.java)
        assertKotlinClass(SearchHistoryAdapter::class.java)
        assertKotlinClass(ReviewItemAdapter::class.java)
        assertKotlinClass(ReviewReplyItemAdapter::class.java)

        assertEquals(0, NovelItemAdapterUpdate().itemCount)
        assertEquals(2, NovelItemAdapterUpdate(listOf(NovelItemInfoUpdate(1), NovelItemInfoUpdate(2))).itemCount)
        assertEquals(2, SearchHistoryAdapter(listOf("alpha", "beta")).itemCount)

        val reviewList = ReviewList().apply {
            list.add(ReviewList.Review(1, Date(0), 2, Date(0), "user", 3, "title"))
        }
        assertEquals(1, ReviewItemAdapter(reviewList).itemCount)

        val replyList = ReviewReplyList().apply {
            list.add(ReviewReplyList.ReviewReply(Date(0), "user", 3, "content"))
        }
        assertEquals(1, ReviewReplyItemAdapter(replyList).itemCount)
    }

    private fun assertKotlinClass(clazz: Class<*>) {
        assertNotNull("${clazz.simpleName} should be compiled from Kotlin", clazz.getAnnotation(Metadata::class.java))
    }
}
