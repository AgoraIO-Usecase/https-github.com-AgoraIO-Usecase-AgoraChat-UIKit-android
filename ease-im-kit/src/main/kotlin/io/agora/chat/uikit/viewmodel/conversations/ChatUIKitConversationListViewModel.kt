package io.agora.chat.uikit.viewmodel.conversations

import androidx.lifecycle.viewModelScope
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.viewmodel.ChatUIKitBaseViewModel
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatManager
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.common.extensions.collectWithCheckErrorCode
import io.agora.chat.uikit.feature.conversation.interfaces.IUIKitConvListResultView
import io.agora.chat.uikit.model.ChatUIKitConversation
import io.agora.chat.uikit.repository.ChatUIKitConversationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class ChatUIKitConversationListViewModel(
    private val chatManager: ChatManager = ChatClient.getInstance().chatManager(),
    private val stopTimeoutMillis: Long = 5000
): ChatUIKitBaseViewModel<IUIKitConvListResultView>(), IConversationListRequest {

    private val repository: ChatUIKitConversationRepository = ChatUIKitConversationRepository(chatManager)

    override fun loadData() {
        viewModelScope.launch {
            flow {
                try {
                    emit(repository.loadData())
                } catch (e:ChatException){
                    emit(Result.failure<ChatException>(e))
                    inMainScope{
                        view?.loadConversationListFail(e.errorCode, e.description)
                    }
                }
            }.flatMapConcat {
                flow {
                    emit(repository.loadLocalConversation())
                }
            }
            .catchChatException { e ->
                view?.loadConversationListFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
            .collect {
                if (it != null) {
                    view?.loadConversationListSuccess(it)
                }
            }
        }
    }

    override fun sortConversationList(conversations: List<ChatUIKitConversation>) {
        // If you need to sort the conversation list, you can override this method.
        // Then call view?.loadConversationListSuccess(conversations) to update the UI.
    }

    override fun makeConversionRead(position: Int, conversation: ChatUIKitConversation) {
        viewModelScope.launch {
            flow {
                emit(repository.makeConversionRead(conversation))
            }
            .catchChatException { e ->
                view?.loadConversationListFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.makeConversionReadSuccess(position, conversation)
            }
        }
    }

    override fun makeSilentForConversation(position: Int, conversation: ChatUIKitConversation) {
        viewModelScope.launch {
            flow {
                emit(repository.makeSilentForConversation(conversation))
            }
            .catchChatException { e ->
                view?.makeSilentForConversationFail(conversation, e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
            .collect {
                if (it != null) {
                    ChatLog.e("conversation", "makeSilentForConversationSuccess")
                    view?.makeSilentForConversationSuccess(position, conversation)
                }
            }
        }
    }

    override fun cancelSilentForConversation(position: Int, conversation: ChatUIKitConversation) {
        viewModelScope.launch {
            flow {
                emit(repository.cancelSilentForConversation(conversation))
            }
            .catchChatException { e ->
                view?.cancelSilentForConversationFail(conversation, e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.cancelSilentForConversationSuccess(position, conversation)
            }
        }
    }

    override fun pinConversation(position: Int, conversation: ChatUIKitConversation) {
        viewModelScope.launch {
            flow {
                emit(repository.pinConversation(conversation))
            }
            .catchChatException { e ->
                view?.pinConversationFail(conversation, e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.pinConversationSuccess(position, conversation)
            }
        }
    }

    override fun unpinConversation(position: Int, conversation: ChatUIKitConversation) {
        viewModelScope.launch {
            flow {
                emit(repository.unpinConversation(conversation))
            }
            .catchChatException { e ->
                view?.unpinConversationFail(conversation, e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.unpinConversationSuccess(position, conversation)
            }
        }
    }

    override fun deleteConversation(position: Int, conversation: ChatUIKitConversation) {
        viewModelScope.launch {
            flow {
                emit(repository.deleteConversation(conversation))
            }
            .catchChatException { e ->
                view?.deleteConversationFail(conversation, e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.deleteConversationSuccess(position, conversation)
            }
        }
    }

    override fun fetchConvGroupInfo(conversationList: List<ChatUIKitConversation>) {
        viewModelScope.launch {
            flow {
                emit(repository.fetchConvGroupInfo(conversationList))
            }
            .catchChatException {  }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
            .collect {
                it?.forEach { item ->
                    ChatUIKitClient.getCache().insertGroup(item.id, item)
                }
                view?.fetchConversationInfoByUserSuccess(it)
            }
        }
    }

    override fun fetchConvUserInfo(conversationList: List<ChatUIKitConversation>) {
        viewModelScope.launch {
            flow {
                emit(repository.fetchConvUserInfo(conversationList))
            }
                .catchChatException {  }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    it?.forEach { item ->
                        ChatUIKitClient.getCache().insertUser(item)
                    }
                    view?.fetchConversationInfoByUserSuccess(it)
                }
        }
    }

    private fun inMainScope(scope: ()->Unit) {
        viewModelScope.launch(context = Dispatchers.Main) {
            scope()
        }
    }

}