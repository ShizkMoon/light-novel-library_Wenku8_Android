@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package org.mewx.wenku8.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.makeramen.roundedimageview.RoundedImageView
import org.mewx.wenku8.R
import org.mewx.wenku8.activity.MainActivity
import org.mewx.wenku8.activity.UserInfoActivity
import org.mewx.wenku8.activity.UserLoginActivity
import org.mewx.wenku8.global.GlobalConfig
import org.mewx.wenku8.network.LightUserSession
import org.mewx.wenku8.util.GoogleServicesHelper
import org.mewx.wenku8.util.LightCache

class NavigationDrawerFragment : Fragment() {
    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var fragmentContainerView: View? = null
    private var backgroundImage: ImageView? = null
    private var mainActivity: MainActivity? = null
    private var drawerLayout: DrawerLayout? = null
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private lateinit var userNameText: TextView
    private lateinit var userAvatarView: RoundedImageView
    private var fakeDarkSwitcher = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.layout_main_menu, container, false)

    private fun generateNavigationButtonOnClickListener(
        targetFragment: MainActivity.FragmentMenuOption,
        fragment: Fragment
    ): View.OnClickListener = View.OnClickListener {
        val activity = mainActivity ?: return@OnClickListener
        if (activity.currentFragment == targetFragment) {
            return@OnClickListener
        }

        clearAllButtonColor()
        setHighLightButton(targetFragment)
        activity.currentFragment = targetFragment
        activity.changeFragment(fragment)
        closeDrawer()

        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, fragment.javaClass.simpleName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, fragment.javaClass.simpleName)
        }
        GoogleServicesHelper.logEvent(firebaseAnalytics, FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mainActivity == null && activity is MainActivity) {
            mainActivity = activity as MainActivity
        }

        try {
            view.findViewById<View>(R.id.main_menu_rklist).setOnClickListener(
                generateNavigationButtonOnClickListener(
                    MainActivity.FragmentMenuOption.RKLIST,
                    RKListFragment()
                )
            )
            view.findViewById<View>(R.id.main_menu_latest).setOnClickListener(
                generateNavigationButtonOnClickListener(
                    MainActivity.FragmentMenuOption.LATEST,
                    LatestFragment()
                )
            )
            view.findViewById<View>(R.id.main_menu_fav).setOnClickListener(
                generateNavigationButtonOnClickListener(
                    MainActivity.FragmentMenuOption.FAV,
                    FavFragment()
                )
            )
            view.findViewById<View>(R.id.main_menu_config).setOnClickListener(
                generateNavigationButtonOnClickListener(
                    MainActivity.FragmentMenuOption.CONFIG,
                    ConfigFragment()
                )
            )

            view.findViewById<View>(R.id.main_menu_open_source).setOnClickListener {
                val fragmentActivity: FragmentActivity = activity ?: return@setOnClickListener
                MaterialAlertDialogBuilder(fragmentActivity)
                    .setTitle(R.string.main_menu_statement)
                    .setMessage(GlobalConfig.getOpensourceLicense())
                    .setPositiveButton(R.string.dialog_positive_known, null)
                    .show()
            }

            view.findViewById<View>(R.id.main_menu_dark_mode_switcher).setOnClickListener {
                openOrCloseDarkMode()
            }
        } catch (e: NullPointerException) {
            Toast.makeText(context, "NullPointerException in onViewCreated();", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }

        userAvatarView = view.findViewById(R.id.user_avatar)
        userNameText = view.findViewById(R.id.user_name)

        val userClickListener = View.OnClickListener {
            val context = context
            val currentActivity = activity
            if (
                !LightUserSession.getLogStatus() &&
                currentActivity != null &&
                GlobalConfig.isNetworkAvailable(currentActivity)
            ) {
                if (!LightUserSession.isUserInfoSet()) {
                    startActivity(Intent(currentActivity, UserLoginActivity::class.java))
                } else if (LightUserSession.aiui?.status == AsyncTask.Status.FINISHED && context != null) {
                    Toast.makeText(currentActivity, "Relogged.", Toast.LENGTH_SHORT).show()
                    LightUserSession.aiui = LightUserSession.AsyncInitUserInfo(
                        context,
                        Runnable {
                            LightCache.deleteFile(GlobalConfig.getFirstFullUserAccountSaveFilePath())
                            LightCache.deleteFile(GlobalConfig.getSecondFullUserAccountSaveFilePath())
                            LightCache.deleteFile(GlobalConfig.getFirstUserAvatarSaveFilePath())
                            LightCache.deleteFile(GlobalConfig.getSecondUserAvatarSaveFilePath())
                            Toast.makeText(
                                context,
                                context.resources.getString(R.string.system_log_info_outofdate),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        Runnable { GlobalConfig.loadUserInfoSet() }
                    )
                    LightUserSession.aiui?.execute()
                }
            } else if (currentActivity != null && !GlobalConfig.isNetworkAvailable(currentActivity)) {
                Toast.makeText(
                    currentActivity,
                    resources.getString(R.string.system_network_error),
                    Toast.LENGTH_SHORT
                ).show()
            } else if (currentActivity != null) {
                startActivity(Intent(currentActivity, UserInfoActivity::class.java))
            }
        }
        userAvatarView.setOnClickListener(userClickListener)
        userNameText.setOnClickListener(userClickListener)

        if (activity != null && !GlobalConfig.isNetworkAvailable(requireActivity())) {
            clearAllButtonColor()
            setHighLightButton(MainActivity.FragmentMenuOption.FAV)
            mainActivity?.apply {
                currentFragment = MainActivity.FragmentMenuOption.FAV
                changeFragment(FavFragment())
            }
        } else {
            clearAllButtonColor()
            mainActivity?.let {
                setHighLightButton(it.currentFragment)
                it.changeFragment(LatestFragment())
            }
        }

        backgroundImage = view.findViewById(R.id.bg_img)
        updateMenuBackground()

        val bottomLayout = view.findViewById<LinearLayout>(R.id.main_menu_bottom_layout)
        if (bottomLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomLayout) { v, windowInsets ->
                val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(0, 0, 0, insets.bottom)
                windowInsets
            }
        }
    }

    fun setup(fragmentId: Int, drawerLayout: DrawerLayout, toolbar: Toolbar) {
        val activity = activity as? MainActivity
        mainActivity = activity
        if (activity == null) {
            Toast.makeText(this.activity, "mainActivity == null !!! in setup()", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAnalytics = GoogleServicesHelper.initFirebase(activity)
        fragmentContainerView = activity.findViewById(fragmentId)
        this.drawerLayout = drawerLayout
        actionBarDrawerToggle = object : ActionBarDrawerToggle(
            activity,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        ) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                if (!isAdded) return
                activity.invalidateOptionsMenu()
            }

            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                if (!isAdded) return
                activity.invalidateOptionsMenu()
            }
        }

        drawerLayout.post { actionBarDrawerToggle?.syncState() }
        drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
    }

    private fun clearOneButtonColor(iconId: Int, textId: Int, backgroundId: Int) {
        val activity = mainActivity ?: return

        activity.findViewById<ImageButton>(iconId)?.setColorFilter(resources.getColor(R.color.menu_text_color))
        activity.findViewById<TextView>(textId)?.setTextColor(resources.getColor(R.color.menu_text_color))
        activity.findViewById<TableRow>(backgroundId)?.background =
            resources.getDrawable(R.drawable.btn_menu_item)
    }

    private fun clearAllButtonColor() {
        clearOneButtonColor(R.id.main_menu_ic_rklist, R.id.main_menu_text_rklist, R.id.main_menu_rklist)
        clearOneButtonColor(R.id.main_menu_ic_latest, R.id.main_menu_text_latest, R.id.main_menu_latest)
        clearOneButtonColor(R.id.main_menu_ic_fav, R.id.main_menu_text_fav, R.id.main_menu_fav)
        clearOneButtonColor(R.id.main_menu_ic_config, R.id.main_menu_text_config, R.id.main_menu_config)
    }

    @SuppressLint("NewApi")
    private fun setHighLightButton(iconId: Int, textId: Int, backgroundId: Int) {
        val activity = mainActivity ?: return

        activity.findViewById<ImageButton>(iconId)
            ?.setColorFilter(resources.getColor(R.color.menu_text_color_selected))
        activity.findViewById<TextView>(textId)
            ?.setTextColor(resources.getColor(R.color.menu_item_white))
        activity.findViewById<TableRow>(backgroundId)?.background =
            resources.getDrawable(R.drawable.btn_menu_item_selected)
    }

    private fun setHighLightButton(targetFragment: MainActivity.FragmentMenuOption) {
        when (targetFragment) {
            MainActivity.FragmentMenuOption.RKLIST ->
                setHighLightButton(R.id.main_menu_ic_rklist, R.id.main_menu_text_rklist, R.id.main_menu_rklist)

            MainActivity.FragmentMenuOption.LATEST ->
                setHighLightButton(R.id.main_menu_ic_latest, R.id.main_menu_text_latest, R.id.main_menu_latest)

            MainActivity.FragmentMenuOption.FAV ->
                setHighLightButton(R.id.main_menu_ic_fav, R.id.main_menu_text_fav, R.id.main_menu_fav)

            MainActivity.FragmentMenuOption.CONFIG ->
                setHighLightButton(R.id.main_menu_ic_config, R.id.main_menu_text_config, R.id.main_menu_config)
        }
    }

    private fun openOrCloseDarkMode() {
        val activity = mainActivity ?: return

        val darkModeSwitcherText = activity.findViewById<TextView>(R.id.main_menu_dark_mode_switcher)
        if (darkModeSwitcherText != null) {
            darkModeSwitcherText.setTextColor(
                resources.getColor(
                    if (fakeDarkSwitcher) R.color.menu_text_color else R.color.menu_text_color_selected
                )
            )
            darkModeSwitcherText.background = resources.getDrawable(
                if (fakeDarkSwitcher) R.drawable.btn_menu_item else R.drawable.btn_menu_item_selected
            )
        }

        fakeDarkSwitcher = !fakeDarkSwitcher
        Toast.makeText(activity, "夜间模式到阅读界面去试试~", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()

        if (LightUserSession.isUserInfoSet()) {
            if (userNameText.text.toString() != LightUserSession.getUsernameOrEmail()) {
                userNameText.text = LightUserSession.getUsernameOrEmail()
            }

            if (
                LightCache.testFileExist(GlobalConfig.getFirstUserAvatarSaveFilePath()) ||
                LightCache.testFileExist(GlobalConfig.getSecondUserAvatarSaveFilePath())
            ) {
                val avatarPath = if (LightCache.testFileExist(GlobalConfig.getFirstUserAvatarSaveFilePath())) {
                    GlobalConfig.getFirstUserAvatarSaveFilePath()
                } else {
                    GlobalConfig.getSecondUserAvatarSaveFilePath()
                }
                val options = BitmapFactory.Options().apply {
                    inSampleSize = 2
                }
                val bitmap = BitmapFactory.decodeFile(avatarPath, options)
                if (bitmap != null) {
                    userAvatarView.setImageBitmap(bitmap)
                }
            }
        } else {
            userNameText.text = resources.getString(R.string.main_menu_not_login)
            userAvatarView.setImageDrawable(resources.getDrawable(R.drawable.ic_noavatar))
        }

        updateMenuBackground()
    }

    private fun updateMenuBackground() {
        val imageView = backgroundImage ?: return

        when (GlobalConfig.getFromAllSetting(GlobalConfig.SettingItems.menu_bg_id)) {
            "0" -> {
                try {
                    imageView.setImageBitmap(
                        BitmapFactory.decodeFile(
                            GlobalConfig.getFromAllSetting(GlobalConfig.SettingItems.menu_bg_path)
                        )
                    )
                } catch (outOfMemoryError: OutOfMemoryError) {
                    try {
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = 2
                        }
                        val bitmap = BitmapFactory.decodeFile(
                            GlobalConfig.getFromAllSetting(GlobalConfig.SettingItems.menu_bg_path),
                            options
                        ) ?: throw Exception("PictureLoadFailureException")
                        imageView.setImageBitmap(bitmap)
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        Toast.makeText(
                            activity,
                            "Exception: $exception\n可能的原因有：图片不在内置SD卡；图片格式不正确；图片像素尺寸太大，请使用小一点的图，谢谢，此功能为试验性功能；",
                            Toast.LENGTH_SHORT
                        ).show()
                        imageView.setImageDrawable(resources.getDrawable(R.drawable.bg_avatar_04))
                    }
                }
            }
            "1" -> imageView.setImageDrawable(resources.getDrawable(R.drawable.bg_avatar_01))
            "2" -> imageView.setImageDrawable(resources.getDrawable(R.drawable.bg_avatar_02))
            "3" -> imageView.setImageDrawable(resources.getDrawable(R.drawable.bg_avatar_03))
            "4" -> imageView.setImageDrawable(resources.getDrawable(R.drawable.bg_avatar_04))
            "5" -> imageView.setImageDrawable(resources.getDrawable(R.drawable.bg_avatar_05))
        }
    }

    fun openDrawer() {
        val containerView = fragmentContainerView ?: return
        drawerLayout?.openDrawer(containerView)
    }

    fun closeDrawer() {
        val containerView = fragmentContainerView ?: return
        drawerLayout?.closeDrawer(containerView)
    }

    fun isDrawerOpen(): Boolean {
        val containerView = fragmentContainerView ?: return false
        return drawerLayout?.isDrawerOpen(containerView) == true
    }
}
