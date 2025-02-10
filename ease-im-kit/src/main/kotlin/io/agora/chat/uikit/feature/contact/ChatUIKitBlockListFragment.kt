package io.agora.chat.uikit.feature.contact

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseFragment
import io.agora.chat.uikit.common.RefreshHeader
import io.agora.chat.uikit.common.helper.SidebarHelper
import io.agora.chat.uikit.common.impl.OnItemLongClickListenerImpl
import io.agora.chat.uikit.databinding.FragmentBlockListLayoutBinding
import io.agora.chat.uikit.feature.contact.adapter.ChatUIKitContactListAdapter
import io.agora.chat.uikit.feature.contact.interfaces.IUIKitContactResultView
import io.agora.chat.uikit.feature.search.ChatUIKitSearchActivity
import io.agora.chat.uikit.feature.search.ChatUIKitSearchType
import io.agora.chat.uikit.interfaces.OnItemLongClickListener
import io.agora.chat.uikit.interfaces.OnUserListItemClickListener
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.viewmodel.contacts.ChatUIKitContactListViewModel

open class ChatUIKitBlockListFragment: ChatUIKitBaseFragment<FragmentBlockListLayoutBinding>(), IUIKitContactResultView {
    private var listAdapter: ChatUIKitContactListAdapter? = null
    private var backPressListener: View.OnClickListener? = null
    private var userListItemClickListener: OnUserListItemClickListener? = null
    private var itemLongClickListener: OnItemLongClickListener? = null
    private var searchType: ChatUIKitSearchType? = ChatUIKitSearchType.BLOCK_USER
    private val contactViewModel by lazy { ViewModelProvider(this)[ChatUIKitContactListViewModel::class.java] }
    private var data:MutableList<ChatUIKitUser> = mutableListOf()

