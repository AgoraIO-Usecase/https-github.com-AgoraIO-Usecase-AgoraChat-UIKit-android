package io.agora.chat.uikit.feature.chat.interfaces

interface OnPeerTypingListener {
    /**
     * Used to monitor peer's data events
     * @param action Input event TypingBegin is start TypingEnd is finish
     */
    fun onPeerTyping(action: String?) {}
}