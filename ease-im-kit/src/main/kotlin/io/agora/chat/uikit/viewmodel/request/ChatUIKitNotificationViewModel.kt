package io.agora.chat.uikit.viewmodel.request

import android.content.Context
import androidx.lifecycle.viewModelScope
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.viewmodel.ChatUIKitBaseViewModel
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.common.extensions.collectWithCheckErrorCode
import io.agora.chat.uikit.feature.invitation.interfaces.IUIKitNotificationResultView
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.repository.ChatUIKitNotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatUIKitNotificationViewModel (
    private val stopTimeoutMillis: Long = 5000
): ChatUIKitBaseViewModel<IUIKitNotificationResultView>(),INotificationRequest{

    private val repository: ChatUIKitNotificationRepository = ChatUIKitNotificationRepository()

    override fun loadMoreMessage(startMsgId:String?,limit:Int) {
        viewModelScope.launch {
            flow {
                emit(repository.loadMoreMessage(startMsgId,limit))
            }
            .catchChatException { e ->
                view?.loadMoreMessageFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
            .collect {
                if (it != null) {
                    view?.loadMoreMessageSuccess(it)
                }
            }
        }
    }

    override fun loadLocalData() {
        viewModelScope.launch {
            flow {
                emit(repository.getAllMessage())
            }
                .catchChatException { e ->
                    view?.getLocalMessageFail(e.errorCode, e.description)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    if (it != null) {
                        view?.getLocalMessageSuccess(it)
                    }
                }
        }
    }

    override fun fetchProfileInfo(members: List<String>?) {
        // 有本地缓存的ids列表
        val filterHasDataList = members?.filter {
            ChatUIKitClient.getUserProvider()?.getUser(it) != null
        }?.map { id->
            ChatUIKitClient.getUserProvider()?.getUser(id)?:ChatUIKitProfile(id)
        }

        // 没有本地缓存的ids列表
        val filterHasNoDataList = members?.filter {
            ChatUIKitClient.getCache().getUser(it) == null
        }
        if (filterHasDataList.isNullOrEmpty() && filterHasNoDataList.isNullOrEmpty()) return

        // 如果需要从服务端请求的ids列表为空 则返回本地有缓存数据的ids列表
        if (filterHasNoDataList.isNullOrEmpty()){
            filterHasDataList?.let { hasDataList ->
                if (hasDataList.isNotEmpty()){
                    val hasDataMap = hasDataList.associateBy { profile->
                        profile.id
                    }
                    if (hasDataMap.isNotEmpty()){
                        view?.fetchProfileSuccess(hasDataMap)
                    }
                    return
                }
            }
        }

        viewModelScope.launch {
            flow {
                emit(repository.fetchProfileInfo(filterHasNoDataList))
            }
            .catchChatException { e ->
                view?.fetchProfileFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null
            ).collect {
                if (it != null) {
                    val map = it.associateBy { profile->
                        ChatUIKitClient.getCache().insertUser(profile)
                        profile.id
                    }
                    filterHasDataList?.let { hasDataList ->
                        if (hasDataList.isNotEmpty()){
                            val hasDataMap = hasDataList.associateBy { profile->
                                profile.id
                            }
                            map.toMutableMap().putAll(hasDataMap)
                        }
                    }
                    view?.fetchProfileSuccess(map)
                }
            }
        }
    }

    override fun agreeInvite(context: Context,msg: ChatMessage) {
        viewModelScope.launch {
            flow {
                emit(repository.agreeInvite(context, msg))
            }
            .catchChatException { e ->
                view?.agreeInviteFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                var userId = ""
                if (msg.ext().containsKey(ChatUIKitConstant.SYSTEM_MESSAGE_FROM)){
                    userId = msg.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_FROM,"")
                }
                view?.agreeInviteSuccess(userId,msg)
            }
        }
    }

    override fun refuseInvite(context: Context,msg: ChatMessage) {
        viewModelScope.launch {
            flow {
                emit(repository.refuseInvite(context, msg))
            }
            .catchChatException { e ->
                view?.refuseInviteFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.refuseInviteSuccess()
            }
        }
    }

    override fun removeInviteMsg(msg: ChatMessage) {
        viewModelScope.launch {
            flow {
                emit(repository.removeInviteMsg(msg))
            }
            .catchChatException { e ->
                ChatLog.e("ChatUIKitNotificationViewModel","removeInviteMsg error")
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), true)
            .collectWithCheckErrorCode {
                ChatLog.d("ChatUIKitNotificationViewModel","removeInviteMsg suc $it")
            }
        }
    }

}