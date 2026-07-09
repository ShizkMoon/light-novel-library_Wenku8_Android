package org.mewx.wenku8.global.api

import java.util.function.IntConsumer

class OldNovelContentParser private constructor() {
    enum class NovelContentType {
        TEXT,
        IMAGE
    }

    class NovelContent {
        @JvmField
        var type: NovelContentType = NovelContentType.TEXT

        @JvmField
        var content: String = ""
    }

    companion object {
        private const val IMAGE_ENTRY = "<!--image-->"

        @JvmStatic
        fun parseNovelContent(raw: String, setMaxProgress: IntConsumer): List<NovelContent> {
            val result = ArrayList<NovelContent>()

            raw.split("\r\n").forEach { line ->
                if (line.all { it == ' ' }) {
                    return@forEach
                }

                if (!line.contains(IMAGE_ENTRY)) {
                    result.add(
                        NovelContent().apply {
                            type = NovelContentType.TEXT
                            content = line.trim()
                        }
                    )
                    setMaxProgress.accept(result.size)
                } else {
                    var currentIndex = 0
                    while (true) {
                        currentIndex = line.indexOf(IMAGE_ENTRY, currentIndex)
                        if (currentIndex == -1) {
                            break
                        }

                        val nextIndex = line.indexOf(IMAGE_ENTRY, currentIndex + IMAGE_ENTRY.length)
                        if (nextIndex < 0) {
                            result.add(
                                NovelContent().apply {
                                    type = NovelContentType.TEXT
                                    content = line.trim()
                                }
                            )
                            break
                        }

                        result.add(
                            NovelContent().apply {
                                type = NovelContentType.IMAGE
                                content = line.substring(currentIndex + IMAGE_ENTRY.length, nextIndex)
                            }
                        )
                        currentIndex = nextIndex + IMAGE_ENTRY.length
                        setMaxProgress.accept(result.size)
                    }
                }
            }

            return result
        }

        @JvmStatic
        fun NovelContentParser_onlyImage(raw: String): List<NovelContent> {
            val result = ArrayList<NovelContent>()

            raw.split("\r\n").forEach { line ->
                if (!line.contains(IMAGE_ENTRY)) {
                    return@forEach
                }

                var currentIndex = 0
                while (true) {
                    currentIndex = line.indexOf(IMAGE_ENTRY, currentIndex)
                    if (currentIndex == -1) {
                        break
                    }

                    val nextIndex = line.indexOf(IMAGE_ENTRY, currentIndex + IMAGE_ENTRY.length)
                    if (nextIndex < 0) {
                        break
                    }

                    result.add(
                        NovelContent().apply {
                            type = NovelContentType.IMAGE
                            content = line.substring(currentIndex + IMAGE_ENTRY.length, nextIndex)
                        }
                    )
                    currentIndex = nextIndex + IMAGE_ENTRY.length
                }
            }

            return result
        }
    }
}
