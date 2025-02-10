package io.agora.chat.uikit.viewmodel.chathistory

import androidx.lifecycle.viewModelScope
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatValueCallback
import io.agora.chat.uikit.feature.chat.interfaces.IChatHistoryResultView
import io.agora.chat.uikit.repository.ChatUIKitManagerRepository
import io.agora.chat.uikit.viewmodel.ChatUIKitBaseViewModel
import kotlinx.coroutines.launch

class ChatUIKitHistoryViewModel: ChatUIKitBaseViewModel<IChatHistoryResultView>(), IChatHistoryRequest {

    private val chatRepository by lazy { ChatUIKitManagerRepository() }

    override fun downloadCombineMessage(message: ChatMessage?) {
        viewModelScope.launch {
            chatRepository.downloadCombinedMessageAttachment(message, object : ChatValueCallback<List<ChatMessage>> {
                override fun onSuccess(value: List<ChatMessage>?) {
                    if (value.isNullOrEmpty()) {
                        view?.downloadCombinedMessagesFail(ChatError.GENERAL_ERROR, "No message found.")
                        return
                    }
                    view?.downloadCombinedMessagesSuccess(value)
                }

                override fun onError(error: Int, errorMsg: String?) {
                    view?.downloadCombinedMessagesFail(error, errorMsg)
                }
            })
        }
    }
}