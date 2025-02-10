package io.agora.chat.uikit.feature.group.viewholders

import android.text.TextUtils
import android.view.View
import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.extensions.toProfile
import io.agora.chat.uikit.feature.contact.adapter.ChatUIKitContactListAdapter
import io.agora.chat.uikit.databinding.UikitLayoutGroupSelectContactBinding
import io.agora.chat.uikit.feature.search.interfaces.OnContactSelectListener
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.provider.getSyncUser

open class ChatUIKitSelectContactViewHolder(
    val viewBinding:UikitLayoutGroupSelectContactBinding,
) : ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitUser>(binding = viewBinding) {
    protected var selectedListener: OnContactSelectListener?=null
    private var user:ChatUIKitUser?=null
    private var position:Int = 0
    protected var checkedList:MutableList<String> = mutableListOf()

    override fun initView(viewBinding: ViewBinding?) {
        super.initView(viewBinding)
        (viewBinding as UikitLayoutGroupSelectContactBinding).let {

            it.itemLayout.setOnClickListener{ view->
                val isChecked = it.cbSelect.isChecked
                it.cbSelect.isChecked = !isChecked
            }

            it.cbSelect.setOnCheckedChangeListener{ view,isChecked->
                user?.let { u->
                    selectedListener?.onContactSelectedChanged(view,u.userId,isChecked)
                }
            }
        }
    }

    fun setSelectedMembers(selectedMember:MutableList<String>){
        this.checkedList = selectedMember
    }

    override fun setData(item: ChatUIKitUser?, position: Int) {
        this.user = item
        this.position = position

        item?.run {
            with(viewBinding) {

                cbSelect.isChecked = checkedList.isNotEmpty() && isContains(checkedList,item.userId)

                emPresence.setUserAvatarData(item.toProfile())
                tvName.text = item.nickname ?: item.userId

                letterHeader.visibility = View.GONE
                if (position == 0 || initialLetter != null && adapter is ChatUIKitContactListAdapter
                    && initialLetter != (adapter as ChatUIKitContactListAdapter).getItem(position - 1)?.initialLetter) {
                    if (!TextUtils.isEmpty(initialLetter)) {
                        letterHeader.visibility = View.VISIBLE
                        letterHeader.text = initialLetter
                    }
                }

                // Set custom data provided by user
                ChatUIKitClient.getUserProvider()?.getSyncUser(userId)?.let { profile ->
                    tvName.text = profile.getRemarkOrName()
                }

            }
        }
    }

    protected fun isContains(data: MutableList<String>?, username: String): Boolean {
        return data != null && data.contains(username)
    }

    fun setCheckBoxSelectListener(listener: OnContactSelectListener?){
        this.selectedListener = listener
    }

}