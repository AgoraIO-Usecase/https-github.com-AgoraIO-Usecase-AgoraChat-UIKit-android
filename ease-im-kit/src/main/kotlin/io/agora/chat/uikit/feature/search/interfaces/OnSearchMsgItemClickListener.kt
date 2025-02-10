package io.agora.chat.uikit.feature.search.interfaces

import android.view.View
import io.agora.chat.uikit.common.ChatMessage

interface OnSearchMsgItemClickListener {
    /**
     * item click
     * @param view
     * @param position
     * @param msg
     */
    fun onSearchItemClick(view: View?, position: Int, msg: ChatMessage)
}