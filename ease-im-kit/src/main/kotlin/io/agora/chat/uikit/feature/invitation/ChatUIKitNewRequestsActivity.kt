package io.agora.chat.uikit.feature.invitation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.RefreshHeader
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.dialog.CustomDialog
import io.agora.chat.uikit.common.extensions.getInviteMessageStatus
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.databinding.UikitLayoutNewRequestBinding
import io.agora.chat.uikit.feature.contact.interfaces.IUIKitContactResultView
import io.agora.chat.uikit.feature.invitation.adapter.ChatUIKitRequestAdapter
import io.agora.chat.uikit.feature.invitation.enums.InviteMessageStatus
import io.agora.chat.uikit.feature.invitation.helper.ChatUIKitNotificationMsgManager
import io.agora.chat.uikit.feature.invitation.interfaces.IUIKitNotificationResultView
import io.agora.chat.uikit.interfaces.ChatUIKitContactListener
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.viewmodel.contacts.ChatUIKitContactListViewModel
import io.agora.chat.uikit.viewmodel.contacts.IContactListRequest
import io.agora.chat.uikit.viewmodel.request.ChatUIKitNotificationViewModel
import io.agora.chat.uikit.viewmodel.request.INotificationRequest


open class ChatUIKitNewRequestsActivity : ChatUIKitBaseActivity<UikitLayoutNewRequestBinding>(),
    IUIKitNotificationResultView,IUIKitContactResultView,
    ChatUIKitBaseRecyclerViewAdapter.OnItemSubViewClickListener {
    private var listAdapter: ChatUIKitRequestAdapter? = null
    private var noticeViewModel: INotificationRequest? = null
    private var contactViewModel: IContactListRequest? = null
    private var isFirstLoadData = false
    private var startMsgId = ""

    private val contactListener = object : ChatUIKitContactListener() {

        override fun onContactDeleted(username: String?) {
            refreshData()
        }

        override fun onContactInvited(username: String?, reason: String?) {
            updateNotifyCount()
        }

    }

    override fun getViewBinding(inflater: LayoutInflater): UikitLayoutNewRequestBinding {
        return UikitLayoutNewRequestBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initListener()
        initData()

    }

    override fun onDestroy() {
        super.onDestroy()
        ChatUIKitClient.removeContactListener(contactListener)
    }

    open fun initView(){
        initNoticeViewModel()
        initContactViewModel()
        binding.let {
            it.rvList.layoutManager = LinearLayoutManager(this)
            listAdapter = ChatUIKitRequestAdapter()
            listAdapter?.setHasStableIds(true)
            listAdapter?.setEmptyView(R.layout.uikit_layout_default_no_data)
            it.rvList.adapter = listAdapter

            // Set refresh layout
            // Can not load more
            it.refreshLayout.setEnableLoadMore(false)
            val refreshHeader = it.refreshLayout.refreshHeader
            if (refreshHeader == null) {
                it.refreshLayout.setRefreshHeader(RefreshHeader(this))
            }
        }
        defaultMenu()
    }

    private fun initNoticeViewModel(){
        noticeViewModel = ViewModelProvider(this)[ChatUIKitNotificationViewModel::class.java]
        noticeViewModel?.attachView(this)
    }

    private fun initContactViewModel(){
        contactViewModel = ViewModelProvider(this)[ChatUIKitContactListViewModel::class.java]
        contactViewModel?.attachView(this)
    }

    fun setContactViewModel(viewModel: IContactListRequest?) {
        this.contactViewModel = viewModel
        this.contactViewModel?.attachView(this)
    }

    open fun initData(){
        loadMoreData()
        initEventBus()
    }

    private fun fetchFirstVisibleData(){
        (binding.rvList.layoutManager as? LinearLayoutManager)?.let { manager->
            Handler(Looper.getMainLooper()).post{
                val firstVisibleItemPosition = manager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = manager.findLastVisibleItemPosition()
                val idList = mutableListOf<String>()
                listAdapter?.mData?.filterIndexed { index, _ ->
                    index in firstVisibleItemPosition..lastVisibleItemPosition
                }?.filter{ conv ->
                    val u = ChatUIKitClient.getCache().getUser(conv.conversationId())
                    (u == null) && (u?.name.isNullOrEmpty() || u?.avatar.isNullOrEmpty())
                }?.map { msg->
                    if (msg.ext().containsKey(ChatUIKitConstant.SYSTEM_MESSAGE_FROM)){
                        idList.add(msg.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_FROM))
                    }
                }
                idList.let {
                    noticeViewModel?.fetchProfileInfo(it)
                }
            }
        }
    }

    private fun initEventBus(){
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name).register(this) {
            if (it.isNotifyChange) {
                refreshData()
            }
        }

        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name).register(this) {
            if (it.isNotifyChange) {
                refreshData()
            }
        }
    }

    open fun defaultMenu(){
        binding.titleBar.inflateMenu(R.menu.menu_new_request_add_contact)
    }

    fun initListener(){
        ChatUIKitClient.addContactListener(contactListener)
        binding.titleBar.setNavigationOnClickListener {
            mContext.onBackPressed()
        }
        listAdapter?.setOnItemSubViewClickListener(this)
        binding.refreshLayout.setOnRefreshListener{
            refreshData()
        }
        binding.refreshLayout.setOnLoadMoreListener {
            loadMoreData(startMsgId)
        }
        binding.titleBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.action_add_contact -> {
                    showAddContactDialog()
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }

        binding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                // When scroll to bottom, load more data
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val visibleList = listAdapter?.mData?.filterIndexed { index, _ ->
                        index in firstVisibleItemPosition..lastVisibleItemPosition
                    }
                    val idList = mutableListOf<String>()
                    visibleList?.forEach { msg->
                        if (msg.ext().containsKey(ChatUIKitConstant.SYSTEM_MESSAGE_FROM)){
                            idList.add(msg.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_FROM))
                        }
                    }
                    if (idList.isEmpty()){
                        return
                    }
                    noticeViewModel?.fetchProfileInfo(idList)
                }
            }
        })
    }

    open fun refreshData(){
        startMsgId = ""
        noticeViewModel?.loadLocalData()
    }

    private fun loadMoreData(startMsgId: String? = ""){
        noticeViewModel?.loadMoreMessage(startMsgId,limit)
    }

    private fun showAddContactDialog(){
        val contactDialog = CustomDialog(
            context = this@ChatUIKitNewRequestsActivity,
            title = getString(R.string.uikit_conv_action_add_contact),
            subtitle = getString(R.string.uikit_conv_dialog_add_contact),
            inputHint = getString(R.string.uikit_dialog_edit_input_id_hint),
            isEditTextMode = true,
            onInputModeConfirmListener = {
                contactViewModel?.addContact(it)
            }
        )
        contactDialog.show()
    }

    override fun getLocalMessageSuccess(msgList: List<ChatMessage>) {
        finishRefresh()
        listAdapter?.setData(msgList.toMutableList())

        if (msgList.isNotEmpty()){
            startMsgId = msgList.last().msgId
        }

        listAdapter?.let {
            (binding.rvList.layoutManager as? LinearLayoutManager)?.let{ layoutManager ->
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val visibleItemCount = lastVisibleItemPosition - firstVisibleItemPosition + 1

                // 减去 ItemDecoration 的数量
                val itemDecorationCount = binding.rvList.itemDecorationCount
                val adjustedVisibleItemCount = visibleItemCount - itemDecorationCount

                if (it.itemCount < adjustedVisibleItemCount){
                    val visibleList = listAdapter?.mData?.filterIndexed { index, _ ->
                        index in firstVisibleItemPosition..lastVisibleItemPosition
                    }
                    val idList = mutableListOf<String>()
                    visibleList?.forEach { msg->
                        if (msg.ext().containsKey(ChatUIKitConstant.SYSTEM_MESSAGE_FROM)){
                            idList.add(msg.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_FROM))
                        }
                    }
                    noticeViewModel?.fetchProfileInfo(idList)
                }
            }
        }
    }

    override fun getLocalMessageFail(code: Int, error: String) {
        finishRefresh()
    }

    override fun loadMoreMessageSuccess(msgList: List<ChatMessage>) {
        finishLoadMore()
        if (msgList.isNotEmpty()){
            startMsgId = msgList.last().msgId
            listAdapter?.addData(msgList.toMutableList())
        }
        if (!isFirstLoadData){
            fetchFirstVisibleData()
            isFirstLoadData = true
        }
    }

    override fun fetchProfileSuccess(members: Map<String, ChatUIKitProfile>?) {
        ChatLog.d(TAG,"fetchProfileSuccess $members")
        finishRefresh()
        refreshData()
    }

    override fun addContactSuccess(userId: String) {

    }

    override fun addContactFail(code: Int, error: String) {

    }

    override fun agreeInviteSuccess(userId: String, msg: ChatMessage) {
        ChatLog.d(TAG,"agreeInviteSuccess")
        refreshData()
        noticeViewModel?.removeInviteMsg(msg)
    }

    override fun agreeInviteFail(code: Int, error: String) {
        ChatLog.e(TAG,"agreeInviteFail $code $error")
    }

    override fun onItemSubViewClick(view: View?, position: Int) {
        when(view?.id){
            R.id.item_action -> {
                listAdapter?.mData?.let {
                    if (position < it.size){
                        it[position].getInviteMessageStatus()?.let { status ->
                            if (status == InviteMessageStatus.BEINVITEED){
                                noticeViewModel?.agreeInvite(this,it[position])
                            }
                        }
                    }
                }
            }
        }
    }

    fun finishRefresh(){
        if (binding.refreshLayout.isRefreshing){
            binding.refreshLayout.finishRefresh()
        }
    }

    fun finishLoadMore(){
        if (binding.refreshLayout.isLoading){
            binding.refreshLayout.finishLoadMore()
        }
    }

    open fun updateNotifyCount(){
        val useDefaultContactSystemMsg = ChatUIKitClient.getConfig()?.systemMsgConfig?.useDefaultContactSystemMsg ?: false
        if (useDefaultContactSystemMsg){
            ChatUIKitNotificationMsgManager.getInstance().markAllMessagesAsRead()
            refreshData()
        }
    }

    companion object {
        private const val TAG = "ChatUIKitNewRequestsActivity"
        private const val limit = 10
        fun createIntent(
            context: Context,
        ): Intent {
            val intent = Intent(context, ChatUIKitNewRequestsActivity::class.java)
            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    return it
                }
            }
            return intent
        }
    }

}