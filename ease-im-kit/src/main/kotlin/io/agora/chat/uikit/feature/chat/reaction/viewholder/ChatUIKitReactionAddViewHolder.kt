package io.agora.chat.uikit.feature.chat.reaction.viewholder

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.databinding.UikitItemMessageMenuReactionBinding
import io.agora.chat.uikit.model.ChatUIKitReaction

class ChatUIKitReactionAddViewHolder(private val binding: UikitItemMessageMenuReactionBinding)
    : ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitReaction>(binding = binding) {

    override fun setData(item: ChatUIKitReaction?, position: Int) {
        item?.let {
            with(binding) {
                ivExpression.background = null
                ivExpression.setImageResource(item.icon)
                ivExpression.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context, R.color.ease_color_on_background))
            }
        }
    }
}