package io.agora.chat.uikit.feature.contact.viewholders

import android.view.View
import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.databinding.UikitLayoutItemHeaderBinding
import io.agora.chat.uikit.feature.contact.config.ChatUIKitContactHeaderConfig
import io.agora.chat.uikit.feature.contact.config.bindView
import io.agora.chat.uikit.model.ChatUIKitCustomHeaderItem

class ContactHeaderViewHolder(
    private val viewBinding: UikitLayoutItemHeaderBinding,
    val config:ChatUIKitContactHeaderConfig? = ChatUIKitContactHeaderConfig()
): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitCustomHeaderItem>(binding = viewBinding)  {

    init {
        config?.bindView(viewBinding)
    }

    override fun initView(viewBinding: ViewBinding?) {
        super.initView(viewBinding)
        if(viewBinding is UikitLayoutItemHeaderBinding){
            ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(viewBinding.headerItem.avatar)
        }
    }

    override fun setData(item: ChatUIKitCustomHeaderItem?, position: Int) {
        val headerItem = viewBinding.headerItem
        headerItem.tvTitle?.text = item?.headerTitle ?: ""
        headerItem.tvContent?.text = item?.headerContent ?: ""

        if (item?.headerIconRes != -1 ){
            item?.headerIconRes?.let {
                headerItem.setAvatarVisibility(View.VISIBLE)
                headerItem.setAvatar(it)
            }
        }
        if (item?.headerEndIconRes != -1){
            item?.headerEndIconRes?.let {
                headerItem.setArrow(it)
            }
        }

        if (item?.headerItemDivider == true){
            headerItem.setItemDividerVisibility(View.VISIBLE)
        }else{
            headerItem.setItemDividerVisibility(View.GONE)
        }

        if (item?.headerItemShowArrow == true){
            headerItem.setArrowVisibility(View.VISIBLE)
        }else{
            headerItem.setArrowVisibility(View.GONE)
        }

        if (item?.headerUnReadCount == 0){
            viewBinding.unreadCount.text = ""
            viewBinding.rlUnreadRight.visibility = View.GONE
        }else{
            viewBinding.unreadCount.text = item?.headerUnReadCount.toString()
            item?.headerUnReadCount?.let {
                if (it > 99){
                    val context = ChatUIKitClient.getContext()
                    context?.let { con->
                        viewBinding.unreadCount.text = con.resources.getString(R.string.uikit_message_unread_count_max)
                    }
                }
            }
            viewBinding.rlUnreadRight.visibility = View.VISIBLE
        }
    }

}