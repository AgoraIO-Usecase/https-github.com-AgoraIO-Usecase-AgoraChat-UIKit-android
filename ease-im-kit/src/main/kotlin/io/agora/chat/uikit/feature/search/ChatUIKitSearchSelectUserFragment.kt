package io.agora.chat.uikit.feature.search

import android.os.Bundle
import android.view.View
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.feature.search.adapter.ChatUIKitSearchUserAdapter
import io.agora.chat.uikit.interfaces.OnContactSelectedListener
import io.agora.chat.uikit.model.ChatUIKitUser

class ChatUIKitSearchSelectUserFragment:ChatUIKitSearchUserFragment(), OnContactSelectedListener {
    private lateinit var adapter:ChatUIKitSearchUserAdapter
    private var selectData:MutableList<String> = mutableListOf()
    private var selectListener:OnContactSelectedListener?=null

    override fun initAdapter(): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser> {
        adapter = ChatUIKitSearchUserAdapter(true)
        return adapter
    }

    override fun initListener() {
        super.initListener()
        adapter.setCheckBoxSelectListener(this)
    }

    private fun setOnSelectListener(onSelectListener: OnContactSelectedListener?) {
        this.selectListener = onSelectListener
    }

    override fun onContactSelectedChanged(v: View, selectedMembers: MutableList<String>) {
        this.selectData = selectedMembers
        if (selectedMembers.isNotEmpty()){
            binding?.tvRight?.text = mContext.resources.getString(R.string.uikit_dialog_confirm)
        }else{
            binding?.tvRight?.text = mContext.resources.getString(R.string.uikit_dialog_cancel)
        }
    }

    override fun onTvRightClick(view: View) {
        selectListener?.onContactSelectedChanged(view,selectData)
        resetSelectList()
    }

    fun resetSelectList(){
        adapter.resetSelect()
    }

    class Builder {
        private val bundle: Bundle = Bundle()
        private var customFragment: ChatUIKitSearchSelectUserFragment? = null
        private var selectListener:OnContactSelectedListener?=null

        fun <T : ChatUIKitSearchSelectUserFragment?> setCustomFragment(fragment: T): Builder {
            customFragment = fragment
            return this
        }

        fun showRightCancel(showCancel: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_SHOW_RIGHT_CANCEL, showCancel)
            return this
        }

        fun setOnSelectListener(onSelectListener: OnContactSelectedListener?): Builder {
            this.selectListener = onSelectListener
            return this
        }

        fun build(): ChatUIKitSearchSelectUserFragment {
            val fragment =
                if (customFragment != null) customFragment else ChatUIKitSearchSelectUserFragment()
            fragment!!.arguments = bundle
            fragment.setOnSelectListener(selectListener)
            return fragment
        }

        private object Constant {
            const val KEY_SHOW_RIGHT_CANCEL = "key_show_right_cancel"
        }
    }

}