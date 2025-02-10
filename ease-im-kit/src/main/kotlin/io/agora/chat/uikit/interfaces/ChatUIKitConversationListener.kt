package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.common.ChatConversationListener

open class ChatUIKitConversationListener: ChatConversationListener {
    override fun onConversationUpdate() {}

    override fun onConversationRead(from: String?, to: String?) {}
}