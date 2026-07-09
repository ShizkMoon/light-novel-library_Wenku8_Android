@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.makeramen.roundedimageview.RoundedImageView
import org.mewx.wenku8.R
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.api.Wenku8Error
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.global.api.UserInfo
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.network.LightUserSession
import org.mewx.wenku8.util.GoogleServicesHelper
import org.mewx.wenku8.util.LightCache
import org.mewx.wenku8.util.LightTool
import org.mewx.wenku8.util.ProgressDialogHelper

/**
 * User info activity.
 */
class UserInfoActivity : BaseMaterialActivity() {
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private lateinit var avatarView: RoundedImageView
    private lateinit var userNameView: TextView
    private lateinit var nickyNameView: TextView
    private lateinit var scoreView: TextView
    private lateinit var experienceView: TextView
    private lateinit var rankView: TextView
    private lateinit var logoutView: TextView
    private var userInfo: UserInfo? = null
    private var asyncGetUserInfo: AsyncGetUserInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_account_info)

        firebaseAnalytics = GoogleServicesHelper.initFirebase(this)

        avatarView = findViewById(R.id.user_avatar)
        userNameView = findViewById(R.id.username)
        nickyNameView = findViewById(R.id.nickname)
        scoreView = findViewById(R.id.score)
        experienceView = findViewById(R.id.experience)
        rankView = findViewById(R.id.rank)
        logoutView = findViewById(R.id.btn_logout)

        loadCachedAvatarInitially()

        asyncGetUserInfo = AsyncGetUserInfo()
        asyncGetUserInfo?.execute()
    }

    private fun loadCachedAvatarInitially() {
        Thread {
            val avatarPath = when {
                LightCache.testFileExist(GlobalConfig.getFirstUserAvatarSaveFilePath()) ->
                    GlobalConfig.getFirstUserAvatarSaveFilePath()
                LightCache.testFileExist(GlobalConfig.getSecondUserAvatarSaveFilePath()) ->
                    GlobalConfig.getSecondUserAvatarSaveFilePath()
                else -> null
            }

            if (avatarPath != null) {
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2
                }
                val cachedBitmap = BitmapFactory.decodeFile(avatarPath, options)
                if (cachedBitmap != null) {
                    runOnUiThread {
                        avatarView.setImageBitmap(cachedBitmap)
                    }
                }
            }
        }.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_user_info, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressed()
        } else if (menuItem.itemId == R.id.action_sign) {
            val currentTask = asyncGetUserInfo
            if (currentTask != null && currentTask.status == AsyncTask.Status.FINISHED) {
                asyncGetUserInfo = AsyncGetUserInfo()
                asyncGetUserInfo?.execute(1)
            } else {
                Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun applyUserInfo(fetchedUserInfo: UserInfo, fetchedAvatar: Bitmap?) {
        userInfo = fetchedUserInfo

        if (fetchedAvatar != null) {
            avatarView.setImageBitmap(fetchedAvatar)
        }

        userNameView.text = fetchedUserInfo.username.orEmpty()
        nickyNameView.text = fetchedUserInfo.nickyname.orEmpty()
        scoreView.text = fetchedUserInfo.score.toString()
        experienceView.text = fetchedUserInfo.experience.toString()
        rankView.text = fetchedUserInfo.rank.orEmpty()

        logoutView.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.dialog_content_sure_to_logout)
                .setPositiveButton(R.string.dialog_positive_ok) { _, _ ->
                    AsyncLogout().execute()
                }
                .setNegativeButton(R.string.dialog_negative_biao, null)
                .show()
        }
    }

    private inner class AsyncGetUserInfo : AsyncTask<Int, Void, UserInfoResult>() {
        private var isSignOperation = false
        private var progressDialog: ProgressDialogHelper? = null

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialogHelper.show(
                this@UserInfoActivity,
                R.string.system_fetching,
                true,
                false,
                null
            )
        }

        override fun doInBackground(vararg params: Int?): UserInfoResult {
            isSignOperation = params.size == 1 && params[0] == 1
            var signStatus: Wenku8Error.ErrorCode? = null

            return try {
                if (isSignOperation) {
                    val signBytes = LightNetwork.LightHttpPostConnection(
                        Wenku8API.BASE_URL,
                        Wenku8API.getUserSignParams()
                    ) ?: return UserInfoResult(Wenku8Error.ErrorCode.NETWORK_ERROR)

                    val signResponse = String(signBytes, Charsets.UTF_8)
                    if (!LightTool.isInteger(signResponse)) {
                        return UserInfoResult(Wenku8Error.ErrorCode.STRING_CONVERSION_ERROR)
                    }

                    signStatus = Wenku8Error.getSystemDefinedErrorCode(signResponse.toInt())
                    if (signStatus == Wenku8Error.ErrorCode.SYSTEM_9_SIGN_FAILED) {
                        return UserInfoResult(signStatus)
                    }
                }

                var infoBytes = LightNetwork.LightHttpPostConnection(
                    Wenku8API.BASE_URL,
                    Wenku8API.getUserInfoParams()
                ) ?: return UserInfoResult(Wenku8Error.ErrorCode.NETWORK_ERROR)

                var xml = String(infoBytes, Charsets.UTF_8)
                if (LightTool.isInteger(xml) &&
                    Wenku8Error.getSystemDefinedErrorCode(xml.toInt()) ==
                    Wenku8Error.ErrorCode.SYSTEM_4_NOT_LOGGED_IN
                ) {
                    val loginTemp = LightUserSession.doLoginFromFile(
                        Runnable { GlobalConfig.loadUserInfoSet() }
                    )
                    if (loginTemp != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                        return UserInfoResult(loginTemp)
                    }

                    infoBytes = LightNetwork.LightHttpPostConnection(
                        Wenku8API.BASE_URL,
                        Wenku8API.getUserInfoParams()
                    ) ?: return UserInfoResult(Wenku8Error.ErrorCode.NETWORK_ERROR)
                    xml = String(infoBytes, Charsets.UTF_8)
                } else if (LightTool.isInteger(xml)) {
                    return UserInfoResult(Wenku8Error.getSystemDefinedErrorCode(xml.toInt()))
                }

                val parsedUserInfo = UserInfo.parseUserInfo(xml)
                    ?: return UserInfoResult(Wenku8Error.ErrorCode.XML_PARSE_FAILED)

                val avatarBytes = LightNetwork.LightHttpDownload(Wenku8API.getAvatarURL(parsedUserInfo.uid))
                var decodedAvatar: Bitmap? = null
                if (avatarBytes != null && avatarBytes.isNotEmpty()) {
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = 2
                    }
                    decodedAvatar = BitmapFactory.decodeByteArray(avatarBytes, 0, avatarBytes.size, options)

                    val avatarPath = GlobalConfig.getFirstUserAvatarSaveFilePath()
                    if (!LightCache.saveFile(avatarPath, avatarBytes, true)) {
                        LightCache.saveFile(GlobalConfig.getSecondUserAvatarSaveFilePath(), avatarBytes, true)
                    }
                }

                UserInfoResult(
                    errorCode = Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED,
                    userInfo = parsedUserInfo,
                    avatar = decodedAvatar
                )
            } catch (e: Exception) {
                e.printStackTrace()
                UserInfoResult(Wenku8Error.ErrorCode.NETWORK_ERROR)
            }
        }

        override fun onPostExecute(result: UserInfoResult) {
            super.onPostExecute(result)
            progressDialog?.dismiss()

            if (isSignOperation) {
                val checkInParams = Bundle().apply {
                    putString(
                        "effective_click",
                        "" + (result.errorCode != Wenku8Error.ErrorCode.SYSTEM_9_SIGN_FAILED)
                    )
                }
                GoogleServicesHelper.logEvent(firebaseAnalytics, "daily_check_in", checkInParams)

                val toastMessage = if (result.errorCode == Wenku8Error.ErrorCode.SYSTEM_9_SIGN_FAILED) {
                    R.string.userinfo_sign_failed
                } else {
                    R.string.userinfo_sign_successful
                }
                Toast.makeText(this@UserInfoActivity, resources.getString(toastMessage), Toast.LENGTH_SHORT).show()

                if (result.errorCode == Wenku8Error.ErrorCode.SYSTEM_9_SIGN_FAILED) {
                    return
                }
            }

            if (result.errorCode == Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED && result.userInfo != null) {
                applyUserInfo(result.userInfo, result.avatar)
            } else if (result.errorCode != Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                Toast.makeText(this@UserInfoActivity, result.errorCode.toString(), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private inner class AsyncLogout : AsyncTask<Int, Int, Wenku8Error.ErrorCode>() {
        private var progressDialog: ProgressDialogHelper? = null

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialogHelper.show(
                this@UserInfoActivity,
                R.string.system_fetching,
                true,
                false,
                null
            )
        }

        override fun doInBackground(vararg params: Int?): Wenku8Error.ErrorCode {
            val bytes = LightNetwork.LightHttpPostConnection(
                Wenku8API.BASE_URL,
                Wenku8API.getUserLogoutParams()
            ) ?: return Wenku8Error.ErrorCode.NETWORK_ERROR

            val result = String(bytes, Charsets.UTF_8)
            if (!LightTool.isInteger(result)) {
                return Wenku8Error.ErrorCode.RETURNED_VALUE_EXCEPTION
            }

            return Wenku8Error.getSystemDefinedErrorCode(result.toInt())
        }

        override fun onPostExecute(errorCode: Wenku8Error.ErrorCode) {
            super.onPostExecute(errorCode)

            if (errorCode == Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED ||
                errorCode == Wenku8Error.ErrorCode.SYSTEM_4_NOT_LOGGED_IN
            ) {
                LightUserSession.logOut(
                    Runnable {
                        LightCache.deleteFile(GlobalConfig.getFirstFullUserAccountSaveFilePath())
                        LightCache.deleteFile(GlobalConfig.getSecondFullUserAccountSaveFilePath())
                        LightCache.deleteFile(GlobalConfig.getFirstUserAvatarSaveFilePath())
                        LightCache.deleteFile(GlobalConfig.getSecondUserAvatarSaveFilePath())
                    }
                )
                Toast.makeText(this@UserInfoActivity, "Logged out!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@UserInfoActivity, errorCode.toString(), Toast.LENGTH_SHORT).show()
            }

            progressDialog?.dismiss()
            finish()
        }
    }

    private data class UserInfoResult(
        val errorCode: Wenku8Error.ErrorCode,
        val userInfo: UserInfo? = null,
        val avatar: Bitmap? = null
    )
}
