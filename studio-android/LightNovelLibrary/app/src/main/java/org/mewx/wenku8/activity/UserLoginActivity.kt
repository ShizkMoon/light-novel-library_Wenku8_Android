@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.ByteArrayOutputStream
import org.mewx.wenku8.MyApp
import org.mewx.wenku8.R
import org.mewx.wenku8.api.Wenku8API
import org.mewx.wenku8.api.Wenku8Error
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.network.LightNetwork
import org.mewx.wenku8.network.LightUserSession
import org.mewx.wenku8.util.GoogleServicesHelper
import org.mewx.wenku8.util.LightCache
import org.mewx.wenku8.util.ProgressDialogHelper

/**
 * User login activity.
 */
class UserLoginActivity : BaseMaterialActivity() {
    private lateinit var userNameOrEmailView: EditText
    private lateinit var passwordView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialStyle(R.layout.layout_user_login)

        GoogleServicesHelper.initFirebase(this)

        userNameOrEmailView = findViewById(R.id.edit_username_or_email)
        passwordView = findViewById(R.id.edit_password)
        val loginView = findViewById<TextView>(R.id.btn_login)
        val registerView = findViewById<TextView>(R.id.btn_register)

        loginView.setOnClickListener {
            val userNameOrEmail = userNameOrEmailView.text.toString()
            val password = passwordView.text.toString()
            if (
                userNameOrEmail.isEmpty() || userNameOrEmail.length > 30 ||
                password.isEmpty() || password.length > 30
            ) {
                Toast.makeText(
                    this,
                    resources.getString(R.string.system_info_fill_not_complete),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            AsyncLoginTask().execute(userNameOrEmail, password)
        }

        registerView.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.dialog_content_verify_register)
                .setPositiveButton(R.string.dialog_positive_ok) { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(Wenku8API.REGISTER_URL)
                    }
                    val title = resources.getString(R.string.system_choose_browser)
                    val chooser = Intent.createChooser(intent, title)
                    startActivity(chooser)
                }
                .setNegativeButton(R.string.dialog_negative_pass, null)
                .show()
        }
    }

    private inner class AsyncLoginTask : AsyncTask<String, Int, Wenku8Error.ErrorCode>() {
        private var progressDialog: ProgressDialogHelper? = null
        private var loginResult: Wenku8Error.ErrorCode = Wenku8Error.ErrorCode.ERROR_DEFAULT

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialogHelper.show(
                this@UserLoginActivity,
                R.string.system_logging_in,
                true,
                false,
                null
            )
        }

        override fun doInBackground(vararg params: String): Wenku8Error.ErrorCode {
            try {
                Thread.sleep(500)
            } catch (exception: InterruptedException) {
                exception.printStackTrace()
            }

            loginResult = LightUserSession.doLoginFromGiven(
                params[0],
                params[1],
                Runnable { GlobalConfig.saveUserInfoSet() }
            )
            if (loginResult == Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED) {
                saveAvatar()
            }

            return loginResult
        }

        private fun saveAvatar() {
            val avatarBytes = LightNetwork.LightHttpPostConnection(Wenku8API.BASE_URL, Wenku8API.getUserAvatar())
                ?: defaultAvatarBytes()

            if (!LightCache.saveFile(GlobalConfig.getFirstUserAvatarSaveFilePath(), avatarBytes, true)) {
                LightCache.saveFile(GlobalConfig.getSecondUserAvatarSaveFilePath(), avatarBytes, true)
            }
        }

        private fun defaultAvatarBytes(): ByteArray {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_noavatar)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            return outputStream.toByteArray()
        }

        override fun onPostExecute(result: Wenku8Error.ErrorCode) {
            super.onPostExecute(result)

            progressDialog?.dismiss()
            when (result) {
                Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED -> {
                    Toast.makeText(
                        MyApp.getContext(),
                        resources.getString(R.string.system_logged),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                Wenku8Error.ErrorCode.SYSTEM_2_ERROR_USERNAME -> {
                    Toast.makeText(
                        MyApp.getContext(),
                        resources.getString(R.string.system_username_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Wenku8Error.ErrorCode.SYSTEM_3_ERROR_PASSWORD -> {
                    Toast.makeText(
                        MyApp.getContext(),
                        resources.getString(R.string.system_password_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> Unit
            }
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(menuItem)
    }
}
