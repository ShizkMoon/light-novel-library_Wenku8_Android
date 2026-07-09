package org.mewx.wenku8.reader.modern.image

class ModernReaderImageCacheRequestGate {
    private val pendingSources = mutableSetOf<String>()

    fun tryStart(source: String): Boolean =
        pendingSources.add(source)

    fun finish(source: String) {
        pendingSources.remove(source)
    }
}
