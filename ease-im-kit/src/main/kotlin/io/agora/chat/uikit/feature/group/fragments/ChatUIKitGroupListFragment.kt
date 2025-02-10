package io.agora.chat.uikit.feature.group.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseFragment
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.RefreshHeader
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.databinding.UikitFragmentGroupListBinding
import io.agora.chat.uikit.feature.group.ChatUIKitGroupDetailActivity
import io.agora.chat.uikit.feature.group.adapter.ChatUIKitGroupListAdapter
import io.agora.chat.uikit.feature.group.interfaces.IUIKitGroupResultView
import io.agora.chat.uikit.interfaces.ChatUIKitGroupListener
import io.agora.chat.uikit.interfaces.OnItemClickListener
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.viewmodel.group.ChatUIKitGroupViewModel
import io.agora.chat.uikit.viewmodel.group.IGroupRequest

open class ChatUIKitGroupListFragment: ChatUIKitBaseFragment<UikitFragmentGroupListBinding>(),
    IUIKitGroupResultView, OnItemClickListener {

    protected lateinit var adapter: ChatUIKitGroupListAdapter
    private var groupViewModel: IGroupRequest? = null
    private var data:MutableList<ChatGroup> = mutableListOf()
    private val layoutManager by lazy { LinearLayoutManager(mContext) }
    private var currentPage:Int = 0

    private val groupChangeListener = object : ChatUIKitGroupListener() {

        override fun onGroupDestroyed(groupId: String?, groupName: String?) {
            refreshData()
        }

        override fun onAutoAcceptInvitationFromGroup(
            groupId: String?,
            inviter: String?,
            inviteMessage: String?
        ) {
            refreshData()
        }

        override fun onSpecificationChanged(group: ChatGroup?) {
            refreshData()
        }

    }
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UikitFragmentGroupListBinding? {
        return UikitFragmentGroupListBinding.inflate(inflater, container, false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.let { binding ->
            binding.rvList.layoutManager = layoutManager
            adapter = getCustomAdapter()
            binding.rvList.adapter = adapter

            groupViewModel = ViewModelProvider(this)[ChatUIKitGroupViewModel::class.java]
            groupViewModel?.attachView(this)

            // Set refresh layout
            // Can not load more
            binding.refreshLayout.setEnableLoadMore(false)
            val refreshHeader = binding.refreshLayout.refreshHeader
            if (refreshHeader == null) {
                binding.refreshLayout.setRefreshHeader(RefreshHeader(mContext))
            }
            updateView()
        }

    }

    open fun getCustomAdapter(): ChatUIKitGroupListAdapter {
        return ChatUIKitGroupListAdapter()
    }

    override fun initListener() {
        super.initListener()
        ChatUIKitClient.addGroupChangeListener(groupChangeListener)
        binding?.let { binding ->
            binding.titleContact.setNavigationOnClickListener {
                mContext.finish()
            }
            adapter.setOnItemClickListener(this)
            binding.refreshLayout.setOnRefreshListener {
                refreshData()
            }
            binding.refreshLayout.setOnLoadMoreListener {
                currentPage++
                loadMoreData()
            }
        }
    }

    override fun initData() {
        super.initData()
        currentPage = 0
        data.clear()
        adapter.clearData()
        groupViewModel?.loadJoinedGroupData(currentPage)
        initEventBus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ChatUIKitClient.removeGroupChangeListener(groupChangeListener)
    }

    private fun updateView(){
        binding?.titleContact?.setTitle(resources.getString(R.string.uikit_group_count,data.size))
    }

    private fun initEventBus(){
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.DESTROY.name).register(this) {
            if (it.isGroupChange) {
                refreshData()
            }
        }

        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.LEAVE.name).register(this) {
            if (it.isGroupChange) {
                refreshData()
            }
        }

        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name).register(this) {
            if (it.isGroupChange) {
                refreshData()
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.GROUP).register(this) {
            if (it.isGroupChange && it.event == ChatUIKitConstant.EVENT_UPDATE_GROUP_NAME) {
                adapter.mData?.forEachIndexed { index, group ->
                    if (group.groupId == it.message) {
                        adapter.notifyItemChanged(index)
                        return@forEachIndexed
                    }
                }
            }
        }
    }

    private fun refreshData(){
        groupViewModel?.loadLocalJoinedGroupData()
    }

    private fun loadMoreData(){
        groupViewModel?.loadJoinedGroupData(currentPage)
    }

    override fun loadGroupListSuccess(list: MutableList<ChatGroup>) {
        data.addAll(list)
        adapter.addData(list)
        binding?.refreshLayout?.finishRefresh()
        updateView()
    }

    override fun loadGroupListFail(code: Int, error: String) {
        binding?.refreshLayout?.finishRefresh()
        updateView()
    }

    override fun loadLocalGroupListSuccess(list: MutableList<ChatGroup>) {
        data.clear()
        adapter.clearData()
        currentPage = 0

        list.reverse().apply {
            data.addAll(list)
            adapter.addData(list)
            binding?.refreshLayout?.finishRefresh()
            updateView()
        }
    }

    override fun loadLocalGroupListFail(code: Int, error: String) {
        ChatLog.e(TAG,"loadLocalGroupListFail $code $error")
        binding?.refreshLayout?.finishRefresh()
        updateView()
    }

    override fun onItemClick(view: View?, position: Int) {
        startActivity(ChatUIKitGroupDetailActivity.createIntent(mContext, data[position].groupId))
    }

    companion object {
        const val TAG = "ChatUIKitGroupListFragment"
    }
}