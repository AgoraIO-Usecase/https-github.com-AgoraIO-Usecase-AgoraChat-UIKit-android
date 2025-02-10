package io.agora.chat.uikit.feature.chat.interfaces

import io.agora.chat.uikit.common.enums.ChatUIKitFinishReason

interface OnChatFinishListener {
    /**
     * \~english
     * The callback that chat is finished.
     * @param reason
     * @param id
     */
    fun onChatFinish(reason: ChatUIKitFinishReason, id: String?)
}