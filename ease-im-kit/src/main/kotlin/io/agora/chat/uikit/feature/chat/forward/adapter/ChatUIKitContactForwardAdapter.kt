package io.agora.chat.uikit.feature.chat.forward.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.extensions.highlightTargetText
import io.agora.chat.uikit.databinding.UikitItemForwardLayoutBinding
import io.agora.chat.uikit.feature.chat.forward.viewholder.ChatUIKitContactForwardViewHolder
import io.agora.chat.uikit.feature.contact.adapter.ChatUIKitContactListAdapter
import io.agora.chat.uikit.interfaces.OnForwardClickListener
import io.agora.chat.uikit.model.ChatUIKitUser

class ChatUIKitContactForwardAdapter: ChatUIKitContactListAdapter() {

    private var searchKey: String? = null
    private var forwardClickListener: OnForwardClickListener? = null
    private val sentUserList = mutableListOf<String>()

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitUser> {
        return ChatUIKitContactForwardViewHolder(UikitItemForwardLayoutBinding.inflate(LayoutInflater.from(parent.context)
            , parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatUIKitUser>, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        if (holder is ChatUIKitContactForwardViewHolder){
            holder.btnForward.text = if (sentUserList.contains(item?.userId)) holder.itemView.context.getString(R.string.uikit_chat_reply_forwarded)
            else holder.itemView.context.getString(R.string.uikit_action_forward)
            holder.btnForward.isEnabled = !sentUserList.contains(item?.userId)
            holder.btnForward.setOnClickListener { view ->
                item?.userId?.let { sentUserList.add(it) }
                holder.btnForward.text = holder.itemView.context.getString(R.string.uikit_chat_reply_forwarded)
                holder.btnForward.isEnabled = false
                item?.let {
                    forwardClickListener?.onForwardClick(view, it.userId, ChatType.Chat)
                }
            }
            searchKey?.let {
                val spannable = holder.tvName.text.toString().trim().highlightTargetText(it
                    , ContextCompat.getColor(holder.itemView.context, R.color.ease_color_primary))
                holder.tvName.text = spannable
            }
        }
    }

    fun setOnForwardClickListener(listener: OnForwardClickListener?){
        this.forwardClickListener = listener
    }

    fun setSearchKey(key: String){
        this.searchKey = key
        notifyDataSetChanged()
    }

    /**
     * Set the user id has been sent.
     */
    fun setSentUserList(userList: List<String>?){
        userList?.let {
            for (userId: String in it){
                if (!sentUserList.contains(userId)) {
                    sentUserList.add(userId)
                }
            }
        }
        notifyDataSetChanged()
    }

    /**
     * Get the user list has been sent.
     */
    fun getSentUserList(): List<String>{
        return sentUserList
    }

}