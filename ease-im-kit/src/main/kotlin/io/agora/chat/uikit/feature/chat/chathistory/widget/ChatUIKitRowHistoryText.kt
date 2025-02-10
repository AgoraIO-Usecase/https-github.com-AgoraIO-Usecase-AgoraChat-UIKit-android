package io.agora.chat.uikit.feature.chat.chathistory.widget

import android.content.Context
import android.util.AttributeSet
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.getDateFormat
import io.agora.chat.uikit.widget.chatrow.ChatUIKitRowText

open class ChatUIKitRowHistoryText @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean = false
) : ChatUIKitRowText(context, attrs, defStyleAttr, isSender) {

    override fun onInflateView() {
        inflater.inflate(R.layout.uikit_row_history_message, this)
    }

    override fun setOtherTimestamp(preMessage: ChatMessage?) {
        timeStampView?.let {
            preMessage?.let { msg ->
                it.text = msg.getDateFormat(true)
                it.visibility = VISIBLE
            }
        }
    }

    override fun onSetUpView() {
        super.onSetUpView()
        usernickView?.let {
            if (!it.text.toString().trim().isNullOrEmpty()) {
                it.visibility = VISIBLE
            }
        }
    }
}
