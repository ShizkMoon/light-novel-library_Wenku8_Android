@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.network

import android.content.Context
import android.os.AsyncTask
import androidx.annotation.NonNull
import org.mewx.wenku8.api.Wenku8Error

@Suppress("unused")
class LightUserSession private constructor() {
    companion object {
        @JvmField
        var aiui: AsyncInitUserInfo? = null

        private var usernameOrEmail: String = ""
        private var password: String = ""
        private var session: String = ""

        @NonNull
        @JvmStatic
        fun getLoggedAs(): String = ""

        @JvmStatic
        fun getUsernameOrEmail(): String = usernameOrEmail

        @JvmStatic
        fun getPassword(): String = password

        @JvmStatic
        fun getSession(): String = session

        @JvmStatic
        fun setSession(s: String?) {
            session = s.orEmpty()
        }

        @JvmStatic
        fun getLogStatus(): Boolean = false

        @JvmStatic
        fun doLoginFromFile(loadUserInfoSet: Runnable?): Wenku8Error.ErrorCode {
            loadUserInfoSet?.run()
            return Wenku8Error.ErrorCode.SYSTEM_4_NOT_LOGGED_IN
        }

        @JvmStatic
        fun doLoginFromGiven(name: String?, pwd: String?, saveUserInfoSet: Runnable?): Wenku8Error.ErrorCode {
            usernameOrEmail = name.orEmpty()
            password = pwd.orEmpty()
            return Wenku8Error.ErrorCode.SYSTEM_4_NOT_LOGGED_IN
        }

        @JvmStatic
        fun logOut(fileDeletionCallback: Runnable?) {
            usernameOrEmail = ""
            password = ""
            session = ""
            fileDeletionCallback?.run()
        }

        @JvmStatic
        fun heartbeatLogin(loadUserInfoSet: Runnable?): Wenku8Error.ErrorCode {
            loadUserInfoSet?.run()
            return Wenku8Error.ErrorCode.SYSTEM_4_NOT_LOGGED_IN
        }

        @JvmStatic
        fun isUserInfoSet(): Boolean = usernameOrEmail.isNotEmpty() && password.isNotEmpty()

        @JvmStatic
        fun setUserInfo(username: String?, password: String?) {
            usernameOrEmail = username.orEmpty()
            this.password = password.orEmpty()
        }

        @JvmStatic
        fun decAndSetUserFile(raw: String?) {
            usernameOrEmail = ""
            password = ""
            session = ""
        }

        @JvmStatic
        fun encUserFile(): String = ""

        @JvmStatic
        fun isInteger(@NonNull value: String): Boolean {
            if (value.isEmpty()) return false
            for (index in value.indices) {
                if (!value[index].isDigit()) return false
            }
            return true
        }
    }

    class AsyncInitUserInfo(
        context: Context?,
        failureCallback: Runnable?,
        private val loadUserInfoSet: Runnable?,
    ) : AsyncTask<Int, Int, Wenku8Error.ErrorCode>() {
        override fun doInBackground(vararg params: Int?): Wenku8Error.ErrorCode {
            loadUserInfoSet?.run()
            return Wenku8Error.ErrorCode.SYSTEM_1_SUCCEEDED
        }

        override fun onPostExecute(e: Wenku8Error.ErrorCode?) {
            // Public stub intentionally performs no network login.
        }
    }
}
