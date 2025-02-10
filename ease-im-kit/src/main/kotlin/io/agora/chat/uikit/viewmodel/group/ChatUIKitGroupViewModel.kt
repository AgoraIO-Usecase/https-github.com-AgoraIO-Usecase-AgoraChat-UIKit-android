package io.agora.chat.uikit.viewmodel.group

import androidx.lifecycle.viewModelScope
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.viewmodel.ChatUIKitBaseViewModel
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatGroupManager
import io.agora.chat.uikit.common.ChatGroupOptions
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.common.extensions.collectWithCheckErrorCode
import io.agora.chat.uikit.common.extensions.parse
import io.agora.chat.uikit.common.helper.ContactSortedHelper
import io.agora.chat.uikit.feature.group.interfaces.IUIKitGroupResultView
import io.agora.chat.uikit.model.setUserInitialLetter
import io.agora.chat.uikit.repository.ChatUIKitThreadRepository
import io.agora.chat.uikit.repository.ChatUIKitConversationRepository
import io.agora.chat.uikit.repository.ChatUIKitGroupRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class ChatUIKitGroupViewModel(
    private val groupManager: ChatGroupManager = ChatClient.getInstance().groupManager(),
    private val stopTimeoutMillis: Long = 5000
): ChatUIKitBaseViewModel<IUIKitGroupResultView>(),IGroupRequest{

    companion object{
        const val pageSize:Int = 20
        const val needMemberCount:Boolean = false
        const val needRole:Boolean = false
    }

    private val repository:ChatUIKitGroupRepository = ChatUIKitGroupRepository(groupManager)
    private val convRepository: ChatUIKitConversationRepository = ChatUIKitConversationRepository()
    private val threadRepository by lazy { ChatUIKitThreadRepository() }

    override fun loadJoinedGroupData(page:Int) {
        viewModelScope.launch {
            flow {
                emit(repository.loadJoinedGroupData(page,pageSize,needMemberCount,needRole))
            }
            .catchChatException { e ->
                view?.loadGroupListFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
            .collect {
                if (it != null) {
                    view?.loadGroupListSuccess(it.toMutableList())
                }
            }
        }
    }

    override fun loadLocalJoinedGroupData() {
        viewModelScope.launch {
            flow {
                emit(repository.loadLocalJoinedGroupData())
            }
                .catchChatException { e ->
                    view?.loadLocalGroupListFail(e.errorCode, e.description)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    if (it != null) {
                        view?.loadLocalGroupListSuccess(it.toMutableList())
                    }
                }
        }
    }

    override fun createGroup(
        groupName: String,
        desc: String,
        members: MutableList<String>,
        reason: String,
        options: ChatGroupOptions
    ) {
        viewModelScope.launch {
            flow {
                emit(repository.createGroup(groupName, desc, members, reason, options))
            }
            .catchChatException { e ->
                view?.createGroupFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
            .collect {
                it?.let { it1 -> view?.createGroupSuccess(it1) }
            }
        }
    }

    override fun fetchGroupMemberFromService(groupId: String) {
        viewModelScope.launch {
            flow {
                emit(repository.fetGroupMemberFromService(groupId))
            }
            .catchChatException { e ->
                view?.fetchGroupMemberFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), mutableListOf())
            .collect {
                val data = it.map { user->
                    user.setUserInitialLetter()
                    user
                }
                val sortedList = ContactSortedHelper.sortedList(data)
                view?.fetchGroupMemberSuccess(sortedList)
            }
        }
    }

    override fun loadLocalMember(groupId: String) {
        viewModelScope.launch {
            flow {
                emit(repository.loadLocalMember(groupId))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), mutableListOf())
            .collect {
                it.let { it1 -> view?.loadLocalMemberSuccess(it1) }
            }
        }
    }

    override fun fetchGroupDetails(groupId: String) {
        viewModelScope.launch {
            flow {
                emit(repository.fetchGroupDetails(groupId))
            }
            .catchChatException { e ->
                view?.fetchGroupDetailFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
            .collect {
                it?.let { it1 -> view?.fetchGroupDetailSuccess(it1) }
            }
        }
    }

    override fun addGroupMember(groupId: String, members: MutableList<String>) {
        viewModelScope.launch {
            flow {
                emit(repository.addGroupMember(groupId,members))
            }
            .catchChatException { e ->
                view?.addGroupMemberFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.addGroupMemberSuccess()
            }
        }
    }

    override fun removeGroupMember(groupId: String, members: MutableList<String>) {
        viewModelScope.launch {
            flow {
                emit(repository.removeGroupMember(groupId,members))
            }
            .catchChatException { e ->
                view?.removeChatGroupMemberFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.removeChatGroupMemberSuccess()
            }
        }
    }

    override fun leaveChatGroup(groupId: String) {
        viewModelScope.launch {
            flow {
                emit(repository.leaveChatGroup(groupId))
            }
            .catchChatException { e ->
                view?.leaveChatGroupFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.leaveChatGroupSuccess()
            }
        }
    }

    override fun destroyChatGroup(groupId: String) {
        viewModelScope.launch {
            flow {
                emit(repository.destroyChatGroup(groupId))
            }
            .catchChatException { e ->
                view?.destroyChatGroupFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.destroyChatGroupSuccess()
            }
        }
    }

    override fun changeChatGroupName(groupId: String, newName: String) {
        viewModelScope.launch {
            flow {
                emit(repository.changeChatGroupName(groupId, newName))
            }
            .catchChatException { e ->
                view?.changeChatGroupNameFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.changeChatGroupNameSuccess()
            }
        }
    }

    override fun changeChatGroupDescription(groupId: String, description: String) {
        viewModelScope.launch {
            flow {
                emit(repository.changeChatGroupDescription(groupId, description))
            }
            .catchChatException { e ->
                view?.changeChatGroupDescriptionFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
            .collectWithCheckErrorCode {
                view?.changeChatGroupDescriptionSuccess()
            }
        }
    }

    override fun changeChatGroupOwner(groupId: String, newOwner: String) {
        viewModelScope.launch {
            flow {
                emit(repository.changeChatGroupOwner(groupId, newOwner))
            }
            .catchChatException { e ->
                view?.changeChatGroupOwnerFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.EM_NO_ERROR)
            .collectWithCheckErrorCode {
                view?.changeChatGroupOwnerSuccess()
            }
        }
    }

    override fun fetchGroupMemberAllAttributes(
        groupId: String,
        userList: List<String>,
        keyList: List<String>,
    ) {
        viewModelScope.launch {
            flow {
                emit(repository.fetchMemberAllAttributes(groupId,userList,keyList))
            }
            .catchChatException { e ->
                view?.getGroupMemberAllAttributesFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
            .collect {
                if (it != null) {
                    view?.getGroupMemberAllAttributesSuccess(userList, it)
                }
            }
        }
    }

    override fun setGroupMemberAttributes(groupId: String, userId: String,attribute:MutableMap<String,String>) {
        viewModelScope.launch {
            flow {
                emit(repository.setGroupMemberAttributes(groupId, userId,attribute))
            }
            .catchChatException { e ->
                view?.setGroupMemberAttributesFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR
            ).collectWithCheckErrorCode {
                view?.setGroupMemberAttributesSuccess()
            }
        }
    }

    override fun fetchMemberInfo(groupId: String?, members: List<String>?) {
        val filterList = members?.filter {
            ChatUIKitClient.getCache().getUser(it) == null
        }
        if (filterList.isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            flow {
                emit(repository.fetchMemberInfo(groupId, members))
            }
            .catchChatException { e ->
                view?.fetchMemberInfoFail(e.errorCode, e.description)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null
            ).collect {
                if (it != null) {
                    val map = it.associateBy { profile->
                        ChatUIKitClient.getCache().insertUser(profile)
                        profile.id
                    }
                    view?.fetchMemberInfoSuccess(map)
                }
            }
        }
    }


    override fun clearConversationMessage(conversationId: String?) {
        viewModelScope.launch {
            ChatClient.getInstance().chatManager().getConversation(conversationId,ChatConversationType.GroupChat)?.parse()?.let {
                flow {
                    emit(convRepository.clearConversationMessage(it))
                }
                    .catchChatException { e ->
                        view?.clearConversationByGroupFail(e.errorCode,e.description)
                    }
                    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.GENERAL_ERROR)
                    .collectWithCheckErrorCode {
                        view?.clearConversationByGroupSuccess(conversationId)
                    }
            } ?: view?.clearConversationByGroupFail(ChatError.INVALID_PARAM,"conversation is null")
        }
    }

    override fun makeSilentModeForConversation(
        conversationId: String,
        conversationType:ChatConversationType
    ) {
        viewModelScope.launch {
            flow {
                emit(convRepository.makeSilentForConversation(conversationId,conversationType))
            }
                .catchChatException { e ->
                    view?.makeSilentForGroupFail(e.errorCode, e.description)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    if (it != null) {
                        ChatLog.e("conversation", "makeSilentForGroupSuccess")
                        view?.makeSilentForGroupSuccess(it)
                    }
                }
        }

    }

    override fun cancelSilentForConversation(
        conversationId: String,
        conversationType: ChatConversationType
    ) {
        viewModelScope.launch {
            flow {
                emit(convRepository.cancelSilentForConversation(conversationId,conversationType))
            }
                .catchChatException { e ->
                    view?.cancelSilentForGroupFail(e.errorCode, e.description)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                .collect {
                    if (it != null) {
                        ChatLog.e("conversation", "cancelSilentForGroupSuccess")
                        view?.cancelSilentForGroupSuccess()
                    }
                }
        }
    }


    override fun updateChatThreadName(chatThreadId: String, chatThreadName: String){
        viewModelScope.launch {
            flow {
                emit(threadRepository.updateChatThreadName(chatThreadId,chatThreadName))
            }
                .catchChatException { e ->
                    view?.changeThreadNameFail(e.errorCode, e.description)
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), ChatError.EM_NO_ERROR)
                .collect {
                    view?.changeThreadNameSuccess()
                }
        }
    }


}