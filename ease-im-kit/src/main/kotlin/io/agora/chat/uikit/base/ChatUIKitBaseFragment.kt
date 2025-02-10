package io.agora.chat.uikit.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.R

abstract class ChatUIKitBaseFragment<B : ViewBinding> : Fragment() {
    var binding: B? = null
    lateinit var mContext: Activity
    private var loadingDialog: AlertDialog? = null

    open fun showLoading(cancelable: Boolean) {
        if (loadingDialog == null) {
            loadingDialog = AlertDialog.Builder(requireActivity()).setView(R.layout.uikit_view_base_loading).create().apply {
                window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        loadingDialog?.setCancelable(cancelable)
        loadingDialog?.show()
    }

    open fun dismissLoading() {
        loadingDialog?.dismiss()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context as Activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = getViewBinding(inflater, container)
        return this.binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(savedInstanceState)
        initViewModel()
        initListener()
        initData()
    }

    /**
     * Initialize the views
     * @param savedInstanceState
     */
    protected open fun initView(savedInstanceState: Bundle?) {
        Log.d("TAG", "fragment = " + this.javaClass.simpleName)
    }

    /**
     * Initialize the viewModels
     */
    protected open fun initViewModel() {}

    /**
     * Initialize the Data
     */
    protected open fun initData() {}

    /**
     * Initialize the listeners
     */
    protected open fun initListener() {}

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    val parentActivity: ChatUIKitBaseActivity<*>
        get() = requireActivity() as ChatUIKitBaseActivity<*>

    protected abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): B?

}

@Suppress("UNCHECKED_CAST")
abstract class ChatUIKitBaseFragmentBuilder<T: ChatUIKitBaseFragmentBuilder<T>> {
    protected abstract val bundle: Bundle

    /**
     * Whether to use default titleBar which is [ChatUIKitTitleBar]
     *
     * @param useTitle
     * @return
     */
    fun useTitleBar(useTitle: Boolean): T {
        bundle.putBoolean(BConstant.KEY_USE_TITLE, useTitle)
        return this as T
    }

    /**
     * Whether to use default titleBar to replace actionBar when activity is a AppCompatActivity.
     * If set true, will call [androidx.appcompat.app.AppCompatActivity.setSupportActionBar].
     * @param replace
     * @return
     */
    fun useTitleBarToReplaceActionBar(replace: Boolean): T {
        bundle.putBoolean(BConstant.KEY_USE_TITLE_REPLACE, replace)
        return this as T
    }

    /**
     * Set titleBar's title
     *
     * @param title
     * @return
     */
    fun setTitleBarTitle(title: String?): T {
        bundle.putString(BConstant.KEY_SET_TITLE, title)
        return this as T
    }

    /**
     * Set titleBar's sub title
     *
     * @param subTitle
     * @return
     */
    fun setTitleBarSubTitle(subTitle: String?): T {
        bundle.putString(BConstant.KEY_SET_SUB_TITLE, subTitle)
        return this as T
    }

    /**
     * Whether show back icon in titleBar
     *
     * @param canBack
     * @return
     */
    fun enableTitleBarPressBack(canBack: Boolean): T {
        bundle.putBoolean(BConstant.KEY_ENABLE_BACK, canBack)
        return this as T
    }

    object BConstant {
        const val KEY_USE_TITLE = "key_use_title"
        const val KEY_USE_TITLE_REPLACE = "key_use_replace_action_bar"
        const val KEY_SET_TITLE = "key_set_title"
        const val KEY_SET_SUB_TITLE = "key_set_sub_title"
        const val KEY_ENABLE_BACK = "key_enable_back"
    }
}

