package io.agora.chat.uikit.provider

import io.agora.chat.uikit.model.ChatUIKitEmojicon

/**
 * Custom emoji icon provider
 */
interface ChatUIKitEmojiconInfoProvider {
    /**
     * return ChatUIKitEmojicon for input emojiconIdentityCode
     * @param emojiconIdentityCode
     * @return
     */
    fun getEmojiconInfo(emojiconIdentityCode: String?): ChatUIKitEmojicon?

    /**
     * get Emojicon map, key is the text of emoji, value is the resource id or local path of emoji icon(can't be URL on internet)
     * @return
     */
    val textEmojiconMapping: Map<String?, Any?>?
}