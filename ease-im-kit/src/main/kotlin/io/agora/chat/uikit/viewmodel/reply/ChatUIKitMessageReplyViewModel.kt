package io.agora.chat.uikit.viewmodel.reply

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.TextAppearanceSpan
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.viewmodel.ChatUIKitBaseViewModel
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatFileMessageBody
import io.agora.chat.uikit.common.ChatImageMessageBody
import io.agora.chat.uikit.common.ChatLocationMessageBody
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.ChatVideoMessageBody
import io.agora.chat.uikit.common.ChatVoiceMessageBody
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.extensions.getEmojiText
import io.agora.chat.uikit.common.extensions.getUserCardInfo
import io.agora.chat.uikit.common.extensions.getUserInfo
import io.agora.chat.uikit.common.extensions.isUserCardMessage
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.common.utils.ChatUIKitFileUtils
import io.agora.chat.uikit.feature.chat.internal.setTargetSpan
import io.agora.chat.uikit.feature.chat.reply.interfaces.IChatMessageReplyResultView
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.getNickname

open class ChatUIKitMessageReplyViewModel: ChatUIKitBaseViewModel<IChatMessageReplyResultView>(), IChatMessageReplyRequest {

    override fun showQuoteMessageInfo(message: ChatMessage?) {
        if (message == null || message.body == null) {
            view?.onShowError(ChatError.GENERAL_ERROR, "Message or body cannot be null.")
            return
        }
        val user: ChatUIKitUser? = message.getUserInfo()?.toUser()
        var from:String? = null
        from = if (user == null) {
            message.from
        } else {
            user.getRemarkOrName()
        }
        view?.showQuoteMessageNickname(from)
        val builder = SpannableStringBuilder()
        var localPath: String? = null
        var remoteUrl: String? = null
        when (message.type) {
            ChatMessageType.TXT -> if (message.getBooleanAttribute(
                    ChatUIKitConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION,
                    false
                )
            ) {
                builder.append(ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_emoji_type))
            } else {
                val textBody = message.body as ChatTextMessageBody
                builder.append(
                    "${textBody.message.getEmojiText(ChatUIKitClient.getContext()!!)}"
                    ).toString()
            }

            ChatMessageType.VOICE -> {
                val voiceBody = message.body as ChatVoiceMessageBody
                builder.append(ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_voice_type))
                    .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                    .append(" ")
                    .append((if (voiceBody.length > 0) voiceBody.length else 0).toString() + "\"")
                view?.showQuoteMessageAttachment(
                    ChatMessageType.VOICE,
                    null,
                    null,
                    R.drawable.uikit_chatfrom_voice_playing
                )
            }

            ChatMessageType.VIDEO -> {
                builder.append(ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_video_type))
                    .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                val videoBody = message.body as ChatVideoMessageBody
                videoBody?.let {
                    if (!TextUtils.isEmpty(it.localThumb) && ChatUIKitFileUtils.isFileExistByUri(
                            ChatUIKitClient.getContext(),
                            it.localThumbUri
                        )
                    ) {
                        localPath = it.localThumb
                    }
                    remoteUrl = it.thumbnailUrl
                    view?.showQuoteMessageAttachment(
                        ChatMessageType.VIDEO,
                        localPath,
                        remoteUrl,
                        R.drawable.uikit_chat_quote_icon_video
                    )
                }

            }

            ChatMessageType.FILE -> {
                val fileBody = message.body as ChatFileMessageBody
                builder.append(ChatUIKitClient.getContext()?.getResources()?.getString(R.string.uikit_message_reply_file_type))
                    .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                    .append(" ")
                    .append(fileBody.fileName)
                view?.showQuoteMessageAttachment(
                    ChatMessageType.FILE,
                    null,
                    null,
                    R.drawable.uikit_chat_quote_message_attachment
                )
            }

            ChatMessageType.IMAGE -> {
                builder.append(ChatUIKitClient.getContext()?.getResources()?.getString(R.string.uikit_message_reply_image_type))
                    .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                val imageBody = message.body as ChatImageMessageBody
                imageBody?.let {
                    if (!imageBody.thumbnailUrl.isNullOrEmpty() && ChatUIKitFileUtils.isFileExistByUri(
                            ChatUIKitClient.getContext(), imageBody.thumbnailLocalUri())) {
                        localPath = imageBody.thumbnailLocalPath()
                    } else if (!imageBody.localUrl.isNullOrEmpty() && ChatUIKitFileUtils.isFileExistByUri(
                        ChatUIKitClient.getContext(), imageBody.localUri)) {
                        localPath = imageBody.localUrl
                    }
                    remoteUrl = imageBody.remoteUrl
                    view?.showQuoteMessageAttachment(
                        ChatMessageType.IMAGE,
                        localPath,
                        remoteUrl,
                        R.drawable.uikit_chat_quote_icon_image
                    )
                }
            }

            ChatMessageType.LOCATION -> {
                val locationBody = message.body as ChatLocationMessageBody
                builder.append(ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_location_type))
                    .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                        0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                if (locationBody != null && !TextUtils.isEmpty(locationBody.address)) {
                    builder.append(": ").append(locationBody.address)
                }
            }

            ChatMessageType.CUSTOM -> {
                if (message.isUserCardMessage()) {
                    view?.showQuoteMessageAttachment(
                        ChatMessageType.CUSTOM,
                        null,
                        null,
                        R.drawable.uikit_chat_quote_icon_user_card
                    )
                    builder.append(message.getUserCardInfo()?.name)
                        .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                            0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    builder.append(ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_custom_type))
                        .setTargetSpan(TextAppearanceSpan(ChatUIKitClient.getContext(), R.style.ease_chat_message_reply_type_style),
                            0, builder.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            ChatMessageType.COMBINE -> {
                view?.showQuoteMessageAttachment(
                    ChatMessageType.COMBINE,
                    null,
                    null,
                    R.drawable.uikit_chat_quote_icon_combine
                )
                builder.append(
                    ChatUIKitClient.getContext()?.resources?.getString(R.string.uikit_message_reply_combine_type)
                )
            }

            else -> {}
        }
        view?.showQuoteMessageContent(builder)
    }

}