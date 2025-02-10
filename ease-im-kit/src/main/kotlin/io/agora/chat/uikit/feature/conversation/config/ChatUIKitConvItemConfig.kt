package io.agora.chat.uikit.feature.conversation.config

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.configs.ChatUIKitAvatarConfig
import io.agora.chat.uikit.feature.conversation.interfaces.UnreadDotPosition
import io.agora.chat.uikit.feature.conversation.interfaces.UnreadStyle
import io.agora.chat.uikit.widget.ChatUIKitImageView

/**
 * Conversation item configuration
 *
 * @param itemNameTextSize The size of the conversation name text
 * @param itemNameTextColor The color of the conversation name text
 * @param itemMessageTextSize The size of the conversation's latest message text
 * @param itemMessageTextColor The color of the conversation's latest message text
 * @param itemDateTextSize The size of the conversation's latest message time text
 * @param itemDateTextColor The color of the conversation's latest message time text
 * @param itemMentionTextSize The size of the conversation's mention text
 * @param itemMentionTextColor The color of the conversation's mention text
 * @param unreadDotPosition Unread display position, see [UnreadDotPosition]
 * @param unreadStyle Set unread view's style , see [UnreadStyle]
 * @param avatarSize The size of the avatar
 * @param avatarConfig The config of avatar, see [ChatUIKitAvatarConfig]
 * @param itemHeight The height of the conversation item
 */
data class ChatUIKitConvItemConfig(
    var itemNameTextSize: Int = -1,
    var itemNameTextColor: Int = -1,
    var itemMessageTextSize: Int = -1,
    var itemMessageTextColor: Int = -1,
    var itemDateTextSize: Int = -1,
    var itemDateTextColor: Int = -1,
    var itemMentionTextSize: Int = -1,
    var itemMentionTextColor: Int = -1,
    var unreadDotPosition: UnreadDotPosition = UnreadDotPosition.RIGHT,
    var unreadStyle: UnreadStyle = UnreadStyle.NUM,
    var avatarSize: Int = -1,
    var avatarConfig: ChatUIKitAvatarConfig = ChatUIKitClient.getConfig()?.avatarConfig?.copy() ?: ChatUIKitAvatarConfig(),
    var itemHeight: Float = -1f,
) {
    internal companion object {

        operator fun invoke(context: Context, attrs: AttributeSet?): ChatUIKitConvItemConfig {
            val itemConfig = ChatUIKitConvItemConfig()
            context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitConversationListLayout).let { a ->
                a.getDimension(R.styleable.ChatUIKitConversationListLayout_ease_con_item_title_text_size, -1f).let {
                    if (it != -1f) itemConfig.itemNameTextSize = it.toInt()
                }
                a.getResourceId(R.styleable.ChatUIKitConversationListLayout_ease_con_item_title_text_color, -1).let {
                    if (it != -1) itemConfig.itemNameTextColor = ContextCompat.getColor(context, it)
                }
                a.getColor(R.styleable.ChatUIKitConversationListLayout_ease_con_item_title_text_color, -1).let {
                    if (it != -1) itemConfig.itemNameTextColor = it
                }
                a.getDimension(R.styleable.ChatUIKitConversationListLayout_ease_con_item_content_text_size, -1f).let {
                    if (it != -1f) itemConfig.itemMessageTextSize = it.toInt()
                }
                a.getResourceId(R.styleable.ChatUIKitConversationListLayout_ease_con_item_content_text_color, -1).let {
                    if (it != -1) itemConfig.itemMessageTextColor = ContextCompat.getColor(context, it)
                }
                a.getColor(R.styleable.ChatUIKitConversationListLayout_ease_con_item_content_text_color, -1).let {
                    if (it != -1) itemConfig.itemMessageTextColor = it
                }
                a.getDimension(R.styleable.ChatUIKitConversationListLayout_ease_con_item_date_text_size, -1f).let {
                    if (it != -1f) itemConfig.itemDateTextSize = it.toInt()
                }
                a.getResourceId(R.styleable.ChatUIKitConversationListLayout_ease_con_item_date_text_color, -1).let {
                    if (it != -1) itemConfig.itemDateTextColor = ContextCompat.getColor(context, it)
                }
                a.getColor(R.styleable.ChatUIKitConversationListLayout_ease_con_item_date_text_color, -1).let {
                    if (it != -1) itemConfig.itemDateTextColor = it
                }
                a.getDimension(R.styleable.ChatUIKitConversationListLayout_ease_con_item_mention_text_size, -1f).let {
                    if (it != -1f) itemConfig.itemMentionTextSize = it.toInt()
                }
                a.getResourceId(R.styleable.ChatUIKitConversationListLayout_ease_con_item_mention_text_color, -1).let {
                    if (it != -1) itemConfig.itemMentionTextColor = ContextCompat.getColor(context, it)
                }
                a.getColor(R.styleable.ChatUIKitConversationListLayout_ease_con_item_mention_text_color, -1).let {
                    if (it != -1) itemConfig.itemMentionTextColor = it
                }
                a.getInt(R.styleable.ChatUIKitConversationListLayout_ease_con_item_unread_dot_position, UnreadDotPosition.RIGHT.ordinal).let {
                    itemConfig.unreadDotPosition = when (it) {
                        0 -> UnreadDotPosition.LEFT
                        else -> UnreadDotPosition.RIGHT
                    }
                }
                a.getInt(R.styleable.ChatUIKitConversationListLayout_ease_con_item_unread_style, 0).let {
                    itemConfig.unreadStyle = when (it) {
                        0 -> UnreadStyle.NUM
                        else -> UnreadStyle.DOT
                    }
                }
                a.getDimension(R.styleable.ChatUIKitConversationListLayout_ease_con_item_avatar_size, -1f).let {
                    if (it != -1f) itemConfig.avatarSize = it.toInt()
                }
                a.getInt(R.styleable.ChatUIKitConversationListLayout_ease_con_item_avatar_shape_type, -1).let {
                    if (it != -1) {
                        itemConfig.avatarConfig.avatarShape = when (it) {
                            0 -> ChatUIKitImageView.ShapeType.NONE
                            1 -> ChatUIKitImageView.ShapeType.ROUND
                            else -> ChatUIKitImageView.ShapeType.RECTANGLE
                        }
                    }
                }
                a.getDimension(R.styleable.ChatUIKitConversationListLayout_ease_con_item_avatar_border_width, -1f).let {
                    if (it != -1f) itemConfig.avatarConfig.avatarBorderWidth = it.toInt()
                }
                a.getResourceId(R.styleable.ChatUIKitConversationListLayout_ease_con_item_avatar_border_color, -1).let {
                    if (it != -1) itemConfig.avatarConfig.avatarBorderColor = ContextCompat.getColor(context, it)
                }
                a.getColor(R.styleable.ChatUIKitConversationListLayout_ease_con_item_avatar_border_color, -1).let {
                    if (it != -1) itemConfig.avatarConfig.avatarBorderColor = it
                }
                a.getDimension(R.styleable.ChatUIKitConversationListLayout_ease_con_item_height, -1f).let {
                    if (it != -1f) itemConfig.itemHeight = it
                }
                a.recycle()
            }
            return itemConfig
        }
    }
}
