package io.agora.chat.uikit.feature.chat.reaction.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.databinding.UikitItemMessageMenuReactionBinding
import io.agora.chat.uikit.databinding.UikitItemMessageReactionBinding
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitReactionType
import io.agora.chat.uikit.feature.chat.reaction.viewholder.ChatUIKitReactionAddViewHolder
import io.agora.chat.uikit.feature.chat.reaction.viewholder.ChatUIKitReactionDefaultViewHolder
import io.agora.chat.uikit.feature.chat.reaction.viewholder.ChatUIKitReactionMoreViewHolder
import io.agora.chat.uikit.feature.chat.reaction.viewholder.ChatUIKitReactionNormalViewHolder
import io.agora.chat.uikit.model.ChatUIKitReaction

object ChatUIKitMessageReactionViewHolderFactory {
    fun createViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitReaction> {
        return when (viewType) {
            ChatUIKitReactionType.DEFAULT.viewType -> ChatUIKitReactionDefaultViewHolder(
                UikitItemMessageMenuReactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
            )

            ChatUIKitReactionType.ADD.viewType -> ChatUIKitReactionAddViewHolder(
                UikitItemMessageMenuReactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
            )

            ChatUIKitReactionType.MORE.viewType -> ChatUIKitReactionMoreViewHolder()

            else -> ChatUIKitReactionNormalViewHolder(
                UikitItemMessageReactionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)
            )
        }
    }

    fun getViewType(reaction: ChatUIKitReaction?): Int {
        return reaction?.let { getReactionType(it) } ?: (ChatUIKitReactionType.NORMAL.viewType)
    }

    private fun getReactionType(reaction: ChatUIKitReaction): Int {
        val type = reaction.type
        return type.viewType
    }
}