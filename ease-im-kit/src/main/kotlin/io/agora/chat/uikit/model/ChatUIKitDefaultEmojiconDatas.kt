package io.agora.chat.uikit.model

import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.helper.ChatUIKitEmojiHelper
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitReactionType

object ChatUIKitDefaultEmojiIconData {
    private val emojis = arrayOf<String>(
        ChatUIKitEmojiHelper.ee_1,
        ChatUIKitEmojiHelper.ee_2,
        ChatUIKitEmojiHelper.ee_3,
        ChatUIKitEmojiHelper.ee_4,
        ChatUIKitEmojiHelper.ee_5,
        ChatUIKitEmojiHelper.ee_6,
        ChatUIKitEmojiHelper.ee_7,
        ChatUIKitEmojiHelper.ee_8,
        ChatUIKitEmojiHelper.ee_9,
        ChatUIKitEmojiHelper.ee_10,
        ChatUIKitEmojiHelper.ee_11,
        ChatUIKitEmojiHelper.ee_12,
        ChatUIKitEmojiHelper.ee_13,
        ChatUIKitEmojiHelper.ee_14,
        ChatUIKitEmojiHelper.ee_15,
        ChatUIKitEmojiHelper.ee_16,
        ChatUIKitEmojiHelper.ee_17,
        ChatUIKitEmojiHelper.ee_18,
        ChatUIKitEmojiHelper.ee_19,
        ChatUIKitEmojiHelper.ee_20,
        ChatUIKitEmojiHelper.ee_21,
        ChatUIKitEmojiHelper.ee_22,
        ChatUIKitEmojiHelper.ee_23,
        ChatUIKitEmojiHelper.ee_24,
        ChatUIKitEmojiHelper.ee_25,
        ChatUIKitEmojiHelper.ee_26,
        ChatUIKitEmojiHelper.ee_27,
        ChatUIKitEmojiHelper.ee_28,
        ChatUIKitEmojiHelper.ee_29,
        ChatUIKitEmojiHelper.ee_30,
        ChatUIKitEmojiHelper.ee_31,
        ChatUIKitEmojiHelper.ee_32,
        ChatUIKitEmojiHelper.ee_33,
        ChatUIKitEmojiHelper.ee_34,
        ChatUIKitEmojiHelper.ee_35,
        ChatUIKitEmojiHelper.ee_36,
        ChatUIKitEmojiHelper.ee_37,
        ChatUIKitEmojiHelper.ee_38,
        ChatUIKitEmojiHelper.ee_39,
        ChatUIKitEmojiHelper.ee_40,
        ChatUIKitEmojiHelper.ee_41,
        ChatUIKitEmojiHelper.ee_42,
        ChatUIKitEmojiHelper.ee_43,
        ChatUIKitEmojiHelper.ee_44,
        ChatUIKitEmojiHelper.ee_45,
        ChatUIKitEmojiHelper.ee_46,
        ChatUIKitEmojiHelper.ee_47,
        ChatUIKitEmojiHelper.ee_48,
        ChatUIKitEmojiHelper.ee_49,
        ChatUIKitEmojiHelper.ee_50,
        ChatUIKitEmojiHelper.ee_51,
        ChatUIKitEmojiHelper.ee_52,
    )
    val defaultReactions = arrayOf(
        ChatUIKitReaction(ChatUIKitEmojiHelper.ee_33, R.drawable.emoji_33, type = ChatUIKitReactionType.DEFAULT),
        ChatUIKitReaction(ChatUIKitEmojiHelper.ee_40, R.drawable.emoji_40, type = ChatUIKitReactionType.DEFAULT),
        ChatUIKitReaction(ChatUIKitEmojiHelper.ee_3, R.drawable.emoji_3, type = ChatUIKitReactionType.DEFAULT),
        ChatUIKitReaction(ChatUIKitEmojiHelper.ee_22, R.drawable.emoji_22, type = ChatUIKitReactionType.DEFAULT),
        ChatUIKitReaction(ChatUIKitEmojiHelper.ee_11, R.drawable.emoji_11, type = ChatUIKitReactionType.DEFAULT),
        ChatUIKitReaction(ChatUIKitEmojiHelper.ee_49, R.drawable.emoji_49, type = ChatUIKitReactionType.DEFAULT)
    )
    private val icons = intArrayOf(
        R.drawable.emoji_1,
        R.drawable.emoji_2,
        R.drawable.emoji_3,
        R.drawable.emoji_4,
        R.drawable.emoji_5,
        R.drawable.emoji_6,
        R.drawable.emoji_7,
        R.drawable.emoji_8,
        R.drawable.emoji_9,
        R.drawable.emoji_10,
        R.drawable.emoji_11,
        R.drawable.emoji_12,
        R.drawable.emoji_13,
        R.drawable.emoji_14,
        R.drawable.emoji_15,
        R.drawable.emoji_16,
        R.drawable.emoji_17,
        R.drawable.emoji_18,
        R.drawable.emoji_19,
        R.drawable.emoji_20,
        R.drawable.emoji_21,
        R.drawable.emoji_22,
        R.drawable.emoji_23,
        R.drawable.emoji_24,
        R.drawable.emoji_25,
        R.drawable.emoji_26,
        R.drawable.emoji_27,
        R.drawable.emoji_28,
        R.drawable.emoji_29,
        R.drawable.emoji_30,
        R.drawable.emoji_31,
        R.drawable.emoji_32,
        R.drawable.emoji_33,
        R.drawable.emoji_34,
        R.drawable.emoji_35,
        R.drawable.emoji_36,
        R.drawable.emoji_37,
        R.drawable.emoji_38,
        R.drawable.emoji_39,
        R.drawable.emoji_40,
        R.drawable.emoji_41,
        R.drawable.emoji_42,
        R.drawable.emoji_43,
        R.drawable.emoji_44,
        R.drawable.emoji_45,
        R.drawable.emoji_46,
        R.drawable.emoji_47,
        R.drawable.emoji_48,
        R.drawable.emoji_49,
        R.drawable.emoji_50,
        R.drawable.emoji_51,
        R.drawable.emoji_52
    )
    val data = createData()
    private fun createData(): Array<ChatUIKitEmojicon?> {
        val data = arrayOfNulls<ChatUIKitEmojicon>(icons.size)
        for (i in icons.indices) {
            data[i] = ChatUIKitEmojicon(icons[i], emojis[i], ChatUIKitEmojicon.Type.NORMAL)
        }
        return data
    }

    val mapData = createMapData()
    private fun createMapData(): Map<String, Int> {
        val mapData = mutableMapOf<String, Int>()
        for (i in icons.indices) {
            mapData[emojis[i]] = icons[i]
        }
        return mapData
    }
}