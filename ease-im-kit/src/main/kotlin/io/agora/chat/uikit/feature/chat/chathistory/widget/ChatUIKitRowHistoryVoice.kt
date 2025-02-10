package io.agora.chat.uikit.feature.chat.chathistory.widget

import android.content.Context
import android.util.AttributeSet
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatVoiceMessageBody
import io.agora.chat.uikit.common.extensions.getDateFormat
import io.agora.chat.uikit.widget.chatrow.ChatUIKitRowVoice

open class ChatUIKitRowHistoryVoice @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean = false
) : ChatUIKitRowVoice(context, attrs, defStyleAttr, isSender) {

    override fun onInflateView() {
        inflater.inflate(R.layout.uikit_row_history_voice, this)
    }

    override fun onSetUpView() {
        message?.run {
            if (body is ChatVoiceMessageBody) {
                val voiceBody = body as ChatVoiceMessageBody
                voiceLengthView?.text = "${context.getString(R.string.uikit_voice)} ${voiceBody.length}'"
            }
        }
        usernickView?.let {
            if (!it.text.toString().trim().isNullOrEmpty()) {
                it.visibility = VISIBLE
            }
        }
    }

    override fun setOtherTimestamp(preMessage: ChatMessage?) {
        timeStampView?.let {
            preMessage?.let { msg ->
                it.text = msg.getDateFormat(true)
                it.visibility = VISIBLE
            }
        }
    }
}
