package io.agora.chat.uikit.feature.contact.interfaces

import android.view.View

interface OnMentionResultListener {
    fun onMentionItemClick(view: View?,position: Int,userId:String?)
}