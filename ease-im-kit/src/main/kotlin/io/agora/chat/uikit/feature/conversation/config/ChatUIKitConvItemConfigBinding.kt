package io.agora.chat.uikit.feature.conversation.config

import android.util.TypedValue
import android.view.View
import io.agora.chat.uikit.common.extensions.maxUnreadCount
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.databinding.UikitItemConversationListBinding
import io.agora.chat.uikit.feature.conversation.interfaces.UnreadDotPosition
import io.agora.chat.uikit.feature.conversation.interfaces.UnreadStyle

/**
 * Binds [ChatUIKitConvItemConfig] with the conversation item view, setting the view's configs.
 */
fun ChatUIKitConvItemConfig.bindView(binding: UikitItemConversationListBinding) {
    with(binding) {
        if (itemNameTextSize != -1) name.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemNameTextSize.toFloat())
        if (itemNameTextColor != -1) name.setTextColor(itemNameTextColor)
        if (itemMessageTextSize != -1) message.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemMessageTextSize.toFloat())
        if (itemMessageTextColor != -1) message.setTextColor(itemMessageTextColor)
        if (itemDateTextSize != -1) time.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemDateTextSize.toFloat())
        if (itemDateTextColor != -1) time.setTextColor(itemDateTextColor)
        if (itemMentionTextSize != -1) mentioned.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemMentionTextSize.toFloat())
        if (itemMentionTextColor != -1) mentioned.setTextColor(itemMentionTextColor)
        if (avatarSize != -1) {
            val layoutParams = avatar.layoutParams
            layoutParams.height = avatarSize
            layoutParams.width = avatarSize
        }
        avatarConfig.setAvatarStyle(avatar)
        if (itemHeight != -1f) {
            val layoutParams = root.layoutParams
            layoutParams.height = itemHeight.toInt()
        }
    }
}

/**
 * Show unread count by the [unreadCount] and [ChatUIKitConvItemConfig].
 */
fun ChatUIKitConvItemConfig.showUnreadCount(unreadCount: Int, isSilent: Boolean, binding: UikitItemConversationListBinding) {
    with(binding) {
        unreadMsgNumber.visibility = View.GONE
        unreadMsgNumberRight.visibility = View.GONE
        unreadMsgDot.visibility = View.GONE
        unreadMsgDotRight.visibility = View.GONE
        if (unreadCount <= 0) return
        unreadMsgNumber.text = unreadCount.maxUnreadCount(root.context)
        unreadMsgNumberRight.text = unreadCount.maxUnreadCount(root.context)
        if (unreadDotPosition == UnreadDotPosition.LEFT) {
            unreadMsgNumber.visibility = View.VISIBLE
            if (unreadStyle != UnreadStyle.NUM || isSilent) {
                unreadMsgNumber.visibility = View.GONE
                unreadMsgDot.visibility = View.VISIBLE
            }
        } else {
            unreadMsgNumberRight.visibility = View.VISIBLE
            if (unreadStyle != UnreadStyle.NUM || isSilent) {
                unreadMsgNumberRight.visibility = View.GONE
                unreadMsgDotRight.visibility = View.VISIBLE
            }
        }
    }
}