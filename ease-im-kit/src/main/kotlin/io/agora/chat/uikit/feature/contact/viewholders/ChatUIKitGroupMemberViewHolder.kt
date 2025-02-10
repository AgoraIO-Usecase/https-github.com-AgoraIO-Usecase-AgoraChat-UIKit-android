package io.agora.chat.uikit.feature.contact.viewholders

import android.text.TextUtils
import android.view.View
import io.agora.chat.uikit.feature.contact.adapter.ChatUIKitContactListAdapter
import io.agora.chat.uikit.model.ChatUIKitUser

class ChatUIKitGroupMemberViewHolder(
    itemView: View
) : ContactViewHolder(itemView) {

    override fun setData(item: ChatUIKitUser?, position: Int) {
        easeUserListRow.setUpView(item, position, true)
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

}