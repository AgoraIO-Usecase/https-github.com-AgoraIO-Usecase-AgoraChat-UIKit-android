package io.agora.chat.uikit.feature.chat.forward.viewholder

import androidx.viewbinding.ViewBinding
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.extensions.setAvatarConfig
import io.agora.chat.uikit.databinding.UikitItemForwardLayoutBinding
import io.agora.chat.uikit.provider.getSyncProfile

class ChatUIKitGroupForwardViewHolder(
    private val binding: UikitItemForwardLayoutBinding
): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatGroup>(binding = binding) {

    val btnForward = binding.btnForward

    override fun initView(viewBinding: ViewBinding?) {
        super.initView(viewBinding)
        (viewBinding as? UikitItemForwardLayoutBinding)?.let {
            it.ivAvatar.setAvatarConfig()
        }
    }
    override fun setData(item: ChatGroup?, position: Int) {
        item?.let {
            with(binding) {
                tvName.text = it.groupName
                ivAvatar.setImageResource(io.agora.chat.uikit.R.drawable.uikit_default_group_avatar)

                // Set custom data provided by group avatar
                ChatUIKitClient.getGroupProfileProvider()?.getSyncProfile(item.groupId)?.let { profile ->
                    if (profile.name.isNullOrEmpty()) {
                        tvName.text = profile.name
                    }
                    ivAvatar.load(profile.avatar) {
                        placeholder(R.drawable.uikit_default_group_avatar)
                        error(R.drawable.uikit_default_group_avatar)
                    }
                }

            }
        }
    }
}