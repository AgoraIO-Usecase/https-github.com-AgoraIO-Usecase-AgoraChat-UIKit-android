package io.agora.chat.uikit.feature.chat.interfaces

import io.agora.chat.uikit.common.ChatMessage

interface OnItemBubbleClickListener {
    fun onBubbleClick(message: ChatMessage?)
}