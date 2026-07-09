package org.mewx.wenku8.global.api

import android.util.Log
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.mewx.wenku8.api.Wenku8API
import org.w3c.dom.Element
import org.xml.sax.InputSource

class UserInfo {
    @JvmField
    var username: String? = null

    @JvmField
    var nickyname: String? = null

    @JvmField
    var uid: Int = 0

    @JvmField
    var score: Int = 0

    @JvmField
    var experience: Int = 0

    @JvmField
    var rank: String? = null

    companion object {
        @JvmStatic
        fun parseUserInfo(xml: String): UserInfo? = try {
            val ui = UserInfo()
            val document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(InputSource(StringReader(xml)))
            val items = document.getElementsByTagName("item")

            for (index in 0 until items.length) {
                val item = items.item(index) as? Element ?: continue
                val value = item.textContent

                when (item.getAttribute("name")) {
                    "uname" -> {
                        ui.username = value
                        Log.d("MewX", ui.username.orEmpty().ifEmpty { Wenku8API.UNKNOWN })
                    }

                    "nickname" -> {
                        ui.nickyname = value
                        Log.d("MewX", ui.nickyname.orEmpty().ifEmpty { Wenku8API.UNKNOWN })
                    }

                    "uid" -> {
                        ui.uid = value.toInt()
                        Log.d("MewX", "uid:" + ui.uid)
                    }

                    "score" -> {
                        ui.score = value.toInt()
                        Log.d("MewX", "score:" + ui.score)
                    }

                    "experience" -> {
                        ui.experience = value.toInt()
                        Log.d("MewX", "experience:" + ui.experience)
                    }

                    "rank" -> {
                        ui.rank = value
                        Log.d("MewX", ui.rank.orEmpty().ifEmpty { Wenku8API.UNKNOWN })
                    }
                }
            }
            ui
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
