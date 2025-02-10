package io.agora.chat.uikit.feature.chat.chathistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import io.agora.chat.uikit.base.ChatUIKitBaseFragment
import io.agora.chat.uikit.base.ChatUIKitBaseFragmentBuilder
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.helper.ChatUIKitTitleBarHelper
import io.agora.chat.uikit.databinding.UikitFragmentChatHistoryBinding
import io.agora.chat.uikit.feature.chat.adapter.ChatUIKitMessagesAdapter
import io.agora.chat.uikit.feature.chat.interfaces.OnCombineMessageDownloadCallback
import io.agora.chat.uikit.feature.chat.interfaces.OnSendCombineMessageCallback

class ChatUIKitHistoryFragment: ChatUIKitBaseFragment<UikitFragmentChatHistoryBinding>(),
    OnCombineMessageDownloadCallback {

    private var historyCallback: OnCombineMessageDownloadCallback? = null
    private var backPressListener: View.OnClickListener? = null
    private var messagesAdapter: ChatUIKitMessagesAdapter? = null
    private var combineMessageCallback: OnSendCombineMessageCallback? = null
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UikitFragmentChatHistoryBinding? {
        return UikitFragmentChatHistoryBinding.inflate(inflater, container, false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        arguments?.let {
            val combinedMessage = it.getParcelable<ChatMessage>(ChatUIKitConstant.EXTRA_CHAT_COMBINE_MESSAGE)
            binding?.layoutChat?.loadData(combinedMessage)

            ChatUIKitTitleBarHelper.parseBundleForTitleBar(it, binding?.titleBar) { bar ->
                bar.setNavigationOnClickListener { v -> backPressListener?.onClick(v) }
            }

            val timeColor: Int = it.getInt(Constant.KEY_MSG_TIME_COLOR, -1)
            if (timeColor != -1) {
                binding?.layoutChat?.getChatMessageListLayout()?.setTimeTextColor(timeColor)
            }
            val timeTextSize: Int = it.getInt(Constant.KEY_MSG_TIME_SIZE, -1)
            if (timeTextSize != -1) {
                binding?.layoutChat?.getChatMessageListLayout()?.setTimeTextSize(timeTextSize)
            }
            val chatBg: Int = it.getInt(Constant.KEY_CHAT_BACKGROUND, -1)
            if (chatBg != -1) {
                binding?.layoutChat?.getChatMessageListLayout()?.setBackgroundResource(chatBg)
            }
            val emptyLayout: Int = it.getInt(Constant.KEY_EMPTY_LAYOUT, -1)
            if (emptyLayout != -1) {
                binding?.layoutChat?.getChatMessageListLayout()?.getMessagesAdapter()?.setEmptyView(emptyLayout)
            }
        }
    }

    override fun initListener() {
        super.initListener()
        binding?.layoutChat?.setOnCombineMessageDownloadCallback(this)
    }

    override fun onDownloadSuccess(messages: List<ChatMessage>) {
        historyCallback?.onDownloadSuccess(messages)
    }

    override fun onDownloadFail(error: Int, errorMsg: String?) {
        historyCallback?.onDownloadFail(error, errorMsg)
    }

    private fun setHeaderBackPressListener(listener: View.OnClickListener?) {
        this.backPressListener = listener
    }

    private fun setCustomAdapter(adapter: ChatUIKitMessagesAdapter?) {
        this.messagesAdapter = adapter
    }

    private fun setOnSendCombineMessageCallback(callback: OnSendCombineMessageCallback?) {
        this.combineMessageCallback = callback
    }

    private fun setOnCombineMessageDownloadCallback(callback: OnCombineMessageDownloadCallback?) {
        this.historyCallback = callback
    }

    class Builder(
        private val combinedMessage: ChatMessage?
    ): ChatUIKitBaseFragmentBuilder<Builder>() {
        override val bundle: Bundle = Bundle()
        private var backPressListener: View.OnClickListener? = null
        private var adapter: ChatUIKitMessagesAdapter? = null
        protected var customFragment: ChatUIKitHistoryFragment? = null
        private var combineMessageCallback: OnSendCombineMessageCallback? = null
        private var historyCallback: OnCombineMessageDownloadCallback? = null

        init {
            bundle.putParcelable(ChatUIKitConstant.EXTRA_CHAT_COMBINE_MESSAGE, combinedMessage)
        }

        /**
         * If you have set [Builder.enableTitleBarPressBack], you can set the listener
         *
         * @param listener
         * @return
         */
        fun setTitleBarBackPressListener(listener: View.OnClickListener?): Builder {
            backPressListener = listener
            return this
        }

        /**
         * Set the text color of message item time
         *
         * @param color
         * @return
         */
        fun setMsgTimeTextColor(@ColorInt color: Int): Builder {
            bundle.putInt(Constant.KEY_MSG_TIME_COLOR, color)
            return this
        }

        /**
         * Set the text size of message item time, unit is px
         *
         * @param size
         * @return
         */
        fun setMsgTimeTextSize(size: Int): Builder {
            bundle.putInt(Constant.KEY_MSG_TIME_SIZE, size)
            return this
        }

        /**
         * Set the background of the chat list region
         *
         * @param bgDrawable
         * @return
         */
        fun setChatBackground(@DrawableRes bgDrawable: Int): Builder {
            bundle.putInt(Constant.KEY_CHAT_BACKGROUND, bgDrawable)
            return this
        }

        /**
         * Set chat list's empty layout if you want replace the default
         *
         * @param emptyLayout
         * @return
         */
        fun setEmptyLayout(@LayoutRes emptyLayout: Int): Builder {
            bundle.putInt(Constant.KEY_EMPTY_LAYOUT, emptyLayout)
            return this
        }

        /**
         * Set custom fragment which should extends ChatUIKitHistoryFragment
         *
         * @param fragment
         * @param <T>
         * @return
        </T> */
        fun <T : ChatUIKitHistoryFragment?> setCustomFragment(fragment: T): Builder {
            customFragment = fragment
            return this
        }

        /**
         * Set custom adapter which should extends EaseMessageAdapter
         *
         * @param adapter
         * @return
         */
        fun setCustomAdapter(adapter: ChatUIKitMessagesAdapter?): Builder {
            this.adapter = adapter
            return this
        }

        fun setOnSendCombineMessageCallback(callback: OnSendCombineMessageCallback?): Builder {
            combineMessageCallback = callback
            return this
        }

        fun setOnCombineMessageDownloadCallback(callback: OnCombineMessageDownloadCallback?): Builder {
            historyCallback = callback
            return this
        }

        fun build(): ChatUIKitHistoryFragment {
            val fragment = customFragment ?: ChatUIKitHistoryFragment()
            fragment.let {
                it.arguments = bundle
                it.setHeaderBackPressListener(backPressListener)
                it.setCustomAdapter(adapter)
                it.setOnSendCombineMessageCallback(combineMessageCallback)
                it.setOnCombineMessageDownloadCallback(historyCallback)
            }
            return fragment
        }
    }

    private object Constant {
        const val KEY_EMPTY_LAYOUT = "key_empty_layout"
        const val KEY_MSG_TIME_COLOR = "key_msg_time_color"
        const val KEY_MSG_TIME_SIZE = "key_msg_time_size"
        const val KEY_CHAT_BACKGROUND = "key_chat_background"
    }

}