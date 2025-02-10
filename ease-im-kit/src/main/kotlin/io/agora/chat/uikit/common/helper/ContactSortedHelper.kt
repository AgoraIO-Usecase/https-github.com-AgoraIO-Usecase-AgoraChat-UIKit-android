package io.agora.chat.uikit.common.helper

import io.agora.chat.uikit.model.ChatUIKitUser

object ContactSortedHelper {
    fun sortedList(list:List<ChatUIKitUser>):List<ChatUIKitUser>{
        val sortedList = list.sortedWith(compareBy(
            { if (it.initialLetter == "#") "ZZZZZ" else it.initialLetter
            }, { it.initialLetter }
        ))
        return sortedList
    }
}