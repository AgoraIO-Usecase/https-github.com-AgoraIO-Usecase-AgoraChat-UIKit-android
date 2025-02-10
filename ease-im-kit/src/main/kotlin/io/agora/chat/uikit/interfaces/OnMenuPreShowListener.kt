package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.menu.ChatUIKitMenuHelper

interface OnMenuPreShowListener {
    /**
     * Monitoring before popupMenu display, you can set PopupMenu
     * @param menuHelper [ChatUIKitMenuHelper]
     * @param position item position
     */
    fun onMenuPreShow(menuHelper: ChatUIKitMenuHelper?, position: Int)
}