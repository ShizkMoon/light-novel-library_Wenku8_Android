package org.mewx.wenku8.global.api

import java.lang.reflect.Modifier
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.function.IntConsumer
import kotlin.Metadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mewx.wenku8.api.Wenku8API

class LegacyApiModelContractTest {

    @Test
    fun chapterInfoIsKotlinSerializableModelWithPublicLegacyFields() {
        assertKotlinClass(ChapterInfo::class.java)
        assertPublicField(ChapterInfo::class.java, "cid")
        assertPublicField(ChapterInfo::class.java, "chapterName")

        val chapter = ChapterInfo()

        assertEquals(0, chapter.cid)
        assertNull(chapter.chapterName)
    }

    @Test
    fun volumeListIsKotlinSerializableModelWithPublicLegacyFields() {
        assertKotlinClass(VolumeList::class.java)
        assertPublicField(VolumeList::class.java, "volumeName")
        assertPublicField(VolumeList::class.java, "vid")
        assertPublicField(VolumeList::class.java, "inLocal")
        assertPublicField(VolumeList::class.java, "chapterList")

        val volume = VolumeList()

        assertNull(volume.volumeName)
        assertEquals(0, volume.vid)
        assertFalse(volume.inLocal)
        assertNull(volume.chapterList)
    }

    @Test
    fun novelItemMetaIsKotlinModelWithExistingDefaultValues() {
        assertKotlinClass(NovelItemMeta::class.java)
        listOf(
            "aid",
            "title",
            "author",
            "dayHitsCount",
            "totalHitsCount",
            "pushCount",
            "favCount",
            "pressId",
            "bookStatus",
            "bookLength",
            "lastUpdate",
            "latestSectionCid",
            "latestSectionName",
            "fullIntro"
        ).forEach { fieldName ->
            assertPublicField(NovelItemMeta::class.java, fieldName)
        }

        val meta = NovelItemMeta()

        assertEquals(1, meta.aid)
        assertEquals("1", meta.title)
        assertEquals(Wenku8API.UNKNOWN, meta.author)
        assertEquals(0, meta.dayHitsCount)
        assertEquals(0, meta.totalHitsCount)
        assertEquals(0, meta.pushCount)
        assertEquals(0, meta.favCount)
        assertEquals(Wenku8API.UNKNOWN, meta.pressId)
        assertEquals(Wenku8API.UNKNOWN, meta.bookStatus)
        assertEquals(0, meta.bookLength)
        assertEquals(Wenku8API.UNKNOWN, meta.lastUpdate)
        assertEquals(0, meta.latestSectionCid)
        assertEquals(Wenku8API.UNKNOWN, meta.latestSectionName)
        assertEquals(Wenku8API.UNKNOWN, meta.fullIntro)
    }

    @Test
    fun reviewListIsKotlinModelAndPreservesMutableListContract() {
        assertKotlinClass(ReviewList::class.java)
        assertKotlinClass(ReviewList.Review::class.java)

        val reviewList = ReviewList()

        assertEquals(1, reviewList.totalPage)
        assertEquals(0, reviewList.currentPage)
        assertTrue(reviewList.list.isEmpty())

        reviewList.totalPage = 7
        reviewList.currentPage = 3
        val postTime = Date(1000L)
        val lastReplyTime = Date(2000L)
        val review = ReviewList.Review(
            12,
            postTime,
            4,
            lastReplyTime,
            "tester",
            34,
            "sample title"
        )
        reviewList.list.add(review)

        assertEquals(7, reviewList.totalPage)
        assertEquals(3, reviewList.currentPage)
        assertEquals(1, reviewList.list.size)
        assertEquals(12, review.rid)
        assertEquals(postTime, review.postTime)
        assertEquals(4, review.noReplies)
        assertEquals(lastReplyTime, review.lastReplyTime)
        assertEquals("tester", review.userName)
        assertEquals(34, review.uid)
        assertEquals("sample title", review.title)

        reviewList.resetList()

        assertEquals(1, reviewList.totalPage)
        assertEquals(0, reviewList.currentPage)
        assertTrue(reviewList.list.isEmpty())
    }

