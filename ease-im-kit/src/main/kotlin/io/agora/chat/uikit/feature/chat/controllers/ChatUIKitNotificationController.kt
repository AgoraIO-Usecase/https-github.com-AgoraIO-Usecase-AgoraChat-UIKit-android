package io.agora.chat.uikit.feature.chat.controllers

import android.view.View
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.databinding.UikitLayoutChatBinding
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitUnreadNotificationView
import io.agora.chat.uikit.viewmodel.messages.IChatViewRequest
import kotlinx.coroutines.launch

class ChatUIKitNotificationController(
    private val binding: UikitLayoutChatBinding,
    private val conversationId: String?,
    private val viewModel: IChatViewRequest?
) {

    private var shouldDismiss: Boolean = false

    init {
        if (ChatUIKitClient.getConfig()?.chatConfig?.showUnreadNotificationInChat == true && !shouldDismiss) {
            showNotificationView(false)
            binding.layoutNotification.setOnNotificationClickListener {
                dismissNotificationView()
                binding.layoutChatMessage.isCanAutoScrollToBottom = true
                binding.layoutChatMessage.refreshToLatest()
            }
        }
    }

    fun showNotificationView(isShow: Boolean) {
        if (ChatUIKitClient.getConfig()?.chatConfig?.showUnreadNotificationInChat == false || shouldDismiss) return
        binding.layoutNotification.context.mainScope().launch {
            binding.layoutNotification.visibility = if (isShow) {
                updateNotificationView()
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    fun isNotificationViewShow(): Boolean {
        return binding.layoutNotification.visibility == View.VISIBLE
    }

    fun updateNotificationView() {
        if (binding.layoutChatMessage.isCanAutoScrollToBottom || shouldDismiss) return
        binding.layoutNotification.context.mainScope().launch {
            binding.layoutNotification.getNotificationView()?.let {
                if (it is ChatUIKitUnreadNotificationView) {
                    ChatClient.getInstance().chatManager().getConversation(conversationId)?.let { conv ->
                        binding.root.context.mainScope().launch {
                            it.updateUnreadCount(conv.unreadMsgCount)
                        }
                    }
                }
            }
        }
    }

    fun dismissNotificationView() {
        if (ChatUIKitClient.getConfig()?.chatConfig?.showUnreadNotificationInChat == false || shouldDismiss) return
        showNotificationView(false)
        binding.layoutChatMessage.isCanAutoScrollToBottom = true
        ChatClient.getInstance().chatManager().getConversation(conversationId)?.let {
            if (it.unreadMsgCount > 0) {
                it.markAllMessagesAsRead()
                viewModel?.sendChannelAck()
            }
        }
    }

    fun setShouldDismiss(shouldDismiss: Boolean) {
        this.shouldDismiss = shouldDismiss
    }

}