package io.agora.chat.uikit.feature.contact.viewholders

import android.text.TextUtils
import android.view.View
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.feature.contact.adapter.ChatUIKitContactListAdapter
import io.agora.chat.uikit.feature.contact.item.ChatUIKitUserContactItem
import io.agora.chat.uikit.model.ChatUIKitUser

open class ContactViewHolder(
    itemView: View
) : ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitUser>(itemView) {
    protected lateinit var easeUserListRow: ChatUIKitUserContactItem
    protected var isShowInitLetter:Boolean = true

    override fun initView(itemView: View?) {
        if (itemView is ChatUIKitUserContactItem){
            easeUserListRow = itemView
        }
    }

    override fun setData(item: ChatUIKitUser?, position: Int) {
        easeUserListRow.setUpView(item,position)
        val header = item?.initialLetter

        easeUserListRow.mViewBinding.header.visibility = View.GONE
        if (position == 0 || header != null && adapter is ChatUIKitContactListAdapter
            && header != (adapter as ChatUIKitContactListAdapter).getItem(position - 1)?.initialLetter) {
            if (!TextUtils.isEmpty(header) && isShowInitLetter) {
                easeUserListRow.mViewBinding.header.visibility = View.VISIBLE
                easeUserListRow.mViewBinding.header.text = header
            }
        }
    }

    fun setShowInitialLetter(isShow:Boolean){
        this.isShowInitLetter = isShow
    }


}