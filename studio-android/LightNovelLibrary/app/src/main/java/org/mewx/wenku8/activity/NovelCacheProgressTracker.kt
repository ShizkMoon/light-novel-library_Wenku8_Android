package org.mewx.wenku8.activity

import org.mewx.wenku8.global.api.VolumeList

sealed class NovelCacheProgressEvent {
    data class MaxChanged(val max: Int) : NovelCacheProgressEvent()
    data class ProgressChanged(val progress: Int) : NovelCacheProgressEvent()
}

class NovelCacheProgressTracker {
    var maxProgress: Int = 0
        private set

    var currentProgress: Int = 0
        private set

    fun startChapterTotal(volumes: List<VolumeList>): NovelCacheProgressEvent.MaxChanged {
        maxProgress = volumes.sumOf { volume -> volume.chapterList.orEmpty().size }
        currentProgress = 0
        return NovelCacheProgressEvent.MaxChanged(maxProgress)
    }

    fun addImageWork(): NovelCacheProgressEvent.MaxChanged {
        maxProgress += 1
        return NovelCacheProgressEvent.MaxChanged(maxProgress)
    }

    fun completeWork(): NovelCacheProgressEvent.ProgressChanged {
        currentProgress += 1
        return NovelCacheProgressEvent.ProgressChanged(currentProgress)
    }
}
