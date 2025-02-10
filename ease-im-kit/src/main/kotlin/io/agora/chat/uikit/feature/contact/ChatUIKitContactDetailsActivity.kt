package io.agora.chat.uikit.feature.contact

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.feature.chat.activities.UIKitChatActivity
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatSilentModeResult
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.dialog.CustomDialog
import io.agora.chat.uikit.common.extensions.dpToPx
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.configs.ChatUIKitBottomMenuConfig
import io.agora.chat.uikit.configs.ChatUIKitDetailMenuConfig
import io.agora.chat.uikit.common.dialog.SimpleListSheetDialog
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.configs.setStatusStyle
import io.agora.chat.uikit.databinding.EaseLayoutContactDetailsBinding
import io.agora.chat.uikit.feature.contact.adapter.ChatUIKitContactDetailItemAdapter
import io.agora.chat.uikit.feature.contact.interfaces.IUIKitContactResultView
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitType
import io.agora.chat.uikit.feature.search.ChatUIKitSearchActivity
import io.agora.chat.uikit.feature.search.ChatUIKitSearchType
import io.agora.chat.uikit.interfaces.ChatUIKitContactListener
import io.agora.chat.uikit.interfaces.OnMenuItemClickListener
import io.agora.chat.uikit.interfaces.SimpleListSheetItemClickListener
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitMenuItem
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.getNickname
import io.agora.chat.uikit.model.isCurrentUser
import io.agora.chat.uikit.viewmodel.contacts.ChatUIKitContactListViewModel
import io.agora.chat.uikit.viewmodel.contacts.IContactListRequest

