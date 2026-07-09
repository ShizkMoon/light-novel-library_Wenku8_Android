package org.mewx.wenku8.global.api

import java.util.ArrayList
import java.util.Date

class ReviewList {
    class Review(
        var rid: Int,
        var postTime: Date,
        var noReplies: Int,
        var lastReplyTime: Date,
        var userName: String,
        var uid: Int,
        var title: String
    )

    val list: MutableList<Review> = ArrayList()
    var totalPage: Int = 1
    var currentPage: Int = 0

    fun resetList() {
        list.clear()
        totalPage = 1
        currentPage = 0
    }
}
