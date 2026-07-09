package org.mewx.wenku8.reader.modern.launch

import android.os.Build
import android.os.Bundle
import org.mewx.wenku8.global.api.VolumeList
import org.mewx.wenku8.reader.modern.catalog.ModernReaderCatalog
import org.mewx.wenku8.reader.modern.catalog.ReaderCatalogChapter
import org.mewx.wenku8.reader.modern.data.ReaderContentSourceMode

data class ReaderLaunchArguments(
    val aid: Int,
    val cid: Int,
    val from: String,
    val forceJump: Boolean,
    val volume: VolumeList?,
    val volumes: List<VolumeList>,
) {
    companion object {
        fun from(bundle: Bundle?): ReaderLaunchArguments {
            val volume = bundle?.getVolumeList()
            val volumes = bundle?.getVolumeLists().orEmpty()
            return ReaderLaunchArguments(
                aid = bundle?.getInt(EXTRA_AID) ?: 0,
                cid = bundle?.getInt(EXTRA_CID) ?: 0,
                from = bundle?.getString(EXTRA_FROM).orEmpty(),
                forceJump = bundle?.getString(EXTRA_FORCE_JUMP) == FORCE_JUMP_YES,
                volume = volume,
                volumes = volumes.ifEmpty { volume?.let { listOf(it) }.orEmpty() },
            )
        }
    }

    fun sourceMode(): ReaderContentSourceMode =
        if (from == SOURCE_LOCAL) ReaderContentSourceMode.LOCAL else ReaderContentSourceMode.CLOUD

    fun hasRequiredIds(): Boolean = aid > 0 && cid > 0

    fun volumeId(): Int = currentVolume()?.vid ?: 0

    fun catalog(): ModernReaderCatalog =
        ModernReaderCatalog.from(
            volumes = volumes.ifEmpty { volume?.let { listOf(it) }.orEmpty() },
            currentCid = cid,
        )

    fun forChapter(chapter: ReaderCatalogChapter): ReaderLaunchArguments =
        copy(
            cid = chapter.cid,
            forceJump = false,
            volume = volumes.firstOrNull { volume ->
                volume.vid == chapter.volumeId && volume.chapterList.orEmpty().any { it.cid == chapter.cid }
            } ?: volumes.firstOrNull { volume ->
                volume.chapterList.orEmpty().any { it.cid == chapter.cid }
            } ?: volume,
        )

    private fun currentVolume(): VolumeList? =
        volume?.takeIf { candidate ->
            candidate.chapterList.orEmpty().any { it.cid == cid }
        } ?: volumes.firstOrNull { candidate ->
            candidate.chapterList.orEmpty().any { it.cid == cid }
        } ?: volume
}

data class ReaderLaunchContext(
    val aid: Int,
    val vid: Int,
    val cid: Int,
) {
    companion object {
        fun from(args: ReaderLaunchArguments): ReaderLaunchContext =
            ReaderLaunchContext(
                aid = args.aid,
                vid = args.volumeId(),
                cid = args.cid,
            )
    }
}

private fun Bundle.getVolumeList(): VolumeList? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(EXTRA_VOLUME, VolumeList::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializable(EXTRA_VOLUME) as? VolumeList
    }

private fun Bundle.getVolumeLists(): List<VolumeList> {
    val value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializable(EXTRA_VOLUMES, java.util.ArrayList::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializable(EXTRA_VOLUMES)
    }
    return (value as? List<*>).orEmpty().filterIsInstance<VolumeList>()
}

private const val SOURCE_LOCAL = "fav"
private const val EXTRA_AID = "aid"
private const val EXTRA_CID = "cid"
private const val EXTRA_FROM = "from"
private const val EXTRA_FORCE_JUMP = "forcejump"
private const val EXTRA_VOLUME = "volume"
private const val EXTRA_VOLUMES = "volumes"
private const val FORCE_JUMP_YES = "yes"
