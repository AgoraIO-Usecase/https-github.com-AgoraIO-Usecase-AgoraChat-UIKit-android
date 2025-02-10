package io.agora.chat.uikit.feature.group.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.extensions.toProfile
import io.agora.chat.uikit.databinding.UikitLayoutGroupMemberSelectItemBinding
import io.agora.chat.uikit.interfaces.OnContactSelectedListener
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.provider.getSyncUser

class ChatUIKitGroupSelectListAdapter(
    private val groupId: String?,
    private val isAddStyle: Boolean = false
): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser>() {

    companion object{
        private var checkedList:MutableList<String> = mutableListOf()
    }

    private var selectedListener: OnContactSelectedListener?=null
    private var memberList:MutableList<String> = mutableListOf()
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitUser> =
        if (isAddStyle){
            GroupAddSelectListViewHolder(
                UikitLayoutGroupMemberSelectItemBinding.inflate(LayoutInflater.from(parent.context))
            )
        }else {
            GroupSelectListViewHolder(
                UikitLayoutGroupMemberSelectItemBinding.inflate(LayoutInflater.from(parent.context))
            )
        }

    override fun onBindViewHolder(holder: ViewHolder<ChatUIKitUser>, position: Int) {
        if (holder is GroupSelectListViewHolder){
            selectedListener?.let {
                holder.setCheckBoxSelectListener(it)
            }
            if (memberList.isNotEmpty()){
                holder.setMemberList(memberList)
            }
        }
        super.onBindViewHolder(holder, position)
    }

    fun setCheckBoxSelectListener(listener: OnContactSelectedListener){
        this.selectedListener = listener
        notifyDataSetChanged()
    }

    fun setGroupMemberList(list: MutableList<String>){
        this.memberList = list
        notifyDataSetChanged()
    }

    fun addSelectList(list: MutableList<String>){
        if (list.isNotEmpty()){
            list.forEach { id ->
                if (!checkedList.contains(id)){
                    checkedList.add(id)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun resetSelect(){
        checkedList.clear()
        notifyDataSetChanged()
    }

    open inner class GroupSelectListViewHolder(
        private val mViewBinding:UikitLayoutGroupMemberSelectItemBinding
    ): ViewHolder<ChatUIKitUser>(binding = mViewBinding) {
        private var selectedListener: OnContactSelectedListener?=null
        private var memberList:MutableList<String> = mutableListOf()

        override fun initView(viewBinding: ViewBinding?) {
            super.initView(viewBinding)
        }

        override fun setData(item: ChatUIKitUser?, position: Int) {
            item?.run {
                with(mViewBinding) {

                    memberList.let {
                        cbSelect.isSelected = it.contains(item.userId)
                    }

                    cbSelect.isChecked = checkedList.isNotEmpty() && isContains(checkedList,item.userId)

                    itemLayout.setOnClickListener{ view->
                        if (!memberList.contains(item.userId)){
                            val isChecked = cbSelect.isChecked
                            if (!isChecked){
                                if (!checkedList.contains(userId)){
                                    checkedList.add(userId)
                                }
                            }else{
                                checkedList.remove(userId)
                            }
                            cbSelect.isChecked = !isChecked
                        }
                        selectedListener?.onContactSelectedChanged(view,checkedList)
                    }

                    emPresence.setUserAvatarData(item.toProfile())
                    tvName.text = item.getRemarkOrName()

                    letterHeader.visibility = View.GONE
                    if (position == 0 || initialLetter != null && adapter is ChatUIKitGroupSelectListAdapter
                        && initialLetter != (adapter as ChatUIKitGroupSelectListAdapter).getItem(position - 1)?.initialLetter) {
                        if (!TextUtils.isEmpty(initialLetter)) {
                            letterHeader.visibility = View.VISIBLE
                            letterHeader.text = initialLetter
                        }
                    }
                    setName(tvName, item.userId)

                }
            }
        }

        open fun setName(tvName: TextView, userId: String?) {
            groupId?.let {
                ChatUIKitProfile.getGroupMember(groupId, userId)?.let {
                    tvName.text = it.getRemarkOrName()
                }
            }

        }

        private fun isContains(data: MutableList<String>?, username: String): Boolean {
            return data != null && data.contains(username)
        }

        fun setCheckBoxSelectListener(listener: OnContactSelectedListener){
            this.selectedListener = listener
        }

        fun setMemberList(list: MutableList<String>){
            this.memberList = list
        }

    }

    inner class GroupAddSelectListViewHolder(
        private val mViewBinding:UikitLayoutGroupMemberSelectItemBinding
    ): GroupSelectListViewHolder(mViewBinding = mViewBinding) {

        override fun setName(tvName: TextView, userId: String?) {
            ChatUIKitClient.getUserProvider()?.getSyncUser(userId)?.let {
                tvName.text = it.getRemarkOrName()
            }
        }
    }
}