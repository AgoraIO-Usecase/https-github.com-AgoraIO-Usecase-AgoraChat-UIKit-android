package io.agora.chat.uikit.feature.thread.interfaces

import android.view.View
import io.agora.chat.uikit.common.ChatThread

interface OnChatThreadListItemClickListener {
    fun onChatThreadItemClick(view: View?,thread: ChatThread)
}