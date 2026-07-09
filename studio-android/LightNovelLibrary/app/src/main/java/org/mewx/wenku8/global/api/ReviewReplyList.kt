package org.mewx.wenku8.global.api

import java.util.ArrayList
import java.util.Date

class ReviewReplyList {
    class ReviewReply(
        var replyTime: Date,
        var userName: String,
        var uid: Int,
        var content: String
    )

    val list: MutableList<ReviewReply> = ArrayList()
    var totalPage: Int = 1
    var currentPage: Int = 0
}
