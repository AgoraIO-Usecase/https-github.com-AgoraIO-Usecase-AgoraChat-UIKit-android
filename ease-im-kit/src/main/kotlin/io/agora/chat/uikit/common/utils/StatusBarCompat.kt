package io.agora.chat.uikit.common.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import java.lang.reflect.Field


object StatusBarCompat {
    private const val INVALID_VAL = -1
    private val COLOR_DEFAULT = Color.parseColor("#20000000")

    /**
     * Can set the status bar's status and change the color of status background
     * @param activity
     * @param fitSystemForTheme If is FALSE, you can see the status bar; Or not see.
     * @param color status color
     */
    fun setFitSystemForTheme(activity: Activity, fitSystemForTheme: Boolean, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        if (fitSystemForTheme) {
            val contentFrameLayout =
                activity.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
            val parentView = contentFrameLayout.getChildAt(0)
            if (parentView != null && Build.VERSION.SDK_INT >= 14) {
                parentView.fitsSystemWindows = true
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = activity.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        compat(activity, color)
    }

    /**
     * Make activity to full screen.
     */
    fun hideStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 获取当前活动的视图
            // 获取当前活动的视图
            val decorView: View = activity.window.decorView
            val controller = decorView.windowInsetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars())
                controller.hide(WindowInsets.Type.navigationBars())
            }
        } else {
            val window = activity.window
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun compat(activity: Activity, statusColor: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (statusColor != INVALID_VAL) {
                activity.window.statusBarColor = statusColor
            }
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            var color = COLOR_DEFAULT
            val contentView = activity.findViewById<View>(android.R.id.content) as ViewGroup
            if (statusColor != INVALID_VAL) {
                color = statusColor
            }
            val childCount = contentView.childCount
            if (childCount > 1) {
                contentView.removeViewAt(1)
            }
            val statusBarView = View(activity)
            val lp = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity)
            )
            statusBarView.setBackgroundColor(color)
            contentView.addView(statusBarView, lp)
        }
    }

    fun compat(activity: Activity) {
        compat(activity, INVALID_VAL)
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun setLightStatusBar(activity: Activity, dark: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            when (RomUtils.lightStatusBarAvailableRomType) {
                RomUtils.AvailableRomType.MIUI -> setMIUISetStatusBarLightMode(activity, dark)
                RomUtils.AvailableRomType.FLYME -> setFlymeLightStatusBar(activity, dark)
                RomUtils.AvailableRomType.ANDROID_NATIVE -> setAndroidNativeLightStatusBar(
                    activity,
                    dark
                )
            }
        }
    }

    fun setMIUISetStatusBarLightMode(activity: Activity, dark: Boolean): Boolean {
        var result = false
        val window = activity.window
        if (window != null) {
            val clazz: Class<*> = window.javaClass
            try {
                var darkModeFlag = 0
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod(
                    "setExtraFlags",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag) //状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag) //清除黑色字体
                }
                result = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && RomUtils.isMiUIV7OrAbove) {
                    if (dark) {
                        activity.window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        activity.window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    }
                }
            } catch (e: Exception) {
            }
        }
        return result
    }

    private fun setFlymeLightStatusBar(activity: Activity?, dark: Boolean): Boolean {
        var result = false
        if (activity != null) {
            try {
                val lp: WindowManager.LayoutParams = activity.window.attributes
                val darkFlag: Field = WindowManager.LayoutParams::class.java
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags: Field = WindowManager.LayoutParams::class.java
                    .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                value = if (dark) {
                    value or bit
                } else {
                    value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                activity.window.attributes = lp
                result = true
            } catch (e: Exception) {
            }
        }
        return result
    }

    private fun setAndroidNativeLightStatusBar(activity: Activity, dark: Boolean) {
        val decor = activity.window.decorView
        if (dark) {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    /**
     * Set custom color for toolbar arrow
     * @param context
     * @param colorId
     */
    fun setToolbarCustomColor(context: Context?, @ColorRes colorId: Int) {
        setToolbarCustomColor(context, colorId, null)
    }

    /**
     * Set custom color for toolbar arrow
     * @param context
     * @param colorId
     * @param leftArrow
     */
    fun setToolbarCustomColor(context: Context?, @ColorRes colorId: Int, leftArrow: Drawable?) {
        if (context == null) {
            return
        }
        setToolbarCustomColorDefault(context, ContextCompat.getColor(context, colorId), leftArrow)
    }

    /**
     * Set custom color for toolbar arrow
     * @param context
     * @param colorId
     */
    fun setToolbarCustomColorDefault(context: Context?, @ColorInt colorId: Int) {
        setToolbarCustomColorDefault(context, colorId, null)
    }

    /**
     * Set custom color for toolbar arrow
     * @param context
     * @param colorId
     * @param leftArrow
     */
    fun setToolbarCustomColorDefault(
        context: Context?,
        @ColorInt colorId: Int,
        leftArrow: Drawable?
    ) {
        var leftArrow = leftArrow
        if (context == null) {
            return
        }
        if (leftArrow == null) {
            leftArrow = ContextCompat.getDrawable(context, R.drawable.uikit_default_navigation_icon)
        }
        if (leftArrow != null) {
            leftArrow.setColorFilter(colorId, PorterDuff.Mode.SRC_ATOP)
            if (context is AppCompatActivity) {
                context.supportActionBar?.setHomeAsUpIndicator(leftArrow)
            }
        }
    }
}