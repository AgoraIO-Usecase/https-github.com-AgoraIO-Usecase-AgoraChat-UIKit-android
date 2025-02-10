package io.agora.chat.uikit.common.impl

import io.agora.chat.uikit.interfaces.OnMenuItemClickListener
import io.agora.chat.uikit.model.ChatUIKitMenuItem

class OnMenuItemClickListenerImpl(
    private val onMenuItemClick: (item: ChatUIKitMenuItem?, position: Int) -> Boolean
): OnMenuItemClickListener {
    override fun onMenuItemClick(item: ChatUIKitMenuItem?, position: Int): Boolean {
        return onMenuItemClick.invoke(item, position)
    }
}