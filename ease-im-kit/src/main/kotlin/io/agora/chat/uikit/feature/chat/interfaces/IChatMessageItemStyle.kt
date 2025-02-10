package io.agora.chat.uikit.feature.chat.interfaces

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitMessageListLayout
import io.agora.chat.uikit.widget.ChatUIKitImageView

interface IChatMessageItemStyle {
    /**
     * Set default avatar.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param src
     */
    fun setAvatarDefaultSrc(src: Drawable?)

    /**
     * Set avatar shape.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param shapeType
     */
    fun setAvatarShapeType(shapeType: ChatUIKitImageView.ShapeType)

    /**
     * Whether to show nickname.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param showNickname
     */
    fun showNickname(showNickname: Boolean)

    /**
     * Set the background of the item sender.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param bg
     */
    fun setItemSenderBackground(bg: Int?)

    /**
     * Set the receiver's background.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param bg
     */
    fun setItemReceiverBackground(bg: Int?)

    /**
     * Set text message font size.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param textSize
     */
    fun setItemTextSize(textSize: Int)

    /**
     * Set text message font color.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param textColor
     */
    fun setItemTextColor(@ColorInt textColor: Int)

    /**
     * Set the timeline text size.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param textSize
     */
    fun setTimeTextSize(textSize: Int)

    /**
     * Set the timeline text color.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param textColor
     */
    fun setTimeTextColor(textColor: Int)

    /**
     * Set the timeline background.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param bgDrawable
     */
    fun setTimeBackground(bgDrawable: Drawable?)

    /**
     * Set the display style of the chat list.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param type
     */
    fun setItemShowType(type: ChatUIKitMessageListLayout.ShowType)

    /**
     * Hide receiver's avatar, default is false.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param hide
     */
    fun hideChatReceiveAvatar(hide: Boolean)

    /**
     * Hide sender's avatar, default is false.
     * After setting the parameter, should call the method [ChatUIKitMessageListLayout.notifyDataSetChanged]
     * to refresh the list.
     * @param hide
     */
    fun hideChatSendAvatar(hide: Boolean)
}