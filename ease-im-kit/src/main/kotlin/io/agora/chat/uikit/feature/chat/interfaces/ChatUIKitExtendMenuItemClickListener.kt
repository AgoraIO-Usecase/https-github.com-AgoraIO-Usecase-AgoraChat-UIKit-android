package io.agora.chat.uikit.feature.chat.interfaces

import android.view.View

/**
 * extend menu item click listener
 */
interface ChatUIKitExtendMenuItemClickListener {
    /**
     * item click
     * @param itemId
     * @param view
     */
    fun onChatExtendMenuItemClick(itemId: Int, view: View?)
}