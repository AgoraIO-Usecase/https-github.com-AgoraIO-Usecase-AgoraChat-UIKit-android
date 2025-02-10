package io.agora.chat.uikit.feature.thread.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseFragment
import io.agora.chat.uikit.common.ChatCursorResult
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatThread
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.RefreshHeader
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.isAdmin
import io.agora.chat.uikit.common.extensions.isOwner
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.databinding.UikitFragmentThreadListBinding
import io.agora.chat.uikit.feature.thread.adapter.ChatUIKitThreadListAdapter
import io.agora.chat.uikit.feature.thread.interfaces.IChatThreadResultView
import io.agora.chat.uikit.feature.thread.interfaces.OnChatThreadListItemClickListener
import io.agora.chat.uikit.interfaces.OnItemClickListener
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.viewmodel.thread.ChatUIKitThreadViewModel
import io.agora.chat.uikit.viewmodel.thread.IChatThreadRequest
import kotlinx.coroutines.launch

open class ChatUIKitThreadListFragment: ChatUIKitBaseFragment<UikitFragmentThreadListBinding>(),IChatThreadResultView, OnItemClickListener {
    private var adapter: ChatUIKitThreadListAdapter? = null
    private val layoutManager by lazy { LinearLayoutManager(mContext) }
    private var viewModel: IChatThreadRequest? = null
    private var backPressListener: View.OnClickListener? = null
    private var itemClickListener:OnChatThreadListItemClickListener? = null
    private var parentId: String? = ""
    private var cursor: String = ""
    private var isGroupAdmin = false
    private var mGroup: ChatGroup? = null
    private var data:MutableList<ChatThread> = mutableListOf()

