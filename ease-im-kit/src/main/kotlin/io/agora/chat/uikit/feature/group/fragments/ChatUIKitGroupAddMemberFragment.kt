package io.agora.chat.uikit.feature.group.fragments

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.base.ChatUIKitBaseListFragment
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.helper.SidebarHelper
import io.agora.chat.uikit.feature.contact.interfaces.IUIKitContactResultView
import io.agora.chat.uikit.feature.group.adapter.ChatUIKitGroupSelectListAdapter
import io.agora.chat.uikit.feature.group.interfaces.IUIKitGroupResultView
import io.agora.chat.uikit.feature.group.interfaces.ISearchResultListener
import io.agora.chat.uikit.interfaces.OnContactSelectedListener
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.viewmodel.contacts.ChatUIKitContactListViewModel
import io.agora.chat.uikit.viewmodel.contacts.IContactListRequest
import io.agora.chat.uikit.widget.ChatUIKitSidebar

open class ChatUIKitGroupAddMemberFragment: ChatUIKitBaseListFragment<ChatUIKitUser>()
    ,IUIKitContactResultView,IUIKitGroupResultView, OnContactSelectedListener, ISearchResultListener {
    private val memberSelectAdapter: ChatUIKitGroupSelectListAdapter by lazy { ChatUIKitGroupSelectListAdapter(groupId) }
    private var groupId:String?=null
    private var currentGroup:ChatGroup?=null
    private var contactViewModel: IContactListRequest? = null
    private var listener:OnContactSelectedListener?=null
    private var sideBarContact:ChatUIKitSidebar?=null

    companion object {
        private const val TAG = "ChatUIKitGroupAddMemberFragment"
    }

    override fun initRecyclerView(): RecyclerView? {
        return binding?.rvList
    }

    override fun initAdapter(): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser> {
        return memberSelectAdapter
    }

    override fun initView(savedInstanceState: Bundle?) {
        groupId = arguments?.getString(ChatUIKitConstant.EXTRA_CONVERSATION_ID) ?: ""
        super.initView(savedInstanceState)
        groupId?.let {
            currentGroup = ChatClient.getInstance().groupManager().getGroup(it)
        }
        initSideBar()
        contactViewModel = ViewModelProvider(context as AppCompatActivity)[ChatUIKitContactListViewModel::class.java]
    }

    open fun initSideBar(){
        sideBarContact?.visibility = View.VISIBLE
        val sidebarHelper = SidebarHelper()
        sidebarHelper.setupWithRecyclerView(
            binding?.rvList,
            memberSelectAdapter
        )
        sideBarContact?.setOnTouchEventListener(sidebarHelper)
    }

    override fun initListener() {
        super.initListener()
        memberSelectAdapter.setCheckBoxSelectListener(this)
        setSearchResultListener(this)
        binding?.rvList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                // When scroll to bottom, load more data
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val visibleList = memberSelectAdapter.mData?.filterIndexed { index, _ ->
                        index in firstVisibleItemPosition..lastVisibleItemPosition
                    }
                    if (visibleList.isNullOrEmpty()) {
                        return
                    }
                    contactViewModel?.fetchContactInfo(visibleList)
                }
            }
        })
    }

    override fun initData() {
        loadData()
    }

    fun setMemberList(members: MutableList<String>){
        memberSelectAdapter.setGroupMemberList(members)
    }

    fun addSelectMember(members: MutableList<String>){
        memberSelectAdapter.addSelectList(members)
    }

    fun setSideBar(sidebar:ChatUIKitSidebar){
        this.sideBarContact = sidebar
    }

    override fun initViewModel() {
        super.initViewModel()
        contactViewModel?.attachView(this)
    }

    override fun refreshData() {
        loadLocalData()
    }

    open fun loadData(){
        contactViewModel?.loadData(true)
    }

    open fun loadLocalData(){
        contactViewModel?.loadData(false)
    }

    fun resetSelect(){
        memberSelectAdapter.resetSelect()
    }

    override fun loadContactListSuccess(list: MutableList<ChatUIKitUser>) {
        finishRefresh()
        memberSelectAdapter.setData(list)
    }

    override fun loadContactListFail(code: Int, error: String) {
        finishRefresh()
    }

    override fun onSearchResultListener(selectMembers: MutableList<String>) {
        ChatLog.d(TAG,"onSearchResultListener $selectMembers")
        if (selectMembers.isNotEmpty()){
            memberSelectAdapter.addSelectList(selectMembers)
            listener?.onSearchSelectedResult(selectMembers)
        }
    }

    override fun onContactSelectedChanged(v: View, selectedMembers: MutableList<String>) {
        listener?.onContactSelectedChanged(v, selectedMembers)
    }

    fun setAddSelectListener(listener: OnContactSelectedListener){
        this.listener = listener
    }

    override fun fetchUserInfoByUserSuccess(users: List<ChatUIKitUser>?) {
        if (!users.isNullOrEmpty()) {
            memberSelectAdapter.notifyDataSetChanged()
        }
    }


}