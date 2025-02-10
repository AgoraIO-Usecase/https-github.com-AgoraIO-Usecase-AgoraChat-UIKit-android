package io.agora.chat.uikit.feature.chat.reply

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import coil.load
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.databinding.UikitWidgetChatMessageReplyBinding
import io.agora.chat.uikit.feature.chat.interfaces.IChatTopExtendMenu
import io.agora.chat.uikit.feature.chat.reply.interfaces.IChatMessageReply
import io.agora.chat.uikit.feature.chat.reply.interfaces.IChatMessageReplyResultView
import io.agora.chat.uikit.viewmodel.reply.ChatUIKitMessageReplyViewModel

class ChatUIKitExtendMessageReplyView(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr),
    IChatMessageReply, IChatTopExtendMenu,
    IChatMessageReplyResultView {

    private val binding: UikitWidgetChatMessageReplyBinding by lazy {
        UikitWidgetChatMessageReplyBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }
    private var viewModel: ChatUIKitMessageReplyViewModel? = null

    init {
        binding.cancelSelect.setOnClickListener { hideQuoteView() }
        viewModel = if (context is ViewModelStoreOwner) {
            ViewModelProvider(context)[ChatUIKitMessageReplyViewModel::class.java]
        } else {
            ChatUIKitMessageReplyViewModel()
        }
        viewModel?.attachView(this)
    }
    override fun showTopExtendMenu(isShow: Boolean) {
        this.visibility = if (isShow) VISIBLE else GONE
    }

    override fun startQuote(message: ChatMessage?) {
        hideQuoteView()
        viewModel?.showQuoteMessageInfo(message)
    }

    override fun hideQuoteView() {
        this.visibility = GONE
        binding.quoteName.text = ""
        binding.quoteContent.text = ""
        binding.quoteIcon.visibility = GONE
        binding.quoteImage.visibility = GONE
        binding.quoteVideoIcon.visibility = GONE
    }

    override fun setViewModel(viewModel: ChatUIKitMessageReplyViewModel?) {
        this.viewModel = viewModel
        this.viewModel?.attachView(this)
    }

    override fun showQuoteMessageNickname(nickname: String?) {
        binding.quoteName.text = nickname
    }

    override fun showQuoteMessageContent(content: CharSequence?) {
        binding.quoteContent.text = content
        showTopExtendMenu(true)
    }

    override fun showQuoteMessageAttachment(
        type: ChatMessageType?,
        localPath: String?,
        remotePath: String?,
        defaultResource: Int
    ) {
        binding.quoteImage.visibility = GONE
        binding.quoteVideoIcon.visibility = GONE
        if (type == ChatMessageType.IMAGE || type == ChatMessageType.VIDEO) {
            binding.quoteImage.load(if (localPath.isNullOrEmpty()) remotePath else localPath) {
                placeholder(defaultResource)
                error(defaultResource)
                listener(onSuccess = { _, _ ->
                    if (type == ChatMessageType.VIDEO) {
                        binding.quoteVideoIcon.visibility = VISIBLE
                    }
                })
            }
            binding.quoteImage.visibility = VISIBLE
        }

        binding.quoteIcon.visibility = VISIBLE
        when(type) {
            ChatMessageType.IMAGE -> {
                binding.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_image)
            }
            ChatMessageType.VIDEO -> {
                binding.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_video)
            }
            ChatMessageType.VOICE ->{
                binding.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_voice)
            }
            ChatMessageType.FILE ->{
                binding.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_file)
            }
            ChatMessageType.CUSTOM ->{
                binding.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_user_card)
            }
            ChatMessageType.COMBINE ->{
                binding.quoteIcon.setImageResource(R.drawable.uikit_chat_quote_icon_combine)
            }
            else -> {
                binding.quoteIcon.visibility = GONE
            }
        }
    }

    override fun onShowError(code: Int, message: String?) {

    }
}