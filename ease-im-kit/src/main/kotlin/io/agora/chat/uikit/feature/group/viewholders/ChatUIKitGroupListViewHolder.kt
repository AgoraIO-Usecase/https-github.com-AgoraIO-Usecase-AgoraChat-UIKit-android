package io.agora.chat.uikit.feature.group.viewholders

import android.graphics.drawable.Drawable
import androidx.viewbinding.ViewBinding
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.databinding.UikitLayoutGroupListItemBinding
import io.agora.chat.uikit.feature.group.config.ChatUIKitGroupListConfig
import io.agora.chat.uikit.feature.group.config.bindView
import io.agora.chat.uikit.provider.getSyncProfile

class ChatUIKitGroupListViewHolder(
    private val viewBinding:UikitLayoutGroupListItemBinding,
    val config: ChatUIKitGroupListConfig = ChatUIKitGroupListConfig()
) : ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatGroup>(binding = viewBinding) {

    private var bgDrawable: Drawable? = null

    init {
        config.bindView(viewBinding)
    }

    override fun initView(viewBinding: ViewBinding?) {
        super.initView(viewBinding)
        viewBinding?.let {
            if (it is UikitLayoutGroupListItemBinding) {
                ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(it.groupAvatar)
                bgDrawable = it.root.background
            }
        }
    }

    override fun setData(item: ChatGroup?, position: Int) {
        item?.let {
            with(viewBinding) {
                groupName.text = it.groupName
                groupAvatar.setImageResource(R.drawable.uikit_default_group_avatar)

                // Set custom data provided by group avatar
                ChatUIKitClient.getGroupProfileProvider()?.getSyncProfile(item.groupId)?.let { profile ->
                    if (profile.name.isNullOrEmpty().not()) {
                        groupName.text = profile.name
                    }
                    groupAvatar.load(profile.avatar) {
                        placeholder(R.drawable.uikit_default_group_avatar)
                        error(R.drawable.uikit_default_group_avatar)
                    }
                }

            }
        }
    }



}