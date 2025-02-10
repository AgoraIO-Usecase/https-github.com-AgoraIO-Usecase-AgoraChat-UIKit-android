package io.agora.chat.uikit.feature.conversation.interfaces

import io.agora.chat.uikit.model.ChatUIKitConversation

interface OnLoadConversationListener {
    fun loadConversationListSuccess(userList: List<ChatUIKitConversation>){}

    fun loadConversationListFail(code: Int, error: String){}
}
