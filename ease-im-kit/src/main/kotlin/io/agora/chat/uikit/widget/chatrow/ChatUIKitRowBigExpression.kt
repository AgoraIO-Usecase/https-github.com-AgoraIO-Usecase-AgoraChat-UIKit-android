package io.agora.chat.uikit.widget.chatrow

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.model.ChatUIKitEmojicon

/**
 * big emoji icons
 *
 */
open class ChatUIKitRowBigExpression @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean = false
) : ChatUIKitRowText(context, attrs, defStyleAttr, isSender) {
    private val imageView: ImageView? by lazy { findViewById(R.id.image) }


    override fun onInflateView() {
        inflater.inflate(
            if (!isSender) R.layout.uikit_row_received_bigexpression else R.layout.uikit_row_sent_bigexpression,
            this
        )
    }

    override fun onSetUpView() {
        message?.run {
            val emojiconId: String = getStringAttribute(ChatUIKitConstant.MESSAGE_ATTR_EXPRESSION_ID, null)
            var emojicon: ChatUIKitEmojicon? = null
            if (ChatUIKitClient.getEmojiconInfoProvider() != null) {
                emojicon = ChatUIKitClient.getEmojiconInfoProvider()?.getEmojiconInfo(emojiconId)
            }
            emojicon?.let {
                if (it.bigIcon != 0) {
                    imageView?.load(it.bigIcon) {
                        placeholder(R.drawable.uikit_default_expression)
                        error(R.drawable.uikit_default_expression)
                    }
                } else if (!it.bigIconPath.isNullOrEmpty()) {
                    imageView?.load(it.bigIconPath) {
                        placeholder(R.drawable.uikit_default_expression)
                        error(R.drawable.uikit_default_expression)
                    }
                } else {
                    imageView?.load(R.drawable.uikit_default_expression)
                }
            }
            //setImageIncludeThread(imageView)
        }
    }
}