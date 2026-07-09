package org.mewx.wenku8.reader.modern.image

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModernReaderImageCacheRequestGateTest {
    @Test
    fun startsFirstRequestForImageSource() {
        val gate = ModernReaderImageCacheRequestGate()

        assertTrue(gate.tryStart("https://img.example.test/cover.png"))
    }

    @Test
    fun blocksDuplicateRequestWhileSourceIsPending() {
        val gate = ModernReaderImageCacheRequestGate()

        assertTrue(gate.tryStart("https://img.example.test/cover.png"))
        assertFalse(gate.tryStart("https://img.example.test/cover.png"))
    }

    @Test
    fun allowsRetryAfterRequestFinishes() {
        val gate = ModernReaderImageCacheRequestGate()

        gate.tryStart("https://img.example.test/cover.png")
        gate.finish("https://img.example.test/cover.png")

        assertTrue(gate.tryStart("https://img.example.test/cover.png"))
    }
}
