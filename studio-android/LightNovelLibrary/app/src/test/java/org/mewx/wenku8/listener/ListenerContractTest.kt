package org.mewx.wenku8.listener

import java.lang.FunctionalInterface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ListenerContractTest {
    @Test
    fun itemClickListenerIsFunctionalAndDispatchesPosition() {
        assertTrue(MyItemClickListener::class.java.isAnnotationPresent(FunctionalInterface::class.java))

        var clickedPosition = -1
        val listener = MyItemClickListener { _, position ->
            clickedPosition = position
        }

        listener.onItemClick(null, 7)

        assertEquals(7, clickedPosition)
    }

    @Test
    fun itemLongClickListenerIsFunctionalAndDispatchesPosition() {
        assertTrue(MyItemLongClickListener::class.java.isAnnotationPresent(FunctionalInterface::class.java))

        var clickedPosition = -1
        val listener = MyItemLongClickListener { _, position ->
            clickedPosition = position
        }

        listener.onItemLongClick(null, 11)

        assertEquals(11, clickedPosition)
    }

    @Test
    fun optionClickListenerIsFunctionalAndDispatchesPosition() {
        assertTrue(MyOptionClickListener::class.java.isAnnotationPresent(FunctionalInterface::class.java))

        var clickedPosition = -1
        val listener = MyOptionClickListener { _, position ->
            clickedPosition = position
        }

        listener.onOptionButtonClick(null, 13)

        assertEquals(13, clickedPosition)
    }
}
