package io.agora.chat.uikit.common.extensions

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.PinyinHelper
import io.agora.chat.uikit.common.helper.ChatUIKitEmojiHelper
import java.util.regex.Pattern


/**
 * Get the initial letter of the string.
 */
fun String.getInitialLetter(): String {
    val defaultLetter = "#"
    if (this.isEmpty()) {
        return defaultLetter
    }
    val firstWord = this.getUpperFirstWord()
    if (firstWord.isNotEmpty()){
        return firstWord
    }
    return defaultLetter
}

internal fun String.getUpperFirstWord(): String {
    val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(this[0])
    return if (pinyinArray == null) {
        if (this[0] in 'a'..'z' || this[0] in 'A'..'Z') {
            this[0].uppercase()
        } else {
            "#"
        }
    } else {
        pinyinArray[0][0].uppercase()
    }
}

/**
 * Get group name by group id.
 */
internal fun String.getGroupNameFromId(): String {
    ChatClient.getInstance().groupManager().getGroup(this)?.let {
        return it.groupName
    } ?: return this
}

/**
 * Get chatroom name by chatroom id.
 */
internal fun String.getChatroomName(): String {
    ChatClient.getInstance().chatroomManager().getChatRoom(this)?.let {
        return it.name
    } ?: return this
}

/**
 * Get emoji text from text message.
 */
internal fun String.getEmojiText(context: Context
                                 , emojiIconSize: Int = context.resources.getDimensionPixelSize(
    R.dimen.ease_chat_emoji_icon_size_show_in_spannable)
): Spannable {
    return ChatUIKitEmojiHelper.getEmojiText(context, this, emojiIconSize)
}

/**
 * Whether the string contains url.
 */
internal fun String.containsUrl(): Boolean {
    return Pattern.compile(URL_REGEX).matcher(this).find()
}

/**
 * Highlight the target text in the string.
 */
fun String.highlightTargetText(target: String?, @ColorInt color: Int): Spannable {
    if (target.isNullOrEmpty()) {
        return SpannableString(this)
    }
    val spannableString = SpannableString(this)
    val pattern = Pattern.compile(target, Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(this)
    while (matcher.find()) {
        val start = matcher.start()
        val end = matcher.end()
        spannableString.setSpan(
            ForegroundColorSpan(color),
            start, end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannableString
}