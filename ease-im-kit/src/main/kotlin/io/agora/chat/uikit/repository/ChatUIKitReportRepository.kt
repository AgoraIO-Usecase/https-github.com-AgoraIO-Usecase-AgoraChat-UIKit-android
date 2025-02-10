package io.agora.chat.uikit.repository

import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatManager
import io.agora.chat.uikit.common.suspends.reportChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatUIKitReportRepository(
    private val chatManager: ChatManager = ChatClient.getInstance().chatManager(),
) {
    suspend fun reportMessage(tag:String,msgId: String,reason:String?=""):Int =
        withContext(Dispatchers.IO) {
            chatManager.reportChatMessage(msgId,tag,reason)
        }

}