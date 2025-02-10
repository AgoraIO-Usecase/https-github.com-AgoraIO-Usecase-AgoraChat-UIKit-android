package io.agora.chat.uikit.common.helper

import android.content.Context
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.model.ChatUIKitDefaultEmojiIconData
import java.io.File
import java.util.regex.Pattern

object ChatUIKitEmojiHelper {
    const val DELETE_KEY = "em_delete_delete_expression"

    const val ee_1 = "😀"
    const val ee_2 = "😄"
    const val ee_3 = "😉"
    const val ee_4 = "😮"
    const val ee_5 = "🤪"
    const val ee_6 = "😎"
    const val ee_7 = "🥱"
    const val ee_8 = "🥴"
    const val ee_9 = "☺️"
    const val ee_10 = "🙁"
    const val ee_11 = "😭"
    const val ee_12 = "😐"
    const val ee_13 = "😇"
    const val ee_14 = "😬"
    const val ee_15 = "🤓"
    const val ee_16 = "😳"
    const val ee_17 = "🥳"
    const val ee_18 = "😠"
    const val ee_19 = "🙄"
    const val ee_20 = "🤐"
    const val ee_21 = "🥺"
    const val ee_22 = "🤨"
    const val ee_23 = "😫"
    const val ee_24 = "😷"
    const val ee_25 = "🤒"
    const val ee_26 = "😱"
    const val ee_27 = "😘"
    const val ee_28 = "😍"
    const val ee_29 = "🤢"
    const val ee_30 = "👿"
    const val ee_31 = "🤬"
    const val ee_32 = "😡"
    const val ee_33 = "👍"
    const val ee_34 = "👎"
    const val ee_35 = "👏"
    const val ee_36 = "🙌"
    const val ee_37 = "🤝"
    const val ee_38 = "🙏"
    const val ee_39 = "❤️"
    const val ee_40 = "💔"
    const val ee_41 = "💕"
    const val ee_42 = "💩"
    const val ee_43 = "💋"
    const val ee_44 = "☀️"
    const val ee_45 = "🌜"
    const val ee_46 = "🌈"
    const val ee_47 = "⭐"
    const val ee_48 = "🌟"
    const val ee_49 = "🎉"
    const val ee_50 = "💐"
    const val ee_51 = "🎂"
    const val ee_52 = "🎁"

    private const val o_ee_1 = "[):]"
    private const val o_ee_2 = "[:D]"
    private const val o_ee_3 = "[;)]"
    private const val o_ee_4 = "[:-o]"
    private const val o_ee_5 = "[:p]"
    private const val o_ee_6 = "[(H)]"
    private const val o_ee_7 = "[:@]"
    private const val o_ee_8 = "[:s]"
    private const val o_ee_9 = "[:$]"
    private const val o_ee_10 = "[:(]"
    private const val o_ee_11 = "[:'(]"
    private const val o_ee_12 = "[:|]"
    private const val o_ee_13 = "[(a)]"
    private const val o_ee_14 = "[8o|]"
    private const val o_ee_15 = "[8-|]"
    private const val o_ee_16 = "[+o(]"
    private const val o_ee_17 = "[<o)]"
    private const val o_ee_18 = "[|-)]"
    private const val o_ee_19 = "[*-)]"
    private const val o_ee_20 = "[:-#]"
    private const val o_ee_21 = "[:-*]"
    private const val o_ee_22 = "[^o)]"
    private const val o_ee_23 = "[8-)]"
    private const val o_ee_24 = "[(|)]"
    private const val o_ee_25 = "[(u)]"
    private const val o_ee_26 = "[(S)]"
    private const val o_ee_27 = "[(*)]"
    private const val o_ee_28 = "[(#)]"
    private const val o_ee_29 = "[(R)]"
    private const val o_ee_30 = "[({)]"
    private const val o_ee_31 = "[(})]"
    private const val o_ee_32 = "[(k)]"
    private const val o_ee_33 = "[(F)]"
    private const val o_ee_34 = "[(W)]"
    private const val o_ee_35 = "[(D)]"
    private const val o_ee_36 = "[(E)]"
    private const val o_ee_37 = "[(T)]"
    private const val o_ee_38 = "[(G)]"
    private const val o_ee_39 = "[(Y)]"
    private const val o_ee_40 = "[(I)]"
    private const val o_ee_41 = "[(J)]"
    private const val o_ee_42 = "[(K)]"
    private const val o_ee_43 = "[(L)]"
    private const val o_ee_44 = "[(M)]"
    private const val o_ee_45 = "[(N)]"
    private const val o_ee_46 = "[(O)]"
    private const val o_ee_47 = "[(P)]"
    private const val o_ee_48 = "[(U)]"
    private const val o_ee_49 = "[(Z)]"
    private const val o_ee_50 = "[-)]"
    private const val o_ee_51 = "[:-]"
    private const val o_ee_52 = "☺"

