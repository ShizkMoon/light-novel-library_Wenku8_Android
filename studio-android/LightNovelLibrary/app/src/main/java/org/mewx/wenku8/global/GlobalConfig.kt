package org.mewx.wenku8.global

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.util.Log
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.ArrayList
import org.mewx.wenku8.MyApp
import org.mewx.wenku8.R
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.network.LightUserSession
import org.mewx.wenku8.util.LightCache
import org.mewx.wenku8.util.LightTool
import org.mewx.wenku8.util.SaveFileMigration

@Suppress("unused", "FunctionName", "DEPRECATION")
class GlobalConfig private constructor() {
    class ReadSaves {
        @JvmField
        var cid: Int = 0

        @JvmField
        var pos: Int = 0

        @JvmField
        var height: Int = 0
    }

    class ReadSavesV1 {
        @JvmField
        var aid: Int = 0

        @JvmField
        var vid: Int = 0

        @JvmField
        var cid: Int = 0

        @JvmField
        var lineId: Int = 0

        @JvmField
        var wordId: Int = 0
    }

    enum class SettingItems {
        version,
        language,
        menu_bg_id,
        menu_bg_path,
        reader_font_path,
        reader_font_size,
        reader_line_distance,
        reader_paragraph_distance,
        reader_paragraph_edge_distance,
        reader_background_path,
        eink_mode
    }

