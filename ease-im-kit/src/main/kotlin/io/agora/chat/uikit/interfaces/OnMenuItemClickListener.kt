package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.model.ChatUIKitMenuItem

/**
 * The menu item click listener.
 */
interface OnMenuItemClickListener {
    /**
     * Callback when menu item is clicked.
     * @param item
     * @param position
     */
    fun onMenuItemClick(item: ChatUIKitMenuItem?, position: Int): Boolean
}