package io.agora.chat.uikit.feature.group

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.feature.chat.activities.UIKitChatActivity
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatSilentModeResult
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.enums.ChatUIKitGroupMemberType
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.dialog.CustomDialog
import io.agora.chat.uikit.common.extensions.isOwner
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.configs.ChatUIKitBottomMenuConfig
import io.agora.chat.uikit.configs.ChatUIKitDetailMenuConfig
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.databinding.UikitLayoutGroupDetailsBinding
import io.agora.chat.uikit.common.dialog.SimpleListSheetDialog
import io.agora.chat.uikit.common.extensions.dpToPx
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.feature.contact.adapter.ChatUIKitContactDetailItemAdapter
import io.agora.chat.uikit.feature.group.interfaces.IUIKitGroupResultView
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitType
import io.agora.chat.uikit.feature.search.ChatUIKitSearchActivity
import io.agora.chat.uikit.feature.search.ChatUIKitSearchType
import io.agora.chat.uikit.interfaces.ChatUIKitGroupListener
import io.agora.chat.uikit.interfaces.OnMenuItemClickListener
import io.agora.chat.uikit.interfaces.SimpleListSheetItemClickListener
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitMenuItem
import io.agora.chat.uikit.provider.getSyncProfile
import io.agora.chat.uikit.viewmodel.group.ChatUIKitGroupViewModel
import io.agora.chat.uikit.viewmodel.group.IGroupRequest
import io.agora.chat.uikit.widget.ChatUIKitSwitchItemView
import kotlinx.coroutines.launch

