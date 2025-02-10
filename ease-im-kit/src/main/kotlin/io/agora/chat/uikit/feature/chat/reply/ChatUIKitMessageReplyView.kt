package io.agora.chat.uikit.feature.chat.reply

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatDownloadStatus
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatFileMessageBody
import io.agora.chat.uikit.common.ChatImageMessageBody
import io.agora.chat.uikit.common.ChatLocationMessageBody
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.ChatVideoMessageBody
import io.agora.chat.uikit.common.ChatVoiceMessageBody
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.extensions.getEmojiText
import io.agora.chat.uikit.common.extensions.getTextHeight
import io.agora.chat.uikit.common.extensions.getUserCardInfo
import io.agora.chat.uikit.common.extensions.isUserCardMessage
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.common.suspends.downloadThumbnailBySuspend
import io.agora.chat.uikit.common.utils.ChatUIKitFileUtils
import io.agora.chat.uikit.feature.chat.reply.interfaces.OnMessageReplyViewClickListener
import io.agora.chat.uikit.common.enums.ChatUIKitReplyMap
import io.agora.chat.uikit.common.extensions.isReplyMessage
import io.agora.chat.uikit.common.extensions.isUnsentMessage
import io.agora.chat.uikit.feature.chat.internal.setTargetSpan
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.getNickname
import io.agora.chat.uikit.provider.getSyncUser
import io.agora.chat.uikit.widget.ChatUIKitImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class ChatUIKitMessageReplyView @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private var isHistory: Boolean = false,
    private var isSender: Boolean = false
) : LinearLayout(
    context, attrs, defStyleAttr
) {

    private var listener: OnMessageReplyViewClickListener? = null
    private var quoteMessage: ChatMessage? = null
    private val message: ChatMessage? = null
    private var quoteSender: String? = null
    private val quoteName: TextView by lazy { findViewById(R.id.quote_name) }
    private val quoteContent: TextView by lazy { findViewById(R.id.quote_content) }
    private val quoteIcon: ImageView by lazy { findViewById(R.id.quote_icon) }
    private val quoteImage: ChatUIKitImageView by lazy { findViewById(R.id.quote_image) }
    private val quoteVideoIcon: ImageView by lazy { findViewById(R.id.quote_video_icon) }

    init {
        initAttrs(context, attrs)
        initListener()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitMessageReplyView).let { a ->
            isSender = isSender || a.getBoolean(R.styleable.ChatUIKitMessageReplyView_ease_chat_message_reply_is_sender, false)
            isHistory = isHistory || a.getBoolean(R.styleable.ChatUIKitMessageReplyView_ease_chat_message_reply_is_history, false)
            a.recycle()
        }
        if (!isHistory) {
            if (isSender) {
                inflate(context, R.layout.uikit_row_sent_reply_layout, this)
            } else {
                inflate(context, R.layout.uikit_row_received_reply_layout, this)
            }
        }
        setReplyViewParams(isSender)
    }

    private fun initListener() {
        setOnClickListener {
            if (listener != null && quoteMessage != null) {
                listener?.onReplyViewClick(quoteMessage)
            }
        }
        setOnLongClickListener {
            return@setOnLongClickListener listener?.onReplyViewLongClick(this, quoteMessage) == true
        }
    }

    fun updateMessageInfo(message: ChatMessage?): Boolean {
        if (message == null) {
            ChatLog.e(
                TAG,
                getContext().getString(R.string.uikit_error_message_not_exist)
            )
            return false
        }
        return message.isReplyMessage {
            quoteName.text = ""
            quoteContent.text = ""
            quoteIcon.visibility = GONE
            quoteImage.visibility = GONE
            quoteVideoIcon.visibility = GONE
            parseJsonObject(it)
        }
    }

    private fun setReplyViewParams(isSender: Boolean) {
        if (isSender) {
            gravity = Gravity.END
        } else {
            gravity = Gravity.START
        }
    }

    private fun parseJsonObject(jsonObject: JSONObject) {
        try {
            val quoteMsgID = jsonObject.getString(ChatUIKitConstant.QUOTE_MSG_ID)
            val quoteSender = jsonObject.getString(ChatUIKitConstant.QUOTE_MSG_SENDER)
            val quoteType = jsonObject.getString(ChatUIKitConstant.QUOTE_MSG_TYPE)
            val quoteContent = jsonObject.getString(ChatUIKitConstant.QUOTE_MSG_PREVIEW)
            var quoteSenderNick: String? = ""
            if (!TextUtils.isEmpty(quoteSender)) {
                val user: ChatUIKitUser? = ChatUIKitClient.getCache().getMessageUserInfo(quoteSender)?.toUser()
                    ?: ChatUIKitClient.getUserProvider()?.getSyncUser(quoteSender)?.toUser()
                quoteSenderNick = if (user == null) {
                    quoteSender
                } else {
                    user.getNickname()
                }
            }
            this.quoteSender = quoteSenderNick
            quoteMessage = ChatClient.getInstance().chatManager().getMessage(quoteMsgID)
            quoteMessage?.let {
                if (it.isUnsentMessage()) {
                    quoteMessage = null
                }
            }
            setMessageReplyInfo(getQuoteMessageType(quoteType), quoteContent)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun setMessageReplyInfo(
        quoteMessageType: ChatMessageType,
        quoteContent: String
    ) {
        this.quoteName.text = quoteSender
        this.quoteName.visibility = VISIBLE
        if (quoteMessage == null) {
            this.quoteContent.text = resources.getString(R.string.uikit_quote_message_not_exist)
            this.quoteContent.visibility = VISIBLE
            this.quoteName.visibility = GONE
            return
        }
        this.quoteIcon.visibility = VISIBLE
        this.quoteContent.visibility = VISIBLE
        this.quoteVideoIcon.visibility = GONE
        when(quoteMessageType) {
            ChatMessageType.TXT -> {
                this.quoteIcon.visibility = GONE
                textTypeDisplay(quoteContent)
            }
            ChatMessageType.IMAGE -> {
                this.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_image)
                imageTypeDisplay(quoteContent)
            }
            ChatMessageType.VIDEO -> {
                this.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_video)
                videoTypeDisplay(quoteContent)
            }
            ChatMessageType.LOCATION -> {
                this.quoteIcon.setImageResource(R.drawable.uikit_chat_item_menu_location)
                locationTypeDisplay(quoteContent)
            }
            ChatMessageType.VOICE -> {
                this.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_voice)
                voiceTypeDisplay(quoteContent)
            }
            ChatMessageType.FILE -> {
                this.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_file)
                fileTypeDisplay(quoteContent)
            }
            ChatMessageType.COMBINE -> {
                this.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_combine)
                combineTypeDisplay(quoteContent)
            }
            ChatMessageType.CUSTOM -> {
                this.quoteIcon.visibility = GONE
                specifyCustomDisplay(quoteContent)
            }
            else -> {
                disableDisplay()
            }
        }

    }

    private fun disableDisplay() {
        this.quoteIcon.visibility = GONE
        this.quoteContent.text = resources.getString(R.string.uikit_message_reply_unknown_type)
        this.quoteContent.setTextColor(ContextCompat.getColor(context, R.color.ease_color_text_not_enable))
    }

    private fun specifyCustomDisplay(quoteContent: String) {
        if (this.quoteMessage?.isUserCardMessage() == true) {
            this.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_user_card)
            this.quoteMessage?.getUserCardInfo()?.let {
                this.quoteContent.text = it.name
            }
            this.quoteIcon.visibility = VISIBLE
        } else {
            disableDisplay()
        }
    }

    private fun textTypeDisplay(quoteContent: String) {
        if (this.quoteMessage?.getBooleanAttribute(ChatUIKitConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false) == true) {
            // show big expression
            bigExpressionDisplay()
        } else {
            val content = (this.quoteMessage?.body as? ChatTextMessageBody)?.message ?: quoteContent
            this.quoteContent.text = content.getEmojiText(context, this.quoteContent.getTextHeight())
            this.quoteContent.ellipsize = TextUtils.TruncateAt.END
            this.quoteContent.maxLines = 2
            this.quoteContent.visibility = VISIBLE
        }
    }

    private fun imageTypeDisplay(quoteContent: String) {
        this.quoteContent.text = resources.getString(R.string.uikit_message_reply_image_type)
        if (this.quoteMessage == null) {
            this.quoteImage.setImageResource(R.drawable.uikit_chat_quote_default_image)
            this.quoteImage.visibility = VISIBLE
        } else {
            imageDisplay()
        }
    }

    private fun videoTypeDisplay(quoteContent: String) {
        this.quoteContent.text = resources.getString(R.string.uikit_message_reply_video_type)
        if (this.quoteMessage == null) {
            this.quoteImage.setImageResource(R.drawable.uikit_chat_quote_default_video)
            this.quoteImage.visibility = VISIBLE
        } else {
            videoDisplay()
        }
    }

    private fun locationTypeDisplay(quoteContent: String) {
        if (this.quoteMessage == null) {
            this.quoteContent.text = resources.getString(R.string.uikit_message_reply_location_type)
        } else {
            (this.quoteMessage?.body as? ChatLocationMessageBody)?.let {
                SpannableStringBuilder(resources.getString(R.string.uikit_message_reply_location_type))
                    .append(": ").let { builder->
                        builder.setTargetSpan(TextAppearanceSpan(context, R.style.ease_chat_row_message_type_text_style), 0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                            .append(it.address)
                        this.quoteContent.text = builder
                    }
            }
        }
    }

    private fun voiceTypeDisplay(quoteContent: String) {
        if (this.quoteMessage == null) {
            this.quoteContent.text = resources.getString(R.string.uikit_message_reply_voice_type)
        } else {
            (this.quoteMessage?.body as? ChatVoiceMessageBody)?.let {
                SpannableStringBuilder(resources.getString(R.string.uikit_message_reply_voice_type))
                    .append(": ").let { builder->
                        builder.setTargetSpan(TextAppearanceSpan(context, R.style.ease_chat_row_message_type_text_style), 0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                            .append(it.length.toString())
                        this.quoteContent.text = builder
                    }
            }
        }
    }

    private fun fileTypeDisplay(quoteContent: String) {
        if (this.quoteMessage == null) {
            this.quoteContent.text = resources.getString(R.string.uikit_message_reply_file_type)
        } else {
            (this.quoteMessage?.body as? ChatFileMessageBody)?.let {
                SpannableStringBuilder(resources.getString(R.string.uikit_message_reply_file_type))
                    .append(": ").let { builder->
                        builder.setTargetSpan(TextAppearanceSpan(context, R.style.ease_chat_row_message_type_text_style), 0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                            .append(it.fileName)
                        this.quoteContent.text = builder
                    }
            }
        }
    }

    private fun combineTypeDisplay(quoteContent: String) {
        this.quoteContent.text = resources.getString(R.string.uikit_combine_default)
    }

    private fun bigExpressionDisplay() {
        this.quoteMessage?.run {
            val emojiconId =
                getStringAttribute(ChatUIKitConstant.MESSAGE_ATTR_EXPRESSION_ID, null)
            ChatUIKitClient.getEmojiconInfoProvider()?.getEmojiconInfo(emojiconId)?.let {
                if (it.bigIcon != -1) {
                    quoteImage.load(it.bigIcon) {
                        placeholder(R.drawable.uikit_default_expression)
                        error(R.drawable.uikit_default_expression)
                    }
                } else if (!it.bigIconPath.isNullOrEmpty()) {
                    quoteImage.load(it.bigIconPath) {
                        placeholder(R.drawable.uikit_default_expression)
                        error(R.drawable.uikit_default_expression)
                    }
                } else {
                    quoteImage.load(R.drawable.uikit_default_expression)
                }
                quoteIcon.visibility = VISIBLE
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun imageDisplay() {
        this.quoteMessage?.run {
            val imgBody = body as ChatImageMessageBody
            if (ChatUIKitFileUtils.isFileExistByUri(context, imgBody.thumbnailLocalUri())
                && imgBody.thumbnailDownloadStatus() != ChatDownloadStatus.FAILED) {

                if (imgBody.thumbnailDownloadStatus() == ChatDownloadStatus.SUCCESSED) {
                    loadImage(imgBody.thumbnailLocalUri(), R.drawable.uikit_chat_quote_default_image)
                } else {
                    context.mainScope().launch {
                        for (i in 0..3) {
                            delay(500)
                            if (imgBody.thumbnailDownloadStatus() == ChatDownloadStatus.SUCCESSED) {
                                loadImage(imgBody.thumbnailLocalUri(), R.drawable.uikit_chat_quote_default_image)
                                break
                            }
                        }
                        if (imgBody.thumbnailDownloadStatus() != ChatDownloadStatus.SUCCESSED) {
                            quoteImage.load(R.drawable.uikit_chat_quote_default_image)
                            quoteImage.visibility = VISIBLE
                        }
                    }
                }
            } else if (ChatUIKitFileUtils.isFileExistByUri(context, imgBody.localUri)) {
                loadImage(imgBody.localUri, R.drawable.uikit_chat_quote_default_image)
            } else {
                context.mainScope().launch {
                    val result = async {
                        try {
                            ChatClient.getInstance().chatManager().downloadThumbnailBySuspend(this@run)
                        } catch (e: Exception) {
                            ChatLog.e(TAG, "download thumbnail failed: ${e.message}")
                        }
                    }
                    result.await()
                    withContext(Dispatchers.Main) {
                        if ((result.getCompleted() as Pair<*, *>).first == ChatError.EM_NO_ERROR) {
                            loadImage(imgBody.thumbnailLocalUri(), R.drawable.uikit_chat_quote_default_image)
                        } else {
                            quoteImage.load(R.drawable.uikit_chat_quote_default_image)
                            quoteImage.visibility = VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun videoDisplay() {
        (this.quoteMessage?.body as? ChatVideoMessageBody)?.let {
            val localThumbUri = it.localThumbUri
            if (ChatUIKitFileUtils.isFileExistByUri(context, localThumbUri)
                && it.thumbnailDownloadStatus() == ChatDownloadStatus.SUCCESSED) {
                loadImage(localThumbUri, R.drawable.uikit_chat_quote_default_video, true)
            } else {
                loadImage(it.thumbnailUrl, R.drawable.uikit_chat_quote_default_video, true)
            }
        }
    }

    private fun loadImage(source: Any, placeholder: Int, isVideo: Boolean = false) {
        this.quoteImage.load(source) {
            placeholder(placeholder)
            error(placeholder)
            listener(onSuccess = { _, _ ->
                if (isVideo) {
                    quoteVideoIcon.visibility = VISIBLE
                }
            })
        }
        this.quoteImage.visibility = VISIBLE
    }

    private fun getQuoteMessageType(quoteType: String): ChatMessageType {
        if (receiveMsgTypes.containsKey(quoteType)) {
            return ChatMessageType.valueOf(receiveMsgTypes[quoteType]!!)
        }
        val type: ChatMessageType
        try {
            type = ChatMessageType.valueOf(quoteType.uppercase(Locale.getDefault()))
            return type
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        return ChatMessageType.TXT
    }

    fun setOnMessageReplyViewClickListener(listener: OnMessageReplyViewClickListener?) {
        this.listener = listener
    }

    companion object {
        private const val TAG = "ChatUIKitMessageReplyView"
        private val receiveMsgTypes: Map<String, String> = mapOf(
            ChatUIKitReplyMap.txt.name to ChatMessageType.TXT.name,
            ChatUIKitReplyMap.img.name to ChatMessageType.IMAGE.name,
            ChatUIKitReplyMap.video.name to ChatMessageType.VIDEO.name,
            ChatUIKitReplyMap.location.name to ChatMessageType.LOCATION.name,
            ChatUIKitReplyMap.audio.name to ChatMessageType.VOICE.name,
            ChatUIKitReplyMap.file.name to ChatMessageType.FILE.name,
            ChatUIKitReplyMap.cmd.name to ChatMessageType.CMD.name,
            ChatUIKitReplyMap.custom.name to ChatMessageType.CUSTOM.name,
            ChatUIKitReplyMap.combine.name to ChatMessageType.COMBINE.name
        )
    }

}