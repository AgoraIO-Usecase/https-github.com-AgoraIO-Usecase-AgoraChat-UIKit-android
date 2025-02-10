package io.agora.chat.uikit.feature.group.interfaces

import android.view.View
import io.agora.chat.uikit.model.ChatUIKitUser

interface IGroupMemberEventListener {

    fun onGroupMemberListItemClick(view: View?, user: ChatUIKitUser)

    fun onGroupMemberLoadSuccess(memberList:MutableList<ChatUIKitUser>){}
}