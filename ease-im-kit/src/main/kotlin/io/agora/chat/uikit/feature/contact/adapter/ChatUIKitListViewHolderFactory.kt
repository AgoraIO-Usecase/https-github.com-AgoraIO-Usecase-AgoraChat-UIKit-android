package io.agora.chat.uikit.feature.contact.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.enums.ChatUIKitListViewType
import io.agora.chat.uikit.databinding.UikitLayoutGroupSelectContactBinding
import io.agora.chat.uikit.feature.contact.viewholders.ContactViewHolder
import io.agora.chat.uikit.feature.contact.item.ChatUIKitUserContactItem
import io.agora.chat.uikit.feature.contact.viewholders.ChatUIKitGroupMemberViewHolder
import io.agora.chat.uikit.feature.group.viewholders.ChatUIKitSelectContactViewHolder
import io.agora.chat.uikit.model.ChatUIKitUser

object ChatUIKitListViewHolderFactory {

    fun createViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: ChatUIKitListViewType?,
    ): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitUser> {
        return when (viewType) {
            ChatUIKitListViewType.LIST_CONTACT -> {
                ContactViewHolder(ChatUIKitUserContactItem(parent.context))
            }
            ChatUIKitListViewType.LIST_SELECT_CONTACT -> {
                ChatUIKitSelectContactViewHolder(
                    UikitLayoutGroupSelectContactBinding.inflate(inflater, parent, false)
                )
            }
            ChatUIKitListViewType.LIST_GROUP_MEMBER -> {
                ChatUIKitGroupMemberViewHolder(ChatUIKitUserContactItem(parent.context))
            }

            else ->  ContactViewHolder(ChatUIKitUserContactItem(parent.context))
        }
    }

}