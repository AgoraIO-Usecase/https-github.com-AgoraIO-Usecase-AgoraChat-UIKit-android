package io.agora.chat.uikit.feature.chat.forward.viewholder

import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.extensions.loadAvatar
import io.agora.chat.uikit.common.extensions.setAvatarConfig
import io.agora.chat.uikit.common.extensions.toProfile
import io.agora.chat.uikit.databinding.UikitItemForwardLayoutBinding
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.getNickname
import io.agora.chat.uikit.provider.getSyncUser

class ChatUIKitContactForwardViewHolder(
    private val binding: UikitItemForwardLayoutBinding
): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitUser>(binding = binding) {

    val btnForward = binding.btnForward
    val tvName = binding.tvName

    override fun initView(viewBinding: ViewBinding?) {
        super.initView(viewBinding)
        (viewBinding as? UikitItemForwardLayoutBinding)?.let {
            it.ivAvatar.setAvatarConfig()
        }
    }
    override fun setData(item: ChatUIKitUser?, position: Int) {
        item?.let {
            ChatUIKitClient.getUserProvider()?.getSyncUser(it.userId)?.let { profile ->
                binding.ivAvatar.loadAvatar(profile)
                if (profile.name.isNullOrEmpty().not()) {
                    binding.tvName.text = profile.name
                } else {
                    binding.tvName.text = it.getNickname()
                }
            } ?: run {
                binding.ivAvatar.loadAvatar(it.toProfile())
                binding.tvName.text = it.getNickname()
            }
        }
    }
}