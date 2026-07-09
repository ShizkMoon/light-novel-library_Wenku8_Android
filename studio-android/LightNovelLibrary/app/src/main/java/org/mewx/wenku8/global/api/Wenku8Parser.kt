package org.mewx.wenku8.global.api

import java.io.StringReader
import java.util.ArrayList
import java.util.Date
import java.util.GregorianCalendar
import javax.xml.parsers.DocumentBuilderFactory
import org.mewx.wenku8.util.LightTool
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource

class Wenku8Parser private constructor() {
    companion object {
        @JvmStatic
        fun parseNovelItemList(str: String): List<Int> {
            val list = ArrayList<Int>()
            val separator = '\''
            var begin = str.indexOf(separator)
            var end = str.indexOf(separator, begin + 1)

            if (begin == -1 || end == -1) {
                return list
            }

            str.substring(begin + 1, end).toIntIfValid()?.let(list::add)
            begin = end + 1

            while (true) {
                begin = str.indexOf(separator, begin)
                end = str.indexOf(separator, begin + 1)
                if (begin == -1 || end == -1) {
                    break
                }

                str.substring(begin + 1, end).toIntIfValid()?.let(list::add)
                begin = end + 1
            }

            return list
        }

        @JvmStatic
        fun parseNovelFullMeta(xml: String): NovelItemMeta? = try {
            val meta = NovelItemMeta()
            val dataNodes = parseDocument(xml).getElementsByTagName("data")

            for (index in 0 until dataNodes.length) {
                val data = dataNodes.item(index) as? Element ?: continue
                when (data.getAttribute("name").ifEmpty { data.firstAttributeValue().orEmpty() }) {
                    "Title" -> {
                        meta.aid = data.getAttribute("aid")
                            .ifEmpty { data.attributeValueAt(1).orEmpty() }
                            .toIntOrNull() ?: meta.aid
                        meta.title = data.textContent
                    }

                    "Author" -> meta.author = data.valueAttribute()
                    "DayHitsCount" -> meta.dayHitsCount = data.valueAttribute().toIntOrNull() ?: 0
                    "TotalHitsCount" -> meta.totalHitsCount = data.valueAttribute().toIntOrNull() ?: 0
                    "PushCount" -> meta.pushCount = data.valueAttribute().toIntOrNull() ?: 0
                    "FavCount" -> meta.favCount = data.valueAttribute().toIntOrNull() ?: 0
                    "PressId" -> meta.pressId = data.valueAttribute()
                    "BookStatus" -> meta.bookStatus = data.valueAttribute()
                    "BookLength" -> meta.bookLength = data.valueAttribute().toIntOrNull() ?: 0
                    "LastUpdate" -> meta.lastUpdate = data.valueAttribute()
                    "LatestSection" -> {
                        meta.latestSectionCid = data.getAttribute("cid")
                            .ifEmpty { data.attributeValueAt(1).orEmpty() }
                            .toIntOrNull() ?: 0
                        meta.latestSectionName = data.textContent
                    }
                }
            }

            meta
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        @JvmStatic
        fun getVolumeList(xml: String): ArrayList<VolumeList> {
            val result = ArrayList<VolumeList>()

            try {
                val volumeNodes = parseDocument(xml).getElementsByTagName("volume")

                for (index in 0 until volumeNodes.length) {
                    val volumeElement = volumeNodes.item(index) as? Element ?: continue
                    val volume = VolumeList()
                    volume.vid = volumeElement.getAttribute("vid")
                        .ifEmpty { volumeElement.firstAttributeValue().orEmpty() }
                        .toIntOrNull() ?: 0
                    volume.volumeName = volumeElement.firstDirectText()
                    volume.chapterList = ArrayList()

                    val chapters = volumeElement.getElementsByTagName("chapter")
                    for (chapterIndex in 0 until chapters.length) {
                        val chapterElement = chapters.item(chapterIndex) as? Element ?: continue
                        volume.chapterList?.add(
                            ChapterInfo().apply {
                                cid = chapterElement.getAttribute("cid")
                                    .ifEmpty { chapterElement.firstAttributeValue().orEmpty() }
                                    .toIntOrNull() ?: 0
                                chapterName = chapterElement.textContent
                            }
                        )
                    }

                    result.add(volume)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return result
        }

        @JvmStatic
        fun parseReviewList(reviewList: ReviewList, xml: String) {
            reviewList.currentPage = reviewList.currentPage + 1

            try {
                val document = parseDocument(xml)
                val page = document.getElementsByTagName("page").item(0) as? Element
                reviewList.totalPage = page?.getAttribute("num")?.toIntOrNull() ?: reviewList.totalPage

                val items = document.getElementsByTagName("item")
                for (index in 0 until items.length) {
                    val item = items.item(index) as? Element ?: continue
                    val user = item.firstChildElement("user")
                    val content = item.firstChildElement("content")

                    reviewList.list.add(
                        ReviewList.Review(
                            rid = item.getAttribute("rid").toIntOrNull() ?: 0,
                            postTime = parseTimestamp(item.getAttribute("posttime")),
                            noReplies = item.getAttribute("replies").toIntOrNull() ?: 0,
                            lastReplyTime = parseTimestamp(item.getAttribute("replytime")),
                            userName = user?.textContent.orEmpty(),
                            uid = user?.getAttribute("uid")?.toIntOrNull() ?: 0,
                            title = content?.textContent.orEmpty().trim()
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun parseReviewReplyList(reviewReplyList: ReviewReplyList, xml: String) {
            reviewReplyList.currentPage = reviewReplyList.currentPage + 1

            try {
                val document = parseDocument(xml)
                val page = document.getElementsByTagName("page").item(0) as? Element
                reviewReplyList.totalPage = page?.getAttribute("num")?.toIntOrNull()
                    ?: reviewReplyList.totalPage

                val items = document.getElementsByTagName("item")
                for (index in 0 until items.length) {
                    val item = items.item(index) as? Element ?: continue
                    val user = item.firstChildElement("user")
                    val content = item.firstChildElement("content")

                    reviewReplyList.list.add(
                        ReviewReplyList.ReviewReply(
                            replyTime = parseTimestamp(item.getAttribute("timestamp")),
                            userName = user?.textContent.orEmpty(),
                            uid = user?.getAttribute("uid")?.toIntOrNull() ?: 0,
                            content = content?.textContent.orEmpty().trim()
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun parseDocument(xml: String) = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(xml)))

        private fun parseTimestamp(raw: String): Date =
            GregorianCalendar(
                raw.substring(0, 4).toInt(),
                raw.substring(4, 6).toInt() - 1,
                raw.substring(6, 8).toInt(),
                raw.substring(8, 10).toInt(),
                raw.substring(10, 12).toInt(),
                raw.substring(12).toInt()
            ).time

        private fun String.toIntIfValid(): Int? =
            if (LightTool.isInteger(this)) toInt() else null

        private fun Element.firstAttributeValue(): String? =
            attributeValueAt(0)

        private fun Element.attributeValueAt(index: Int): String? =
            if (attributes.length > index) attributes.item(index).nodeValue else null

        private fun Element.valueAttribute(): String =
            getAttribute("value").ifEmpty { attributeValueAt(1).orEmpty() }

        private fun Element.firstDirectText(): String {
            for (index in 0 until childNodes.length) {
                val child = childNodes.item(index)
                if (child.nodeType == Node.TEXT_NODE || child.nodeType == Node.CDATA_SECTION_NODE) {
                    val text = child.nodeValue?.trim().orEmpty()
                    if (text.isNotEmpty()) {
                        return text
                    }
                }
            }
            return ""
        }

        private fun Element.firstChildElement(tagName: String): Element? {
            val nodes = getElementsByTagName(tagName)
            return nodes.item(0) as? Element
        }
    }
}
