package io.agora.chat.uikit.base

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.utils.StatusBarCompat

abstract class ChatUIKitBaseActivity<B : ViewBinding> :  AppCompatActivity() {

    lateinit var binding: B
    private var loadingDialog: AlertDialog? = null
    lateinit var mContext: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        beforeSetContentView()
        val binding = getViewBinding(layoutInflater)
        if (binding == null) {
            ChatLog.e("ChatUIKitBaseActivity", "$this binding is null.")
            finish()
            return
        } else {
            this.binding = binding
            setContentView(this.binding.root)
        }

        //WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    open fun beforeSetContentView() {}

    protected abstract fun getViewBinding(inflater: LayoutInflater): B?

    fun <T : ViewModel> getViewModel(viewModelClass: Class<T>, owner: ViewModelStoreOwner): T {
        return ViewModelProvider(owner, ViewModelProvider.NewInstanceFactory())[viewModelClass]
    }

    fun <T : ViewModel> getViewModel(
        viewModelClass: Class<T>,
        factory: ViewModelProvider.NewInstanceFactory,
        owner: ViewModelStoreOwner
    ): T {
        return ViewModelProvider(owner, factory)[viewModelClass]
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        setActivityTheme()
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        setActivityTheme()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setActivityTheme()
    }

    open fun setActivityTheme() {
        setFitSystemForTheme(true)
    }

    /**
     * Common settings for activity
     * @param fitSystemForTheme
     */
    open fun setFitSystemForTheme(fitSystemForTheme: Boolean) {
        val colorResource = ContextCompat.getColor(this, R.color.ease_color_background)
        val isDark = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES
        setFitSystemForTheme(fitSystemForTheme, colorResource, isDark)
    }

    /**
     * Can set the status bar's style and change the background color
     * @param fitSystemForTheme
     * @param color Color
     */
    open fun setFitSystemForTheme(
        fitSystemForTheme: Boolean,
        @ColorInt color: Int,
        isDark: Boolean
    ) {
        setFitSystem(fitSystemForTheme)
        StatusBarCompat.compat(this, color)
        StatusBarCompat.setLightStatusBar(this, isDark)
    }

    /**
     * Can set the status bar's style and change the background color
     * @param fitSystemForTheme
     * @param color Color string
     */
    open fun setFitSystemForTheme(fitSystemForTheme: Boolean, color: String?, isDark: Boolean) {
        setFitSystem(fitSystemForTheme)
        StatusBarCompat.compat(this, Color.parseColor(color))
        StatusBarCompat.setLightStatusBar(this, isDark)
    }

    /**
     * Set status bar's style
     * @param fitSystemForTheme
     */
    private fun setFitSystem(fitSystemForTheme: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
        if (fitSystemForTheme) {
            val contentFrameLayout = findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup
            val parentView = contentFrameLayout.getChildAt(0)
            if (parentView != null && Build.VERSION.SDK_INT >= 14) {
                parentView.fitsSystemWindows = true
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
    }

    open fun showLoading(cancelable: Boolean) {
        if (loadingDialog == null) {
            loadingDialog = AlertDialog.Builder(this).setView(R.layout.uikit_view_base_loading).create().apply {
                // Change to background to transparent
                window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        loadingDialog?.setCancelable(cancelable)
        loadingDialog?.show()
    }

    open fun dismissLoading() {
        loadingDialog?.dismiss()
    }

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (currentFocus != null) {
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    open fun showKeyboard(editText: EditText) {
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                val imm = editText.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, 0)
            }, 500)
        }
    }
}