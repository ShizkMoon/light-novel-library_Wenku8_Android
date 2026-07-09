package org.mewx.wenku8.global.api.custom

import java.util.ArrayList
import org.json.JSONException
import org.json.JSONObject
import org.mewx.wenku8.global.api.NovelItemInfoUpdate

class NovelListWithInfoParser private constructor() {
    class Result {
        @JvmField
        var pageNum: Int = 0

        @JvmField
        var items: MutableList<NovelItemInfoUpdate> = ArrayList()
    }

    companion object {
        @JvmStatic
        fun parse(jsonString: String?): Result? {
            if (jsonString.isNullOrEmpty()) {
                return null
            }

            return try {
                val json = JSONObject(jsonString)
                val result = Result()

                if (json.has("page_num")) {
                    result.pageNum = json.getInt("page_num")
                }

                if (json.has("items")) {
                    val itemsArray = json.getJSONArray("items")
                    for (index in 0 until itemsArray.length()) {
                        val item = itemsArray.getJSONObject(index)
                        val info = NovelItemInfoUpdate(item.optInt("aid", 0))

                        if (item.has("Title")) info.title = item.getString("Title")
                        if (item.has("Author")) info.author = item.getString("Author")
                        if (item.has("BookStatus")) info.status = item.getString("BookStatus")
                        if (item.has("LastUpdate")) info.update = item.getString("LastUpdate")
                        if (item.has("IntroPreview")) {
                            info.intro_short = item.getString("IntroPreview")
                                .replace("[ |　]".toRegex(), " ")
                                .trim()
                        }
                        if (item.has("Tags")) info.tags = item.getString("Tags")

                        NovelItemInfoUpdate.putToCache(info)
                        result.items.add(info)
                    }
                }
                result
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
    }
}
