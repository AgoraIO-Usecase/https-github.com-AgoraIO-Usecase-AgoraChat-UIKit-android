package io.agora.chat.uikit.feature.chat.interfaces

import android.view.View
import io.agora.chat.uikit.common.ChatMessage

interface OnMessageItemClickListener {
    /**
     * Click on the message bubble area
     * @param message
     * @return
     */
    fun onBubbleClick(message: ChatMessage?): Boolean

    /**
     * Long press the message bubble area
     * @param v
     * @param message
     * @return
     */
    fun onBubbleLongClick(v: View?, message: ChatMessage?): Boolean

    /**
     * Click the resend view.
     * @param message
     * @return
     */
    fun onResendClick(message: ChatMessage?): Boolean

    /**
     * Click on the avatar
     * @param userId
     */
    fun onUserAvatarClick(userId: String?)

    /**
     * Long press on the avatar
     * @param userId
     */
    fun onUserAvatarLongClick(userId: String?)

}