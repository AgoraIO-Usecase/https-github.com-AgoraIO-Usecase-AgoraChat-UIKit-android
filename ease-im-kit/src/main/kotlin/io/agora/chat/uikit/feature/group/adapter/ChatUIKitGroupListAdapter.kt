package io.agora.chat.uikit.feature.group.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.databinding.UikitLayoutGroupListItemBinding
import io.agora.chat.uikit.feature.group.config.ChatUIKitGroupListConfig
import io.agora.chat.uikit.feature.group.viewholders.ChatUIKitGroupListViewHolder

open class ChatUIKitGroupListAdapter(
    var config:ChatUIKitGroupListConfig = ChatUIKitGroupListConfig()
): ChatUIKitBaseRecyclerViewAdapter<ChatGroup>(){

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatGroup> {
        return ChatUIKitGroupListViewHolder(UikitLayoutGroupListItemBinding.inflate(LayoutInflater.from(parent.context)),config)
    }

    /**
     * Set group list item config.
     */
    fun setGroupListItemConfig(config: ChatUIKitGroupListConfig) {
        this.config = config
        notifyDataSetChanged()
    }

}