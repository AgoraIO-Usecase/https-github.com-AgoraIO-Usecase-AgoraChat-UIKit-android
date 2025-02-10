package io.agora.chat.uikit.feature.contact.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.feature.contact.adapter.ChatUIKitContactListAdapter
import io.agora.chat.uikit.common.enums.ChatUIKitListViewType
import io.agora.chat.uikit.common.RefreshHeader
import io.agora.chat.uikit.common.helper.SidebarHelper
import io.agora.chat.uikit.common.impl.OnItemClickListenerImpl
import io.agora.chat.uikit.common.impl.OnItemLongClickListenerImpl
import io.agora.chat.uikit.feature.contact.interfaces.IContactListLayout
import io.agora.chat.uikit.feature.contact.interfaces.IUIKitContactResultView
import io.agora.chat.uikit.feature.contact.interfaces.OnContactEventListener
import io.agora.chat.uikit.interfaces.OnItemClickListener
import io.agora.chat.uikit.interfaces.OnItemLongClickListener
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.viewmodel.contacts.ChatUIKitContactListViewModel
import io.agora.chat.uikit.viewmodel.contacts.IContactListRequest
import io.agora.chat.uikit.widget.ChatUIKitSidebar
import io.agora.chat.uikit.widget.RefreshLayout
import java.util.concurrent.ConcurrentHashMap

class ChatUIKitContactListLayout@JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr), IContactListLayout ,IUIKitContactResultView {
    private var isFirstLoadInfo = false
    /**
     * Refresh layout.
     */
    private val refreshLayout: RefreshLayout by lazy { findViewById(R.id.refresh_layout) }

    /**
     * Contact list view
     */
    val rvContactList: RecyclerView by lazy { findViewById(R.id.rv_list) }

    /**
     * side bar
     */
    private val sideBarContact: ChatUIKitSidebar by lazy { findViewById(R.id.side_bar_contact) }

    /**
     * floating_header
     */
    private val floatingHeader: TextView by lazy { findViewById(R.id.floating_header) }


    /**
     * Item click listener set by user.
     */
    private var itemClickListener: OnItemClickListener? = null

    /**
     * Item long click listener set by user.
     */
    private var itemLongClickListener: OnItemLongClickListener? = null

    /**
     * Load contact listener.
     */
    private var loadContactListener:OnContactEventListener? = null


    private var contactViewModel: IContactListRequest? = null

    private var viewType: ChatUIKitListViewType? = ChatUIKitListViewType.LIST_CONTACT

    private var isFirstLoadFromServer:Boolean = true

    /**
     * Concat adapter
     */
    private val concatAdapter: ConcatAdapter by lazy {
        val config = ConcatAdapter.Config.Builder()
            .setStableIdMode(ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS)
            .build()
        ConcatAdapter(config)
    }

    /**
     * user list adapter
     */
    private var listAdapter: ChatUIKitContactListAdapter? = null

    init {
        initAttrs(context, attrs)
        initViews()
        initListener()
    }

    private fun initViews() {
        LayoutInflater.from(context).inflate(R.layout.uikit_contact_list, this, true)
        rvContactList.layoutManager = LinearLayoutManager(context)
        listAdapter = ChatUIKitContactListAdapter()
        listAdapter?.setHasStableIds(true)
        concatAdapter.addAdapter(listAdapter!!)
        rvContactList.adapter = concatAdapter

        rvContactList.isNestedScrollingEnabled = false

        // Set refresh layout
        // Can not load more
        refreshLayout.setEnableLoadMore(false)
        val refreshHeader = refreshLayout.refreshHeader
        if (refreshHeader == null) {
            refreshLayout.setRefreshHeader(RefreshHeader(context))
        }

        contactViewModel = ViewModelProvider(context as AppCompatActivity)[ChatUIKitContactListViewModel::class.java]
        contactViewModel?.attachView(this)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        if (attrs == null) return
    }

