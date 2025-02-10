package io.agora.chat.uikit.feature.conversation.viewholders

import android.graphics.drawable.Drawable
import android.view.View
import androidx.viewbinding.ViewBinding
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatMessageDirection
import io.agora.chat.uikit.common.ChatMessageStatus
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.extensions.getChatroomName
import io.agora.chat.uikit.common.extensions.getEmojiText
import io.agora.chat.uikit.common.extensions.getGroupNameFromId
import io.agora.chat.uikit.common.extensions.getMessageDigest
import io.agora.chat.uikit.common.extensions.getTextHeight
import io.agora.chat.uikit.common.extensions.getDateFormat
import io.agora.chat.uikit.common.extensions.getUserInfo
import io.agora.chat.uikit.common.extensions.isAlertMessage
import io.agora.chat.uikit.common.helper.ChatUIKitAtMessageHelper
import io.agora.chat.uikit.common.helper.ChatUIKitPreferenceManager
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.databinding.UikitItemConversationListBinding
import io.agora.chat.uikit.feature.conversation.config.ChatUIKitConvItemConfig
import io.agora.chat.uikit.feature.conversation.config.bindView
import io.agora.chat.uikit.feature.conversation.config.showUnreadCount
import io.agora.chat.uikit.model.ChatUIKitConversation
import io.agora.chat.uikit.model.isChatRoom
import io.agora.chat.uikit.model.isGroupChat
import io.agora.chat.uikit.provider.getSyncProfile
import io.agora.chat.uikit.provider.getSyncUser

class ChatUIKitConversationViewHolder(
    private val viewBinding: UikitItemConversationListBinding,
    var config: ChatUIKitConvItemConfig? = ChatUIKitConvItemConfig()
): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitConversation>(binding = viewBinding) {
    private var bgDrawable: Drawable? = null

    init {
        config?.bindView(viewBinding)
    }

    override fun initView(viewBinding: ViewBinding?) {
        super.initView(viewBinding)
        viewBinding?.let {
            if (it is UikitItemConversationListBinding) {
                ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(it.avatar)
                bgDrawable = it.root.background
            }
        }
    }

    override fun setData(item: ChatUIKitConversation?, position: Int) {
        item?.let {
            with(viewBinding) {
                // Rest mentioned view's status.
                mentioned.visibility = View.GONE
                ivTopLabel.visibility = if (item.isPinned) View.VISIBLE else View.GONE
                msgMute.visibility = if (item.isSilent()) View.VISIBLE else View.GONE
                item.setOnSelectedListener{ isSelected->
                    if (isSelected) {
                        itemView.setBackgroundResource(R.drawable.uikit_conv_item_selected)
                    } else {
                        if (item.isPinned) {
                            itemView.setBackgroundResource(R.drawable.uikit_conv_item_pinned)
                        } else {
                            itemView.background = bgDrawable
                        }
                    }
                }
                // If conversation pinned, set background.
                if (item.isPinned) {
                    itemView.setBackgroundResource(R.drawable.uikit_conv_item_pinned)
                } else {
                    itemView.background = bgDrawable
                }
                // Set conversation avatar and name.
                var placeholderRes = R.drawable.uikit_default_avatar
                if (item.isGroupChat()) {
                    placeholderRes = R.drawable.uikit_default_group_avatar
                    avatar.setImageResource(placeholderRes)
                    name.text = item.conversationId.getGroupNameFromId()
                    // Set custom data provided by user
                    ChatUIKitClient.getGroupProfileProvider()?.getSyncProfile(item.conversationId)?.let { profile ->
                        avatar.load(profile.avatar) {
                            placeholder(placeholderRes)
                            error(placeholderRes)
                        }
                        if (profile.name.isNullOrEmpty()) {
                            name.text = profile.name
                        }
                    }
                } else if (item.isChatRoom()) {
                    placeholderRes = R.drawable.ease_default_chatroom_avatar
                    avatar.setImageResource(placeholderRes)
                    name.text = item.conversationId.getChatroomName()
                } else {
                    name.text = item.conversationId
                    avatar.setImageResource(placeholderRes)
                    ChatUIKitClient.getUserProvider()?.getSyncUser(item.conversationId)?.let { user ->
                        avatar.load(user.avatar) {
                            placeholder(placeholderRes)
                            error(placeholderRes)
                        }
                        name.text = user.getRemarkOrName()
                    }
                }




                // Set @ message in group chat
                if (item.isGroupChat()) {
                    if (ChatUIKitAtMessageHelper.get().hasAtMeMsg(item.conversationId)) {
                        mentioned.setText(R.string.uikit_chat_were_mentioned)
                        item.lastMessage?.run {
                            getStringAttribute(ChatUIKitConstant.MESSAGE_ATTR_AT_MSG, null)?.let { tag ->
                                if (tag == ChatUIKitConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL) {
                                    mentioned.setText(R.string.uikit_chat_were_mentioned_all)
                                }
                            }
                        }
                        mentioned.visibility = View.VISIBLE
                    }
                }

                config?.showUnreadCount(item.unreadMsgCount, item.isSilent(), this)

                // Set latest message and failed status
                item.lastMessage?.let {
                    if (it.chatType != ChatType.Chat) {
                        if (it.isAlertMessage()) {
                            message.text = it.getMessageDigest(itemView.context)
                                .getEmojiText(itemView.context, message.getTextHeight())
                        } else {
                            val name = it.getUserInfo()?.getRemarkOrName() ?: it.from
                            message.text = name.plus(": ").plus(it.getMessageDigest(itemView.context)
                                .getEmojiText(itemView.context, message.getTextHeight()))
                        }
                    } else {
                        message.text = it.getMessageDigest(itemView.context)
                            .getEmojiText(itemView.context, message.getTextHeight())
                    }
                    time.text = it.getDateFormat()
                    if (it.direct() == ChatMessageDirection.SEND && it.status() == ChatMessageStatus.FAIL) {
                        msgState.visibility = View.VISIBLE
                    } else {
                        msgState.visibility = View.GONE
                    }
                }?:kotlin.run {
                    message.text = ""
                }

                // Set un-send message info
                if (mentioned.visibility != View.VISIBLE) {
                    ChatUIKitPreferenceManager.getInstance().getUnSendMsgInfo(item.conversationId)?.let { info ->
                        if (info.isNotEmpty()) {
                            mentioned.setText(R.string.uikit_chat_were_not_send_msg)
                            message.text = info
                            mentioned.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}