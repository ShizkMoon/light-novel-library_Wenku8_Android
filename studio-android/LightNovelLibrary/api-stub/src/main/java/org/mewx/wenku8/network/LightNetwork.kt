package org.mewx.wenku8.network

import android.content.ContentValues
import androidx.annotation.Nullable
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

@Suppress("unused", "FunctionName")
class LightNetwork private constructor() {
    companion object {
        @JvmStatic
        fun encodeToHttp(str: String?): String = encodeToHttp(str, "UTF-8")

        @JvmStatic
        fun encodeToHttp(str: String?, encoding: String?): String {
            val source = str.orEmpty()
            return try {
                URLEncoder.encode(source, encoding ?: "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                source
            }
        }

        @Nullable
        @JvmStatic
        fun LightHttpPostConnection(u: String?, values: ContentValues?): ByteArray? = null

        @Nullable
        @JvmStatic
        fun LightHttpDownload(url: String?): ByteArray? = null
    }
}
