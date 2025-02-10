package io.agora.chat.uikit.interfaces

interface OnVoiceRecorderClickListener {
    /**
     * Click the send button.
     */
    fun onClick(filePath: String?, length: Int)
}