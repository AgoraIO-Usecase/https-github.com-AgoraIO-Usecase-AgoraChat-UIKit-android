package io.agora.chat.uikit.feature.group.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.base.ChatUIKitBaseListFragment
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.helper.SidebarHelper
import io.agora.chat.uikit.feature.group.adapter.ChatUIKitGroupSelectListAdapter
import io.agora.chat.uikit.feature.group.interfaces.IUIKitGroupResultView
import io.agora.chat.uikit.feature.group.interfaces.ISearchResultListener
import io.agora.chat.uikit.interfaces.OnContactSelectedListener
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.viewmodel.group.ChatUIKitGroupViewModel
import io.agora.chat.uikit.viewmodel.group.IGroupRequest
import io.agora.chat.uikit.widget.ChatUIKitSidebar

open class ChatUIKitGroupRemoveMemberFragment : ChatUIKitBaseListFragment<ChatUIKitUser>(), IUIKitGroupResultView,
    OnContactSelectedListener, ISearchResultListener {
    private val memberSelectAdapter: ChatUIKitGroupSelectListAdapter by lazy { ChatUIKitGroupSelectListAdapter(groupId) }
    private var listener:OnContactSelectedListener?=null
    private var groupId:String?=null
    private var groupViewModel: IGroupRequest? = null
    private var data:MutableList<ChatUIKitUser> = mutableListOf()
    private var sideBarContact: ChatUIKitSidebar?=null

    companion object {
        private const val TAG = "ChatUIKitGroupRemoveMemberFragment"
    }

    override fun initRecyclerView(): RecyclerView? {
        return binding?.rvList
    }

    override fun initAdapter(): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser> {
        return memberSelectAdapter
    }

    override fun refreshData() {
        loadData()
    }

    override fun initData() {
        super.initData()
        loadData()
    }

    fun resetSelect(){
        memberSelectAdapter.resetSelect()
    }

    fun setMemberList(members: MutableList<String>){
        memberSelectAdapter.setGroupMemberList(members)
    }

    fun addSelectList(members: MutableList<String>){
        memberSelectAdapter.addSelectList(members)
    }

    open fun loadData(){
        finishRefresh()
        data.clear()
        groupId?.let { groupId->
            groupViewModel?.loadLocalMember(groupId)
        }
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

    fun setSideBar(sidebar:ChatUIKitSidebar){
        this.sideBarContact = sidebar
    }

    override fun initViewModel() {
        super.initViewModel()
        groupViewModel?.attachView(this)
    }

    override fun initView(savedInstanceState: Bundle?) {
        groupId = arguments?.getString(ChatUIKitConstant.EXTRA_CONVERSATION_ID) ?: ""
        super.initView(savedInstanceState)
        groupViewModel = ViewModelProvider(this)[ChatUIKitGroupViewModel::class.java]
        initSideBar()
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
                    groupId?.let { id ->
                        val idList = visibleList?.map { user->
                            user.userId
                        }
                        if (idList.isNullOrEmpty()){
                            return
                        }
                        groupViewModel?.fetchMemberInfo(id, idList)
                    }
                }
            }
        })
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

    fun setRemoveSelectListener(listener: OnContactSelectedListener){
        this.listener = listener
    }

    override fun loadLocalMemberSuccess(members: List<ChatUIKitUser>) {
        data = members.toMutableList().filter {
            it.userId != ChatUIKitClient.getCurrentUser()?.id
        }.toMutableList()
        memberSelectAdapter.setData(data)
    }

    override fun fetchMemberInfoSuccess(members: Map<String, ChatUIKitProfile>?) {
        finishRefresh()
        mListAdapter.notifyDataSetChanged()
    }

    override fun fetchMemberInfoFail(code: Int, error: String) {
        finishRefresh()
    }

}