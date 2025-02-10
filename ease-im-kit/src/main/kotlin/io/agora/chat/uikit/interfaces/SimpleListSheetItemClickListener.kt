package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.model.ChatUIKitMenuItem

interface SimpleListSheetItemClickListener {
    fun onItemClickListener(position:Int,menu:ChatUIKitMenuItem){}

}