open class ChatUIKitContactDetailsActivity:ChatUIKitBaseActivity<EaseLayoutContactDetailsBinding>(),
     View.OnClickListener, IUIKitContactResultView, OnMenuItemClickListener{
    protected var user: ChatUIKitUser? = null
    private var dialog: SimpleListSheetDialog? = null
    private var gridAdapter: ChatUIKitContactDetailItemAdapter? = null
    private var contactViewModel: IContactListRequest? = null
    /**
     * The clipboard manager.
     */
    private val clipboard by lazy { mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }


    private val contactListener = object : ChatUIKitContactListener() {
        override fun onContactDeleted(username: String?) {
            if (username == user?.userId){
                finish()
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): EaseLayoutContactDetailsBinding{
        return EaseLayoutContactDetailsBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(KEY_USER_INFO,ChatUIKitUser::class.java)
        }else{
            intent.getSerializableExtra(KEY_USER_INFO) as ChatUIKitUser?
        }
        initView()
        initData()
        initListener()
        initEvent()
    }

    open fun initView(){
        initSwitch()
        initUserAvatarInfo()
        initMenu()
    }

    open fun initData(){
        contactViewModel = ViewModelProvider(this)[ChatUIKitContactListViewModel::class.java]
        contactViewModel?.attachView(this)

        contactViewModel?.let {
            val isLoad = ChatUIKitClient.isLoadBlockListFromServer?: false
            if (!isLoad){
                it.fetchBlockListFromServer()
            }else{
                it.getBlockListFromLocal()
            }
        }
    }

    private fun initSwitch(){
        val isSilent = ChatUIKitClient.getCache().getMutedConversationList().containsKey(user?.userId)
        if (isSilent){
            binding.switchItemDisturb.setChecked(true)
            binding.icNotice.visibility = View.VISIBLE
        }else{
            binding.switchItemDisturb.setChecked(false)
            binding.icNotice.visibility = View.GONE
        }
        binding.switchItemDisturb.setSwitchTarckDrawable(R.drawable.uikit_switch_track_selector)
        binding.switchItemDisturb.setSwitchThumbDrawable(R.drawable.uikit_switch_thumb_selector)

        binding.switchItemBlack.setSwitchTarckDrawable(R.drawable.uikit_switch_track_selector)
        binding.switchItemBlack.setSwitchThumbDrawable(R.drawable.uikit_switch_thumb_selector)
    }

    open fun initUserAvatarInfo(){
        ChatUIKitClient.getConfig()?.avatarConfig?.setStatusStyle(binding.epPresence.getStatusView(),4.dpToPx(mContext),
            ContextCompat.getColor(mContext, R.color.ease_color_background))
        binding.epPresence.setPresenceStatusMargin(end = -3, bottom = -3)
        binding.epPresence.setPresenceStatusSize(resources.getDimensionPixelSize(R.dimen.ease_contact_status_icon_size))

        val layoutParams = binding.epPresence.getUserAvatar().layoutParams
        layoutParams.width = 100.dpToPx(this)
        layoutParams.height = 100.dpToPx(this)
        binding.epPresence.getUserAvatar().layoutParams = layoutParams

        user?.let {
            ChatUIKitClient.getUserProvider()?.getUser(it.userId)?.let { profile->
                binding.epPresence.setUserAvatarData(profile)
                binding.tvName.text = profile.getRemarkOrName()
                binding.tvNumber.text = profile.id
            }

            if (it.isCurrentUser()) {
                binding.gvGridview.visibility = View.GONE
                binding.titleBar.getToolBar().menu.forEach {menu-> menu.isVisible = false }
                binding.switchItemDisturb.visibility = View.GONE
                binding.switchItemBlack.visibility = View.GONE
            }
        }
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
        return ChatUIKitDetailMenuConfig(this).getDefaultContactDetailMenu()
    }

    open fun initListener(){
        ChatUIKitClient.addContactListener(contactListener)
        binding.tvNumber.setOnClickListener(this)
        binding.switchItemDisturb.setOnClickListener(this)
        binding.switchItemBlack.setOnClickListener(this)
        binding.itemClear.setOnClickListener(this)
        gridAdapter?.setContactDetailItemClickListener(this)
        binding.titleBar.setNavigationOnClickListener { mContext.onBackPressed() }
        binding.titleBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_more -> {
                    showDeleteContentDialog()
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatUIKitClient.removeContactListener(contactListener)
    }

    open fun initEvent() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name).register(this) {
            if (it.isContactChange && it.message == user?.userId && !mContext.isFinishing) {
                finish()
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.item_clear -> {
                showClearMsgDialog()
            }
            R.id.tv_number -> {
                copyId()
            }
            R.id.switch_item_disturb -> {
                binding.switchItemDisturb.switch?.let { switch ->
                    val isChecked = switch.isChecked.not()
                    user?.let {
                        if (isChecked){
                            contactViewModel?.makeSilentModeForConversation(it.userId,ChatConversationType.Chat)
                        }else{
                            contactViewModel?.cancelSilentForConversation(it.userId,ChatConversationType.Chat)
                        }
                    }
                }
            }
            R.id.switch_item_black -> {
                binding.switchItemBlack.switch?.let { switch ->
                    val isChecked = switch.isChecked.not()
                    if (isChecked){
                        showBlockDialog()
                    }else{
                        user?.let {
                            contactViewModel?.removeUserFromBlockList(it.userId)
                        }
                    }
                }
            }
            else -> {}
        }
    }

    open fun copyId(){
        val id = binding.tvNumber.text
        if (id.isNotEmpty()){
            clipboard.setPrimaryClip(
                ClipData.newPlainText(null, id)
            )
        }
    }

    private fun showClearMsgDialog(){
        val clearDialog = CustomDialog(
            context = this@ChatUIKitContactDetailsActivity,
            title = resources.getString(R.string.uikit_dialog_clear),
            isEditTextMode = false,
            onLeftButtonClickListener = {

            },
            onRightButtonClickListener = {
                contactViewModel?.clearConversationMessage(user?.userId)
            }
        )
        clearDialog.show()
    }

    private fun showBlockDialog(){
        val blackDialog = CustomDialog(
            this@ChatUIKitContactDetailsActivity,
            resources.getString(R.string.uikit_dialog_black_title),
            resources.getString(R.string.uikit_dialog_black),
            false,
            onLeftButtonClickListener = {},
            onRightButtonClickListener = {
                user?.let {
                    contactViewModel?.addUserToBlockList(mutableListOf(it.userId))
                }
            }
        )
        blackDialog.show()
    }

    open fun updateBlockSwitch(list:MutableList<ChatUIKitUser>){
        list.map {
            if (it.userId == user?.userId){
                binding.switchItemBlack.setChecked(true)
                updateBlockLayout(true)
            }else{
                binding.switchItemBlack.setChecked(false)
                updateBlockLayout(false)
            }
        }
    }

    open fun updateBlockLayout(isChecked:Boolean){
        if (isChecked){
            binding.run {
                functionLayout.visibility = View.GONE
                switchItemDisturb.visibility = View.GONE
                itemClear.visibility = View.GONE
            }
        }else{
            binding.run {
                functionLayout.visibility = View.VISIBLE
                switchItemDisturb.visibility = View.VISIBLE
                itemClear.visibility = View.VISIBLE
            }
        }
    }

    open fun getDeleteDialogMenu(): MutableList<ChatUIKitMenuItem>?{
        return ChatUIKitBottomMenuConfig(this).getDefaultContactBottomSheetMenu()
    }

    open fun simpleSheetMenuItemClick(position: Int, menu: ChatUIKitMenuItem){
        when(menu.menuId){
            R.id.bottom_sheet_item_remove_contact -> {
                val dialogDelete = CustomDialog(
                    context = this@ChatUIKitContactDetailsActivity,
                    title = resources.getString(R.string.uikit_dialog_delete_contact_title),
                    subtitle = resources.getString(R.string.uikit_dialog_delete_contact_subtitle,user?.getNickname()),
                    isEditTextMode = false,
                    onLeftButtonClickListener = {
                        dialog?.dismiss()
                    },
                    onRightButtonClickListener = {
                        user?.userId?.let { contactViewModel?.deleteContact(it,false) }
                        dialog?.dismiss()
                    }
                )
                dialogDelete.show()
            }
            else -> {}
        }
    }

    private fun showDeleteContentDialog(){
        val context = this@ChatUIKitContactDetailsActivity
        val menu = getDeleteDialogMenu()
        dialog = SimpleListSheetDialog(
            context = context,
            itemList = menu,
            itemListener = object : SimpleListSheetItemClickListener{
                override fun onItemClickListener(position: Int, menu: ChatUIKitMenuItem) {
                    simpleSheetMenuItemClick(position, menu)
                }
            })
        supportFragmentManager.let { dialog?.show(it,"more_dialog") }
    }

    override fun fetchBlockListFromServerSuccess(list: MutableList<ChatUIKitUser>) {
        ChatUIKitClient.isLoadBlockListFromServer = true
        updateBlockSwitch(list)
    }

    override fun fetchBlockListFromServerFail(code: Int, error: String) {
        ChatUIKitClient.isLoadBlockListFromServer = true
    }

    override fun getBlockListFromLocalSuccess(list: MutableList<ChatUIKitUser>) {
        updateBlockSwitch(list)
    }

    override fun addUserToBlockListSuccess() {
        binding.switchItemBlack.setChecked(true)
        updateBlockLayout(true)
    }

    override fun addUserToBlockListFail(code: Int, error: String) {
        binding.switchItemBlack.setChecked(false)
        updateBlockLayout(false)
    }

    override fun removeUserFromBlockListSuccess() {
        binding.switchItemBlack.setChecked(false)
        updateBlockLayout(false)
    }

    override fun removeUserFromBlockListFail(code: Int, error: String) {
        ChatLog.e(TAG,"removeUserFromBlockListFail $code $error")
    }

    override fun clearConversationSuccess(conversationId: String?) {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitEvent.EVENT.REMOVE.name, ChatUIKitEvent.TYPE.CONVERSATION, conversationId))
    }

    override fun clearConversationFail(code: Int, error: String?) {
        ChatLog.e(TAG,"deleteConversationFail $code $error")
    }

    override fun deleteContactSuccess() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitEvent.EVENT.REMOVE.name, ChatUIKitEvent.TYPE.CONTACT, user?.userId))
        finish()
    }

    override fun deleteContactFail(code: Int, error: String) {
        ChatLog.e(TAG,"deleteContactFail $code $error")
    }

    override fun makeSilentForContactSuccess(silentResult: ChatSilentModeResult) {
        binding.icNotice.visibility = View.VISIBLE
        binding.switchItemDisturb.setChecked(true)
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name)
            .post(lifecycleScope
                , ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name
                    , ChatUIKitEvent.TYPE.SILENT, user?.userId))

    }

    override fun makeSilentForContactFail(
        code: Int,
        error: String?
    ) {
        ChatLog.e(TAG,"makeSilentForConversationFail $code $error")
    }

    override fun cancelSilentForContactSuccess() {
        binding.icNotice.visibility = View.GONE
        binding.switchItemDisturb.setChecked(false)
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name)
            .post(mContext.mainScope()
                , ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name
                    , ChatUIKitEvent.TYPE.CONVERSATION, user?.userId))
    }

    override fun cancelSilentForContactFail(code: Int, error: String?) {
        ChatLog.e(TAG,"cancelSilentForContactFail $code $error")
    }

    override fun onMenuItemClick(item: ChatUIKitMenuItem?, position: Int): Boolean {
        item?.let {
            when(item.menuId){
                R.id.extend_item_message -> {
                    user?.userId?.let { conversationId ->
                        UIKitChatActivity.actionStart(mContext,
                            conversationId, ChatUIKitType.SINGLE_CHAT)
                    }
                }
                R.id.extend_item_search -> {
                    mContext.startActivity(ChatUIKitSearchActivity.createIntent(mContext, ChatUIKitSearchType.MESSAGE,user?.userId))
                }
                else -> {}
            }
            return true
        }
        return false
    }

    companion object {
        private const val TAG = "ChatUIKitContactDetailsActivity"
        private const val KEY_USER_INFO = "userInfo"

        fun createIntent(
            context: Context,
            user: ChatUIKitUser
        ): Intent {
            val intent = Intent(context, ChatUIKitContactDetailsActivity::class.java)
            intent.putExtra(KEY_USER_INFO, user)
            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    return it
                }
            }
            return intent
        }
    }

}