package io.agora.chat.uikit.feature.thread.interfaces

import android.view.View
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatThread

interface OnMessageChatThreadClickListener {
    fun onThreadViewItemClick(view:View, thread:ChatThread?,topicMsg:ChatMessage)
}