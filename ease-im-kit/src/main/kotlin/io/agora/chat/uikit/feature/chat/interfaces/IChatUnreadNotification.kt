package io.agora.chat.uikit.feature.chat.interfaces

import android.view.View

interface IChatUnreadNotification {

    /**
     * Update unread message count.
     * @param unreadCount
     */
    fun updateUnreadCount(unreadCount: Int)

    /**
     * Set notification click listener
     * @param listener
     */
    fun setOnNotificationClickListener(listener: View.OnClickListener?)

}