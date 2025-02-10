package io.agora.chat.uikit.configs

import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.widget.ChatUIKitImageView

/**
 * Used to control the display style of user avatars in the SDK.
 * @param avatarShape The shape of the avatar, the default is [ChatUIKitAvatarShape.DEFAULT].
 * @param avatarRadius The radius of the avatar, the default is 0.
 * @param avatarBorderColor The border color of the avatar, the default is 0.
 * @param avatarBorderWidth The border width of the avatar, the default is 0.
 */
data class ChatUIKitAvatarConfig(
    var avatarShape: ChatUIKitImageView.ShapeType = ChatUIKitImageView.ShapeType.NONE,
    var avatarRadius: Int = -1,
    var avatarBorderColor: Int = -1,
    var avatarBorderWidth: Int = -1,
) {
    fun getAvatarShapeIncludeDefault(): ChatUIKitImageView.ShapeType {
        return if (avatarShape != ChatUIKitImageView.ShapeType.NONE) {
            avatarShape
        } else {
            if (ChatUIKitClient.isInited()) {
                when (ChatUIKitClient.getContext()?.resources?.getInteger(R.integer.ease_avatar_shape_type) ?: 0) {
                    0 -> ChatUIKitImageView.ShapeType.NONE
                    1 -> ChatUIKitImageView.ShapeType.ROUND
                    2 -> ChatUIKitImageView.ShapeType.RECTANGLE
                    else -> ChatUIKitImageView.ShapeType.NONE
                }
            } else {
                avatarShape
            }
        }
    }

    fun getAvatarRadiusIncludeDefault(): Int {
        return if (avatarRadius != -1) {
            avatarRadius
        } else {
            if (ChatUIKitClient.isInited()) {
                ChatUIKitClient.getContext()?.resources?.getDimensionPixelSize(R.dimen.ease_avatar_round_radius) ?: avatarRadius
            } else {
                avatarRadius
            }
        }
    }

    fun getAvatarBorderColorIncludeDefault(): Int {
        return if (avatarBorderColor != -1) {
            avatarBorderColor
        } else {
            if (ChatUIKitClient.isInited()) {
                if (ChatUIKitClient.getContext() == null) {
                    avatarBorderColor
                } else {
                    ContextCompat.getColor(ChatUIKitClient.getContext()!!, R.color.ease_avatar_border_color)
                }
            } else {
                avatarBorderColor
            }
        }
    }

    fun getAvatarBorderWidthIncludeDefault(): Int {
        return if (avatarBorderWidth != -1) {
            avatarBorderWidth
        } else {
            if (ChatUIKitClient.isInited()) {
                ChatUIKitClient.getContext()?.resources?.getDimensionPixelSize(R.dimen.ease_avatar_border_width) ?: avatarBorderWidth
            } else {
                avatarBorderWidth
            }
        }
    }
}

/**
 * Set avatar by [ChatUIKitAvatarConfig].
 */
fun ChatUIKitAvatarConfig.setAvatarStyle(avatar: ChatUIKitImageView?) {
    if (avatar == null) return
    if (getAvatarShapeIncludeDefault() != ChatUIKitImageView.ShapeType.NONE) avatar.setShapeType(getAvatarShapeIncludeDefault())
    if (getAvatarRadiusIncludeDefault() != -1) avatar.setRadius(getAvatarRadiusIncludeDefault())
    if (getAvatarBorderWidthIncludeDefault() != -1) {
        avatar.setBorderWidth(getAvatarBorderWidthIncludeDefault())
        if (getAvatarBorderColorIncludeDefault() != -1) avatar.setBorderColor(getAvatarBorderColorIncludeDefault())
    }
}

/**
 * Set status by [ChatUIKitAvatarConfig].
 */
fun ChatUIKitAvatarConfig.setStatusStyle(avatar: ChatUIKitImageView?,borderWidth:Int,@ColorInt color:Int){
    if (avatar == null) return
    avatar.setShapeType(ChatUIKitImageView.ShapeType.ROUND)
    if (getAvatarBorderWidthIncludeDefault() != -1) {
        avatar.setBorderWidth(borderWidth)
        avatar.setBorderColor(color)
    }
}