    private val mapping: Map<String, String> = mapOf(
        o_ee_1 to "☺️",
        o_ee_2 to "😄",
        o_ee_3 to "😉",
        o_ee_4 to "😮",
        o_ee_5 to "\uD83E\uDD2A",
        o_ee_6 to "😎",
        o_ee_7 to "😡",
        o_ee_8 to "\uD83E\uDD74",
        o_ee_9 to "😳",
        o_ee_10 to "🙁",
        o_ee_11 to "😭",
        o_ee_12 to "😐",
        o_ee_13 to "😇",
        o_ee_14 to "😬",
        o_ee_15 to "\uD83E\uDD13",
        o_ee_16 to "😱",
        o_ee_17 to "\uD83C\uDF85",
        o_ee_18 to "\uD83D\uDE34",
        o_ee_19 to "😕",
        o_ee_20 to "😷",
        o_ee_21 to "😯",
        o_ee_22 to "\uD83D\uDE0F",
        o_ee_23 to "\uD83D\uDE11",
        o_ee_24 to "❤️",
        o_ee_25 to "💔",
        o_ee_26 to "🌜",
        o_ee_27 to "🌟",
        o_ee_28 to "☀️",
        o_ee_29 to "🌈",
        o_ee_30 to "😍",
        o_ee_31 to "😘",
        o_ee_32 to "💋",
        o_ee_33 to "🌹",
        o_ee_34 to "\uD83C\uDF42",
        o_ee_35 to "👍",
        o_ee_36 to "😂",
        o_ee_37 to "🤗",
        o_ee_38 to "👏",
        o_ee_39 to "🤝",
        o_ee_40 to "👍",
        o_ee_41 to "👎",
        o_ee_42 to "👌",
        o_ee_43 to "❤️",
        o_ee_44 to "💔",
        o_ee_45 to "💣",
        o_ee_46 to "💩",
        o_ee_47 to "🌹",
        o_ee_48 to "🙏",
        o_ee_49 to "🎉",
        o_ee_50 to "🤢",
        o_ee_51 to "🙄",
        o_ee_52 to "☺️",

    )

    private val emojiMap by lazy { mutableMapOf<String, Any>() }

    init {
        // Add default emoji.
        ChatUIKitDefaultEmojiIconData.data.run {
            forEach { item ->
                item?.run {
                    addPattern(emojiText, icon)
                }
            }
        }
        // Add custom emoji.
        ChatUIKitClient.getEmojiconInfoProvider()?.textEmojiconMapping?.run {
            forEach { item ->
                item.run {
                    addPattern(key, value)
                }
            }
        }
    }

    private fun addPattern(emojiText: String?, icon: Any?) {
        if (emojiText.isNullOrEmpty() || icon == null) {
            return
        }
        emojiMap[emojiText] = icon
    }

