package io.agora.chat.uikit.feature.chat.reaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.agora.chat.uikit.base.ChatUIKitBaseFragment
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.RefreshHeader
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.databinding.UikitFragmentReactionUserListBinding
import io.agora.chat.uikit.feature.chat.reaction.adapter.ChatUIKitReactionUserAdapter
import io.agora.chat.uikit.feature.chat.reaction.interfaces.IReactionUserListResultView
import io.agora.chat.uikit.feature.chat.reaction.interfaces.IMessageReactionUsers
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.viewmodel.reaction.ChatUIKitReactionUserListViewModel
import io.agora.chat.uikit.viewmodel.reaction.IReactionUserListRequest
import kotlinx.coroutines.launch

class ChatUIKitReactionUserListFragment
    : ChatUIKitBaseFragment<UikitFragmentReactionUserListBinding>(), IMessageReactionUsers, IReactionUserListResultView {
    private var viewModel: IReactionUserListRequest? = null
    private var adapter = ChatUIKitReactionUserAdapter()
    private var message: ChatMessage? = null
    private var reaction: String? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UikitFragmentReactionUserListBinding? {
        return UikitFragmentReactionUserListBinding.inflate(inflater, container, false)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        arguments?.let {
            message = ChatClient.getInstance().chatManager().getMessage(it.getString("messageId"))
            reaction = it.getString("reaction")
        }

        binding?.rvReactionUserList?.let {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = adapter
        }

        if (binding?.rlReactionUserList?.refreshHeader == null) {
            binding?.rlReactionUserList?.setRefreshHeader(RefreshHeader(context))
        }
    }

    override fun initViewModel() {
        super.initViewModel()
        if (viewModel == null) {
            viewModel = ViewModelProvider(this)[ChatUIKitReactionUserListViewModel::class.java]
        }
        viewModel?.attachView(this)
    }

    override fun initListener() {
        super.initListener()
        adapter.setOnDeleteClickListener {
            removeReaction(reaction)
        }
        binding?.rlReactionUserList?.setOnRefreshListener {
            binding?.rlReactionUserList?.setEnableLoadMore(true)
            fetchReactionDetail(reaction)
        }
        binding?.rlReactionUserList?.setOnLoadMoreListener {
            fetchMoreReactionDetail(reaction)
        }
    }

    override fun initData() {
        super.initData()
        fetchReactionDetail(reaction)
    }

    override fun setViewModel(viewModel: IReactionUserListRequest?) {
        this.viewModel = viewModel
    }

    override fun removeReaction(reaction: String?) {
        message?.let {
            viewModel?.removeReaction(it, reaction)
        }
    }

    override fun fetchReactionDetail(reaction: String?) {
        message?.let {
            viewModel?.fetchReactionDetail(it, reaction, PAGE_SIZE)
        }
    }

    override fun fetchMoreReactionDetail(reaction: String?) {
        message?.let {
            viewModel?.fetchReactionDetail(it, reaction, PAGE_SIZE)
        }
    }

    override fun removeReactionSuccess(messageId: String) {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name).post(this.lifecycleScope,
            ChatUIKitEvent(ChatUIKitEvent.EVENT.REMOVE.name, ChatUIKitEvent.TYPE.REACTION, messageId))
    }

    override fun removeReactionFail(messageId: String, errorCode: Int, errorMsg: String?) {
        ChatLog.e("EaseReactionUserListUserListFragment",
            "removeReactionFail messageId: $messageId errorCode: $errorCode errorMsg: $errorMsg")
    }

    override fun fetchReactionDetailSuccess(
        messageId: String,
        nextCursor: String,
        result: List<ChatUIKitUser>
    ) {
        finishRefresh()
        if (result.isEmpty() || result.size < PAGE_SIZE || nextCursor.isEmpty()) {
            binding?.rlReactionUserList?.setEnableLoadMore(false)
        }
        adapter.setData(result.toMutableList())
    }

    override fun fetchReactionDetailFail(messageId: String, errorCode: Int, errorMsg: String?) {
        ChatLog.e("EaseReactionUserListUserListFragment",
            "fetchReactionDetailFail messageId: $messageId errorCode: $errorCode errorMsg: $errorMsg")
        finishRefresh()
    }

    override fun fetchMoreReactionDetailSuccess(
        messageId: String,
        nextCursor: String,
        result: List<ChatUIKitUser>
    ) {
        finishLoadMore()
        if (result.isEmpty() || result.size < PAGE_SIZE || nextCursor.isEmpty()) {
            binding?.rlReactionUserList?.setEnableLoadMore(false)
        }
        adapter.addData(result.toMutableList())
    }

    override fun fetchMoreReactionDetailFail(messageId: String, errorCode: Int, errorMsg: String?) {
        ChatLog.e("EaseReactionUserListUserListFragment",
            "fetchMoreReactionDetailFail messageId: $messageId errorCode: $errorCode errorMsg: $errorMsg")
        finishLoadMore()
    }

    private fun finishRefresh() {
        mContext.mainScope().launch {
            binding?.rlReactionUserList?.finishRefresh()
        }
    }

    private fun finishLoadMore() {
        mContext.mainScope().launch {
            binding?.rlReactionUserList?.finishLoadMore()
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }

}