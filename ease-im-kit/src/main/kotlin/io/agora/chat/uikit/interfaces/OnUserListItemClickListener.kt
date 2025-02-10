package io.agora.chat.uikit.interfaces

import android.view.View
import io.agora.chat.uikit.model.ChatUIKitUser

interface OnUserListItemClickListener {
    fun onAvatarClick(v: View,position:Int){}
    fun onUserListItemClick(v: View?, position:Int, user: ChatUIKitUser?){}
}