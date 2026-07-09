package org.mewx.wenku8.activity

import org.mewx.wenku8.global.api.VolumeList

object NovelVolumeCacheMarker {
    fun markInLocalVolumes(
        volumes: List<VolumeList>,
        isChapterCached: (Int) -> Boolean,
    ) {
        volumes.forEach { volume ->
            val chapters = volume.chapterList.orEmpty()
            volume.inLocal = chapters.isNotEmpty() && chapters.all { chapter ->
                isChapterCached(chapter.cid)
            }
        }
    }
}
