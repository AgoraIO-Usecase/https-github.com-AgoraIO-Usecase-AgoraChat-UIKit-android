package io.agora.chat.uikit.common.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.collection.ArrayMap

class AlertController(
    private val mContext: Context,
    val dialog: Dialog,
    val window: Window
) {
    private var viewHelper: DialogViewHelper? = null
    fun getContext(): Context {
        return mContext
    }

    fun <T : View?> getViewById(viewId: Int): T? {
        return viewHelper?.getViewById<T>(viewId)
    }

    internal class AlertParams(val mContext: Context, val mThemeResId: Int) {
        // Out of the area whether to click cancel
        var mCancelable = true
        var mOnCancelListener: DialogInterface.OnCancelListener? = null
        var mOnDismissListener: DialogInterface.OnDismissListener? = null
        var mOnKeyListener: DialogInterface.OnKeyListener? = null

        //The container is used to hold the text of the Settings
        var texts: SparseArray<CharSequence?>
        var imageViews: ArrayMap<Int, Int>
        var listeners: SparseArray<View.OnClickListener?>
        var contentViewId = 0
        var contentView: View? = null
        var customDialog: ChatUIKitAlertDialog? = null
        var mWidth = ViewGroup.LayoutParams.WRAP_CONTENT
        var mHeight = ViewGroup.LayoutParams.WRAP_CONTENT
        var mAnimation = 0
        var mGravity = Gravity.CENTER

        init {
            texts = SparseArray<CharSequence?>()
            listeners = SparseArray<View.OnClickListener?>()
            imageViews = ArrayMap()
        }

        fun apply(mAlert: AlertController) {
            var viewHelper: DialogViewHelper? = null
            if (contentView != null) {
                viewHelper = DialogViewHelper(mContext, contentView)
            }
            if (contentViewId != 0) {
                viewHelper = DialogViewHelper(mContext, contentViewId)
            }
            if (customDialog != null) {
                viewHelper = DialogViewHelper(mContext)
            }
            requireNotNull(viewHelper) { "Not set layout" }
            if (customDialog == null) {
                mAlert.dialog.setContentView(viewHelper.contentView!!)
            }
            // set view helper
            mAlert.setViewHelper(viewHelper)
            // set text
            val textsSize = texts.size()
            for (i in 0 until textsSize) {
                mAlert.setText(texts.keyAt(i), texts.valueAt(i))
            }
            // set image
            val imageViewsCount: Int = imageViews.size
            for (i in 0 until imageViewsCount) {
                mAlert.setImageView(imageViews.keyAt(i), imageViews.valueAt(i))
            }
            // set click listener
            val listenerSize = listeners.size()
            for (i in 0 until listenerSize) {
                mAlert.setOnClickListener(listeners.keyAt(i), listeners.valueAt(i))
            }
            // set layout params
            val window = mAlert.window
            val params = window.attributes
            // set width and height
            if (mWidth != 0) {
                params.width = mWidth
                params.height = mHeight
            }
            window.attributes = params
            // set gravity
            window.setGravity(mGravity)
            // set pop_up animation
            if (mAnimation != 0) {
                window.setWindowAnimations(mAnimation)
            }
        }
    }

    private fun setImageView(viewId: Int, resId: Int) {
        viewHelper?.setImageView(viewId, resId)
    }

    fun setOnClickListener(viewId: Int, onClickListener: View.OnClickListener?) {
        viewHelper?.setOnClickListener(viewId, onClickListener)
    }

    private fun setViewHelper(viewHelper: DialogViewHelper) {
        this.viewHelper = viewHelper
    }

    fun setText(viewId: Int, text: CharSequence?) {
        viewHelper?.setText(viewId, text)
    }
}