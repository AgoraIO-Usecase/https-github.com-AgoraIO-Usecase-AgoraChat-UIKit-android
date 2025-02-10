package io.agora.chat.uikit.common.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import io.agora.chat.uikit.R

/**
 * Sample:
 * val dialog = ChatUIKitAlertDialog.Builder(this)
 * .setContentView(R.layout.dialog_reset_pwd)
 * .setText(R.id.tv_message, msg)
 * .setLayoutParams(UIUtils.dp2px(this, 256), ViewGroup.LayoutParams.WRAP_CONTENT)
 * .show()
 * dialog.setOnClickListener(R.id.btn_input_again, object : View.OnClickListener {
 * override fun onClick(v: View?) {
 * dialog.dismiss()
 * }
 * })
 */
open class ChatUIKitAlertDialog @JvmOverloads constructor(context: Context, themeResId: Int = 0) :
    Dialog(context, themeResId) {
    //Controller
    private val mAlert: AlertController

    init {
        mAlert = AlertController(getContext(), this, window!!)
    }

    fun setText(viewId: Int, text: String?) {
        mAlert.setText(viewId, text)
    }

    fun setOnClickListener(viewId: Int, onClickListener: View.OnClickListener?) {
        mAlert.setOnClickListener(viewId, onClickListener)
    }

    fun <T : View?> getViewById(viewId: Int): T? {
        return mAlert.getViewById<T>(viewId)
    }

    /**
     * builder
     */
    class Builder<T : ChatUIKitAlertDialog?> @JvmOverloads constructor(
        context: Context?,
        themeResId: Int = R.style.dialog
    ) {
        private val P: AlertController.AlertParams
        private var dialog: T? = null
        private var customDialog: T? = null

        init {
            P = AlertController.AlertParams(
                context!!, themeResId
            )
        }

        /**
         * Set the layout as a resource ID
         *
         * @param contentViewId
         * @return
         */
        fun setContentView(@LayoutRes contentViewId: Int): Builder<T> {
            P.contentViewId = contentViewId
            return this
        }

        /**
         * Set the layout as a View
         *
         * @param contentView
         * @return
         */
        fun setContentView(contentView: View?): Builder<T> {
            P.contentView = contentView
            return this
        }

        /**
         * Sets the text content based on the control ID
         *
         * @param viewId
         * @param text
         * @return
         */
        fun setText(@IdRes viewId: Int, text: String?): Builder<T> {
            P.texts.put(viewId, text)
            return this
        }

        fun setText(viewId: Int, text: CharSequence?): Builder<T> {
            P.texts.put(viewId, text)
            return this
        }

        /**
         * Set the image based on the control ID
         *
         * @param viewId
         * @param imageId
         * @return
         */
        fun setImageview(viewId: Int, imageId: Int): Builder<T> {
            P.imageViews[viewId] = imageId
            return this
        }

        /**
         * Set the listener based on the control ID
         *
         * @param viewId
         * @param listener
         * @return
         */
        fun setOnClickListener(viewId: Int, listener: View.OnClickListener?): Builder<T> {
            P.listeners.put(viewId, listener)
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder<T> {
            P.mCancelable = cancelable
            return this
        }

        fun setOnCancelListener(onCancelListener: DialogInterface.OnCancelListener?): Builder<T> {
            P.mOnCancelListener = onCancelListener
            return this
        }

        fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener?): Builder<T> {
            P.mOnDismissListener = onDismissListener
            return this
        }

        fun setOnKeyListener(onKeyListener: DialogInterface.OnKeyListener?): Builder<T> {
            P.mOnKeyListener = onKeyListener
            return this
        }

        fun setCustomDialog(dialog: T): Builder<T> {
            customDialog = dialog
            P.customDialog = dialog
            return this
        }

        fun create(): T {
            // We can't use Dialog's 3-arg constructor with the createThemeContextWrapper param,
            // so we always have to re-set the theme
            val dialog = if (customDialog != null) customDialog!! else ChatUIKitAlertDialog(
                P.mContext,
                P.mThemeResId
            ) as T
            P.apply(dialog!!.mAlert)
            dialog.setCancelable(P.mCancelable)
            if (P.mCancelable) {
                dialog.setCanceledOnTouchOutside(true)
            }
            dialog.setOnCancelListener(P.mOnCancelListener)
            dialog.setOnDismissListener(P.mOnDismissListener)
            if (P.mOnKeyListener != null) {
                dialog.setOnKeyListener(P.mOnKeyListener)
            }
            return dialog
        }

        fun show(): T? {
            dialog = create()
            dialog!!.show()
            return dialog
        }

        fun dismiss() {
            if (dialog != null) {
                dialog!!.dismiss()
            }
        }

        /**
         * Set up the full width
         *
         * @return
         */
        fun setFullWidth(): Builder<T> {
            P.mWidth = ViewGroup.LayoutParams.MATCH_PARENT
            return this
        }

        /**
         * Set the bottom pop-up animation
         *
         * @return
         */
        fun setFromBottomAnimation(): Builder<T> {
            P.mAnimation = R.style.dialog_from_bottom_anim
            return this
        }

        /**
         * Set gravity
         *
         * @param gravity
         * @return
         */
        fun setGravity(gravity: Int): Builder<T> {
            P.mGravity = gravity
            return this
        }

        /**
         * Set animation
         *
         * @param animation
         * @return
         */
        fun setAnimation(animation: Int): Builder<T> {
            P.mAnimation = animation
            return this
        }

        /**
         * Set LayoutParams
         *
         * @param width
         * @param height
         * @return
         */
        fun setLayoutParams(width: Int, height: Int): Builder<T> {
            P.mWidth = width
            P.mHeight = height
            return this
        }
    }
}