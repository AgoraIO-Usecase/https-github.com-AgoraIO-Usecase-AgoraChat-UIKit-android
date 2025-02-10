package io.agora.chat.uikit.feature.chat.translation

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.ChatUIKitConstant

class ChatUIKitMessageTranslationView @JvmOverloads constructor(
    var isSender:Boolean? = null,
    private var isHistory:Boolean = false,
    private val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
) {
    private var isTranslation = false

    private val tvTranslationContent: TextView by lazy { findViewById(R.id.tv_translation_content) }
    private val tvTranslationStatus: TextView by lazy { findViewById(R.id.tv_translation_status) }
    private val ivTranslationIcon: AppCompatImageView by lazy { findViewById(R.id.iv_translation) }

    init {
        initAttrs(context, attrs)
    }

    @SuppressLint("RestrictedApi")
    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitMessageTranslationView).let { a ->
            if (this.isSender == null){
                isSender = a.getBoolean(R.styleable.ChatUIKitMessageTranslationView_ease_chat_message_translation_is_sender, false)
            }
            isHistory = a.getBoolean(R.styleable.ChatUIKitMessageTranslationView_ease_chat_message_translation_is_history, false)
            a.recycle()
        }

        isSender?.let {
            val colorId = if (it) {
                inflate(context, R.layout.uikit_row_sent_translation_layout, this)
                R.color.ease_color_text_special_higher
            } else {
                inflate(context, R.layout.uikit_row_received_translation_layout, this)
                R.color.ease_color_text_special_high
            }

            ivTranslationIcon.supportImageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context,colorId)
            )
        }
    }

    fun updateMessageInfo(message: ChatMessage?): Boolean {
        if (message == null) return false
        if (isHistory) return false
        if (message.type != ChatMessageType.TXT)  return false

        if (message.ext().containsKey(ChatUIKitConstant.TRANSLATION_STATUS)){
            isTranslation = message.getBooleanAttribute(ChatUIKitConstant.TRANSLATION_STATUS,false)
            if (!isTranslation) return false
        }

        if (message.body is ChatTextMessageBody){
            val body = message.body as ChatTextMessageBody
            val translations = body.translations
            if (translations.size > 0){
                val translationInfo = translations[0]
                tvTranslationContent.text = translationInfo.translationText
            }else{
                return false
            }
        }else{
            return false
        }
        return true
    }
}