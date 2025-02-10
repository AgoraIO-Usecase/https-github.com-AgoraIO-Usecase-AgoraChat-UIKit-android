package io.agora.chat.uikit.repository

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatGroupManager
import io.agora.chat.uikit.common.ChatGroupOptions
import io.agora.chat.uikit.common.extensions.getOwnerInfo
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.common.helper.ContactSortedHelper
import io.agora.chat.uikit.common.suspends.addGroupMember
import io.agora.chat.uikit.common.suspends.changeChatGroupDescription
import io.agora.chat.uikit.common.suspends.changeChatGroupName
import io.agora.chat.uikit.common.suspends.changeChatGroupOwner
import io.agora.chat.uikit.common.suspends.createChatGroup
import io.agora.chat.uikit.common.suspends.destroyChatGroup
import io.agora.chat.uikit.common.suspends.fetchChatGroupMembers
import io.agora.chat.uikit.common.suspends.fetchGroupDetails
import io.agora.chat.uikit.common.suspends.fetchGroupMemberAllAttributes
import io.agora.chat.uikit.common.suspends.fetchJoinedGroupsFromServer
import io.agora.chat.uikit.common.suspends.leaveChatGroup
import io.agora.chat.uikit.common.suspends.removeChatGroupMember
import io.agora.chat.uikit.common.suspends.setGroupMemberAttributes
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.setUserInitialLetter
import io.agora.chat.uikit.provider.fetchUsersBySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatUIKitGroupRepository(
    private val groupManager: ChatGroupManager = ChatClient.getInstance().groupManager()
) {
    private var Max:Int = 1000

    init {
        ChatUIKitClient.getContext()?.resources?.getInteger(R.integer.ease_group_member_max_count)?.let {
            Max = it
        }
    }

    companion object {
        private const val TAG = "GroupRep"
        private const val LIMIT = 200
    }

    /**
     * Load joined group list from local db or server.
     */
    suspend fun loadJoinedGroupData(
        page:Int,pageSize: Int,needMemberCount:Boolean,needRole:Boolean
    ):MutableList<ChatGroup> =
        withContext(Dispatchers.IO){
            groupManager.fetchJoinedGroupsFromServer(page, pageSize, needMemberCount, needRole)
        }.toMutableList()

    suspend fun loadLocalJoinedGroupData():MutableList<ChatGroup> =
        withContext(Dispatchers.IO){
            groupManager.allGroups
        }.toMutableList()

    /**
     * create group
     */
    suspend fun createGroup(
        groupName:String,
        desc:String,
        members:MutableList<String>,
        reason:String,
        options:ChatGroupOptions,
    ):ChatGroup =
        withContext(Dispatchers.IO){
            groupManager.createChatGroup(
                groupName, desc, members, reason, options
            )
        }

    suspend fun fetchGroupDetails(
        groupId:String
    ):ChatGroup = withContext(Dispatchers.IO){
            groupManager.fetchGroupDetails(groupId)
    }

    suspend fun loadLocalMember(groupId:String):MutableList<ChatUIKitUser> =
        withContext(Dispatchers.IO) {
            val data = mutableListOf<ChatUIKitUser>()
            val currentGroup = ChatClient.getInstance().groupManager().getGroup(groupId)
            currentGroup?.members?.let {
                for (userId in it) {
                    ChatUIKitProfile.getGroupMember(groupId, userId)?.let { profile->
                        data.add(profile.toUser())
                    } ?: run {
                        data.add(ChatUIKitUser(userId))
                    }
                }
            }
            val ownerInfo = currentGroup?.getOwnerInfo()
            ownerInfo?.let { owner->
                data.add(owner)
            }
            data.forEach {
                it.setUserInitialLetter()
            }
            val sortedList = ContactSortedHelper.sortedList(data)
            sortedList.toMutableList()
        }


    suspend fun fetGroupMemberFromService(
        groupId:String
    ):MutableList<ChatUIKitUser> = withContext(Dispatchers.IO){
        val groupMemberList = mutableListOf<ChatUIKitUser>()
        var cursor: String? = null
        do {
            val result = groupManager.fetchChatGroupMembers(groupId,cursor,LIMIT)
            val data = result.data.map {
                ChatUIKitProfile.getGroupMember(groupId, it)?.toUser() ?: ChatUIKitUser(it)
            }
            cursor = result.cursor
            groupMemberList.addAll(data)
        }while (!cursor.isNullOrEmpty() && groupMemberList.size <= Max )
        groupMemberList
    }


    suspend fun addGroupMember(
        groupId: String,
        members: MutableList<String>,
    ):Int = withContext(Dispatchers.IO){
        groupManager.addGroupMember(groupId, members)
    }


    suspend fun removeGroupMember(
        groupId: String,
        members: MutableList<String>,
    ):Int = withContext(Dispatchers.IO){
        groupManager.removeChatGroupMember(groupId, members)
    }


    suspend fun changeChatGroupName(
        groupId: String,
        newName: String,
    ):Int = withContext(Dispatchers.IO){
        groupManager.changeChatGroupName(groupId, newName)
    }

    suspend fun changeChatGroupDescription(
        groupId: String,
        description: String,
    ):Int = withContext(Dispatchers.IO){
        groupManager.changeChatGroupDescription(groupId, description)
    }

    suspend fun changeChatGroupOwner(
        groupId: String,
        newOwner: String,
    ) :ChatGroup = withContext(Dispatchers.IO){
        groupManager.changeChatGroupOwner(groupId, newOwner)
    }

    suspend fun leaveChatGroup(
        groupId: String
    ) : Int = withContext(Dispatchers.IO){
        groupManager.leaveChatGroup(groupId)
    }

    suspend fun destroyChatGroup(
        groupId: String
    ):Int = withContext(Dispatchers.IO){
        groupManager.destroyChatGroup(groupId)
    }

    suspend fun fetchMemberAllAttributes(
        groupId: String,
        userList: List<String>,
        keyList: List<String>,
    ):MutableMap<String,MutableMap<String,String>> = withContext(Dispatchers.IO){
        groupManager.fetchGroupMemberAllAttributes(groupId,userList,keyList)
    }

    suspend fun setGroupMemberAttributes(
        groupId: String,
        userId: String,
        attribute:MutableMap<String,String>
    ):Int = withContext(Dispatchers.IO){
        groupManager.setGroupMemberAttributes(groupId,userId,attribute)
    }

    suspend fun fetchMemberInfo(groupId: String?, members: List<String>?) =
        withContext(Dispatchers.IO) {
            if (groupId.isNullOrEmpty()) {
                throw ChatException(ChatError.INVALID_PARAM, "groupId is null or empty.")
            }
            if (members.isNullOrEmpty()) {
                throw ChatException(ChatError.INVALID_PARAM, "members is null or empty.")
            }
            val data = members.filter {
                    ChatUIKitProfile.getGroupMember(groupId,it) == null
            }
            ChatUIKitClient.getUserProvider()?.fetchUsersBySuspend(data)
        }
}