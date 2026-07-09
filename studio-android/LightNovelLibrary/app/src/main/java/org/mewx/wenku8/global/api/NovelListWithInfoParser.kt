package org.mewx.wenku8.global.api

import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import org.xml.sax.InputSource

class NovelListWithInfoParser private constructor() {
    class NovelListWithInfo {
        @JvmField
        var aid: Int = 0

        @JvmField
        var name: String = ""

        @JvmField
        var hit: Int = 0

        @JvmField
        var push: Int = 0

        @JvmField
        var fav: Int = 0
    }

    companion object {
        @JvmStatic
        fun getNovelListWithInfoPageNum(xml: String): Int = try {
            val page = parseRoot(xml).getElementsByTagName("page").item(0) as? Element

            page?.firstAttributeValue()?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

        @JvmStatic
        fun getNovelListWithInfo(xml: String): List<NovelListWithInfo> = try {
            val document = parseRoot(xml)
            val items = document.getElementsByTagName("item")
            val result = ArrayList<NovelListWithInfo>()

            for (index in 0 until items.length) {
                val item = items.item(index) as? Element ?: continue
                val novel = NovelListWithInfo()
                novel.aid = item.firstAttributeValue()?.toIntOrNull() ?: 0

                val dataNodes = item.getElementsByTagName("data")
                for (dataIndex in 0 until dataNodes.length) {
                    val data = dataNodes.item(dataIndex) as? Element ?: continue
                    when (data.getAttribute("name").ifEmpty { data.firstAttributeValue().orEmpty() }) {
                        "Title" -> novel.name = data.textContent
                        "TotalHitsCount" -> novel.hit = data.valueAttribute()
                        "PushCount" -> novel.push = data.valueAttribute()
                        "FavCount" -> novel.fav = data.valueAttribute()
                    }
                }

                result.add(novel)
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        private fun parseRoot(xml: String) = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(xml)))

        private fun Element.firstAttributeValue(): String? =
            if (attributes.length > 0) attributes.item(0).nodeValue else null

        private fun Element.valueAttribute(): Int =
            getAttribute("value").ifEmpty { attributes.item(1)?.nodeValue.orEmpty() }.toIntOrNull() ?: 0
    }
}
