package io.agora.chat.uikit.feature.thread.interfaces

import io.agora.chat.uikit.common.ChatMessage

interface IMessageThread {
    fun setupWithMessage(message:ChatMessage)

    fun showThread()

    fun setThreadEventListener(listener: OnMessageChatThreadClickListener?)
}