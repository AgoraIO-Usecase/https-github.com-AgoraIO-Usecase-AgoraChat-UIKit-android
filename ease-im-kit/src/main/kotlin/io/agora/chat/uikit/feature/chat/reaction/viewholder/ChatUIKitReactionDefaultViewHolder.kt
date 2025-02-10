package io.agora.chat.uikit.feature.chat.reaction.viewholder

import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.databinding.UikitItemMessageMenuReactionBinding
import io.agora.chat.uikit.model.ChatUIKitReaction

class ChatUIKitReactionDefaultViewHolder(private val binding: UikitItemMessageMenuReactionBinding)
    : ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitReaction>(binding = binding) {

    override fun initView(viewBinding: ViewBinding?) {
        super.initView(viewBinding)
    }
    override fun setData(item: ChatUIKitReaction?, position: Int) {
        item?.let {
            with(binding) {
                ivExpression.setImageResource(item.icon)
                ivExpression.isSelected = it.isAddedBySelf
            }
        }
    }
}