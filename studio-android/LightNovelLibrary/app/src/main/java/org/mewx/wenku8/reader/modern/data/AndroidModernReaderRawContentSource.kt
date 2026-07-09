package org.mewx.wenku8.reader.modern.data

import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.network.LightNetwork

class AndroidModernReaderRawContentSource : ModernReaderRawContentSource {
    override fun loadLocalChapterXml(cid: Int): String =
        GlobalConfig.loadFullFileFromSaveFolder("novel", "$cid.xml")

    override fun loadCloudChapterXml(aid: Int, cid: Int): String? =
        try {
            val params = Wenku8API.getNovelContent(aid, cid, GlobalConfig.getCurrentLang())
            val bytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, params)
                ?: return null
            String(bytes, Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
}
