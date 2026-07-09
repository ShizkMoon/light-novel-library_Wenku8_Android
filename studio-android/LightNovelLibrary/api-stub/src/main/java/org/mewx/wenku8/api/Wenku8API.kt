package org.mewx.wenku8.api

import android.content.ContentValues
import androidx.annotation.Nullable

@Suppress("unused")
class Wenku8API private constructor() {
    enum class AppLanguage {
        SC,
        TC,
    }

    enum class NovelPublicationStatus {
        FINISHED,
        NOT_FINISHED,
    }

    enum class NovelSortedBy {
        allVisit,
        allVote,
        monthVisit,
        monthVote,
        weekVisit,
        weekVote,
        dayVisit,
        dayVote,
        postDate,
        lastUpdate,
        goodNum,
        size,
        fullFlag,
    }

    companion object {
        const val UNKNOWN: String = "Unknown"
        const val REGISTER_URL: String = UNKNOWN
        const val BASE_URL: String = UNKNOWN
        const val MIN_REPLY_TEXT: Int = -1

        @JvmField
        var CurrentLang: AppLanguage = AppLanguage.SC

        @JvmField
        var AppVer: String = UNKNOWN

        @JvmField
        var NoticeString: String = UNKNOWN

        @JvmStatic
        fun getCoverURL(aid: Int): String = unavailable()

        @JvmStatic
        fun getAvatarURL(uid: Int): String = unavailable()

        @JvmStatic
        fun getNovelPublicationStatusByInt(i: Int): NovelPublicationStatus = unavailable()

        @JvmStatic
        fun getNovelPublicationStatusByString(s: String?): NovelPublicationStatus = unavailable()

        @JvmStatic
        fun getStatusByNovelPublicationStatus(s: NovelPublicationStatus?): String = unavailable()

        @JvmStatic
        fun getNovelSortedBy(n: String?): NovelSortedBy = unavailable()

        @JvmStatic
        fun getNovelSortedBy(n: NovelSortedBy?): String = unavailable()

        @JvmStatic
        fun getNovelCover(aid: Int): ContentValues = unavailable()

        @JvmStatic
        fun getNovelShortInfo(aid: Int, l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun getNovelShortInfoUpdate_CV(aid: Int, l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun getNovelFullIntro(aid: Int, l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun getNovelFullMeta(aid: Int, l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun getNovelIndex(aid: Int, l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun getNovelContent(aid: Int, cid: Int, l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun searchNovelByNovelName(novelName: String?, l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun searchNovelByAuthorName(authorName: String?, l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun getNovelList(n: NovelSortedBy?, page: Int): ContentValues = unavailable()

        @JvmStatic
        fun getMewxNovelList(n: NovelSortedBy?, page: Int, l: AppLanguage?): ContentValues = ContentValues()

        @JvmStatic
        fun getNovelListWithInfo(n: NovelSortedBy?, page: Int, l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun getLibraryList(): ContentValues = unavailable()

        @JvmStatic
        fun getNovelListByLibrary(sortId: Int, page: Int): ContentValues = unavailable()

        @JvmStatic
        fun getNovelListByLibraryWithInfo(sortId: Int, page: Int, l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun getUserLoginParams(username: String?, password: String?): ContentValues = unavailable()

        @JvmStatic
        fun getUserLoginEmailParams(email: String?, password: String?): ContentValues = unavailable()

        @JvmStatic
        fun getUserAvatar(): ContentValues = unavailable()

        @JvmStatic
        fun getUserLogoutParams(): ContentValues = unavailable()

        @JvmStatic
        fun getUserInfoParams(): ContentValues = unavailable()

        @JvmStatic
        fun getUserSignParams(): ContentValues = unavailable()

        @JvmStatic
        fun getVoteNovelParams(aid: Int): ContentValues = unavailable()

        @JvmStatic
        fun getBookshelfListAid(l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun getBookshelfListParams(l: AppLanguage?): ContentValues = unavailable()

        @JvmStatic
        fun getAddToBookshelfParams(aid: Int): ContentValues = unavailable()

        @JvmStatic
        fun getDelFromBookshelfParams(aid: Int): ContentValues = unavailable()

        @Nullable
        @JvmStatic
        fun searchBadWords(source: String?): String? = unavailable()

        @JvmStatic
        fun getCommentListParams(aid: Int, page: Int): ContentValues = unavailable()

        @JvmStatic
        fun getCommentContentParams(rid: Int, page: Int): ContentValues = unavailable()

        @JvmStatic
        fun getCommentNewThreadParams(aid: Int, title: String?, content: String?): ContentValues = unavailable()

        @JvmStatic
        fun getCommentReplyParams(rid: Int, content: String?): ContentValues = unavailable()

        private fun <T> unavailable(): T {
            throw UnsupportedOperationException(PublicApiStubContract.failureMessage)
        }
    }
}
