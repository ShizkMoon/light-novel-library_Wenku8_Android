package org.mewx.wenku8.global.api

import android.util.LruCache
import java.io.StringReader
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

class NovelItemInfoUpdate(aid: Int) {
    @JvmField
    var aid: Int = aid

    @JvmField
    var title: String = aid.toString()

    @JvmField
    var author: String = LOADING_STRING

    @JvmField
    var status: String = LOADING_STRING

    @JvmField
    var update: String = LOADING_STRING

    @JvmField
    var intro_short: String = LOADING_STRING

    @JvmField
    var tags: String = ""

    @JvmField
    var latest_chapter: String = LOADING_STRING

    fun isInitialized(): Boolean = title == aid.toString()

    companion object {
        const val LOADING_STRING: String = "Loading..."

        private val cache = LruCache<Int, NovelItemInfoUpdate>(500)

        @JvmStatic
        fun convertFromMeta(nim: NovelItemMeta): NovelItemInfoUpdate =
            NovelItemInfoUpdate(0).apply {
                title = nim.title
                aid = nim.aid
                author = nim.author
                status = nim.bookStatus
                update = nim.lastUpdate
                latest_chapter = nim.latestSectionName
            }

        @JvmStatic
        fun parse(xml: String): NovelItemInfoUpdate? = try {
            val niiu = NovelItemInfoUpdate(0)
            val factory = XmlPullParserFactory.newInstance()
            val xmlPullParser = factory.newPullParser()
            xmlPullParser.setInput(StringReader(xml))
            var eventType = xmlPullParser.eventType

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (xmlPullParser.name) {
                        "metadata" -> {
                            niiu.aid = 0
                            niiu.title = ""
                            niiu.author = ""
                            niiu.status = ""
                            niiu.update = ""
                            niiu.intro_short = ""
                            niiu.tags = ""
                            niiu.latest_chapter = ""
                        }

                        "data" -> when (xmlPullParser.getAttributeValue(0)) {
                            "Title" -> {
                                niiu.aid = xmlPullParser.getAttributeValue(1).toInt()
                                niiu.title = xmlPullParser.nextText()
                            }

                            "Author" -> niiu.author = xmlPullParser.getAttributeValue(1)
                            "BookStatus" -> niiu.status = xmlPullParser.getAttributeValue(1)
                            "LastUpdate" -> niiu.update = xmlPullParser.getAttributeValue(1)
                            "IntroPreview" -> {
                                niiu.intro_short = xmlPullParser.nextText()
                                    .replace("[ |　]".toRegex(), " ")
                                    .trim()
                            }
                        }
                    }
                }
                eventType = xmlPullParser.next()
            }
            niiu
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        @JvmStatic
        fun getFromCache(aid: Int): NovelItemInfoUpdate? = cache.get(aid)

        @JvmStatic
        fun putToCache(item: NovelItemInfoUpdate) {
            if (getFromCache(item.aid) == null) {
                cache.put(item.aid, item)
            }
        }
    }
}
