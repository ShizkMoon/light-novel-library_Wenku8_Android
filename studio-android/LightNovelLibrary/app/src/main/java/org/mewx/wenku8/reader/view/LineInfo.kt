package org.mewx.wenku8.reader.view

import org.mewx.wenku8.reader.loader.WenkuReaderLoader

class LineInfo(
    type: WenkuReaderLoader.ElementType,
    text: String,
) {
    private val typeValue: WenkuReaderLoader.ElementType = requireNotNull(type)
    private val textValue: String = requireNotNull(text)

    fun type(): WenkuReaderLoader.ElementType = typeValue

    fun text(): String = textValue

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LineInfo) return false

        return typeValue == other.typeValue && textValue == other.textValue
    }

    override fun hashCode(): Int {
        var result = typeValue.hashCode()
        result = 31 * result + textValue.hashCode()
        return result
    }

    override fun toString(): String {
        return "LineInfo[type=$typeValue, text=$textValue]"
    }
}
