package io.agora.chat.uikit.common.interfaces

import android.graphics.drawable.Drawable
import io.agora.chat.uikit.widget.ChatUIKitImageView

interface IAvatarStyle {
    /**
     * Set default avatar
     * @param src
     */
    fun setAvatarDefaultSrc(src: Drawable?) {}

    /**
     * Set the size of the avatar, the length and width are the same
     * @param avatarSize
     */
    fun setAvatarSize(avatarSize: Float)

    /**
     * Set avatar style
     * @param shapeType
     */
    fun setAvatarShapeType(shapeType: ChatUIKitImageView.ShapeType)

    /**
     * Set avatar radius
     * @param radius
     */
    fun setAvatarRadius(radius: Int)

    /**
     * Set the width of the outer border
     * @param borderWidth
     */
    fun setAvatarBorderWidth(borderWidth: Int)

    /**
     * Set the outer border color
     * @param borderColor
     */
    fun setAvatarBorderColor(borderColor: Int)
}