    companion object {
        const val blogPageUrl: String = "https://wenku8.mewx.org/"
        const val versionCheckUrl: String = "https://wenku8.mewx.org/version"
        const val noticeCheckSc: String = "https://wenku8.mewx.org/args/notice_sc"
        const val noticeCheckTc: String = "https://wenku8.mewx.org/args/notice_tc"

        const val saveFolderName: String = "saves"
        const val imgsSaveFolderName: String = "imgs"
        const val customFolderName: String = "custom"

        private const val saveSearchHistoryFileName = "search_history.wk8"
        private const val saveReadSavesFileName = "read_saves.wk8"
        private const val saveReadSavesV1FileName = "read_saves_v1.wk8"
        private const val saveLocalBookshelfFileName = "bookshelf_local.wk8"
        private const val saveSetting = "settings.wk8"
        private const val saveUserAccountFileName = "cert.wk8"
        private const val saveUserAvatarFileName = "avatar.jpg"
        private const val saveNoticeString = "notice.wk8"

        private var maxSearchHistory = 20
        private var lookupInternalStorageOnly = false
        private var isInBookshelf = false
        private var isInLatest = false
        private var doLoadImage = true
        private var externalStoragePathAvailable = true
        private var currentLang = Wenku8API.AppLanguage.SC

        @JvmField
        var pathPickedSave: String? = null

        private var searchHistory: ArrayList<String>? = null
        private var readSaves: ArrayList<ReadSaves>? = null
        private var bookshelf: ArrayList<Int>? = null
        private var readSavesV1: ArrayList<ReadSavesV1>? = null
        private var allSetting: ContentValues? = null

        @JvmStatic
        fun setCurrentLang(l: Wenku8API.AppLanguage) {
            currentLang = l
            Wenku8API.CurrentLang = currentLang
            setToAllSetting(SettingItems.language, currentLang.toString())
        }

        @JvmStatic
        fun getCurrentLang(): Wenku8API.AppLanguage {
            val setting = getFromAllSetting(SettingItems.language)
            if (setting == null) {
                setToAllSetting(SettingItems.language, currentLang.toString())
            } else if (setting != currentLang.toString()) {
                currentLang = when (setting) {
                    Wenku8API.AppLanguage.TC.toString() -> Wenku8API.AppLanguage.TC
                    else -> Wenku8API.AppLanguage.SC
                }
            }
            Wenku8API.CurrentLang = currentLang
            return currentLang
        }

        @JvmStatic
        fun isEinkModeEnabled(): Boolean {
            val einkMode = getFromAllSetting(SettingItems.eink_mode)
            return einkMode != null && einkMode.toIntOrNull() == 1
        }

        @JvmStatic
        fun initImageLoader(context: Context) {
            val diskCache = UnlimitedDiscCache(
                File(getDefaultStoragePath() + "cache"),
                File(context.cacheDir.toString() + File.separator + "imgs")
            )
            val displayOptions = DisplayImageOptions.Builder()
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .resetViewBeforeLoading(true)
                .displayer(FadeInBitmapDisplayer(250))
                .build()
            val configuration = ImageLoaderConfiguration.Builder(context)
                .diskCache(diskCache)
                .defaultDisplayImageOptions(displayOptions)
                .build()
            ImageLoader.getInstance().init(configuration)
        }

        @JvmStatic
        fun getOpensourceLicense(): String {
            val inputStream = MyApp.getContext().resources.openRawResource(R.raw.license)
            val builder = StringBuilder()

            inputStream.use { stream ->
                BufferedReader(InputStreamReader(stream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        builder.append(line).append('\n')
                        line = reader.readLine()
                    }
                }
            }

            return builder.toString()
        }

        @JvmStatic
        fun setExternalStoragePathAvailable(available: Boolean) {
            externalStoragePathAvailable = available
        }

        @JvmStatic
        fun getDefaultStoragePath(): String =
            if (lookupInternalStorageOnly || !externalStoragePathAvailable) {
                SaveFileMigration.getInternalSavePath()
            } else {
                SaveFileMigration.getExternalStoragePath()
            }

        @JvmStatic
        fun getBackupStoragePath(): String {
            val internalPath = SaveFileMigration.getInternalSavePath()
            return if (getDefaultStoragePath() == internalPath) {
                SaveFileMigration.getExternalStoragePath()
            } else {
                internalPath
            }
        }

        @JvmStatic
        fun doCacheImage(): Boolean = doLoadImage

        @JvmStatic
        fun getShowTextSize(): Int = 18

        @JvmStatic
        fun getShowTextPaddingTop(): Int = 48

        @JvmStatic
        fun getShowTextPaddingLeft(): Int = 32

        @JvmStatic
        fun getShowTextPaddingRight(): Int = 32

        @JvmStatic
        fun getTextLoadWay(): Int = 2

        @JvmStatic
        fun getFirstFullSaveFilePath(): String =
            getDefaultStoragePath() + saveFolderName + File.separator

        @JvmStatic
        fun getSecondFullSaveFilePath(): String =
            getBackupStoragePath() + saveFolderName + File.separator

        @JvmStatic
        fun getFirstFullUserAccountSaveFilePath(): String =
            getFirstFullSaveFilePath() + saveUserAccountFileName

        @JvmStatic
        fun getSecondFullUserAccountSaveFilePath(): String =
            getSecondFullSaveFilePath() + saveUserAccountFileName

        @JvmStatic
        fun getFirstUserAvatarSaveFilePath(): String =
            getFirstFullSaveFilePath() + saveUserAvatarFileName

        @JvmStatic
        fun getSecondUserAvatarSaveFilePath(): String =
            getSecondFullSaveFilePath() + saveUserAvatarFileName

        @JvmStatic
        fun generateImageFileNameByURL(url: String): String {
            val result = StringBuilder()
            var canStart = false

            url.replace("<!--image-->", "").split("/").forEach { segment ->
                if (!canStart && segment.contains(".")) {
                    canStart = true
                } else if (canStart) {
                    result.append(segment)
                }
            }

            return result.toString()
        }

        private fun loadFullSaveFileContent(fileName: String): String {
            val primary = getDefaultStoragePath() + saveFolderName + File.separator + fileName
            val fallback = getBackupStoragePath() + saveFolderName + File.separator + fileName
            val loadPath = when {
                LightCache.testFileExist(primary) -> primary
                LightCache.testFileExist(fallback) -> fallback
                else -> return ""
            }

            return try {
                LightCache.loadFile(loadPath)?.toString(Charsets.UTF_8).orEmpty()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }

        private fun writeFullSaveFileContent(fileName: String, content: String): Boolean {
            var path = ""
            var actualFileName = fileName
            if (fileName.contains(File.separator)) {
                path = fileName.substring(0, fileName.lastIndexOf(File.separator))
                actualFileName = fileName.substring(fileName.lastIndexOf(File.separator) + File.separator.length)
            }

            val primaryPath = getDefaultStoragePath() + saveFolderName + File.separator + path
            val fallbackPath = getBackupStoragePath() + saveFolderName + File.separator + path
            val bytes = content.toByteArray()

            return LightCache.saveFile(primaryPath, actualFileName, bytes, true) ||
                LightCache.saveFile(fallbackPath, actualFileName, bytes, true)
        }

        @JvmStatic
        fun loadFullFileFromSaveFolder(subFolderName: String, fileName: String): String =
            loadFullSaveFileContent(subFolderName + File.separator + fileName)

        @JvmStatic
        fun writeFullFileIntoSaveFolder(subFolderName: String, fileName: String, s: String): Boolean =
            writeFullSaveFileContent(subFolderName + File.separator + fileName, s)

        @JvmStatic
        fun loadLocalBookShelf() {
            bookshelf = ArrayList()
            val content = loadFullSaveFileContent(saveLocalBookshelfFileName)
            content.split("||").forEach { item ->
                if (item.isNotEmpty()) {
                    bookshelf?.add(item.toInt())
                }
            }
        }

        @JvmStatic
        fun writeLocalBookShelf() {
            val currentBookshelf = ensureBookshelf()
            writeFullSaveFileContent(saveLocalBookshelfFileName, currentBookshelf.joinToString("||"))
        }

        @JvmStatic
        fun addToLocalBookshelf(aid: Int) {
            val currentBookshelf = ensureBookshelf()
            if (!currentBookshelf.contains(aid)) {
                currentBookshelf.add(0, aid)
            }
            writeLocalBookShelf()
        }

        @JvmStatic
        fun removeFromLocalBookshelf(aid: Int) {
            ensureBookshelf().remove(aid)
            writeLocalBookShelf()
        }

        @JvmStatic
        fun getLocalBookshelfList(): ArrayList<Int> = ensureBookshelf()

        @JvmStatic
        fun testInLocalBookshelf(aid: Int): Boolean = ensureBookshelf().contains(aid)

        @JvmStatic
        fun moveBookToTheTopOfBookshelf(aid: Int) {
            val currentBookshelf = ensureBookshelf()
            val index = currentBookshelf.indexOf(aid)
            if (index == -1) {
                return
            }

            currentBookshelf.removeAt(index)
            currentBookshelf.add(0, aid)
            writeLocalBookShelf()
        }

        @JvmStatic
        fun testInBookshelf(): Boolean = isInBookshelf

        @JvmStatic
        fun EnterBookshelf() {
            isInBookshelf = true
        }

        @JvmStatic
        fun LeaveBookshelf() {
            isInBookshelf = false
        }

        @JvmStatic
        fun testInLatest(): Boolean = isInLatest

        @JvmStatic
        fun EnterLatest() {
            isInLatest = true
        }

        @JvmStatic
        fun LeaveLatest() {
            isInLatest = false
        }

        @JvmStatic
        fun readSearchHistory() {
            searchHistory = ArrayList()
            val content = loadFullSaveFileContent(saveSearchHistoryFileName)
            var index = 0
            while (true) {
                val begin = content.indexOf("[", index)
                if (begin == -1) break
                index = begin + 1
                val end = content.indexOf("]", index)
                if (end == -1) break
                searchHistory?.add(content.substring(index, end))
            }
        }

        @JvmStatic
        fun writeSearchHistory() {
            val serialized = ensureSearchHistory().joinToString(separator = "", prefix = "", postfix = "") {
                "[$it]"
            }
            writeFullSaveFileContent(saveSearchHistoryFileName, serialized)
        }

        @JvmStatic
        fun getSearchHistory(): ArrayList<String> = ensureSearchHistory()

        @JvmStatic
        fun addSearchHistory(record: String) {
            val history = ensureSearchHistory()
            if (history.contains("[")) return

            while (history.remove(record)) {
                // Remove duplicates.
            }

            while (history.size >= maxSearchHistory) {
                history.removeAt(maxSearchHistory - 1)
            }
            history.add(0, record)
            writeSearchHistory()
        }

        @JvmStatic
        fun deleteSearchHistory(record: String) {
            val history = ensureSearchHistory()
            if (history.contains("[")) return

            while (history.remove(record)) {
                // Remove duplicates.
            }
            writeSearchHistory()
        }

        @Deprecated("Deprecated legacy search ordering helper")
        @JvmStatic
        fun onSearchClicked(index: Int) {
            val history = ensureSearchHistory()
            if (index >= history.size) return

            val item = history.removeAt(index)
            history.add(0, item)
            writeSearchHistory()
        }

        @JvmStatic
        fun clearSearchHistory() {
            searchHistory = ArrayList()
            writeSearchHistory()
        }

        @JvmStatic
        fun getMaxSearchHistory(): Int = maxSearchHistory

        @JvmStatic
        fun setMaxSearchHistory(size: Int) {
            if (size > 0) {
                maxSearchHistory = size
            }
        }

        @JvmStatic
        fun loadReadSaves() {
            readSaves = ArrayList()
            val content = loadFullSaveFileContent(saveReadSavesFileName)
            content.split("||").forEach { item ->
                val parts = item.split(",,")
                if (parts.size == 3) {
                    readSaves?.add(
                        ReadSaves().apply {
                            cid = parts[0].toInt()
                            pos = parts[1].toInt()
                            height = parts[2].toInt()
                        }
                    )
                }
            }
        }

        @JvmStatic
        fun writeReadSaves() {
            val serialized = ensureReadSaves().joinToString("||") {
                "${it.cid},,${it.pos},,${it.height}"
            }
            writeFullSaveFileContent(saveReadSavesFileName, serialized)
        }

        @JvmStatic
        fun addReadSavesRecord(c: Int, p: Int, h: Int) {
            if (p < 100) return

            val saves = ensureReadSaves()
            val existing = saves.firstOrNull { it.cid == c }
            if (existing != null) {
                existing.pos = p
                existing.height = h
            } else {
                saves.add(
                    ReadSaves().apply {
                        cid = c
                        pos = p
                        height = h
                    }
                )
            }
            writeReadSaves()
        }

        @JvmStatic
        fun getReadSavesRecord(c: Int, h: Int): Int =
            ensureReadSaves().firstOrNull { it.cid == c }?.pos ?: 0

        @JvmStatic
        fun loadReadSavesV1() {
            readSavesV1 = ArrayList()
            val content = loadFullSaveFileContent(saveReadSavesV1FileName)

            content.split("||").forEach { item ->
                val parts = item.split(":")
                if (parts.size == 5 && parts.all(LightTool::isInteger)) {
                    readSavesV1?.add(
                        ReadSavesV1().apply {
                            aid = parts[0].toInt()
                            vid = parts[1].toInt()
                            cid = parts[2].toInt()
                            lineId = parts[3].toInt()
                            wordId = parts[4].toInt()
                        }
                    )
                }
            }
        }

        @JvmStatic
        fun writeReadSavesV1() {
            val serialized = ensureReadSavesV1().joinToString("||") {
                "${it.aid}:${it.vid}:${it.cid}:${it.lineId}:${it.wordId}"
            }
            writeFullSaveFileContent(saveReadSavesV1FileName, serialized)
        }

        @JvmStatic
        fun addReadSavesRecordV1(aid: Int, vid: Int, cid: Int, lineId: Int, wordId: Int) {
            val saves = ensureReadSavesV1()
            val existing = saves.firstOrNull { it.aid == aid }
            if (existing != null) {
                existing.vid = vid
                existing.cid = cid
                existing.lineId = lineId
                existing.wordId = wordId
            } else {
                saves.add(
                    ReadSavesV1().apply {
                        this.aid = aid
                        this.vid = vid
                        this.cid = cid
                        this.lineId = lineId
                        this.wordId = wordId
                    }
                )
            }
            writeReadSavesV1()
        }

        @JvmStatic
        fun removeReadSavesRecordV1(aid: Int) {
            ensureReadSavesV1().removeAll { it.aid == aid }
            writeReadSavesV1()
        }

        @JvmStatic
        fun getReadSavesRecordV1(aid: Int): ReadSavesV1? =
            ensureReadSavesV1().firstOrNull { it.aid == aid }

        @JvmStatic
        fun loadAllSetting() {
            lookupInternalStorageOnly = SaveFileMigration.migrationCompleted()
            allSetting = ContentValues()

            val content = loadFullSaveFileContent(saveSetting)
            content.split("||||").forEach { setting ->
                val parts = setting.split("::::")
                if (parts.size == 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
                    allSetting?.put(parts[0], parts[1])
                }
            }

            val version = getFromAllSetting(SettingItems.version)
            if (version.isNullOrEmpty()) {
                setToAllSetting(SettingItems.version, "1")
            }
        }

        @JvmStatic
        fun saveAllSetting() {
            val settings = ensureAllSetting()
            val serialized = settings.keySet().joinToString("||||") { key ->
                "$key::::${settings.getAsString(key)}"
            }
            writeFullSaveFileContent(saveSetting, serialized)
        }

        @JvmStatic
        fun getFromAllSetting(name: SettingItems): String? =
            ensureAllSetting().getAsString(name.toString())

        @JvmStatic
        fun setToAllSetting(name: SettingItems?, value: String?) {
            if (name != null && value != null) {
                val settings = ensureAllSetting()
                settings.remove(name.toString())
                settings.put(name.toString(), value)
                saveAllSetting()
            }
        }

        @JvmStatic
        fun saveNovelContentImage(url: String): Boolean {
            val imageFileName = generateImageFileNameByURL(url)
            val defaultFullPath = getFirstFullSaveFilePath() + imgsSaveFolderName + File.separator + imageFileName
            val fallbackFullPath = getSecondFullSaveFilePath() + imgsSaveFolderName + File.separator + imageFileName

            if (!LightCache.testFileExist(defaultFullPath) && !LightCache.testFileExist(fallbackFullPath)) {
                val fileContent = LightNetwork.LightHttpDownload(url) ?: return false
                return LightCache.saveFile(defaultFullPath, fileContent, true) ||
                    LightCache.saveFile(fallbackFullPath, fileContent, true)
            }
            return true
        }

        @JvmStatic
        fun saveNovelCoverImage(fileName: String, fileContent: ByteArray): Boolean {
            val defaultFullPath = getFirstFullSaveFilePath() + imgsSaveFolderName + File.separator + fileName
            val fallbackFullPath = getSecondFullSaveFilePath() + imgsSaveFolderName + File.separator + fileName

            if (!LightCache.testFileExist(defaultFullPath) && !LightCache.testFileExist(fallbackFullPath)) {
                return LightCache.saveFile(defaultFullPath, fileContent, true) ||
                    LightCache.saveFile(fallbackFullPath, fileContent, true)
            }
            return true
        }

        @JvmStatic
        fun getExistingNovelContentImagePath(fileName: String): String? {
            val defaultFullPath = getFirstFullSaveFilePath() + imgsSaveFolderName + File.separator + fileName
            val fallbackFullPath = getSecondFullSaveFilePath() + imgsSaveFolderName + File.separator + fileName

            return when {
                LightCache.testFileExist(defaultFullPath) -> defaultFullPath
                LightCache.testFileExist(fallbackFullPath) -> fallbackFullPath
                else -> null
            }
        }

        @JvmStatic
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            return connectivityManager?.activeNetworkInfo != null
        }

        @JvmStatic
        fun loadSavedNotice(): String = loadFullSaveFileContent(saveNoticeString)

        @JvmStatic
        fun writeTheNotice(noticeStr: String) {
            writeFullSaveFileContent(saveNoticeString, noticeStr)
        }

        @JvmStatic
        fun loadUserInfoSet(): Boolean {
            val bytes = when {
                LightCache.testFileExist(getFirstFullUserAccountSaveFilePath()) ->
                    LightCache.loadFile(getFirstFullUserAccountSaveFilePath())

                LightCache.testFileExist(getSecondFullUserAccountSaveFilePath()) ->
                    LightCache.loadFile(getSecondFullUserAccountSaveFilePath())

                else -> null
            } ?: return false

            return try {
                val raw = bytes.toString(Charsets.UTF_8)
                Log.d("MewX", raw)
                LightUserSession.decAndSetUserFile(raw)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        @JvmStatic
        fun saveUserInfoSet(): Boolean {
            LightCache.saveFile(
                getFirstFullUserAccountSaveFilePath(),
                LightUserSession.encUserFile().toByteArray(),
                true
            )
            if (!LightCache.testFileExist(getFirstFullUserAccountSaveFilePath())) {
                LightCache.saveFile(
                    getSecondFullUserAccountSaveFilePath(),
                    LightUserSession.encUserFile().toByteArray(),
                    true
                )
                return LightCache.testFileExist(getSecondFullUserAccountSaveFilePath())
            }
            return true
        }

        private fun ensureSearchHistory(): ArrayList<String> {
            if (searchHistory == null) readSearchHistory()
            return searchHistory ?: ArrayList<String>().also { searchHistory = it }
        }

        private fun ensureReadSaves(): ArrayList<ReadSaves> {
            if (readSaves == null) loadReadSaves()
            return readSaves ?: ArrayList<ReadSaves>().also { readSaves = it }
        }

        private fun ensureBookshelf(): ArrayList<Int> {
            if (bookshelf == null) loadLocalBookShelf()
            return bookshelf ?: ArrayList<Int>().also { bookshelf = it }
        }

        private fun ensureReadSavesV1(): ArrayList<ReadSavesV1> {
            if (readSavesV1 == null) loadReadSavesV1()
            return readSavesV1 ?: ArrayList<ReadSavesV1>().also { readSavesV1 = it }
        }

        private fun ensureAllSetting(): ContentValues {
            if (allSetting == null) loadAllSetting()
            return allSetting ?: ContentValues().also { allSetting = it }
        }
    }
}
