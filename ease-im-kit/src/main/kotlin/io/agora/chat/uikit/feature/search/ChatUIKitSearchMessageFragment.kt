package io.agora.chat.uikit.feature.search

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.base.ChatUIKitBaseSearchFragment
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatSearchScope
import io.agora.chat.uikit.feature.search.ChatUIKitSearchMessageFragment.Constant.KEY_SHOW_RIGHT_CANCEL
import io.agora.chat.uikit.feature.search.adapter.ChatUIKitSearchMessageAdapter
import io.agora.chat.uikit.feature.search.interfaces.IUIKitSearchResultView
import io.agora.chat.uikit.feature.search.interfaces.OnSearchMsgItemClickListener
import io.agora.chat.uikit.viewmodel.search.ChatUIKitSearchViewModel
import io.agora.chat.uikit.viewmodel.search.IUIKitSearchRequest

class ChatUIKitSearchMessageFragment: ChatUIKitBaseSearchFragment<ChatMessage>(), IUIKitSearchResultView {

    private var searchViewModel: IUIKitSearchRequest? = null
    private var onCancelListener: OnCancelClickListener?=null
    private var itemClickListener: OnSearchMsgItemClickListener? = null
    private var data:MutableList<ChatMessage> = mutableListOf()
    private var keyWords:String? = null
    private var conversationId:String? = null

    override fun initViewModel() {
        super.initViewModel()
        searchViewModel = ViewModelProvider(context as AppCompatActivity)[ChatUIKitSearchViewModel::class.java]
        searchViewModel?.attachView(this)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        arguments?.run {
            binding?.run {
                tvRight.visibility = if (getBoolean(KEY_SHOW_RIGHT_CANCEL, false)) View.VISIBLE else View.GONE
            }
        }
        binding?.etSearch?.hint = context?.resources?.getString(R.string.uikit_chat_search_message)
        binding?.etSearch?.requestFocus()
    }

    override fun initRecyclerView(): RecyclerView? {
        return binding?.rvList
    }

    override fun initAdapter(): ChatUIKitBaseRecyclerViewAdapter<ChatMessage> {
        return ChatUIKitSearchMessageAdapter()
    }

    override fun searchText(query: String) {
        this.keyWords = query
        if (!mListAdapter.data.isNullOrEmpty()){
            mListAdapter.clearData()
        }

        if (!TextUtils.isEmpty(query)) {
            if (mListAdapter is ChatUIKitSearchMessageAdapter){
                (mListAdapter as ChatUIKitSearchMessageAdapter).searchText(query)
            }
            searchData()
        }
    }

    override fun refreshData() {
        searchData()
    }

    private fun searchData(){
        if (!keyWords.isNullOrEmpty()){
            keyWords?.let {
                if (conversationId.isNullOrEmpty()){
                    searchViewModel?.searchMessage(
                        keywords = it,
                        chatScope = ChatSearchScope.CONTENT
                    )
                }else{
                    conversationId?.let { id->
                        searchViewModel?.searchMessage(
                            keywords = it,
                            conversationId= id,
                            chatScope = ChatSearchScope.CONTENT
                        )
                    }
                }
            }
        }
    }

    override fun onTvRightClick(view: View) {
        onCancelListener?.onCancelClick(view)
    }

    override fun searchSuccess(result: Any) {
        finishRefresh()
        if (result is MutableList<*>) {
            this.data = result as MutableList<ChatMessage>
            mListAdapter.setData(result)
        }
    }

    private fun setItemClickListener(itemClickListener: OnSearchMsgItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    private fun setOnCancelListener(onCancelListener: OnCancelClickListener?) {
        this.onCancelListener = onCancelListener
    }

    private fun setConversationId(conversationId: String?){
        this.conversationId = conversationId
    }

    override fun onItemClick(view: View?, position: Int) {
        super.onItemClick(view, position)
        mListAdapter.getItem(position)?.let {
            itemClickListener?.onSearchItemClick(view,position,it)
        }
    }

    interface OnCancelClickListener{
        fun onCancelClick(view:View)
    }

    class Builder {
        private val bundle: Bundle = Bundle()
        private var customFragment: ChatUIKitSearchMessageFragment? = null
        private var itemClickListener: OnSearchMsgItemClickListener? = null
        private var onCancelListener: OnCancelClickListener? = null
        private var conversationId: String? = null


        fun <T : ChatUIKitSearchMessageFragment?> setCustomFragment(fragment: T): Builder {
            customFragment = fragment
            return this
        }

        fun showRightCancel(showCancel: Boolean): Builder {
            bundle.putBoolean(KEY_SHOW_RIGHT_CANCEL, showCancel)
            return this
        }

        fun setItemClickListener(itemClickListener: OnSearchMsgItemClickListener): Builder {
            this.itemClickListener = itemClickListener
            return this
        }

        fun setOnCancelListener(onCancelListener: OnCancelClickListener?): Builder {
            this.onCancelListener = onCancelListener
            return this
        }

        fun setConversationId(conversationId:String?):Builder{
            this.conversationId = conversationId
            return this
        }

        fun build(): ChatUIKitSearchMessageFragment {
            val fragment =
                if (customFragment != null) customFragment else ChatUIKitSearchMessageFragment()
            fragment!!.arguments = bundle
            fragment.setConversationId(conversationId)
            fragment.setOnCancelListener(onCancelListener)
            fragment.setItemClickListener(itemClickListener)
            return fragment
        }
    }

    private object Constant {
        const val KEY_SHOW_RIGHT_CANCEL = "key_show_right_cancel"
    }
}