    /**
     * Replace the emoji icon in the string with the corresponding icon.
     * @param context
     * @param spannable
     * @return
     */
    fun addEmojis(context: Context, spannable: Spannable, emojiIconSize: Int){
        mappingEmoji(context, spannable, emojiIconSize)
        emojiMappingToIcon(context,spannable,emojiIconSize)
    }

    private fun mappingEmoji(context: Context, spannable: Spannable, emojiIconSize: Int): Boolean {
        var hasChanges = false
        val matcherIndexPairs = mutableListOf<Pair<Int, Int>>()
        for ((key, value) in mapping.entries) {
            val matcher = Pattern.compile(Pattern.quote(key)).matcher(spannable)
            matcherIndexPairs.clear()
            while (matcher.find()) {
                var set = true
                for (span in spannable.getSpans(
                    matcher.start(),
                    matcher.end(), ImageSpan::class.java)) {
                    if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end()) {
                        spannable.removeSpan(span)
                    } else {
                        set = false
                        break
                    }
                }
                if (set) {
                    hasChanges = true
                    if (emojiMap.containsKey(value)) {
                        val iconValue = emojiMap[value]
                        if (iconValue is Int) {
                            val drawable = ContextCompat.getDrawable(context, iconValue)
                            drawable?.let {
                                val width = if (it.intrinsicWidth < emojiIconSize) it.intrinsicWidth else emojiIconSize
                                val height = if (it.intrinsicHeight < emojiIconSize) it.intrinsicHeight else emojiIconSize
                                it.setBounds(
                                    0, 0, width,
                                    height
                                )
                                spannable.setSpan(
                                    ImageSpan(it),
                                    matcher.start(), matcher.end(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                        }
                    } else {
                        matcherIndexPairs.add(Pair(matcher.start(), matcher.end()))
                    }
                }

            }
            if (matcherIndexPairs.isNotEmpty()) {
                if (spannable is SpannableStringBuilder) {
                    for (i in matcherIndexPairs.reversed()) {
                        spannable.replace(i.first, i.second, value)
                    }
                }
            }
        }
        return hasChanges
    }

    private fun emojiMappingToIcon(context: Context,spannable: Spannable,emojiIconSize: Int): Boolean{
        var hasChanges = false
        for ((key, value) in emojiMap.entries) {
            val matcher = Pattern.compile(Pattern.quote(key)).matcher(spannable)
            while (matcher.find()) {
                var set = true
                for (span in spannable.getSpans(
                    matcher.start(),
                    matcher.end(), ImageSpan::class.java)) {
                    if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end()) {
                        spannable.removeSpan(span)
                    } else {
                        set = false
                        break
                    }
                }
                if (set) {
                    hasChanges = true
                    if (value is String && !value.startsWith("http")) {
                        val file = File(value as String)
                        if (!file.exists() || file.isDirectory) {
                            return false
                        }
                        spannable.setSpan(
                            ImageSpan(context, Uri.fromFile(file)),
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        if (value is Int) {
                            val drawable = ContextCompat.getDrawable(context, value)
                            drawable?.let {
                                val width = if (it.intrinsicWidth < emojiIconSize) it.intrinsicWidth else emojiIconSize
                                val height = if (it.intrinsicHeight < emojiIconSize) it.intrinsicHeight else emojiIconSize
                                it.setBounds(
                                    0, 0, width,
                                    height
                                )
                                spannable.setSpan(
                                    ImageSpan(it),
                                    matcher.start(), matcher.end(),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                        }
                    }
                }
            }
        }
        return hasChanges
    }

    /**
     * Map the emoji text in the string to the corresponding icon.
     * @param context
     * @param text
     */
    fun getEmojiText(context: Context, text: CharSequence
                     , emojiIconSize: Int = context.resources.getDimensionPixelSize(
            R.dimen.ease_chat_emoji_icon_size_show_in_spannable)
    ): Spannable {
        val spannable = SpannableStringBuilder(text)
        addEmojis(context, spannable, emojiIconSize)
        return spannable
    }
}