    @Test
    fun reviewReplyListIsKotlinModelAndPreservesMutableListContract() {
        assertKotlinClass(ReviewReplyList::class.java)
        assertKotlinClass(ReviewReplyList.ReviewReply::class.java)

        val replyList = ReviewReplyList()

        assertEquals(1, replyList.totalPage)
        assertEquals(0, replyList.currentPage)
        assertTrue(replyList.list.isEmpty())

        replyList.totalPage = 2
        replyList.currentPage = 1
        val replyTime = Date(3000L)
        val reply = ReviewReplyList.ReviewReply(replyTime, "reply-user", 56, "reply body")
        replyList.list.add(reply)

        assertEquals(2, replyList.totalPage)
        assertEquals(1, replyList.currentPage)
        assertEquals(1, replyList.list.size)
        assertEquals(replyTime, reply.replyTime)
        assertEquals("reply-user", reply.userName)
        assertEquals(56, reply.uid)
        assertEquals("reply body", reply.content)
    }

    @Test
    fun userInfoIsKotlinModelAndParsesLegacyXml() {
        assertKotlinClass(UserInfo::class.java)
        assertPublicField(UserInfo::class.java, "username")
        assertPublicField(UserInfo::class.java, "nickyname")
        assertPublicField(UserInfo::class.java, "uid")
        assertPublicField(UserInfo::class.java, "score")
        assertPublicField(UserInfo::class.java, "experience")
        assertPublicField(UserInfo::class.java, "rank")

        val parsed = UserInfo.parseUserInfo(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <metadata>
                <item name="uname"><![CDATA[apptest]]></item>
                <item name="nickname"><![CDATA[nick]]></item>
                <item name="uid">123</item>
                <item name="score">10</item>
                <item name="experience">20</item>
                <item name="rank"><![CDATA[reader]]></item>
            </metadata>
            """.trimIndent()
        )

        assertNotNull(parsed)
        requireNotNull(parsed)
        assertEquals("apptest", parsed.username)
        assertEquals("nick", parsed.nickyname)
        assertEquals(123, parsed.uid)
        assertEquals(10, parsed.score)
        assertEquals(20, parsed.experience)
        assertEquals("reader", parsed.rank)
    }

    @Test
    fun novelItemInfoUpdateIsKotlinModelAndKeepsConversionDefaults() {
        assertKotlinClass(NovelItemInfoUpdate::class.java)
        listOf(
            "aid",
            "title",
            "author",
            "status",
            "update",
            "intro_short",
            "tags",
            "latest_chapter"
        ).forEach { fieldName ->
            assertPublicField(NovelItemInfoUpdate::class.java, fieldName)
        }

        val loading = NovelItemInfoUpdate(42)

        assertEquals(42, loading.aid)
        assertEquals("42", loading.title)
        assertEquals(NovelItemInfoUpdate.LOADING_STRING, loading.author)
        assertEquals(NovelItemInfoUpdate.LOADING_STRING, loading.status)
        assertEquals(NovelItemInfoUpdate.LOADING_STRING, loading.update)
        assertEquals(NovelItemInfoUpdate.LOADING_STRING, loading.intro_short)
        assertEquals("", loading.tags)
        assertEquals(NovelItemInfoUpdate.LOADING_STRING, loading.latest_chapter)
        assertTrue(loading.isInitialized())

        val meta = NovelItemMeta().apply {
            aid = 99
            title = "converted title"
            author = "converted author"
            bookStatus = "completed"
            lastUpdate = "2026-07-09"
            latestSectionName = "final chapter"
        }
        val converted = NovelItemInfoUpdate.convertFromMeta(meta)

        assertEquals(99, converted.aid)
        assertEquals("converted title", converted.title)
        assertEquals("converted author", converted.author)
        assertEquals("completed", converted.status)
        assertEquals("2026-07-09", converted.update)
        assertEquals("final chapter", converted.latest_chapter)
    }

    @Test
    fun customNovelListWithInfoParserIsKotlinAndKeepsResultFields() {
        assertKotlinClass(org.mewx.wenku8.global.api.custom.NovelListWithInfoParser::class.java)
        assertKotlinClass(org.mewx.wenku8.global.api.custom.NovelListWithInfoParser.Result::class.java)
        assertPublicField(org.mewx.wenku8.global.api.custom.NovelListWithInfoParser.Result::class.java, "pageNum")
        assertPublicField(org.mewx.wenku8.global.api.custom.NovelListWithInfoParser.Result::class.java, "items")

        val result = org.mewx.wenku8.global.api.custom.NovelListWithInfoParser.Result()

        assertEquals(0, result.pageNum)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun oldNovelContentParserIsKotlinAndPreservesTextImageParsing() {
        assertKotlinClass(OldNovelContentParser::class.java)
        assertKotlinClass(OldNovelContentParser.NovelContent::class.java)
        assertKotlinClass(OldNovelContentParser.NovelContentType::class.java)
        assertPublicField(OldNovelContentParser.NovelContent::class.java, "type")
        assertPublicField(OldNovelContentParser.NovelContent::class.java, "content")

        val progress = mutableListOf<Int>()
        val blocks = OldNovelContentParser.parseNovelContent(
            "  first line  \r\n<!--image-->image-a.jpg<!--image-->\r\n    \r\n",
            IntConsumer { progress.add(it) }
        )

        assertEquals(2, blocks.size)
        assertEquals(OldNovelContentParser.NovelContentType.TEXT, blocks[0].type)
        assertEquals("first line", blocks[0].content)
        assertEquals(OldNovelContentParser.NovelContentType.IMAGE, blocks[1].type)
        assertEquals("image-a.jpg", blocks[1].content)
        assertEquals(listOf(1, 2), progress)

        val imageOnly = OldNovelContentParser.NovelContentParser_onlyImage(
            "prefix<!--image-->one.png<!--image-->middle<!--image-->two.png<!--image-->\r\n" +
                "<!--image-->broken"
        )

        assertEquals(2, imageOnly.size)
        assertEquals("one.png", imageOnly[0].content)
        assertEquals("two.png", imageOnly[1].content)
        assertTrue(imageOnly.all { it.type == OldNovelContentParser.NovelContentType.IMAGE })
    }

    @Test
    fun novelListWithInfoParserIsKotlinAndParsesLegacyXml() {
        assertKotlinClass(NovelListWithInfoParser::class.java)
        assertKotlinClass(NovelListWithInfoParser.NovelListWithInfo::class.java)
        assertPublicField(NovelListWithInfoParser.NovelListWithInfo::class.java, "aid")
        assertPublicField(NovelListWithInfoParser.NovelListWithInfo::class.java, "name")
        assertPublicField(NovelListWithInfoParser.NovelListWithInfo::class.java, "hit")
        assertPublicField(NovelListWithInfoParser.NovelListWithInfo::class.java, "push")
        assertPublicField(NovelListWithInfoParser.NovelListWithInfo::class.java, "fav")

        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <metadata>
                <page num="3" />
                <item aid="101">
                    <data name="Title"><![CDATA[First Novel]]></data>
                    <data name="TotalHitsCount" value="321" />
                    <data name="PushCount" value="45" />
                    <data name="FavCount" value="67" />
                </item>
                <item aid="102">
                    <data name="Title"><![CDATA[Second Novel]]></data>
                    <data name="TotalHitsCount" value="123" />
                    <data name="PushCount" value="4" />
                    <data name="FavCount" value="5" />
                </item>
            </metadata>
        """.trimIndent()

        assertEquals(3, NovelListWithInfoParser.getNovelListWithInfoPageNum(xml))

        val list = NovelListWithInfoParser.getNovelListWithInfo(xml)

        assertEquals(2, list.size)
        assertEquals(101, list[0].aid)
        assertEquals("First Novel", list[0].name)
        assertEquals(321, list[0].hit)
        assertEquals(45, list[0].push)
        assertEquals(67, list[0].fav)
        assertEquals(102, list[1].aid)
        assertEquals("Second Novel", list[1].name)
        assertEquals(123, list[1].hit)
        assertEquals(4, list[1].push)
        assertEquals(5, list[1].fav)
    }

    @Test
    fun wenku8ParserIsKotlinAndParsesNovelListMetaAndVolumes() {
        assertKotlinClass(Wenku8Parser::class.java)

        val novelListXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <result>
                <page num='166'/>
                <item aid='1143'/>
                <item aid='1034'/>
            </result>
        """.trimIndent()

        assertEquals(listOf(166, 1143, 1034), Wenku8Parser.parseNovelItemList(novelListXml))

        val meta = Wenku8Parser.parseNovelFullMeta(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <metadata>
                <data name="Title" aid="1306"><![CDATA[向森之魔物献上花束]]></data>
                <data name="Author" value="小木君人"/>
                <data name="DayHitsCount" value="26"/>
                <data name="TotalHitsCount" value="43984"/>
                <data name="PushCount" value="1735"/>
                <data name="FavCount" value="848"/>
                <data name="PressId" value="小学馆" sid="10"/>
                <data name="BookStatus" value="已完成"/>
                <data name="BookLength" value="105985"/>
                <data name="LastUpdate" value="2012-11-02"/>
                <data name="LatestSection" cid="41897"><![CDATA[第一卷 插图]]></data>
            </metadata>
            """.trimIndent()
        )

        assertNotNull(meta)
        requireNotNull(meta)
        assertEquals(1306, meta.aid)
        assertEquals("向森之魔物献上花束", meta.title)
        assertEquals("小木君人", meta.author)
        assertEquals(26, meta.dayHitsCount)
        assertEquals(43984, meta.totalHitsCount)
        assertEquals(1735, meta.pushCount)
        assertEquals(848, meta.favCount)
        assertEquals("小学馆", meta.pressId)
        assertEquals("已完成", meta.bookStatus)
        assertEquals(105985, meta.bookLength)
        assertEquals("2012-11-02", meta.lastUpdate)
        assertEquals(41897, meta.latestSectionCid)
        assertEquals("第一卷 插图", meta.latestSectionName)

        val volumes = Wenku8Parser.getVolumeList(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <metadata>
                <volume vid="41748"><![CDATA[第一卷 告白于苍刻之夜]]>
                    <chapter cid="41749"><![CDATA[序章]]></chapter>
                    <chapter cid="41750"><![CDATA[第一章]]></chapter>
                </volume>
            </metadata>
            """.trimIndent()
        )

        assertEquals(1, volumes.size)
        assertEquals(41748, volumes[0].vid)
        assertEquals("第一卷 告白于苍刻之夜", volumes[0].volumeName)
        assertEquals(2, volumes[0].chapterList?.size)
        assertEquals(41749, volumes[0].chapterList?.get(0)?.cid)
        assertEquals("序章", volumes[0].chapterList?.get(0)?.chapterName)
        assertEquals(41750, volumes[0].chapterList?.get(1)?.cid)
        assertEquals("第一章", volumes[0].chapterList?.get(1)?.chapterName)
    }

    @Test
    fun wenku8ParserParsesReviewLists() {
        assertKotlinClass(Wenku8Parser::class.java)

        val reviewList = ReviewList()
        Wenku8Parser.parseReviewList(
            reviewList,
            """
            <?xml version="1.0" encoding="utf-8"?>
            <metadata>
                <page num="5"/>
                <item rid="10" replies="2" posttime="20260709112233" replytime="20260710112233">
                    <user uid="7"><![CDATA[Alice]]></user>
                    <content><![CDATA[ Review title ]]></content>
                </item>
            </metadata>
            """.trimIndent()
        )

        assertEquals(1, reviewList.currentPage)
        assertEquals(5, reviewList.totalPage)
        assertEquals(1, reviewList.list.size)
        val review = reviewList.list[0]
        assertEquals(10, review.rid)
        assertEquals(2, review.noReplies)
        assertEquals(timestamp(2026, Calendar.JULY, 9, 11, 22, 33), review.postTime)
        assertEquals(timestamp(2026, Calendar.JULY, 10, 11, 22, 33), review.lastReplyTime)
        assertEquals("Alice", review.userName)
        assertEquals(7, review.uid)
        assertEquals("Review title", review.title)

        val replyList = ReviewReplyList()
        Wenku8Parser.parseReviewReplyList(
            replyList,
            """
            <?xml version="1.0" encoding="utf-8"?>
            <metadata>
                <page num="3"/>
                <item timestamp="20260711112233">
                    <user uid="8"><![CDATA[Bob]]></user>
                    <content><![CDATA[ Reply body ]]></content>
                </item>
            </metadata>
            """.trimIndent()
        )

        assertEquals(1, replyList.currentPage)
        assertEquals(3, replyList.totalPage)
        assertEquals(1, replyList.list.size)
        val reply = replyList.list[0]
        assertEquals(timestamp(2026, Calendar.JULY, 11, 11, 22, 33), reply.replyTime)
        assertEquals("Bob", reply.userName)
        assertEquals(8, reply.uid)
        assertEquals("Reply body", reply.content)
    }

    private fun assertKotlinClass(clazz: Class<*>) {
        assertNotNull("${clazz.simpleName} should be compiled from Kotlin", clazz.getAnnotation(Metadata::class.java))
    }

    private fun assertPublicField(clazz: Class<*>, fieldName: String) {
        val field = clazz.getField(fieldName)

        assertTrue("$fieldName should remain public for Java callers", Modifier.isPublic(field.modifiers))
    }

    private fun timestamp(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Date = GregorianCalendar(year, month, day, hour, minute, second).time
}