    companion object{
        const val limit:Int = 10
        const val TAG = "ChatUIKitThreadListFragment"
        const val KEY_ENABLE_BACK = "key_enable_back"
        const val KEY_USE_TITLE_REPLACE = "key_use_replace_action_bar"
        const val KEY_USE_TITLE = "key_use_title"
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UikitFragmentThreadListBinding {
        return UikitFragmentThreadListBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        arguments?.let {
            parentId = it.getString(ChatUIKitConstant.EXTRA_CONVERSATION_ID)
            binding?.titleContact?.visibility = View.GONE
            val useHeader: Boolean = it.getBoolean(KEY_USE_TITLE, false)
            binding?.titleContact?.visibility = if (useHeader) View.VISIBLE else View.GONE
            if (useHeader){
                val canBack: Boolean = it.getBoolean(KEY_ENABLE_BACK, false)
                binding?.titleContact?.setDisplayHomeAsUpEnabled(canBack
                    , it.getBoolean(KEY_USE_TITLE_REPLACE, false))
                binding?.titleContact?.setNavigationOnClickListener {
                    if (backPressListener != null) {
                        backPressListener?.onClick(it)
                        return@setNavigationOnClickListener
                    }
                    mContext.onBackPressed()
                }
            }
        }

        binding?.run {
            rvList.layoutManager = layoutManager
            if (adapter == null){
                adapter = ChatUIKitThreadListAdapter()
            }
            rvList.adapter = adapter

            refreshLayout.setEnableLoadMore(true)
            val refreshHeader = refreshLayout.refreshHeader
            if (refreshHeader == null) {
                refreshLayout.setRefreshHeader(RefreshHeader(mContext))
            }
            updateTitle()
        }
    }

    override fun initViewModel() {
        super.initViewModel()

        viewModel = ViewModelProvider(this)[ChatUIKitThreadViewModel::class.java]
        viewModel?.attachView(this)
    }

    override fun initListener() {
        super.initListener()
        binding?.run {
            adapter?.setOnItemClickListener(this@ChatUIKitThreadListFragment)
            titleContact.setNavigationOnClickListener {
                if (backPressListener != null) {
                    backPressListener?.onClick(it)
                    return@setNavigationOnClickListener
                }
                mContext.finish()
            }
            refreshLayout.setOnRefreshListener {
                refreshData()
            }
            refreshLayout.setOnLoadMoreListener {
                loadMoreData()
            }
            rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    // When scroll to bottom, load more data
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                        val visibleList = adapter?.mData?.filterIndexed { index, _ ->
                            index in firstVisibleItemPosition..lastVisibleItemPosition
                        }
                        visibleList?.let {
                            parseResult(it.toMutableList())
                        }
                    }
                }
            })
        }
    }

    open fun initEventBus() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.DESTROY.name).register(viewLifecycleOwner) {
            if (it.isThreadChange) {
                refreshData()
            }
        }
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(viewLifecycleOwner) {
            if (it.isThreadChange) {
                refreshData()
            }
        }
    }

    private fun setHeaderBackPressListener(listener: View.OnClickListener?) {
        this.backPressListener = listener
    }

    override fun initData() {
        super.initData()
        parentId?.let {
            viewModel?.setGroupInfo(it)
        }
        initEventBus()
    }

    private fun loadMoreData(){
        getThreadInfo()
    }

    private fun refreshData(){
        cursor = ""
        data.clear()
        adapter?.clearData()
        adapter?.clearLatestMessages()
        getThreadInfo()
    }

    private fun getThreadInfo(){
        if (isGroupAdmin){
            mGroup?.groupId?.let {
                viewModel?.fetchChatThreadsFromServer(it,limit,cursor)
            }
        }else{
            mGroup?.groupId?.let {
                viewModel?.getJoinedChatThreadsFromServer(it,limit,cursor)
            }
        }
    }

    private fun updateTitle(){
        if (adapter?.mData == null){
            binding?.titleContact?.setTitle(resources.getString(R.string.uikit_thread_list_title,""))
        }else{
            data.let {
                val count = "(${it.size})"
                binding?.titleContact?.setTitle(resources.getString(R.string.uikit_thread_list_title,count))
            }
        }
    }

    private fun setOnChatThreadListItemClickListener(listener:OnChatThreadListItemClickListener?){
        this.itemClickListener = listener
    }

    private fun setCustomAdapter(adapter: ChatUIKitThreadListAdapter?) {
        this.adapter = adapter
    }

    override fun onItemClick(view: View?, position: Int) {
        if (data.isNotEmpty() && position < data.size){
            itemClickListener?.onChatThreadItemClick(view,data[position])
        }
    }

    override fun settGroupInfoSuccess(parent: ChatGroup?) {
        mGroup = parent
        mGroup?.let {
            isGroupAdmin = it.isOwner() || it.isAdmin()
            getThreadInfo()
        }
    }

    override fun setGroupInfoFail(code: Int, message: String?) {
        ChatLog.e(TAG,"getThreadParentInfoFail $code $message")
    }

    override fun getJoinedChatThreadsFromServerSuccess(result: ChatCursorResult<ChatThread>) {
        cursor = result.cursor
        finishRefresh()
        result.data.let {
            parseResult(it)
            data.addAll(it)
            adapter?.setData(data)
            updateTitle()
        }
    }

    override fun getJoinedChatThreadsFromServerFail(code: Int, message: String?) {
        ChatLog.e(TAG,"getJoinedChatThreadsFromServerFail $code $message")
        finishRefresh()
    }

    override fun fetchChatThreadsFromServerSuccess(result: ChatCursorResult<ChatThread>) {
        cursor = result.cursor
        finishRefresh()
        result.data.let {
            parseResult(it)
            data.addAll(it)
            adapter?.setData(data)
            updateTitle()
        }
    }

    private fun parseResult(result:MutableList<ChatThread>){
        val messageMap:MutableMap<String,ChatMessage> = mutableMapOf()
        val first = mutableListOf<String>()
        result.forEach{
            val lastMsgMap = adapter?.getLatestMessages()
            lastMsgMap?.let { map->
                if (map.containsKey(it.chatThreadId)){
                    return@forEach
                }
            }
            if (it.lastMessage == null){
                first.add(it.chatThreadId)
            }else{
                messageMap[it.chatThreadId] = it.lastMessage
            }
        }
        messageMap.let {
            if (it.isNotEmpty()){
                adapter?.setLatestMessages(it)
            }
        }
        first.let {
            if (it.isNotEmpty()){
                viewModel?.getChatThreadLatestMessage(it)
            }
        }
    }

    fun finishRefresh(){
        mContext.mainScope().launch {
            binding?.refreshLayout?.let {
                if (it.isRefreshing){
                    it.finishRefresh()
                }
                if (it.isLoading){
                    it.finishLoadMore()
                }
            }
        }
    }

    override fun fetchChatThreadsFromServerFail(code: Int, message: String?) {
        ChatLog.e(TAG,"fetchChatThreadsFromServerFail $code $message")
        finishRefresh()
        updateTitle()
    }

    override fun getChatThreadLatestMessageSuccess(result: MutableMap<String, ChatMessage>) {
        ChatLog.e(TAG,"getChatThreadLatestMessageSuccess ${result.size}")
        adapter?.setLatestMessages(result)
        finishRefresh()
    }

    override fun getChatThreadLatestMessageFail(code: Int, message: String?) {
        ChatLog.e(TAG,"getChatThreadLatestMessageFail $code $message")
        finishRefresh()
    }

    class Builder(
        private val conversationId: String?,
    ){
        protected val bundle: Bundle = Bundle()
        private var adapter: ChatUIKitThreadListAdapter? = null
        private var backPressListener: View.OnClickListener? = null
        protected var customFragment: ChatUIKitThreadListFragment? = null
        private var itemClickListener:OnChatThreadListItemClickListener? = null

        init {
            bundle.putString(ChatUIKitConstant.EXTRA_CONVERSATION_ID, conversationId)
        }

        /**
         * Set custom fragment which should extends EaseMessageFragment
         *
         * @param fragment
         * @param <T>
         * @return
        </T> */
        fun <T : ChatUIKitThreadListFragment?> setCustomFragment(fragment: T): Builder {
            customFragment = fragment
            return this
        }

        /**
         * Set custom adapter which should extends EaseMessageAdapter
         *
         * @param adapter
         * @return
         */
        fun setCustomAdapter(adapter: ChatUIKitThreadListAdapter?): Builder {
            this.adapter = adapter
            return this
        }

        /**
         * Whether show back icon in titleBar
         *
         * @param canBack
         * @return
         */
        fun enableTitleBarPressBack(canBack: Boolean): Builder {
            bundle.putBoolean(KEY_ENABLE_BACK, canBack)
            return this
        }

        /**
         * If you have set [Builder.enableTitleBarPressBack], you can set the listener
         *
         * @param listener
         * @return
         */
        fun setTitleBarBackPressListener(listener: View.OnClickListener?):Builder {
            backPressListener = listener
            return this
        }

        /**
         * Whether to use default titleBar to replace actionBar when activity is a AppCompatActivity.
         * If set true, will call [androidx.appcompat.app.AppCompatActivity.setSupportActionBar].
         * @param replace
         * @return
         */
        fun useTitleBarToReplaceActionBar(replace: Boolean): Builder {
            bundle.putBoolean(KEY_USE_TITLE_REPLACE, replace)
            return this
        }

        /**
         * Whether to use default titleBar which is [ChatUIKitTitleBar]
         *
         * @param useTitle
         * @return
         */
        fun useTitleBar(useTitle: Boolean): Builder {
            bundle.putBoolean(KEY_USE_TITLE, useTitle)
            return this
        }

        fun setOnChatThreadListItemClickListener(listener:OnChatThreadListItemClickListener?):Builder{
            this.itemClickListener = listener
            return this
        }

        fun build(): ChatUIKitThreadListFragment? {
            val fragment = if (customFragment != null) customFragment else ChatUIKitThreadListFragment()
            fragment?.let {
                it.arguments = bundle
                it.setOnChatThreadListItemClickListener(itemClickListener)
                it.setHeaderBackPressListener(backPressListener)
                it.setCustomAdapter(adapter)
            }
            return fragment
        }
    }

}