open class ChatUIKitGroupDetailActivity:ChatUIKitBaseActivity<UikitLayoutGroupDetailsBinding>(),
    ChatUIKitSwitchItemView.OnCheckedChangeListener, View.OnClickListener,
    IUIKitGroupResultView, OnMenuItemClickListener {
    private var group: ChatGroup? = null
    protected var groupId: String? = null
    private var dialog: SimpleListSheetDialog? = null
    private var gridAdapter: ChatUIKitContactDetailItemAdapter? = null
    private var groupViewModel: IGroupRequest? = null

    companion object {
        private const val TAG = "ChatUIKitGroupDetailActivity"
        private const val KEY_GROUP_INFO = "groupId"

        fun createIntent(
            context: Context,
            groupId: String
        ): Intent {
            val intent = Intent(context, ChatUIKitGroupDetailActivity::class.java)
            intent.putExtra(KEY_GROUP_INFO, groupId)
            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    return it
                }
            }
            return intent
        }
    }

    private val groupChangeListener = object : ChatUIKitGroupListener() {

        override fun onGroupDestroyed(groupId: String?, groupName: String?) {
            if (this@ChatUIKitGroupDetailActivity.groupId == groupId ){
                finish()
            }
        }

        override fun onSpecificationChanged(group: ChatGroup?) {
            group?.let {
                if (groupId == it.groupId){
                    binding.tvSignature.text = it.description
                    binding.tvName.text = it.groupName
                }
            }
        }

        override fun onAnnouncementChanged(groupId: String?, announcement: String?) {
            if (this@ChatUIKitGroupDetailActivity.groupId == groupId){
                binding.tvSignature.text = announcement
            }
        }

        override fun onOwnerChanged(groupId: String?, newOwner: String?, oldOwner: String?) {
            if (this@ChatUIKitGroupDetailActivity.groupId == groupId && newOwner == ChatUIKitClient.getCurrentUser()?.id){
                mainScope().launch {
                    binding.itemSpacing.visibility = View.VISIBLE
                    binding.itemGroupName.visibility = View.VISIBLE
                    binding.itemGroupDescribe.visibility = View.VISIBLE
                }
            }
        }

        override fun onUserRemoved(groupId: String?, groupName: String?) {
            super.onUserRemoved(groupId, groupName)
            if (groupId == this@ChatUIKitGroupDetailActivity.groupId){
                ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.LEAVE.name)
                    .post(lifecycleScope, ChatUIKitEvent(ChatUIKitEvent.EVENT.LEAVE.name, ChatUIKitEvent.TYPE.GROUP, groupId))
                finish()
            }
        }

    }

    /**
     * The clipboard manager.
     */
    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    override fun getViewBinding(inflater: LayoutInflater): UikitLayoutGroupDetailsBinding{
        return UikitLayoutGroupDetailsBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        groupId = intent.getStringExtra(KEY_GROUP_INFO)
        groupId?.let {
            group = ChatClient.getInstance().groupManager().getGroup(it)
        }

        initView()
        initListener()
        initData()
        initEvent()
    }

    protected open fun initView(){
        ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(binding.ivGroupAvatar)
        initSwitch()
        updateView()
        initMenu()
    }

    private fun initSwitch(){
        val isSilent = ChatUIKitClient.getCache().getMutedConversationList().containsKey(groupId)
        if (isSilent){
            binding.switchItemDisturb.setChecked(true)
        }else{
            binding.switchItemDisturb.setChecked(false)
        }
        binding.switchItemDisturb.setSwitchTarckDrawable(R.drawable.uikit_switch_track_selector)
        binding.switchItemDisturb.setSwitchThumbDrawable(R.drawable.uikit_switch_thumb_selector)
    }

    private fun initMenu(){
        val menuData = getDetailItem()?.filter { it.isVisible }?.toMutableList()
        menuData?.let { ChatUIKitDetailMenuConfig(mContext).sortByOrder(it) }
        menuData?.let { menu->
            binding.gvGridview.let {
                it.horizontalSpacing = 8.dpToPx(this)
                it.numColumns = menu.size
                gridAdapter = ChatUIKitContactDetailItemAdapter(this, 1, menu)
                it.adapter = gridAdapter
            }
        }
        if (menuData.isNullOrEmpty()) binding.functionLayout.visibility = View.GONE
    }

    open fun getDetailItem():MutableList<ChatUIKitMenuItem>?{
        return ChatUIKitDetailMenuConfig(this).getDefaultGroupDetailMenu()
    }

    protected open fun initListener(){
        ChatUIKitClient.addGroupChangeListener(groupChangeListener)
        binding.titleBar.setNavigationOnClickListener {
            mContext.onBackPressed()
        }
        binding.titleBar.setOnMenuItemClickListener { item->
            when(item.itemId){
                R.id.action_more -> {
                    showDialog()
                }
            }
            true
        }
        binding.itemMemberList.setOnClickListener(this)
        binding.switchItemDisturb.setOnCheckedChangeListener(this)
        binding.itemClear.setOnClickListener(this)
        binding.itemGroupName.setOnClickListener(this)
        binding.itemGroupDescribe.setOnClickListener(this)
        binding.tvNumber.setOnClickListener(this)
        gridAdapter?.setContactDetailItemClickListener(this)
    }

    protected open fun initEvent() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.LEAVE.name).register(this) {
            if (it.isGroupChange && it.message == groupId && !mContext.isFinishing) {
                finish()
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.DESTROY.name).register(this) {
            if (it.isGroupChange && it.message == groupId && !mContext.isFinishing) {
                finish()
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.GROUP).register(this) {
            if (it.isGroupChange && it.message == groupId) {
                group = ChatClient.getInstance().groupManager().getGroup(groupId)
                if (it.event == ChatUIKitConstant.EVENT_UPDATE_GROUP_OWNER){
                    binding.itemSpacing.visibility = View.GONE
                    binding.itemGroupName.visibility = View.GONE
                    binding.itemGroupDescribe.visibility = View.GONE
                }else{
                    updateView()
                }
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE + ChatUIKitEvent.TYPE.GROUP + ChatUIKitEvent.TYPE.CONTACT).register(this) {
            if (it.isGroupChange && it.message == groupId) {
                group = ChatClient.getInstance().groupManager().getGroup(groupId)
                binding.itemMemberList.tvContent?.text = group?.memberCount?.toString()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatUIKitClient.removeGroupChangeListener(groupChangeListener)
    }

    protected open fun initData(){
        groupViewModel = ViewModelProvider(this)[ChatUIKitGroupViewModel::class.java]
        groupViewModel?.attachView(this)

        fetchGroupDetail()
    }

    private fun fetchGroupDetail() {
        groupId?.let {
            groupViewModel?.fetchGroupDetails(it)
        }
    }

    private fun fetchGroupNickname() {
        groupId?.let {
            groupViewModel?.fetchGroupMemberAllAttributes(
                it,
                mutableListOf(ChatClient.getInstance().currentUser),
                mutableListOf(ChatUIKitConstant.GROUP_MEMBER_ATTRIBUTE_NICKNAME)
            )
        }
    }

    private fun updateView(){
        binding.tvName.text = group?.groupName ?: groupId
        binding.tvNumber.text = resources.getString(R.string.uikit_group_detail_group_id, group?.groupId ?: groupId)
        group?.description?.let {
            if (it.isNotEmpty()){
                binding.tvSignature.visibility = View.VISIBLE
                binding.tvSignature.text = group?.description
            }else{
                binding.tvSignature.visibility = View.GONE
            }
        }
        binding.itemMemberList.tvContent?.text = group?.memberCount?.toString()
        ChatUIKitClient.getGroupProfileProvider()?.getSyncProfile(group?.groupId).let {
            it?.let {
                it.avatar?.let {
                    binding.ivGroupAvatar.load(it) {
                        error(R.drawable.uikit_default_group_avatar)
                        placeholder(R.drawable.uikit_default_group_avatar)
                    }
                }
            }
        }
        if (group?.isOwner() == true){
            binding.itemSpacing.visibility = View.VISIBLE
            binding.itemGroupName.visibility = View.VISIBLE
            binding.itemGroupDescribe.visibility = View.VISIBLE
            binding.itemGroupName.tvContent?.text = group?.groupName
            binding.itemGroupDescribe.tvContent?.text = group?.description
        }else{
            binding.itemSpacing.visibility = View.GONE
            binding.itemGroupName.visibility = View.GONE
            binding.itemGroupDescribe.visibility = View.GONE
        }
    }

    override fun onCheckedChanged(buttonView: ChatUIKitSwitchItemView?, isChecked: Boolean) {
        when(buttonView?.id) {
            R.id.switch_item_disturb -> {
                groupId?.let {
                    if (isChecked){
                        groupViewModel?.makeSilentModeForConversation(it,ChatConversationType.GroupChat)
                    }else{
                        groupViewModel?.cancelSilentForConversation(it,ChatConversationType.GroupChat)
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.item_clear -> {
                val dialog = CustomDialog(
                    context = this@ChatUIKitGroupDetailActivity,
                    title = resources.getString(R.string.uikit_dialog_clear),
                    isEditTextMode = false,
                    onLeftButtonClickListener = {

                    },
                    onRightButtonClickListener = {
                        groupViewModel?.clearConversationMessage(groupId)
                    }
                )
                dialog.show()
            }
            R.id.item_member_list -> {
                groupId?.let {
                    startActivity(
                        ChatUIKitGroupMembersListActivity.createIntent(
                            context = this,
                            groupId = it,
                        )
                    )
                }
            }
            R.id.item_group_name -> {
                groupId?.let {
                    startActivity(ChatUIKitGroupDetailEditActivity.createIntent(
                        this,it,EditType.ACTION_EDIT_GROUP_NAME)
                    )
                }
            }
            R.id.item_group_describe -> {
                groupId?.let {
                    startActivity(ChatUIKitGroupDetailEditActivity.createIntent(
                        this,it,EditType.ACTION_EDIT_GROUP_DESCRIBE)
                    )
                }
            }
            R.id.tv_number -> {
                copyGroupId()
            }
            else -> {}
        }
    }

    open fun copyGroupId(){
        val indexOfSpace = binding.tvNumber.text.indexOf(":")
        if (indexOfSpace != -1) {
            val substring = binding.tvNumber.text.substring(indexOfSpace + 1)
            clipboard.setPrimaryClip(
                ClipData.newPlainText(
                    null,
                    substring
                )
            )
        }
    }

    open fun getBottomSheetMenu(): MutableList<ChatUIKitMenuItem>?{
        val mutableListOf = if (group?.isOwner() == true){
            ChatUIKitBottomMenuConfig(this).getDefaultGroupOwnerBottomSheetMenu()
        }else{
            ChatUIKitBottomMenuConfig(this).getDefaultGroupBottomSheetMenu()
        }
        return mutableListOf
    }

    private fun showDialog(){
        val context = this@ChatUIKitGroupDetailActivity
        dialog = SimpleListSheetDialog(
            context = context,
            itemList = getBottomSheetMenu(),
            itemListener = object : SimpleListSheetItemClickListener {
                override fun onItemClickListener(position: Int, menu: ChatUIKitMenuItem) {
                    simpleMenuItemClickListener(position, menu)
                    dialog?.dismiss()
                }
            })
        supportFragmentManager.let { dialog?.show(it,"group_more_dialog") }
    }

    open fun simpleMenuItemClickListener(position: Int,menu: ChatUIKitMenuItem){
        when(menu.menuId){
            R.id.bottom_sheet_item_change_owner -> {
                if (group?.isOwner() == true){
                    changeOwner()
                }
            }
            R.id.bottom_sheet_item_destroy_group -> {
                leaveOrDestroy()
            }
            R.id.bottom_sheet_item_leave_group -> {
                leaveOrDestroy()
            }
            else -> {}
        }
    }

    open fun changeOwner(){
        groupId?.let {
            startActivity(ChatUIKitGroupMembersListActivity.createIntent(
                this@ChatUIKitGroupDetailActivity,
                it, ChatUIKitGroupMemberType.GROUP_MEMBER_CHANGE_OWNER)
            )
        }
    }

    open fun leaveOrDestroy(){
        val dialogDelete = CustomDialog(
            context = this@ChatUIKitGroupDetailActivity,
            title = resources.getString(
                if (group?.isOwner() == true) R.string.uikit_group_detail_leave_dialog_destroy else
                    R.string.uikit_group_detail_leave_dialog
            ),
            subtitle = resources.getString(
                if (group?.isOwner() == true) R.string.uikit_group_detail_leave_dialog_owner_des
                else R.string.uikit_group_detail_leave_dialog_des),
            isEditTextMode = false,
            onLeftButtonClickListener = {
                dialog?.dismiss()
            },
            onRightButtonClickListener = {
                groupId?.let {
                    if (group?.isOwner() == true){
                        groupViewModel?.destroyChatGroup(it)
                    }else{
                        groupViewModel?.leaveChatGroup(it)
                    }
                }
                dialog?.dismiss()
            }
        )
        dialogDelete.show()
    }

    override fun fetchGroupDetailSuccess(group: ChatGroup) {
        this.group = group
        updateView()
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name, ChatUIKitEvent.TYPE.GROUP, groupId))
    }

    override fun fetchGroupDetailFail(code: Int, error: String) {
        ChatLog.e(TAG,"fetchGroupDetailFail $code $error")
    }

    override fun leaveChatGroupSuccess() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.LEAVE.name)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitEvent.EVENT.LEAVE.name, ChatUIKitEvent.TYPE.GROUP, groupId))
        finish()
    }

    override fun leaveChatGroupFail(code: Int, error: String) {
        ChatLog.e(TAG,"leaveChatGroupFail $code $error")
    }

    override fun destroyChatGroupSuccess() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.DESTROY.name)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitEvent.EVENT.DESTROY.name, ChatUIKitEvent.TYPE.GROUP, groupId))
        finish()
    }

    override fun destroyChatGroupFail(code: Int, error: String) {
        ChatLog.e(TAG,"destroyChatGroupFail $code $error")
    }

    override fun clearConversationByGroupSuccess(conversationId: String?) {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitEvent.EVENT.REMOVE.name, ChatUIKitEvent.TYPE.CONVERSATION, conversationId))
    }

    override fun clearConversationByGroupFail(code: Int, error: String?) {
        ChatLog.e(TAG,"deleteConversationByGroupFail $code $error")
    }

    override fun makeSilentForGroupSuccess(silentResult: ChatSilentModeResult) {
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name)
            .post(mContext.mainScope()
                , ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name
                    , ChatUIKitEvent.TYPE.SILENT, groupId))
    }

    override fun makeSilentForGroupFail(
        code: Int,
        error: String?
    ) {
        ChatLog.e(TAG,"makeSilentForGroupFail $code $error")
    }

    override fun cancelSilentForGroupSuccess() {
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name)
            .post(mContext.mainScope()
                , ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name
                    , ChatUIKitEvent.TYPE.CONVERSATION, groupId))
    }

    override fun cancelSilentForGroupFail(code: Int, error: String?) {
        ChatLog.e(TAG,"cancelSilentForGroupFail $code $error")
    }

    override fun onMenuItemClick(item: ChatUIKitMenuItem?, position: Int): Boolean {
        item?.let {menu->
            when(menu.menuId){
                R.id.extend_item_message -> {
                    groupId?.let {
                        UIKitChatActivity.actionStart(mContext, it, ChatUIKitType.GROUP_CHAT)
                    }
                }
                R.id.extend_item_search -> {
                    groupId?.let {
                        mContext.startActivity(ChatUIKitSearchActivity.createIntent(mContext, ChatUIKitSearchType.MESSAGE,it))
                    }
                }
                else -> {}
            }
            return true
        }
        return false
    }
}