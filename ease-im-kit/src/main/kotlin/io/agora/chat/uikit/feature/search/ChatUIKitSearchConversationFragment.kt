package io.agora.chat.uikit.feature.search

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.feature.chat.activities.UIKitChatActivity
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.base.ChatUIKitBaseSearchFragment
import io.agora.chat.uikit.feature.search.adapter.ChatUIKitSearchConversationAdapter
import io.agora.chat.uikit.feature.search.interfaces.IUIKitSearchResultView
import io.agora.chat.uikit.model.ChatUIKitConversation
import io.agora.chat.uikit.model.getChatType
import io.agora.chat.uikit.viewmodel.search.ChatUIKitSearchViewModel
import io.agora.chat.uikit.viewmodel.search.IUIKitSearchRequest

class ChatUIKitSearchConversationFragment: ChatUIKitBaseSearchFragment<ChatUIKitConversation>(),
    IUIKitSearchResultView {
    private var query:String? = null
    private var searchViewModel: IUIKitSearchRequest? = null

    override fun initViewModel() {
        super.initViewModel()
        searchViewModel = ViewModelProvider(context as AppCompatActivity)[ChatUIKitSearchViewModel::class.java]
        searchViewModel?.attachView(this)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.etSearch?.hint = getString(R.string.uikit_search_conversation_hint)
        binding?.etSearch?.requestFocus()
    }
    override fun initRecyclerView(): RecyclerView? {
        return binding?.rvList
    }

    override fun initAdapter(): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitConversation> {
        return ChatUIKitSearchConversationAdapter()
    }

    override fun searchText(query: String) {
        this.query = query
        if (!mListAdapter.data.isNullOrEmpty()){
            mListAdapter.clearData()
        }

        if (!TextUtils.isEmpty(query)) {
            if (mListAdapter is ChatUIKitSearchConversationAdapter){
                (mListAdapter as ChatUIKitSearchConversationAdapter).searchText(query)
            }
            query.let { content->
                searchViewModel?.searchConversation(content)
            }
        }
    }

    override fun refreshData() {
        if (!query.isNullOrEmpty()){
            query?.let {
                searchViewModel?.searchConversation(it)
            }
        }
    }

    override fun onTvRightClick(view: View) {
        mContext.finish()
    }

    override fun searchSuccess(result: Any) {
        finishRefresh()
        if (result is MutableList<*>) {
            mListAdapter.setData(result as MutableList<ChatUIKitConversation>)
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        super.onItemClick(view, position)
        mListAdapter.getItem(position)?.let {
            UIKitChatActivity.actionStart(mContext,it.conversationId, it.getChatType())
        }
    }
}