package io.agora.chat.uikit.demo.base

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import io.agora.chat.uikit.demo.R

class ChatUIKitProgressDialog protected constructor(context: Context, themeResId: Int = 0) :
    ProgressDialog(context, themeResId) {
    class Builder(private val mContext: Context) {
        private var message: String? = null
        private var cancelable = false
        private var canceledOnTouchOutside = false
        private var cancelListener: DialogInterface.OnCancelListener? = null
        fun setLoadingMessage(@StringRes message: Int): Builder {
            this.message = mContext.getString(message)
            return this
        }

        fun setLoadingMessage(message: String?): Builder {
            this.message = message
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            this.cancelable = cancelable
            return this
        }

        fun setCanceledOnTouchOutside(cancel: Boolean): Builder {
            canceledOnTouchOutside = cancel
            return this
        }

        fun setOnCancelListener(cancelListener: DialogInterface.OnCancelListener?): Builder {
            this.cancelListener = cancelListener
            return this
        }

        fun build(): ChatUIKitProgressDialog {
            val dialog = ChatUIKitProgressDialog(mContext, R.style.Dialog_Light)
            dialog.setCancelable(cancelable)
            dialog.setCanceledOnTouchOutside(canceledOnTouchOutside)
            dialog.setMessage(message)
            dialog.setOnCancelListener(cancelListener)
            return dialog
        }

        fun show(): ChatUIKitProgressDialog {
            val dialog = build()
            dialog.show()
            return dialog
        }
    }
}