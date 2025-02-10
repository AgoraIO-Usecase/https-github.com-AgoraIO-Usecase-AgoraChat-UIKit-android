package io.agora.chat.uikit.feature.conversation.interfaces

import android.graphics.drawable.Drawable
import io.agora.chat.uikit.common.interfaces.IAvatarStyle

interface IConvItemStyle : IAvatarStyle, IConvItemTextStyle {
    fun setItemBackGround(backGround: Drawable?)
    fun setItemHeight(height: Int)

    /**
     * Unread display position
     * Currently supports left and right
     * @param position
     */
    fun showUnreadDotPosition(position: UnreadDotPosition)

    /**
     * Set unread view's style , see [UnreadStyle]
     * @param style
     */
    fun setUnreadStyle(style: UnreadStyle)
}

/**
 * Unread display position
 */
enum class UnreadDotPosition {
    LEFT, RIGHT
}

/**
 * Unread view's style
 */
enum class UnreadStyle {
    NUM, DOT
}