package io.agora.chat.uikit.feature.chat.reaction.viewholder

import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.databinding.UikitItemMessageMenuReactionBinding
import io.agora.chat.uikit.databinding.UikitItemMessageReactionBinding
import io.agora.chat.uikit.model.ChatUIKitReaction

class ChatUIKitReactionNormalViewHolder(
    private val binding: UikitItemMessageReactionBinding
): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitReaction>(binding = binding) {
    override fun setData(item: ChatUIKitReaction?, position: Int) {
        item?.let { reaction ->
            with(binding) {
                ivEmoji.setImageResource(reaction.icon)
                tvEmojiCount.text = reaction.count.toString()
                root.isSelected = reaction.isAddedBySelf
            }
        }
    }
}