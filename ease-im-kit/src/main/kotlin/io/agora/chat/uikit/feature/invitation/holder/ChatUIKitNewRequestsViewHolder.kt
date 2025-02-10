package io.agora.chat.uikit.feature.invitation.holder

import android.view.View
import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.extensions.getSyncUserFromProvider
import io.agora.chat.uikit.common.extensions.loadAvatar
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.databinding.UikitLayoutInvitationItemBinding
import io.agora.chat.uikit.feature.invitation.enums.InviteMessageStatus
import io.agora.chat.uikit.feature.invitation.helper.RequestMsgHelper
import io.agora.chat.uikit.provider.getSyncUser

class ChatUIKitNewRequestsViewHolder(
    private val viewBinding: UikitLayoutInvitationItemBinding,
    private var listener:ChatUIKitBaseRecyclerViewAdapter.OnItemSubViewClickListener?
): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatMessage>(binding = viewBinding) {

    override fun initView(viewBinding: ViewBinding?) {
        if(viewBinding is UikitLayoutInvitationItemBinding){
            ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(viewBinding.itemAvatar)
        }
    }

    override fun setData(item: ChatMessage?, position: Int) {
        item?.run {
            with(viewBinding) {
                val from = item.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_FROM)
                val user = getSyncUserFromProvider()?.toUser()
                val reason = RequestMsgHelper.getSystemMessage(viewBinding.root.context, item)
                if (user == null){
                    itemTitle.text = from
                }else{
                    itemTitle.text = user.userId
                }
                itemReason.text = reason

                ChatUIKitClient.getUserProvider()?.getSyncUser(from)?.let {
                    itemTitle.text = it.getRemarkOrName()
                    itemAvatar.loadAvatar(it)
                }

                val statusParams: String = item.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_STATUS)
                when(InviteMessageStatus.valueOf(statusParams)){
                    InviteMessageStatus.BEINVITEED -> {
                        itemAction.setText(R.string.uikit_invitation_action_add)
                        itemAction.isSelected = true
                        itemReason.visibility = View.VISIBLE
                    }
                    InviteMessageStatus.AGREED -> {
                        itemAction.setText(R.string.uikit_invitation_action_added)
                        itemAction.isSelected = false
                        itemReason.visibility = View.GONE
                    }
                    else -> {}
                }

                itemAction.setOnClickListener{
                    listener?.onItemSubViewClick(it,position)
                }
            }
        }
    }

}