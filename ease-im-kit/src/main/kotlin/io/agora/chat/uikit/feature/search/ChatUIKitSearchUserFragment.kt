package io.agora.chat.uikit.feature.search

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.base.ChatUIKitBaseSearchFragment
import io.agora.chat.uikit.feature.search.adapter.ChatUIKitSearchUserAdapter
import io.agora.chat.uikit.feature.search.interfaces.IUIKitSearchResultView
import io.agora.chat.uikit.feature.search.interfaces.OnSearchUserItemClickListener
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.viewmodel.search.ChatUIKitSearchViewModel
import io.agora.chat.uikit.viewmodel.search.IUIKitSearchRequest

open class ChatUIKitSearchUserFragment: ChatUIKitBaseSearchFragment<ChatUIKitUser>(),IUIKitSearchResultView {

    private var searchViewModel: IUIKitSearchRequest? = null
    private var keyWords:String? = null
    private var data:MutableList<ChatUIKitUser> = mutableListOf()
    private var onCancelListener:OnCancelClickListener?=null
    private var itemClickListener: OnSearchUserItemClickListener? = null
    private var isSearchBlockUser:Boolean = false

    override fun initViewModel() {
        super.initViewModel()
        searchViewModel = ViewModelProvider(context as AppCompatActivity)[ChatUIKitSearchViewModel::class.java]
        searchViewModel?.attachView(this)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        arguments?.run {
            isSearchBlockUser = this.getBoolean(Constant.KEY_IS_SEARCH_BLOCK,false)
            binding?.run {
                tvRight.visibility = if (getBoolean(Constant.KEY_SHOW_RIGHT_CANCEL, false)) View.VISIBLE else View.GONE
            }
        }
        binding?.etSearch?.requestFocus()
    }

    override fun initRecyclerView(): RecyclerView? {
        return binding?.rvList
    }

    override fun initAdapter(): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser> {
        return ChatUIKitSearchUserAdapter()
    }

    override fun searchText(query: String) {
        this.keyWords = query
        if (!mListAdapter.data.isNullOrEmpty()){
            mListAdapter.clearData()
        }

        if (!TextUtils.isEmpty(query)) {
            if (mListAdapter is ChatUIKitSearchUserAdapter){
                (mListAdapter as ChatUIKitSearchUserAdapter).searchText(query)
            }
            query.let { content->
                if (isSearchBlockUser){
                    searchViewModel?.searchBlockUser(content)
                }else{
                    searchViewModel?.searchUser(content)
                }
            }
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        super.onItemClick(view, position)
        mListAdapter.getItem(position)?.let {
            itemClickListener?.onSearchItemClick(view,position,it)
        }
    }

    override fun refreshData() {
        if (!keyWords.isNullOrEmpty()){
            keyWords?.let {
                searchViewModel?.searchUser(it)
            }
        }
    }

    override fun onTvRightClick(view: View) {
        onCancelListener?.onCancelClick(view)
    }

    override fun searchSuccess(result: Any) {
        finishRefresh()
        if (result is MutableList<*>) {
            this.data = result as MutableList<ChatUIKitUser>
            mListAdapter.setData(result)
        }
    }

    override fun searchBlockUserSuccess(result: Any) {
        finishRefresh()
        if (result is MutableList<*>) {
            this.data = result as MutableList<ChatUIKitUser>
            mListAdapter.setData(result)
        }
    }

    interface OnCancelClickListener{
        fun onCancelClick(view:View)
    }

    private fun setItemClickListener(itemClickListener: OnSearchUserItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    private fun setOnCancelListener(onCancelListener: OnCancelClickListener?) {
        this.onCancelListener = onCancelListener
    }


    class Builder {
        private val bundle: Bundle = Bundle()
        private var customFragment: ChatUIKitSearchUserFragment? = null
        private var itemClickListener: OnSearchUserItemClickListener? = null
        private var onCancelListener:OnCancelClickListener?=null

        fun <T : ChatUIKitSearchUserFragment?> setCustomFragment(fragment: T): Builder {
            customFragment = fragment
            return this
        }

        fun showRightCancel(showCancel: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_SHOW_RIGHT_CANCEL, showCancel)
            return this
        }

        fun setItemClickListener(itemClickListener: OnSearchUserItemClickListener): Builder {
            this.itemClickListener = itemClickListener
            return this
        }

        fun setOnCancelListener(onCancelListener: OnCancelClickListener?): Builder {
            this.onCancelListener = onCancelListener
            return this
        }

        fun setSearchBlockUser(isSearchBlockUser: Boolean): Builder{
            bundle.putBoolean(Constant.KEY_IS_SEARCH_BLOCK, isSearchBlockUser)
            return this
        }

        fun build(): ChatUIKitSearchUserFragment {
            val fragment =
                if (customFragment != null) customFragment else ChatUIKitSearchUserFragment()
            fragment!!.arguments = bundle
            fragment.setItemClickListener(itemClickListener)
            fragment.setOnCancelListener(onCancelListener)
            return fragment
        }
    }

    private object Constant {
        const val KEY_SHOW_RIGHT_CANCEL = "key_show_right_cancel"
        const val KEY_IS_SEARCH_BLOCK = "key_is_search_block"
    }

}