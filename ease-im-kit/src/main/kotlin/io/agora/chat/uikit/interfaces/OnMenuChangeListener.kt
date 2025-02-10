package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.menu.chat.ChatUIKitChatMenuHelper
import io.agora.chat.uikit.model.ChatUIKitMenuItem

interface OnMenuChangeListener {
    /**
     * Before showing the Menu
     * @param helper
     * @param message
     */
    fun onPreMenu(helper: ChatUIKitChatMenuHelper?, message: ChatMessage?)

    /**
     * Item click
     * @param item
     * @param message
     */
    fun onMenuItemClick(item: ChatUIKitMenuItem?, message: ChatMessage?): Boolean

    /**
     * Dismiss event
     */
    fun onDismiss() {}
}