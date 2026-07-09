package org.mewx.wenku8.global.api

import java.io.Serializable
import java.util.ArrayList

class VolumeList : Serializable {
    @JvmField
    var volumeName: String? = null

    @JvmField
    var vid: Int = 0

    @JvmField
    var inLocal: Boolean = false

    @JvmField
    var chapterList: ArrayList<ChapterInfo>? = null
}
