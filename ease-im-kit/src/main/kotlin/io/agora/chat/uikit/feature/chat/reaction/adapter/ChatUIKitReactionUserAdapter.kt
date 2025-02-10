package io.agora.chat.uikit.feature.chat.reaction.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.extensions.loadAvatar
import io.agora.chat.uikit.common.extensions.setAvatarConfig
import io.agora.chat.uikit.common.extensions.toProfile
import io.agora.chat.uikit.databinding.UikitItemReactionUserBinding
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.getNickname
import io.agora.chat.uikit.model.isCurrentUser

class ChatUIKitReactionUserAdapter: ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser>() {
    private var onDeleteClickListener: OnClickListener? = null
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitUser> {
        return ChatUIKitReactionUserViewHolder(UikitItemReactionUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    private inner class ChatUIKitReactionUserViewHolder(val binding: UikitItemReactionUserBinding)
        : ViewHolder<ChatUIKitUser>(binding = binding) {

        override fun initView(viewBinding: ViewBinding?) {
            super.initView(viewBinding)
            viewBinding?.let {
                (it as UikitItemReactionUserBinding).ivAvatar.setAvatarConfig()
            }
        }

        override fun setData(item: ChatUIKitUser?, position: Int) {
            item?.let {
                binding.ivAvatar.loadAvatar(it.toProfile())
                binding.tvName.text = it.getRemarkOrName()

                if (it.isCurrentUser()) {
                    binding.ivDelete.visibility = View.VISIBLE
                } else {
                    binding.ivDelete.visibility = View.GONE
                }
                binding.ivDelete.setOnClickListener {
                    onDeleteClickListener?.onClick(it)
                }
            }
        }
    }

    /**
     * Set the listener for the delete button.
     */
    fun setOnDeleteClickListener(listener: OnClickListener) {
        onDeleteClickListener = listener
    }
}