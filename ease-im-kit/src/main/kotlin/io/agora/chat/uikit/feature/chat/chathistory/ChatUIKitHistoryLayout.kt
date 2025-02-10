package io.agora.chat.uikit.feature.chat.chathistory

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.databinding.UikitLayoutChatHistoryBinding
import io.agora.chat.uikit.feature.chat.adapter.ChatUIKitMessagesAdapter
import io.agora.chat.uikit.feature.chat.config.ChatUIKitMessageItemConfig
import io.agora.chat.uikit.feature.chat.interfaces.IChatHistoryLayout
import io.agora.chat.uikit.feature.chat.interfaces.IChatHistoryResultView
import io.agora.chat.uikit.feature.chat.interfaces.OnCombineMessageDownloadCallback
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitMessageListLayout
import io.agora.chat.uikit.viewmodel.chathistory.ChatUIKitHistoryViewModel
import io.agora.chat.uikit.viewmodel.chathistory.IChatHistoryRequest
import kotlinx.coroutines.launch

open class ChatUIKitHistoryLayout @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): RelativeLayout(context, attrs, defStyleAttr), IChatHistoryLayout, IChatHistoryResultView {

    /**
     * The view model of the chat history view.
     */
    private var viewModel: IChatHistoryRequest? = null

    /**
     * The adapter of the chat history view.
     */
    private var messagesAdapter: ChatUIKitMessagesAdapter? = null

    /**
     * The callback of the combine message download or parse.
     */
    private var historyCallback: OnCombineMessageDownloadCallback? = null

    protected val binding: UikitLayoutChatHistoryBinding by lazy { UikitLayoutChatHistoryBinding.inflate(
        LayoutInflater.from(context), this, true) }

    init {
        initView()
    }

    private fun initView() {
        viewModel = if (context is AppCompatActivity) {
            ViewModelProvider(context)[ChatUIKitHistoryViewModel::class.java]
        } else {
            ChatUIKitHistoryViewModel()
        }
        viewModel?.attachView(this)

        // Set the history adapter for the ChatUIKitMessageListLayout
        messagesAdapter = ChatUIKitHistoryAdapter()
        messagesAdapter?.setItemConfig(ChatUIKitMessageItemConfig(context, null))
        binding.root.setMessagesAdapter(messagesAdapter)

        binding.root.refreshLayout?.let {
            it.setEnableLoadMore(false)
            it.setEnableRefresh(false)
        }
    }

    fun loadData(message: ChatMessage?) {
        if (message == null) {
            return
        }
        if (message.type != ChatMessageType.COMBINE) {
            return
        }
        viewModel?.downloadCombineMessage(message)
    }

    override fun setViewModel(viewModel: IChatHistoryRequest?) {
        this.viewModel = viewModel
        this.viewModel?.attachView(this)
    }

    override fun setMessagesAdapter(adapter: ChatUIKitMessagesAdapter?) {
        adapter?.let {
            this.messagesAdapter = adapter
            binding.root.setMessagesAdapter(messagesAdapter)
        }
    }

    override fun setOnCombineMessageDownloadCallback(callback: OnCombineMessageDownloadCallback?) {
        this.historyCallback = callback
    }

    override fun getChatMessageListLayout(): ChatUIKitMessageListLayout {
        return binding.root
    }

    override fun downloadCombinedMessagesSuccess(messageList: List<ChatMessage>) {
        context.mainScope().launch {
            messagesAdapter?.setData(messageList.toMutableList())
            historyCallback?.onDownloadSuccess(messageList)
        }

    }

    override fun downloadCombinedMessagesFail(error: Int, errorMsg: String?) {
        context.mainScope().launch {
            historyCallback?.onDownloadFail(error, errorMsg)
        }
    }


}