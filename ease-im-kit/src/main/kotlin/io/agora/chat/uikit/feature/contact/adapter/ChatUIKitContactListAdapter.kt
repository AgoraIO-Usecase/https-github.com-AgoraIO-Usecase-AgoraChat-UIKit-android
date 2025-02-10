package io.agora.chat.uikit.feature.contact.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.enums.ChatUIKitListViewType
import io.agora.chat.uikit.feature.contact.viewholders.ContactViewHolder
import io.agora.chat.uikit.feature.contact.item.ChatUIKitUserContactItem
import io.agora.chat.uikit.feature.group.viewholders.ChatUIKitSelectContactViewHolder
import io.agora.chat.uikit.feature.search.interfaces.OnContactSelectListener
import io.agora.chat.uikit.interfaces.OnUserListItemClickListener
import io.agora.chat.uikit.model.ChatUIKitUser
import java.util.concurrent.ConcurrentHashMap

open class ChatUIKitContactListAdapter(
    private var viewType: ChatUIKitListViewType = ChatUIKitListViewType.LIST_CONTACT
): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser>() {
    private var itemClickListener: OnUserListItemClickListener? = null
    private var selectedListener: OnContactSelectListener? = null
    private var userAvatarInfo: ConcurrentHashMap<String, Int>? = null
    private var isShowInitLetter:Boolean = true
    private var selectedMember:MutableList<String> = mutableListOf()

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitUser> {
        return ChatUIKitListViewHolderFactory.createViewHolder(
            LayoutInflater.from(parent.context),parent, ChatUIKitListViewType.fromCode(viewType))
    }

    override fun getItemNotEmptyViewType(position: Int): Int {
        return viewType.code
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatUIKitUser>, position: Int) {
        if ((holder.itemView) is ChatUIKitUserContactItem){
            userAvatarInfo?.let {
                ((holder.itemView) as ChatUIKitUserContactItem).setUserAvatarInfo(it)
            }
        }
        if (holder is ContactViewHolder){
            holder.setShowInitialLetter(isShowInitLetter)
        }

        if (holder is ChatUIKitSelectContactViewHolder){
            holder.setSelectedMembers(selectedMember)
        }

        super.onBindViewHolder(holder, position)
        if ((holder.itemView) is ChatUIKitUserContactItem){
            itemClickListener?.let {
                ((holder.itemView) as ChatUIKitUserContactItem).setOnUserListItemClickListener(it)
            }
        }
        if (holder is ChatUIKitSelectContactViewHolder){
            selectedListener?.let {
                holder.setCheckBoxSelectListener(it)
            }
        }
    }

    fun setListViewItemType(viewType: ChatUIKitListViewType){
        this.viewType = viewType
        notifyDataSetChanged()
    }

    fun setUserAvatarInfo(info: ConcurrentHashMap<String, Int>?) {
        this.userAvatarInfo = info
        notifyDataSetChanged()
    }

    fun setShowInitialLetter(isShow:Boolean){
        this.isShowInitLetter = isShow
    }

    fun setSelectedMembers(selectedMember:MutableList<String>){
        this.selectedMember = selectedMember
        notifyDataSetChanged()
    }

    /**
     * Set message item click listeners.
     * @param listener
     */
    fun setOnUserListItemClickListener(listener: OnUserListItemClickListener) {
        this.itemClickListener = listener
    }


    fun setCheckBoxSelectListener(listener: OnContactSelectListener){
        this.selectedListener = listener
    }



}