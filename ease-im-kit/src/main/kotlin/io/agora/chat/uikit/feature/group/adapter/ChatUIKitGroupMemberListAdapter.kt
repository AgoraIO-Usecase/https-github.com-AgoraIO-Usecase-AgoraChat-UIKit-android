package io.agora.chat.uikit.feature.group.adapter

import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.extensions.toProfile
import io.agora.chat.uikit.databinding.UikitLayoutContactItemBinding
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser

class ChatUIKitGroupMemberListAdapter(
    private val groupId: String?
): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser>() {
    private var isShowInitLetter:Boolean = true
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitUser> =
        GroupMemberListViewHolder(
            UikitLayoutContactItemBinding.inflate(LayoutInflater.from(parent.context))
        )

    override fun onBindViewHolder(holder: ViewHolder<ChatUIKitUser>, position: Int) {
        if (holder is GroupMemberListViewHolder){
            holder.setShowInitialLetter(isShowInitLetter)
        }
        super.onBindViewHolder(holder, position)
    }


    fun setShowInitialLetter(isShow:Boolean){
        this.isShowInitLetter = isShow
    }

    inner class GroupMemberListViewHolder(
        private val mViewBinding: UikitLayoutContactItemBinding
    ) : ViewHolder<ChatUIKitUser>(binding = mViewBinding) {
        private var bgDrawable: Drawable? = null
        private var isShowInitLetter:Boolean = true
        private var user:ChatUIKitUser?=null

        override fun initView(viewBinding: ViewBinding?) {
            super.initView(viewBinding)
            viewBinding?.let {
                if (it is UikitLayoutContactItemBinding) {
                    bgDrawable = it.root.background
                }
            }
        }

        fun setShowInitialLetter(isShow:Boolean){
            this.isShowInitLetter = isShow
        }

        override fun setData(item: ChatUIKitUser?, position: Int) {
            this.user = item
            mViewBinding.let {
                val header = item?.initialLetter
                it.header.visibility = View.GONE
                it.emPresence.setUserAvatarData(user?.toProfile())
                it.tvName.text = user?.nickname ?: user?.userId

                groupId?.let { id ->
                    ChatUIKitProfile.getGroupMember(groupId, user?.userId)?.let { profile ->
                        it.emPresence.setUserAvatarData(profile)
                        it.tvName.text = profile.getRemarkOrName()
                    }
                }

                if (position == 0 || header != null && adapter is ChatUIKitGroupMemberListAdapter
                    && header != (adapter as ChatUIKitGroupMemberListAdapter).getItem(position - 1)?.initialLetter) {
                    if (!TextUtils.isEmpty(header) && isShowInitLetter) {
                        it.header.visibility = View.VISIBLE
                        it.header.text = header
                    }
                }
            }
        }

    }
}