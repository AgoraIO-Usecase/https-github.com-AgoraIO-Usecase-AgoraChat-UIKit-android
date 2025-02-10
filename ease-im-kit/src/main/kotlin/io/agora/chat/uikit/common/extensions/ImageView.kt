package io.agora.chat.uikit.common.extensions

import android.app.Activity
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import coil.load
import coil.request.CachePolicy
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatImageMessageBody
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatVideoMessageBody
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitSize
import io.agora.chat.uikit.widget.ChatUIKitImageView


/**
 * Load user avatar from ChatUIKitProfile.
 * @param profile ChatUIKitProfile
 * @param placeholder placeholder drawable
 * @param errorPlaceholder error placeholder drawable
 */
internal fun ImageView.loadAvatar(profile: ChatUIKitProfile?,
                                  placeholder: Drawable? = null,
                                  errorPlaceholder: Drawable? = null) {

    val ph = placeholder ?: AppCompatResources.getDrawable(context, R.drawable.uikit_default_avatar)
    val ep = errorPlaceholder ?: AppCompatResources.getDrawable(context, R.drawable.uikit_default_avatar)
    load(profile?.avatar ?: ph) {
        placeholder(ph)
        error(ep)
    }
}

/**
 * Load user avatar from message's ext.
 * @param message ChatMessage
 * @param placeholder placeholder drawable
 */
internal fun ImageView.loadAvatar(message: ChatMessage?,
                                  placeholder: Drawable? = null,
                                  errorPlaceholder: Drawable? = null) {

    if (message == null) {
        loadAvatar(ChatUIKitProfile(""), placeholder, errorPlaceholder)
        return
    }
    loadAvatar(message.getUserInfo(), placeholder, errorPlaceholder)
}

internal fun ImageView.loadImageFromLocalOrUrl(localUri: Uri?
                                               , remoteUrl: String?
                                               , showSize: ChatUIKitSize?
                                               , @DrawableRes placeholder: Int
                                               , onSuccess: () -> Unit = {}
                                               , onError: (Throwable) -> Unit = {}) {
    load(localUri ?: remoteUrl) {
        error(placeholder)
        fallback(placeholder)
        diskCachePolicy(CachePolicy.ENABLED)
        if (showSize?.isEmpty() == false) {
            size(showSize.width, showSize.height)
        }
        listener(
            onStart = {
                ChatLog.d("msg", "start load image")
            },
            onSuccess = { _, _ ->
                ChatLog.d("msg", "load image success")
                showSize?.let {
                    layoutParams = ViewGroup.LayoutParams(it.width, it.height)
                }
                onSuccess.invoke()
            },
            onError = { _, throwable ->
                ChatLog.e("msg", "load image error: ${throwable.throwable.message}")
                onError.invoke(throwable.throwable)
            }
        )
    }

}

/**
 * Load image from image or video message.
 * Note:
 * If [ChatOptions.setAutoTransferMessageAttachments] is true, will load image from SDK interface;
 * otherwise will load image remote url.
 */
fun ImageView.loadImageFromMessage(message: ChatMessage?
                                   , scaleType: ScaleType = ScaleType.CENTER_CROP
                                   , onSuccess: () -> Unit = {}
                                   , onError: (Throwable) -> Unit = {}) {
    message?.let { msg ->
        if (msg.type != ChatMessageType.IMAGE && msg.type != ChatMessageType.VIDEO) {
            ChatLog.e("ImageView", "loadImageFromMessage: message type is not image or video")
            return
        }
        if (context is Activity && ((context as Activity).isFinishing || (context as Activity).isDestroyed)) {
            return
        }
        val showSize = msg.getImageShowSize(context)
        val imageLocalUri = msg.getThumbnailLocalUri(context)
        setScaleType(scaleType)
        if (imageLocalUri != null && imageLocalUri.isFileExist(context)) {
            loadImageFromLocalOrUrl(imageLocalUri
                , null
                , showSize
                , if (msg.type == ChatMessageType.IMAGE) R.drawable.uikit_default_image
                else R.drawable.uikit_default_video_thumbnail
                , onSuccess
                , onError = {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                        , ViewGroup.LayoutParams.WRAP_CONTENT)
                    onError.invoke(it)
                })
            return
        }
        // if not auto transfer image, get the remote url
        if (!ChatClient.getInstance().options.autoTransferMessageAttachments) {
            ChatLog.e("ImageView", "loadImageFromMessage: autoTransferMessageAttachments is false, will show remote url")
            var remoteUrl: String? = null
            if (msg.type == ChatMessageType.IMAGE) {
                remoteUrl = (msg.body as ChatImageMessageBody).thumbnailUrl
                // Send image message may not have the thumbnail url, so get the remote url
                if (remoteUrl.isNullOrEmpty()) {
                    remoteUrl = (msg.body as ChatImageMessageBody).remoteUrl
                }
            } else if (msg.type == ChatMessageType.VIDEO) {
                remoteUrl = (msg.body as ChatVideoMessageBody).thumbnailUrl
            }
            loadImageFromLocalOrUrl(null
                , remoteUrl
                , showSize
                , if (msg.type == ChatMessageType.IMAGE) R.drawable.uikit_default_image
                else R.drawable.uikit_default_video_thumbnail
                , onSuccess
                , onError = {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                        , ViewGroup.LayoutParams.WRAP_CONTENT)
                    onError.invoke(it)
                })
        }
    }
}

/**
 * Set avatar common config.
 */
fun ChatUIKitImageView.setAvatarConfig() {
    ChatUIKitClient.getConfig()?.avatarConfig?.let {
        if (it.getAvatarShapeIncludeDefault() != ChatUIKitImageView.ShapeType.NONE) setShapeType(it.getAvatarShapeIncludeDefault())
        if (it.getAvatarRadiusIncludeDefault() != -1) setRadius(it.getAvatarRadiusIncludeDefault())
        if (it.getAvatarBorderWidthIncludeDefault() != -1) {
            setBorderWidth(it.getAvatarBorderWidthIncludeDefault())
            if (it.getAvatarBorderColorIncludeDefault() != -1) setBorderColor(it.getAvatarBorderColorIncludeDefault())
        }
    }
}