package io.agora.chat.uikit.feature.contact.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.databinding.UikitLayoutItemHeaderBinding
import io.agora.chat.uikit.feature.contact.config.ChatUIKitContactHeaderConfig
import io.agora.chat.uikit.feature.contact.viewholders.ContactHeaderViewHolder
import io.agora.chat.uikit.feature.contact.interfaces.OnHeaderItemClickListener
import io.agora.chat.uikit.model.ChatUIKitCustomHeaderItem

class ChatUIKitCustomHeaderAdapter(
    val config: ChatUIKitContactHeaderConfig? = ChatUIKitContactHeaderConfig()
) : ChatUIKitBaseRecyclerViewAdapter<ChatUIKitCustomHeaderItem>(){
    private var listener: OnHeaderItemClickListener? = null
    override fun getViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<ChatUIKitCustomHeaderItem> {
        return ContactHeaderViewHolder(UikitLayoutItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false),config)
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatUIKitCustomHeaderItem>, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.setOnClickListener{
            listener?.onHeaderItemClick(it,position,getItem(position)?.headerId)
        }
    }

    fun addItem(data:ChatUIKitCustomHeaderItem){
        this.addData(data)
    }

    fun setItems(data:MutableList<ChatUIKitCustomHeaderItem>){
        this.setData(data)
    }

    fun setOnHeaderItemClickListener(listener: OnHeaderItemClickListener?){
        this.listener = listener
    }
}