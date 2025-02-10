package io.agora.chat.uikit.feature.chat.interfaces

import android.view.View

interface IChatNotification {

    /**
     * Set custom notification view
     * @param view
     */
    fun setCustomNotificationView(view: View?)

    /**
     * Get notification view
     * @return  notification view
     */
    fun getNotificationView(): View?

    /**
     * Whether to show the notification view
     * @param show
     */
    fun showNotificationView(show: Boolean)

    /**
     * Only can be set the flowing values:
     * [android.view.Gravity.LEFT], [android.view.Gravity.RIGHT], [android.view.Gravity.CENTER], [android.view.Gravity.START], [android.view.Gravity.END]
     */
    fun setGravity(gravity: Int)

    /**
     * Set notification click listener
     * @param listener
     */
    fun setOnNotificationClickListener(listener: View.OnClickListener?)

}