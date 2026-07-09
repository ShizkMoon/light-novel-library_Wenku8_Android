package org.mewx.wenku8.global.api

import java.io.Serializable

class ChapterInfo : Serializable {
    @JvmField
    var cid: Int = 0

    @JvmField
    var chapterName: String? = null
}
