package org.mewx.wenku8.global.api

import org.mewx.wenku8.api.Wenku8API

class NovelItemMeta {
    @JvmField
    var aid: Int = 1

    @JvmField
    var title: String = aid.toString()

    @JvmField
    var author: String = Wenku8API.UNKNOWN

    @JvmField
    var dayHitsCount: Int = 0

    @JvmField
    var totalHitsCount: Int = 0

    @JvmField
    var pushCount: Int = 0

    @JvmField
    var favCount: Int = 0

    @JvmField
    var pressId: String = Wenku8API.UNKNOWN

    @JvmField
    var bookStatus: String = Wenku8API.UNKNOWN

    @JvmField
    var bookLength: Int = 0

    @JvmField
    var lastUpdate: String = Wenku8API.UNKNOWN

    @JvmField
    var latestSectionCid: Int = 0

    @JvmField
    var latestSectionName: String = Wenku8API.UNKNOWN

    @JvmField
    var fullIntro: String = Wenku8API.UNKNOWN
}
