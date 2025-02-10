package io.agora.chat.uikit.feature.search.interfaces

import android.view.View
import io.agora.chat.uikit.model.ChatUIKitUser

interface OnSearchUserItemClickListener {
    /**
     * item click
     * @param view
     * @param position
     * @param user
     */
    fun onSearchItemClick(view: View?, position: Int,user: ChatUIKitUser)
}