    private fun initListener() {
        refreshLayout.setOnRefreshListener {
            contactViewModel?.loadData()
        }

        rvContactList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val visibleList = listAdapter?.mData?.filterIndexed { index, _ ->
                        index in firstVisibleItemPosition..lastVisibleItemPosition
                    }
                    if (!visibleList.isNullOrEmpty()) {
                        fetchContactInfo(visibleList)
                    }
                }
            }
        })

        listAdapter?.setOnItemClickListener(OnItemClickListenerImpl {
                view, position ->
            itemClickListener?.onItemClick(view, position)
        })

        listAdapter?.setOnItemLongClickListener(OnItemLongClickListenerImpl {
                view, position ->
            if (itemLongClickListener != null) {
                return@OnItemLongClickListenerImpl itemLongClickListener?.onItemLongClick(view, position) ?: true
            }
            return@OnItemLongClickListenerImpl true
        })
    }

    fun loadContactData(fetchServerData: Boolean) {
        contactViewModel?.loadData(fetchServerData)
    }

    fun fetchContactInfo(visibleList:List<ChatUIKitUser>?){
        contactViewModel?.fetchContactInfo(visibleList)
    }

    override fun setViewModel(viewModel: IContactListRequest?) {
        this.contactViewModel = viewModel
        this.contactViewModel?.attachView(this)
    }

    override fun setListAdapter(adapter: ChatUIKitContactListAdapter?) {
        adapter?.run {
            listAdapter?.let {
                if (concatAdapter.adapters.contains(it)) {
                    val index = concatAdapter.adapters.indexOf(it)
                    concatAdapter.removeAdapter(it)
                    concatAdapter.addAdapter(index, adapter)
                } else {
                    concatAdapter.addAdapter(adapter)
                }
            } ?: concatAdapter.addAdapter(adapter)
            listAdapter = this
        }
    }

    override fun getListAdapter(): ChatUIKitContactListAdapter? {
        return listAdapter
    }

    override fun getItem(position: Int): ChatUIKitUser? {
        return listAdapter?.getItem(position)
    }

    override fun setListViewType(type: ChatUIKitListViewType?) {
        type?.let {
            this.viewType = it
            listAdapter?.setListViewItemType(it)
        }
        when(viewType){
            ChatUIKitListViewType.LIST_CONTACT -> {  loadContactData(true) }
            ChatUIKitListViewType.LIST_SELECT_CONTACT -> { loadContactData(false) }
            else -> { loadContactData(false) }
        }
    }

    override fun setSideBarVisible(isVisible: Boolean?) {
        if (isVisible == true){
            sideBarContact.visibility = View.VISIBLE
            val sidebarHelper = SidebarHelper()
            sidebarHelper.setupWithRecyclerView(
                rvContactList,
                listAdapter,
                floatingHeader
            )
            sideBarContact.setOnTouchEventListener(sidebarHelper)
        }else{
            sideBarContact.visibility = View.GONE
        }
    }

    override fun addHeaderAdapter(adapter: RecyclerView.Adapter<*>?) {
        concatAdapter.addAdapter(0, adapter!!)
    }

    override fun addFooterAdapter(adapter: RecyclerView.Adapter<*>?) {
        concatAdapter.addAdapter(adapter!!)
    }

    override fun removeAdapter(adapter: RecyclerView.Adapter<*>?) {
        concatAdapter.removeAdapter(adapter!!)
    }

    override fun addItemDecoration(decor: RecyclerView.ItemDecoration) {
        rvContactList.addItemDecoration(decor)
    }

    override fun removeItemDecoration(decor: RecyclerView.ItemDecoration) {
        rvContactList.removeItemDecoration(decor)
    }

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemClickListener = listener
    }

    override fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        this.itemLongClickListener = listener
    }

    override fun setLoadContactListener(listener: OnContactEventListener) {
        this.loadContactListener = listener
    }

    override fun loadContactListSuccess(list: MutableList<ChatUIKitUser>) {
        refreshLayout.finishRefresh()
        listAdapter?.setData(list.toMutableList())
        listAdapter?.mData?.let {
            if (it.size > 0){
                loadContactListener?.loadContactListSuccess(list)
            }else {
                if (isFirstLoadFromServer){
                    loadContactData(true)
                    isFirstLoadFromServer = false
                }else{}
            }
        }
    }

    override fun loadContactListFail(code: Int, error: String) {
        refreshLayout.finishRefresh()
        loadContactListener?.loadContactListFail(code, error)
    }

    override fun addContactSuccess(userId: String) {
        loadContactListener?.addContactSuccess(userId)
    }

    override fun addContactFail(code: Int, error: String) {
        loadContactListener?.addContactFail(code, error)
    }

    override fun setUserAvatarInfo(info: ConcurrentHashMap<String, Int>?) {
        listAdapter?.setUserAvatarInfo(info)
    }

    override fun fetchUserInfoByUserSuccess(users: List<ChatUIKitUser>?) {
        if (!users.isNullOrEmpty()) {
            listAdapter?.notifyItemRangeChanged(0, listAdapter?.itemCount ?: 0)
            if (!isFirstLoadInfo){
                contactViewModel?.loadData()
                isFirstLoadInfo = true
            }
        }
    }

    override fun onDetachedFromWindow() {
        itemLongClickListener = null
        contactViewModel?.detachView()
        super.onDetachedFromWindow()
    }
}