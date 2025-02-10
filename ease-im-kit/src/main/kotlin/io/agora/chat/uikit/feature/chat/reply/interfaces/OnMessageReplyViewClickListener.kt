package io.agora.chat.uikit.feature.chat.reply.interfaces

import android.view.View
import io.agora.chat.uikit.common.ChatMessage

interface OnMessageReplyViewClickListener {
    /**
     * on quote click for quote
     * @param message
     * @return
     */
    fun onReplyViewClick(message: ChatMessage?)

    /**
     * An error occurred when clicking on the quote view.
     * @param code
     * @param message
     */
    fun onReplyViewClickError(code: Int, message: String?) {}

    /**
     * on long click for quote
     * @param v
     * @param message
     * @return
     */
    fun onReplyViewLongClick(v: View?, message: ChatMessage?): Boolean {
        return false
    }
}