    private val returnSearchClickResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onClickResult(result) }


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBlockListLayoutBinding{
        return FragmentBlockListLayoutBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        arguments?.run {
            binding?.run {
                updateCount(data.size)
                rvList.layoutManager = LinearLayoutManager(context)
                listAdapter = ChatUIKitContactListAdapter()
                rvList.adapter = listAdapter
                rvList.isNestedScrollingEnabled = false

                refreshLayout.setEnableLoadMore(false)
                val refreshHeader = refreshLayout.refreshHeader
                if (refreshHeader == null) {
                    refreshLayout.setRefreshHeader(RefreshHeader(context))
                }

                searchBar.visibility = if (getBoolean(Constant.KEY_USE_SEARCH, false)) View.VISIBLE else View.GONE
                searchBar.setText(context?.getString(R.string.uikit_search_block_user))
                getString(Constant.KEY_SET_SEARCH_TITLE)?.let {
                    if (it.isNotEmpty()){
                        searchBar.setText(it)
                    }
                }
                titleContact.visibility = if (getBoolean(Constant.KEY_USE_TITLE, false)) View.VISIBLE else View.GONE
                getString(Constant.KEY_SET_TITLE)?.let {
                    if (it.isNotEmpty()) {
                        titleContact.setTitle(it)
                    }
                }
                titleContact.setDisplayHomeAsUpEnabled(getBoolean(Constant.KEY_ENABLE_BACK, false)
                    , getBoolean(Constant.KEY_USE_TITLE_REPLACE, false))
                titleContact.setNavigationOnClickListener {
                    backPressListener?.onClick(it) ?: activity?.finish()
                }
                getInt(Constant.KEY_EMPTY_LAYOUT, -1).takeIf { it != -1 }?.let {
                    listAdapter?.setEmptyView(it)
                }

                val sideBarVisible = getBoolean(Constant.KEY_SIDEBAR_VISIBLE,true)
                if (sideBarVisible){
                    sideBarContact.visibility = View.VISIBLE
                    val sidebarHelper = SidebarHelper()
                    sidebarHelper.setupWithRecyclerView(
                        rvList,
                        listAdapter,
                        floatingHeader
                    )
                    sideBarContact.setOnTouchEventListener(sidebarHelper)
                }else{
                    sideBarContact.visibility = View.GONE
                }

            }
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        contactViewModel.attachView(this)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
    override fun initListener() {
        super.initListener()
        binding?.run {

            searchBar.setOnClickListener {
                returnSearchClickResult.launch(
                    ChatUIKitSearchActivity.createIntent(
                        context = mContext,
                        searchType = searchType
                    )
                )
            }

            refreshLayout.setOnRefreshListener {
                loadData()
            }

            rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                        val visibleList = listAdapter?.mData?.filterIndexed { index, _ ->
                            index in firstVisibleItemPosition..lastVisibleItemPosition
                        }
                        if (!visibleList.isNullOrEmpty()) {
                            contactViewModel.fetchContactInfo(visibleList)
                        }
                    }
                }
            })

            listAdapter?.setOnUserListItemClickListener(object : OnUserListItemClickListener{
                override fun onUserListItemClick(v: View?, position: Int, user: ChatUIKitUser?) {
                    userListItemClickListener?.onUserListItemClick(v, position, user) ?:kotlin.run {
                        defaultOnItemClickEvent(v, position, user)
                    }
                }
            })

            listAdapter?.setOnItemLongClickListener(OnItemLongClickListenerImpl {
                    view, position ->
                if (itemLongClickListener != null) {
                    return@OnItemLongClickListenerImpl itemLongClickListener?.onItemLongClick(view, position) ?: true
                }else{
                    defaultOnLongItemClickListener(view,position)
                }
                return@OnItemLongClickListenerImpl true
            })

        }
    }

    fun loadData(){
        val isLoad = ChatUIKitClient.isLoadBlockListFromServer?: false
        if (!isLoad){
            contactViewModel.fetchBlockListFromServer()
        }else{
            contactViewModel.getBlockListFromLocal()
        }
    }

    private fun onClickResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getSerializableExtra(Constant.KEY_USER)?.let {
                if (it is ChatUIKitUser) {
                    userListItemClickListener?.onUserListItemClick(null, -1, it) ?: defaultAction(it)
                }
            }
        }
    }

    private fun defaultAction(user: ChatUIKitUser?) {
        user?.run {
            if (searchType == null || searchType == ChatUIKitSearchType.BLOCK_USER) {
                startActivity(ChatUIKitContactDetailsActivity.createIntent(mContext, this))
            }
        }
    }

    open fun updateCount(count:Int){
        binding?.run {
            titleContact.setTitle(context?.getString(R.string.uikit_block_title,count))
        }
    }

    private fun setCustomAdapter(adapter: ChatUIKitContactListAdapter?) {
        this.listAdapter = adapter
    }

    private fun setOnBackPressListener(backPressListener: View.OnClickListener?) {
        this.backPressListener = backPressListener
    }

    private fun setOnItemLongClickListener(itemLongClickListener: OnItemLongClickListener?) {
        this.itemLongClickListener = itemLongClickListener
    }

    private fun setOnUserListItemClickListener(listener:OnUserListItemClickListener?){
        this.userListItemClickListener = listener
    }

    override fun fetchBlockListFromServerSuccess(list: MutableList<ChatUIKitUser>) {
        ChatUIKitClient.isLoadBlockListFromServer = true
        binding?.refreshLayout?.finishRefresh()
        listAdapter?.setData(list.toMutableList())
        data = list
        updateCount(list.size)
    }

    override fun fetchBlockListFromServerFail(code: Int, error: String) {
        binding?.refreshLayout?.finishRefresh()
    }

    override fun getBlockListFromLocalSuccess(list: MutableList<ChatUIKitUser>) {
        binding?.refreshLayout?.finishRefresh()
        listAdapter?.setData(list.toMutableList())
        data = list
        updateCount(list.size)
    }

    override fun getBlockListFromLocalFail(code: Int, error: String) {
        binding?.refreshLayout?.finishRefresh()
    }

    override fun fetchUserInfoByUserSuccess(users: List<ChatUIKitUser>?) {
        if (!users.isNullOrEmpty()) {
            listAdapter?.notifyItemRangeChanged(0, listAdapter?.itemCount ?: 0)
        }
    }

    open fun defaultOnLongItemClickListener(view:View?, position:Int){

    }

    open fun defaultOnItemClickEvent(v: View?, position: Int, user: ChatUIKitUser?){
        user?.let {
            startActivity(ChatUIKitContactDetailsActivity.createIntent(mContext,it))
        }

    }


    open class Builder {
        private val bundle: Bundle = Bundle()
        protected var customFragment: ChatUIKitBlockListFragment? = null
        protected var adapter: ChatUIKitContactListAdapter? = null
        private var userListItemClickListener: OnUserListItemClickListener? = null
        private var itemLongClickListener: OnItemLongClickListener? = null
        private var backPressListener: View.OnClickListener? = null

        /**
         * Set custom fragment which should extends ChatUIKitBlockListFragment
         * @param fragment
         * @param <T>
         * @return
        </T> */
        fun <T : ChatUIKitBlockListFragment?> setCustomFragment(fragment: T): Builder {
            customFragment = fragment
            return this
        }

        /**
         * Whether to use default titleBar which is [io.agora.chat.uikit.widget.ChatUIKitTitleBar]
         * @param useTitle
         * @return
         */
        fun useTitleBar(useTitle: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_USE_TITLE, useTitle)
            return this
        }

        /**
         * Whether to use default titleBar to replace actionBar when activity is a AppCompatActivity.
         * If set true, will call [androidx.appcompat.app.AppCompatActivity.setSupportActionBar].
         * @param replace
         * @return
         */
        fun useTitleBarToReplaceActionBar(replace: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_USE_TITLE_REPLACE, replace)
            return this
        }

        /**
         * Set titleBar's title
         * @param title
         * @return
         */
        fun setTitleBarTitle(title: String?): Builder {
            bundle.putString(Constant.KEY_SET_TITLE, title)
            return this
        }

        /**
         * Whether show back icon in titleBar
         * @param canBack
         * @return
         */
        fun enableTitleBarPressBack(canBack: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_ENABLE_BACK, canBack)
            return this
        }

        /**
         * If you have set [ChatUIKitBlockListFragment.Builder.enableTitleBarPressBack], you can set the listener
         * @param listener
         * @return
         */
        fun setTitleBarBackPressListener(listener: View.OnClickListener?): Builder {
            backPressListener = listener
            return this
        }

        /**
         * Whether to use search bar.
         * @param useSearchBar
         */
        fun useSearchBar(useSearchBar: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_USE_SEARCH, useSearchBar)
            return this
        }


        /**
         * Set searchBar's title
         * @param title
         * @return
         */
        fun setSearchBarTitle(title: String?): Builder {
            bundle.putString(Constant.KEY_SET_SEARCH_TITLE, title)
            return this
        }

        /**
         * Set chat list's empty layout if you want replace the default
         * @param emptyLayout
         * @return
         */
        fun setEmptyLayout(@LayoutRes emptyLayout: Int): Builder {
            bundle.putInt(Constant.KEY_EMPTY_LAYOUT, emptyLayout)
            return this
        }

        /**
         * Set custom adapter which should extends ChatUIKitContactListAdapter
         * @param adapter
         * @return
         */
        fun setCustomAdapter(adapter: ChatUIKitContactListAdapter?): Builder {
            this.adapter = adapter
            return this
        }

        /**
         * Set is show side bar
         * @param isVisible
         * @return
         */
        fun setSideBarVisible(isVisible:Boolean): Builder {
            bundle.putBoolean(Constant.KEY_SIDEBAR_VISIBLE,true)
            return this
        }

        /**
         * Set contact list item click listener return ChatUIKitUser
         * @param listener
         * @return
         */
        fun setOnUserListItemClickListener(listener:OnUserListItemClickListener?): Builder {
            this.userListItemClickListener = listener
            return this
        }

        /**
         * Set contact item long click listener
         * @param itemLongClickListener
         */
        fun setOnItemLongClickListener(itemLongClickListener: OnItemLongClickListener?): Builder {
            this.itemLongClickListener = itemLongClickListener
            return this
        }

        open fun build(): ChatUIKitBlockListFragment {
            val fragment =
                if (customFragment != null) customFragment else ChatUIKitBlockListFragment()
            fragment!!.arguments = bundle
            fragment.setCustomAdapter(adapter)
            fragment.setOnBackPressListener(backPressListener)
            fragment.setOnUserListItemClickListener(userListItemClickListener)
            fragment.setOnItemLongClickListener(itemLongClickListener)
            return fragment
        }

    }

    private object Constant {
        const val KEY_USE_TITLE = "key_use_title"
        const val KEY_USE_TITLE_REPLACE = "key_use_replace_action_bar"
        const val KEY_SET_TITLE = "key_set_title"
        const val KEY_SET_SEARCH_TITLE = "key_set_search_title"
        const val KEY_USE_SEARCH = "key_use_search"
        const val KEY_EMPTY_LAYOUT = "key_empty_layout"
        const val KEY_ENABLE_BACK = "key_enable_back"
        const val KEY_SIDEBAR_VISIBLE = "key_side_bar_visible"
        const val KEY_USER = "user"
    }
}