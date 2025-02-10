package io.agora.chat.uikit.viewmodel.search

import androidx.lifecycle.viewModelScope
import io.agora.chat.uikit.common.ChatSearchDirection
import io.agora.chat.uikit.common.ChatSearchScope
import io.agora.chat.uikit.viewmodel.ChatUIKitBaseViewModel
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.feature.search.interfaces.IUIKitSearchResultView
import io.agora.chat.uikit.repository.ChatUIKitSearchRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class ChatUIKitSearchViewModel(
    private val stopTimeoutMillis: Long = 5000
): ChatUIKitBaseViewModel<IUIKitSearchResultView>(),IUIKitSearchRequest{

    private val repository: ChatUIKitSearchRepository = ChatUIKitSearchRepository()

    companion object{
        const val maxSearchCount:Int = 200
    }

    override fun searchUser(query: String) {
        viewModelScope.launch {
            flow {
                emit(repository.searchUser(query))
            }
            .catchChatException { }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
            .collect {
                if (it != null) {
                    view?.searchSuccess(it)
                }
            }
        }
    }

    override fun searchBlockUser(query: String) {
        viewModelScope.launch {
            flow {
                emit(repository.searchBlockUser(query))
            }
                .catchChatException { }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    if (it != null) {
                        view?.searchBlockUserSuccess(it)
                    }
                }
        }
    }

    override fun searchConversation(query: String) {
        viewModelScope.launch {
            flow {
                emit(repository.searchConversation(query))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
            .collect {
                if (it != null) {
                    view?.searchSuccess(it)
                }
            }
        }
    }

    override fun searchMessage(
        keywords:String,
        conversationId:String,
        timeStamp:Long,
        from:String?,
        direction:ChatSearchDirection,
        chatScope:ChatSearchScope
    ) {
        viewModelScope.launch {
            flow {
                if (conversationId.isEmpty()){
                    emit(repository.searchMessage(keywords,timeStamp,maxSearchCount,from,direction,chatScope))
                }else{
                    emit(repository.searchMessageByConversation(conversationId,keywords,timeStamp,maxSearchCount,from,direction,ChatSearchScope.CONTENT))
                }
            }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    if (it != null) {
                        view?.searchSuccess(it)
                    }
                }
        }
    }


}