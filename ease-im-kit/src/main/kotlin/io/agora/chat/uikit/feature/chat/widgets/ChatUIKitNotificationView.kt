package io.agora.chat.uikit.feature.chat.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import io.agora.chat.uikit.feature.chat.interfaces.IChatNotification

class ChatUIKitNotificationView @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr), IChatNotification {
    private var notificationView: View? = null
    private var listener: OnClickListener? = null

    init {
        initNotification()
    }

    private fun initNotification() {
        showNotificationView()
    }

    override fun setCustomNotificationView(view: View?) {
        this.notificationView = view
    }

    override fun getNotificationView(): View? {
        return notificationView
    }

    override fun showNotificationView(show: Boolean) {
        if (show) {
            showNotificationView()
        } else {
            notificationView?.visibility = View.GONE
        }
    }

    override fun setGravity(gravity: Int) {
        if (gravity == Gravity.LEFT
            || gravity == Gravity.RIGHT
            || gravity == Gravity.CENTER
            || gravity == Gravity.START
            || gravity == Gravity.END) {

            if (parent is RelativeLayout) {
                (layoutParams as RelativeLayout.LayoutParams).apply {
                    if (gravity == Gravity.LEFT) {
                        addRule(RelativeLayout.ALIGN_PARENT_LEFT)
                    } else if (gravity == Gravity.START) {
                        addRule(RelativeLayout.ALIGN_PARENT_START)
                    } else if (gravity == Gravity.RIGHT) {
                        addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    } else if (gravity == Gravity.END) {
                        addRule(RelativeLayout.ALIGN_PARENT_END)
                    } else {
                        addRule(RelativeLayout.CENTER_HORIZONTAL)
                    }
                }
            }
        }
    }

    private fun showNotificationView() {
        if (notificationView == null) {
            notificationView = ChatUIKitUnreadNotificationView(context)
        }
        notificationView?.visibility = View.VISIBLE
        notificationView?.let {
            this.removeAllViews()
            this.addView(it)
            if (it is ChatUIKitUnreadNotificationView) {
                it.setOnNotificationClickListener(listener)
            }
        }
    }

    override fun setOnNotificationClickListener(listener: OnClickListener?) {
        this.listener = listener
        notificationView?.let {
            if (it is ChatUIKitUnreadNotificationView) {
                it.setOnNotificationClickListener(listener)
            }
        }
    }

}