package io.agora.chat.uikit.widget.chatrow

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatCombineMessageBody

open class ChatUIKitRowCombine @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean = false
) : ChatUIKitRow(context, attrs, defStyleAttr, isSender) {
    protected val contentView: TextView? by lazy { findViewById(R.id.tv_chatcontent) }
    protected val tvChatSummary: TextView? by lazy { findViewById(R.id.tv_chat_summary) }

    override fun onInflateView() {
        inflater.inflate(
            if (!isSender) R.layout.uikit_row_received_combine else R.layout.uikit_row_sent_combine,
            this
        )
    }

    override fun onSetUpView() {
        message?.run {
            (body as? ChatCombineMessageBody)?.let {
                contentView?.text = it.title
                tvChatSummary?.let { view ->
                    if (!TextUtils.isEmpty(it.summary)) {
                        view.text = it.summary
                        view.visibility = View.VISIBLE
                    } else {
                        view.visibility = View.GONE
                    }
                }
            }
        }
    }
}