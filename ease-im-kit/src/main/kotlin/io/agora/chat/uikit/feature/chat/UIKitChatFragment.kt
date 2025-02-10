package io.agora.chat.uikit.feature.chat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatMultiDeviceListener.GROUP_DESTROY
import io.agora.chat.uikit.common.ChatMultiDeviceListener.GROUP_LEAVE
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseFragment
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatThread
import io.agora.chat.uikit.common.ChatThreadChangeListener
import io.agora.chat.uikit.common.ChatThreadEvent
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.enums.ChatUIKitFinishReason
import io.agora.chat.uikit.common.extensions.plus
import io.agora.chat.uikit.common.extensions.showToast
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.common.helper.ChatUIKitMenuFilterHelper
import io.agora.chat.uikit.common.helper.ChatUIKitThreadNotifyHelper
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.configs.setStatusStyle
import io.agora.chat.uikit.databinding.UikitFragmentChatBinding
import io.agora.chat.uikit.feature.group.ChatUIKitGroupDetailActivity
import io.agora.chat.uikit.feature.chat.adapter.ChatUIKitMessagesAdapter
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitAttachmentController
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitMentionController
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitType
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitLoadDataType
import io.agora.chat.uikit.feature.chat.interfaces.OnChatExtendMenuItemClickListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatFinishListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatInputChangeListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatLayoutListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatRecordTouchListener
import io.agora.chat.uikit.feature.chat.interfaces.OnSendCombineMessageCallback
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageForwardCallback
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageItemClickListener
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageSendCallback
import io.agora.chat.uikit.feature.chat.interfaces.OnModifyMessageListener
import io.agora.chat.uikit.feature.chat.interfaces.OnMultipleSelectRemoveMsgListener
import io.agora.chat.uikit.feature.chat.interfaces.OnPeerTypingListener
import io.agora.chat.uikit.feature.chat.interfaces.OnReactionMessageListener
import io.agora.chat.uikit.feature.chat.interfaces.OnReportMessageListener
import io.agora.chat.uikit.feature.chat.interfaces.OnTranslationMessageListener
import io.agora.chat.uikit.feature.chat.interfaces.OnWillSendMessageListener
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitExtendMenu
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitLayout
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitMessageListLayout
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitInputMenuStyle
import io.agora.chat.uikit.feature.contact.ChatUIKitContactCheckActivity
import io.agora.chat.uikit.feature.thread.ChatUIKitThreadActivity
import io.agora.chat.uikit.feature.thread.ChatUIKitThreadListActivity
import io.agora.chat.uikit.feature.thread.interfaces.OnMessageChatThreadClickListener
import io.agora.chat.uikit.interfaces.ChatUIKitMultiDeviceListener
import io.agora.chat.uikit.interfaces.IActivityBackPressed
import io.agora.chat.uikit.interfaces.OnMenuChangeListener
import io.agora.chat.uikit.menu.chat.ChatUIKitExtendMenuDialog
import io.agora.chat.uikit.menu.chat.ChatUIKitChatMenuHelper
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitMenuItem
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.provider.getSyncProfile
import io.agora.chat.uikit.provider.getSyncUser

open class UIKitChatFragment: ChatUIKitBaseFragment<UikitFragmentChatBinding>(), OnChatLayoutListener,
    OnMenuChangeListener, OnWillSendMessageListener, OnModifyMessageListener, OnReportMessageListener,
    OnChatFinishListener, OnTranslationMessageListener, OnMessageChatThreadClickListener,
    ChatThreadChangeListener,IActivityBackPressed, OnMultipleSelectRemoveMsgListener {
    private var backPressListener: View.OnClickListener? = null
    private var extendMenuItemClickListener: OnChatExtendMenuItemClickListener? = null
    private var chatInputChangeListener: OnChatInputChangeListener? = null
    private var chatItemClickListener: OnMessageItemClickListener? = null
    private var messageSendCallback: OnMessageSendCallback? = null
    private var otherTypingListener: OnPeerTypingListener? = null
    private var onWillSendMessageListener: OnWillSendMessageListener? = null
    private var recordTouchListener: OnChatRecordTouchListener? = null
    private var reactionMessageListener: OnReactionMessageListener? = null
    private var modifyMessageListener: OnModifyMessageListener? = null
    private var reportMessageListener: OnReportMessageListener? = null
    private var translationMessageListener:OnTranslationMessageListener? = null
    private var messageForwardCallback: OnMessageForwardCallback? = null
    private var sendCombineMessageCallback: OnSendCombineMessageCallback? = null

    private var menuDialog:ChatUIKitExtendMenuDialog? = null
    private var messagesAdapter: ChatUIKitMessagesAdapter? = null
    var conversationId: String? = null
    var chatType: ChatUIKitType = ChatUIKitType.SINGLE_CHAT
    var searchMsgId: String? = null
    var isFromServer = false
    var isThread = false

    private var sendOriginalImage = false
    private var extendMenu:ChatUIKitExtendMenu? = null

    protected val attachmentController: ChatUIKitAttachmentController by lazy {
        ChatUIKitAttachmentController(mContext, binding?.layoutChat, conversationId, sendOriginalImage)
    }
    protected val mentionController: ChatUIKitMentionController by lazy {
        ChatUIKitMentionController(mContext,binding?.layoutChat )
    }

    private val launcherToCamera: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> attachmentController.onActivityResult(result, REQUEST_CODE_CAMERA) }
    private val launcherToAlbum: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> attachmentController.onActivityResult(result, REQUEST_CODE_LOCAL) }
    private val launcherToVideo: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> attachmentController.onActivityResult(result, REQUEST_CODE_SELECT_VIDEO) }
    private val launcherToFile: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> attachmentController.onActivityResult(result, REQUEST_CODE_SELECT_FILE) }

    private val multiDeviceListener = object : ChatUIKitMultiDeviceListener() {

        override fun onGroupEvent(event: Int, target: String?, usernames: MutableList<String>?) {
            if (event == GROUP_DESTROY || event == GROUP_LEAVE) {
                if (TextUtils.equals(target, conversationId)) {
                    mContext.finish()
                }
            }
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UikitFragmentChatBinding? {
        return UikitFragmentChatBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        messagesAdapter?.let {
            binding?.layoutChat?.chatMessageListLayout?.setMessagesAdapter(it)
        }
        arguments?.let {
            conversationId = it.getString(ChatUIKitConstant.EXTRA_CONVERSATION_ID)
            chatType = ChatUIKitType.values()[it.getInt(
                ChatUIKitConstant.EXTRA_CHAT_TYPE,
                ChatUIKitType.SINGLE_CHAT.ordinal
            )]
            searchMsgId = it.getString(ChatUIKitConstant.EXTRA_SEARCH_MSG_ID)
            isFromServer = it.getBoolean(ChatUIKitConstant.EXTRA_IS_FROM_SERVER, false)
            isThread = it.getBoolean(Constant.KEY_THREAD_MESSAGE_FLAG, false)

            binding?.titleBar?.visibility = View.GONE
            val useHeader: Boolean = it.getBoolean(Constant.KEY_USE_TITLE, false)
            binding?.titleBar?.visibility = if (useHeader) View.VISIBLE else View.GONE
            if (useHeader) {
                binding?.chatHeaderDivider?.visibility = View.VISIBLE
                val title: String = it.getString(Constant.KEY_SET_TITLE, "")
                if (!TextUtils.isEmpty(title)) {
                    binding?.titleBar?.setTitle(title)
                }
                val subTitle: String = it.getString(Constant.KEY_SET_SUB_TITLE, "")
                if (!TextUtils.isEmpty(subTitle)) {
                    binding?.titleBar?.setSubtitle(subTitle)
                }
                val canBack: Boolean = it.getBoolean(Constant.KEY_ENABLE_BACK, false)
                binding?.titleBar?.setDisplayHomeAsUpEnabled(canBack
                    , it.getBoolean(Constant.KEY_USE_TITLE_REPLACE, false))
                binding?.titleBar?.setNavigationOnClickListener {
                    if (backPressListener != null) {
                        backPressListener?.onClick(it)
                        return@setNavigationOnClickListener
                    }
                    mContext.onBackPressed()
                }
                setDefaultHeader()
            }

            val timeColor: Int = it.getInt(Constant.KEY_MSG_TIME_COLOR, -1)
            if (timeColor != -1) {
                binding?.layoutChat?.chatMessageListLayout?.setTimeTextColor(timeColor)
            }
            val timeTextSize: Int = it.getInt(Constant.KEY_MSG_TIME_SIZE, -1)
            if (timeTextSize != -1) {
                binding?.layoutChat?.chatMessageListLayout?.setTimeTextSize(timeTextSize)
            }
            val leftBubbleBg: Int = it.getInt(Constant.KEY_MSG_LEFT_BUBBLE, -1)
            if (leftBubbleBg != -1) {
                binding?.layoutChat?.chatMessageListLayout?.
                setItemReceiverBackground(leftBubbleBg)
            }
            val rightBubbleBg: Int = it.getInt(Constant.KEY_MSG_RIGHT_BUBBLE, -1)
            if (rightBubbleBg != -1) {
                binding?.layoutChat?.chatMessageListLayout?.
                setItemSenderBackground(rightBubbleBg)
            }
            val showNickname: Boolean = it.getBoolean(Constant.KEY_SHOW_NICKNAME, false)
            binding?.layoutChat?.chatMessageListLayout?.showNickname(showNickname)
            val messageListShowType: String =
                it.getString(Constant.KEY_MESSAGE_LIST_SHOW_STYLE, "")
            if (!TextUtils.isEmpty(messageListShowType)) {
                ChatUIKitMessageListLayout.ShowType.valueOf(messageListShowType)?.let { type ->
                    binding?.layoutChat?.chatMessageListLayout?.setItemShowType(type)
                }
            }
            val hideReceiveAvatar: Boolean =
                it.getBoolean(Constant.KEY_HIDE_RECEIVE_AVATAR, false)
            binding?.layoutChat?.chatMessageListLayout?.hideChatReceiveAvatar(hideReceiveAvatar)
            val hideSendAvatar: Boolean = it.getBoolean(Constant.KEY_HIDE_SEND_AVATAR, false)
            binding?.layoutChat?.chatMessageListLayout?.hideChatSendAvatar(hideSendAvatar)
            var turnOnTypingMonitor: Boolean = ChatUIKitClient.getConfig()?.chatConfig?.enableChatTyping ?:true
            if (it.containsKey(Constant.KEY_TURN_ON_TYPING_MONITOR)){
                turnOnTypingMonitor =  it.getBoolean(Constant.KEY_TURN_ON_TYPING_MONITOR, true)
            }
            binding?.layoutChat?.turnOnTypingMonitor(turnOnTypingMonitor)
            val chatBg: Int = it.getInt(Constant.KEY_CHAT_BACKGROUND, -1)
            if (chatBg != -1) {
                binding?.layoutChat?.chatMessageListLayout?.setBackgroundResource(chatBg)
            }
            val chatMenuStyle: String = it.getString(Constant.KEY_CHAT_MENU_STYLE, "")
            if (!TextUtils.isEmpty(chatMenuStyle)) {
                ChatUIKitInputMenuStyle.valueOf(chatMenuStyle)?.let { style ->
                    binding?.layoutChat?.chatInputMenu?.chatPrimaryMenu?.setMenuShowType(style)
                }
            }
            val inputBg: Int = it.getInt(Constant.KEY_CHAT_MENU_INPUT_BG, -1)
            if (inputBg != -1) {
                binding?.layoutChat?.chatInputMenu?.chatPrimaryMenu?.setMenuBackground(ContextCompat.getDrawable(mContext, inputBg))
            }
            val inputHint: String = it.getString(Constant.KEY_CHAT_MENU_INPUT_HINT, "")
            if (!TextUtils.isEmpty(inputHint)) {
                binding?.layoutChat?.chatInputMenu?.chatPrimaryMenu?.editText?.hint = inputHint
            }
            sendOriginalImage = it.getBoolean(Constant.KEY_SEND_ORIGINAL_IMAGE_MESSAGE, false)
            val emptyLayout: Int = it.getInt(Constant.KEY_EMPTY_LAYOUT, -1)
            if (emptyLayout != -1) {
                binding?.layoutChat?.chatMessageListLayout?.getMessagesAdapter()?.setEmptyView(emptyLayout)
            }
        }
        setCustomExtendView()
    }

    open fun setDefaultHeader(updateName: Boolean = false) {
        ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(binding?.titleBar?.getLogoView())
        ChatUIKitClient.getConfig()?.avatarConfig?.setStatusStyle(binding?.titleBar?.getStatusView(),4,
            ContextCompat.getColor(mContext, R.color.ease_color_background))
        updateSilent()
        addMenu()
        chatType.let { type ->
            when(type) {
                ChatUIKitType.GROUP_CHAT -> {
                    ChatUIKitClient.getGroupProfileProvider()
                        ?.getSyncProfile(conversationId)
                        ?.let { profile ->
                            binding?.run {
                                if (titleBar.getTitle().isNullOrEmpty() || updateName) {
                                    titleBar.setTitle(profile.name)
                                }
                                titleBar.setLogo(
                                    profile.avatar,
                                    R.drawable.uikit_default_group_avatar,
                                    resources.getDimensionPixelSize(R.dimen.ease_title_bar_icon_size)
                                )
                            }
                        } ?: kotlin.run {
                            setDefaultInfo(type)
                        }
                }

                ChatUIKitType.SINGLE_CHAT -> {
                    ChatUIKitClient.getUserProvider()?.getSyncUser(conversationId)?.let { user ->
                        binding?.run {
                            if (titleBar.getTitle().isNullOrEmpty() || updateName) {
                                titleBar.setTitle(user.getRemarkOrName())
                            }
                            titleBar.setLogo(
                                user.avatar,
                                R.drawable.uikit_default_avatar,
                                resources.getDimensionPixelSize(R.dimen.ease_title_bar_icon_size)
                            )
                        }
                    } ?: kotlin.run {
                        setDefaultInfo(type)
                    }
                }

                else -> {}
            }
        }
    }

    open fun addMenu() {
        binding?.titleBar?.inflateMenu(R.menu.uikit_menu_group_topic)
        setMenuItemClickListener()
        if (chatType != ChatUIKitType.GROUP_CHAT || isThread || ChatUIKitClient.getConfig()?.chatConfig?.enableChatThreadMessage == false){
            binding?.titleBar?.setMenuIconVisible(R.id.chat_menu_topic,false)
        }
        if (isThread || ChatUIKitClient.getConfig()?.chatConfig?.enableChatPingMessage == false){
            binding?.titleBar?.setMenuIconVisible(R.id.chat_menu_pin,false)
        }
    }

    private fun setMenuItemClickListener() {
        binding?.titleBar?.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener setMenuItemClick(it)
        }
    }

    open fun setMenuItemClick(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.chat_menu_topic -> {
                ChatUIKitThreadListActivity.actionStart(mContext,conversationId)
                return true
            }
            R.id.chat_menu_pin -> {
                binding?.layoutChat?.initPinView()
                return true
            }
            else -> return false
        }
    }

    private fun defaultSkipLogic() {
        if (chatType == ChatUIKitType.SINGLE_CHAT) {
            startActivity(ChatUIKitContactCheckActivity.createIntent(mContext
                , ChatUIKitClient.getCache().getUser(conversationId!!)?.toUser() ?: ChatUIKitUser(conversationId!!)))
        } else if (chatType == ChatUIKitType.GROUP_CHAT){
            if (!isThread){
                startActivity(ChatUIKitGroupDetailActivity.createIntent(mContext, conversationId!!))
            }
        }
    }

    /**
     * Update the group name.
     */
    open fun updateGroupInfo() {
        ChatClient.getInstance().groupManager().getGroup(conversationId)?.let { group ->
            binding?.titleBar?.setTitle(group.groupName)
        }
    }

    private fun setDefaultInfo(chatType: ChatUIKitType) {
        if (binding?.titleBar?.getTitle().isNullOrEmpty()) {
            binding?.titleBar?.setTitle(
                when(chatType) {
                    ChatUIKitType.GROUP_CHAT -> ChatClient.getInstance().groupManager().getGroup(conversationId)?.groupName ?: conversationId
                    ChatUIKitType.CHATROOM -> ChatClient.getInstance().chatroomManager().getChatRoom(conversationId)?.name ?: conversationId
                    else -> conversationId
                }
            )
        }
        val defaultRes = when(chatType) {
            ChatUIKitType.GROUP_CHAT -> R.drawable.uikit_default_group_avatar
            ChatUIKitType.CHATROOM -> R.drawable.ease_default_chatroom_avatar
            else -> R.drawable.uikit_default_avatar
        }
        binding?.titleBar?.setLogo(defaultRes)
        binding?.titleBar?.setLogoSize(resources.getDimensionPixelSize(R.dimen.ease_title_bar_icon_size))
    }

    private fun setCustomExtendView() {
        val enableWxStyle = ChatUIKitClient.getConfig()?.chatConfig?.enableWxExtendStyle
        if (enableWxStyle == true){
            extendMenu = ChatUIKitExtendMenu(mContext)
            (extendMenu as ChatUIKitExtendMenu).init()
            extendMenu?.registerMenuItem(
                R.string.uikit_attach_contact_card,
                R.drawable.em_chat_card_selector,
                R.id.extend_item_contact_card,
                titleColor = ContextCompat.getColor(mContext,R.color.ease_color_wx_style_extend_menu_text_tint),
                resourceTintColor = ContextCompat.getColor(mContext,R.color.ease_color_wx_style_extend_menu_tint)
            )
            binding?.layoutChat?.chatInputMenu?.setCustomExtendMenu(extendMenu)
        }else{
            menuDialog = ChatUIKitExtendMenuDialog(mContext)
            menuDialog?.init()
            binding?.layoutChat?.chatInputMenu?.setCustomExtendMenu(menuDialog)
        }
    }

    override fun initListener() {
        super.initListener()
        binding?.let { root ->
            root.layoutChat.let {
                it.setOnChatLayoutListener(this)
                it.setOnMenuChangeListener(this)
                it.setOnWillSendMessageListener(this)
                it.setOnEditMessageListener(this)
                it.setOnReportMessageListener(this)
                it.setOnChatRecordTouchListener(recordTouchListener)
                it.setOnChatFinishListener(this)
                it.setOnTranslationMessageListener(this)
                it.setOnMessageThreadViewClickListener(this)
                it.setMultipleSelectRemoveMsgListener(this)
            }
            root.titleBar.let {
                it.setLogoClickListener {
                    defaultSkipLogic()
                }
                it.setTitleClickListener {
                    defaultSkipLogic()
                }
            }
        }
        ChatUIKitClient.addMultiDeviceListener(multiDeviceListener)
        ChatUIKitClient.addThreadChangeListener(this)
    }

    override fun initData() {
        super.initData()
        initChatLayout()
        loadData()
        initEventBus()
    }


    override fun onBackPressed(): Boolean {
        if (binding?.layoutChat?.messageMultipleSelectController?.isInMultipleSelectStyle == true) {
            binding?.layoutChat?.messageMultipleSelectController?.cancelMultiSelectStyle(binding?.titleBar?.getToolBar()) {
                cancelMultipleSelectStyle()
            }
            return true
        }
        return false
    }

    open fun initEventBus() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name).register(viewLifecycleOwner) {
            if (it.isConversationChange && it.message == conversationId) {
                loadData()
            }
        }
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name).register(viewLifecycleOwner) {
            if (it.isConversationChange && it.message == conversationId) {
                loadData()
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(viewLifecycleOwner) {
            if (it.isConversationChange && it.message == conversationId) {
                binding?.layoutChat?.chatMessageListLayout?.refreshMessages()
                binding?.layoutChat?.chatNotificationController?.updateNotificationView()
            }
        }
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(this) {
            if (it.isSilentChange ) {
                updateSilent()
            }
        }
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(viewLifecycleOwner) {
            if (it.isMessageChange) {
                if (it.message.isNullOrEmpty()) return@register
                ChatClient.getInstance().chatManager().getMessage(it.message)?.let { message ->
                    if (message.conversationId() == conversationId) {
                        binding?.layoutChat?.chatMessageListLayout?.refreshMessage(message)
                    }
                }
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name).register(viewLifecycleOwner) {
            if (it.isMessageChange) {
                if (it.message.isNullOrEmpty()) return@register
                ChatClient.getInstance().chatManager().getMessage(it.message)?.let { message ->
                    if (message.conversationId() == conversationId) {
                        binding?.layoutChat?.chatMessageListLayout?.refreshToLatest()
                    }
                }
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.DESTROY.name).register(viewLifecycleOwner) {
            if (it.isGroupChange && it.message == conversationId) {
                finishCurrentActivity(ChatUIKitFinishReason.onGroupDestroyed)
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.LEAVE.name).register(viewLifecycleOwner) {
            if (it.isGroupChange && it.message == conversationId) {
                finishCurrentActivity(ChatUIKitFinishReason.onGroupLeft)
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name).register(viewLifecycleOwner) {
            if (it.isContactChange && it.message == conversationId) {
                finishCurrentActivity(ChatUIKitFinishReason.onContactRemoved)
            }
        }

        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitConstant.EASE_MULTIPLE_SELECT).register(this) {
            if (it.isNotifyChange && it.message == mContext + conversationId) {
                setMultipleSelectStyle()
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.GROUP).register(this) {
            if (it.isGroupChange && it.message == conversationId && it.event == ChatUIKitConstant.EVENT_UPDATE_GROUP_NAME) {
                updateGroupInfo()
            }
        }
    }

    open fun setMultipleSelectStyle() {
        binding?.layoutChat?.messageMultipleSelectController?.makeToolBarMultipleSelectStyle(binding?.titleBar?.getToolBar()) {
            setMenuItemClickListener()
            cancelMultipleSelectStyle()
        }
    }

    open fun cancelMultipleSelectStyle() {}

    open fun initChatLayout() {
        binding?.layoutChat?.let {
            if (!TextUtils.isEmpty(searchMsgId)) {
                it.init(
                    conversationId,
                    chatType,
                    ChatUIKitLoadDataType.SEARCH
                )
            } else {
                if (isThread) {
                    it.init(
                        conversationId,
                        chatType,
                        ChatUIKitLoadDataType.THREAD
                    )
                } else {
                    if (isFromServer) {
                        it.init(
                            conversationId,
                            chatType,
                            ChatUIKitLoadDataType.ROAM
                        )
                    } else {
                        it.init(
                            conversationId,
                            chatType
                        )
                    }
                }
            }
        }
    }

    open fun loadData() {
        if (!TextUtils.isEmpty(searchMsgId)) {
            binding?.layoutChat?.loadData(searchMsgId)
        } else {
            binding?.layoutChat?.loadData()
        }
    }

    override fun onDestroyView() {
        binding?.layoutChat?.chatInputMenu?.setCustomExtendMenu(null)
        val enableWxStyle = ChatUIKitClient.getConfig()?.chatConfig?.enableWxExtendStyle
        if (enableWxStyle == true) {
            extendMenu?.clear()
            extendMenu = null
        }else{
            menuDialog?.unregisterListener()
            menuDialog = null
        }
        ChatUIKitClient.removeMultiDeviceListener(multiDeviceListener)
        ChatUIKitClient.removeThreadChangeListener(this)
        super.onDestroyView()
    }

    fun selectPicFromLocal() {
        attachmentController.selectPicFromLocal(launcherToAlbum)
    }

    fun selectVideoFromLocal() {
        attachmentController.selectVideoFromLocal(launcherToVideo)
    }

    fun selectFileFromLocal() {
        attachmentController.selectFileFromLocal(launcherToFile)
    }

    open fun finishCurrentActivity(reason: ChatUIKitFinishReason) {
        when(reason) {
            ChatUIKitFinishReason.onGroupLeft,
            ChatUIKitFinishReason.onGroupDestroyed,
            ChatUIKitFinishReason.onContactRemoved -> {
                ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name)
                    .post(lifecycleScope, ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name, ChatUIKitEvent.TYPE.CONVERSATION, conversationId))
                mContext.finish()
            }
            ChatUIKitFinishReason.onGroupUserRemoved -> {
                mContext.finish()
            }
            else -> {}
        }
    }

    override fun multipleSelectRemoveMsgSuccess() {
        if (binding?.layoutChat?.messageMultipleSelectController?.isInMultipleSelectStyle == true) {
            binding?.layoutChat?.messageMultipleSelectController?.cancelMultiSelectStyle(binding?.titleBar?.getToolBar()) {
                cancelMultipleSelectStyle()
            }
        }
    }

    override fun onBubbleClick(message: ChatMessage?): Boolean {
        return chatItemClickListener?.onBubbleClick(message) ?: false
    }

    override fun onBubbleLongClick(v: View?, message: ChatMessage?): Boolean {
        return chatItemClickListener?.onBubbleLongClick(v, message) ?: false
    }

    override fun onResendClick(message: ChatMessage?): Boolean {
        return chatItemClickListener?.onResendClick(message) ?: false
    }

    override fun onUserAvatarClick(userId: String?) {
        chatItemClickListener?.onUserAvatarClick(userId)
        ?: kotlin.run {
            if (!userId.isNullOrEmpty() && userId != ChatClient.getInstance().currentUser) {
                val user = ChatUIKitClient.getUserProvider()?.getSyncUser(userId)?.toUser() ?: ChatUIKitUser(userId)
                startActivity(ChatUIKitContactCheckActivity.createIntent(mContext,user))
            }
        }
    }

    override fun onUserAvatarLongClick(userId: String?) {
        chatItemClickListener?.onUserAvatarLongClick(userId)
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        val currentConversation = binding?.layoutChat?.chatMessageListLayout?.currentConversation
        currentConversation?.let { cv->
            binding?.layoutChat?.chatMessageListLayout?.isGroupChat(cv)?.let { isGroupChat->
                if (isGroupChat && s != null){
                    if (count == 1 && "@" == s[start].toString()) {
                        conversationId?.let { it1 -> mentionController.showMentionDialog(it1) }
                    }
                }
            }
        }
        chatInputChangeListener?.onTextChanged(s, start, before, count)
    }

    override fun afterTextChanged(s: Editable?) {
        s?.let { mentionController.setPickAtContentStyle(s) }
        chatInputChangeListener?.afterTextChanged(s)
    }

    override fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        return mentionController.removePickAt(v,keyCode,event)
    }

    override fun onChatFinish(reason: ChatUIKitFinishReason, id: String?) {
        finishCurrentActivity(reason)
    }

    override fun onSuccess(message: ChatMessage?) {
        super.onSuccess(message)
        messageSendCallback?.onSuccess(message)
    }

    override fun onError(code: Int, errorMsg: String?) {
        messageSendCallback?.onError(code, errorMsg)
    }

    override fun onChatExtendMenuItemClick(view: View?, itemId: Int): Boolean {
        if (extendMenuItemClickListener != null && extendMenuItemClickListener?.onChatExtendMenuItemClick(view, itemId) == true) {
            return true
        }
        when(itemId) {
            R.id.extend_item_take_picture -> {
                attachmentController.selectPicFromCamera(launcherToCamera)
                return true
            }
            R.id.extend_item_picture -> {
                attachmentController.selectPicFromLocal(launcherToAlbum)
                return true
            }
            R.id.extend_item_video -> {
                attachmentController.selectVideoFromLocal(launcherToVideo)
                return true
            }
            R.id.extend_item_file -> {
                attachmentController.selectFileFromLocal(launcherToFile)
                return true
            }
            R.id.extend_item_contact_card -> {
                attachmentController.selectContact(childFragmentManager, chatType)
                return true
            }
        }
        return false
    }

    override fun onForwardSuccess(message: ChatMessage?) {
        messageForwardCallback?.onForwardSuccess(message)
    }

    override fun onForwardError(code: Int, errorMsg: String?) {
        messageForwardCallback?.onForwardError(code, errorMsg)
    }

    override fun onSendCombineSuccess(message: ChatMessage?) {
        if (binding?.layoutChat?.messageMultipleSelectController?.isInMultipleSelectStyle == true) {
            binding?.layoutChat?.messageMultipleSelectController?.cancelMultiSelectStyle(binding?.titleBar?.getToolBar()) {
                cancelMultipleSelectStyle()
            }
        }
        sendCombineMessageCallback?.onSendCombineSuccess(message)
    }

    override fun onSendCombineError(message: ChatMessage?, code: Int, errorMsg: String?) {
        sendCombineMessageCallback?.onSendCombineError(message, code, errorMsg)
    }

    override fun onSendAckSuccess(message: ChatMessage?) {
        // do nothing
    }

    override fun onSendAckError(message: ChatMessage?, code: Int, errorMsg: String?) {
        // do nothing
    }

    override fun onPreMenu(helper: ChatUIKitChatMenuHelper?, message: ChatMessage?) {
        ChatUIKitMenuFilterHelper.filterMenu(helper,message)
    }

    override fun onMenuItemClick(item: ChatUIKitMenuItem?, message: ChatMessage?): Boolean {
        return false
    }

    override fun onWillSendMessage(message: ChatMessage?) {
        this.onWillSendMessageListener?.onWillSendMessage(message)
    }

    override fun onModifyMessageSuccess(messageModified: ChatMessage?) {
        ChatUIKitClient.getCache().cleanUrlPreviewInfo(messageModified?.msgId)
        modifyMessageListener?.onModifyMessageSuccess(messageModified)
    }

    override fun onModifyMessageFailure(messageId: String?, code: Int, error: String?) {
        modifyMessageListener?.onModifyMessageFailure(messageId, code, error)
    }

    override fun onReportMessageSuccess(msgId:String) {
        reportMessageListener?.onReportMessageSuccess(msgId)
        ?: kotlin.run {
            mContext.showToast(R.string.uikit_report_success)
        }
    }

    override fun onReportMessageFailure(msgId: String?, code: Int, error: String?) {
        reportMessageListener?.onReportMessageFailure(msgId,code, error)
    }

    override fun onTranslationMessageSuccess(message: ChatMessage?) {
        translationMessageListener?.onTranslationMessageSuccess(message)
    }

    override fun onHideTranslationMessage(message: ChatMessage?) {
        translationMessageListener?.onHideTranslationMessage(message)
    }

    override fun onTranslationMessageFailure(code: Int, error: String?) {
        translationMessageListener?.onTranslationMessageFailure(code, error)
    }

    open fun updateSilent(){
        val isSilent = ChatUIKitClient.getCache().getMutedConversationList().containsKey(conversationId)
        binding?.run {
            if (isSilent){
                titleBar.setTitleEndDrawable(R.drawable.uikit_do_not_disturb)
            }else{
                titleBar.setTitleEndDrawable()
            }
        }
    }

    override fun onPeerTyping(action: String?) {
        ChatUIKitClient.getConfig()?.chatConfig?.enableChatTyping?.let {
            if (!it) {
                return
            }
        }
        if (TextUtils.equals(action, ChatUIKitLayout.ACTION_TYPING_BEGIN)) {
            binding?.titleBar?.setSubtitle(getString(R.string.alert_during_typing))
            binding?.titleBar?.visibility = View.VISIBLE
        } else if (TextUtils.equals(action, ChatUIKitLayout.ACTION_TYPING_END)) {
            binding?.titleBar?.setSubtitle("")
        }
        otherTypingListener?.onPeerTyping(action)
    }

    override fun onThreadViewItemClick(view: View, thread: ChatThread?, topicMsg: ChatMessage) {
        ChatUIKitClient.getConfig()?.chatConfig?.enableChatThreadMessage?.let {
            if (!it) {
                return
            }
        }
        thread?.let {
            ChatUIKitThreadActivity.actionStart(
                context = mContext,
                conversationId = it.parentId,
                threadId = it.chatThreadId,
                topicMsgId = topicMsg.msgId,
            )
        }
    }

    override fun onChatThreadCreated(event: ChatThreadEvent?) {
        ChatUIKitClient.getConfig()?.chatConfig?.enableChatThreadMessage?.let {
            if (!it) {
                return
            }
        }
        ChatUIKitThreadNotifyHelper.createThreadCreatedMsg(event)
        event?.let {
            if (conversationId == it.chatThread.parentId){
                binding?.layoutChat?.chatMessageListLayout?.refreshToLatest()
            }
        }
    }

    override fun onChatThreadUpdated(event: ChatThreadEvent?) {
        ChatUIKitClient.getConfig()?.chatConfig?.enableChatThreadMessage?.let {
            if (!it) {
                return
            }
        }
        event?.let {
            if (conversationId == it.chatThread.parentId){
                binding?.layoutChat?.chatMessageListLayout?.refreshMessage(it.chatThread.messageId)
            }
        }
    }

    override fun onChatThreadDestroyed(event: ChatThreadEvent?) {
        ChatUIKitClient.getConfig()?.chatConfig?.enableChatThreadMessage?.let {
            if (!it) {
                return
            }
        }
        event?.let {
            if (conversationId == it.chatThread.parentId){
                conversationId?.let { it1 -> ChatUIKitThreadNotifyHelper.removeCreateThreadNotify(it1,it.chatThread.chatThreadId) }
                binding?.layoutChat?.chatMessageListLayout?.refreshMessages()
            }
        }
    }

    override fun onChatThreadUserRemoved(event: ChatThreadEvent?) {
        ChatUIKitClient.getConfig()?.chatConfig?.enableChatThreadMessage?.let {
            if (!it) {
                return
            }
        }
        if (TextUtils.equals(event?.chatThread?.chatThreadId, conversationId)) {
            mContext.finish()
        }
    }

    private fun setHeaderBackPressListener(listener: View.OnClickListener?) {
        this.backPressListener = listener
    }

    private fun setOnChatExtendMenuItemClickListener(listener: OnChatExtendMenuItemClickListener?) {
        this.extendMenuItemClickListener = listener
    }

    private fun setOnChatInputChangeListener(listener: OnChatInputChangeListener?) {
        this.chatInputChangeListener = listener
    }

    private fun setOnMessageItemClickListener(listener: OnMessageItemClickListener?) {
        this.chatItemClickListener = listener
    }

    private fun setOnMessageSendCallback(callBack: OnMessageSendCallback?) {
        this.messageSendCallback = callBack
    }

    private fun setOnPeerTypingListener(listener: OnPeerTypingListener?) {
        this.otherTypingListener = listener
    }

    private fun setOnWillSendMessageListener(onWillSendMessageListener: OnWillSendMessageListener?) {
        this.onWillSendMessageListener = onWillSendMessageListener
    }

    private fun setOnChatRecordTouchListener(recordTouchListener: OnChatRecordTouchListener?) {
        this.recordTouchListener = recordTouchListener
    }

    private fun setOnReactionMessageListener(reactionMessageListener: OnReactionMessageListener?) {
        this.reactionMessageListener = reactionMessageListener
    }

    private fun setOnModifyMessageListener(listener: OnModifyMessageListener?) {
        this.modifyMessageListener = listener
    }

    private fun setOnReportMessageListener(listener: OnReportMessageListener?){
        this.reportMessageListener = listener
    }

    private fun setOnTranslationMessageListener(listener: OnTranslationMessageListener?){
        this.translationMessageListener = listener
    }

    private fun setOnMessageForwardCallback(callback: OnMessageForwardCallback?) {
        this.messageForwardCallback = callback
    }

    private fun setOnSendCombineMessageCallback(callback: OnSendCombineMessageCallback?) {
        this.sendCombineMessageCallback = callback
    }

    private fun setCustomAdapter(adapter: ChatUIKitMessagesAdapter?) {
        this.messagesAdapter = adapter
    }

    open class Builder(
        private val conversationId: String?,
        private val chatType: ChatUIKitType = ChatUIKitType.SINGLE_CHAT,
        private val searchMessageId: String? = null
    ) {
        protected val bundle: Bundle = Bundle()
        private var backPressListener: View.OnClickListener? = null
        private var adapter: ChatUIKitMessagesAdapter? = null
        private var extendMenuItemClickListener: OnChatExtendMenuItemClickListener? = null
        private var chatInputChangeListener: OnChatInputChangeListener? = null
        private var messageItemClickListener: OnMessageItemClickListener? = null
        private var messageSendCallback: OnMessageSendCallback? = null
        private var peerTypingListener: OnPeerTypingListener? = null
        private var willSendMessageListener: OnWillSendMessageListener? = null
        private var recordTouchListener: OnChatRecordTouchListener? = null
        private var reactionMessageListener: OnReactionMessageListener? = null
        protected var customFragment: UIKitChatFragment? = null
        private var modifyMessageListener: OnModifyMessageListener? = null
        private var reportMessageListener: OnReportMessageListener? = null
        private var translationMessageListener: OnTranslationMessageListener? = null
        private var messageForwardCallback: OnMessageForwardCallback? = null
        private var sendCombineMessageCallback: OnSendCombineMessageCallback? = null

        init {
            bundle.putString(ChatUIKitConstant.EXTRA_CONVERSATION_ID, conversationId)
            bundle.putInt(ChatUIKitConstant.EXTRA_CHAT_TYPE, chatType.ordinal)
            bundle.putString(ChatUIKitConstant.EXTRA_SEARCH_MSG_ID, searchMessageId)
        }

        /**
         * Set search message id.
         *
         * @param searchMessageId
         * @return
         */
        fun setSearchMessageId(searchMessageId: String?): Builder {
            bundle.putString(ChatUIKitConstant.EXTRA_SEARCH_MSG_ID, searchMessageId)
            return this
        }

        /**
         * Whether to use default titleBar which is [ChatUIKitTitleBar]
         *
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
         *
         * @param title
         * @return
         */
        fun setTitleBarTitle(title: String?): Builder {
            bundle.putString(Constant.KEY_SET_TITLE, title)
            return this
        }

        /**
         * Set titleBar's sub title
         *
         * @param subTitle
         * @return
         */
        fun setTitleBarSubTitle(subTitle: String?): Builder {
            bundle.putString(Constant.KEY_SET_SUB_TITLE, subTitle)
            return this
        }

        /**
         * Whether show back icon in titleBar
         *
         * @param canBack
         * @return
         */
        fun enableTitleBarPressBack(canBack: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_ENABLE_BACK, canBack)
            return this
        }

        /**
         * If you have set [Builder.enableTitleBarPressBack], you can set the listener
         *
         * @param listener
         * @return
         */
        fun setTitleBarBackPressListener(listener: View.OnClickListener?): Builder {
            backPressListener = listener
            return this
        }

        /**
         * Set Whether to get history message from server or local database
         *
         * @param isFromServer
         * @return
         */
        fun getHistoryMessageFromServerOrLocal(isFromServer: Boolean): Builder {
            bundle.putBoolean(ChatUIKitConstant.EXTRA_IS_FROM_SERVER, isFromServer)
            return this
        }

        /**
         * Set chat extension menu item click listener
         *
         * @param listener
         * @return
         */
        fun setOnChatExtendMenuItemClickListener(listener: OnChatExtendMenuItemClickListener?): Builder {
            extendMenuItemClickListener = listener
            return this
        }

        /**
         * Set chat menu's text change listener
         *
         * @param listener
         * @return
         */
        fun setOnChatInputChangeListener(listener: OnChatInputChangeListener?): Builder {
            chatInputChangeListener = listener
            return this
        }

        /**
         * Set message item click listener, include bubble click, bubble long click, avatar click
         * and avatar long click
         *
         * @param listener
         * @return
         */
        fun setOnMessageItemClickListener(listener: OnMessageItemClickListener?): Builder {
            messageItemClickListener = listener
            return this
        }

        /**
         * Set message's callback after which is sent
         *
         * @param callBack
         * @return
         */
        fun setOnMessageSendCallback(callBack: OnMessageSendCallback?): Builder {
            messageSendCallback = callBack
            return this
        }

        /**
         * Turn on other peer's typing monitor, only for single chat
         *
         * @param turnOn
         * @return
         */
         fun turnOnTypingMonitor(turnOn: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_TURN_ON_TYPING_MONITOR, turnOn)
            return this
        }

        /**
         * Set peer's typing listener, only for single chat. You need call [Builder.turnOnTypingMonitor] first.
         *
         * @param listener
         * @return
         */
        fun setOnPeerTypingListener(listener: OnPeerTypingListener?): Builder {
            peerTypingListener = listener
            return this
        }

        /**
         * Set the event you can add message's attrs before send message
         *
         * @param willSendMessageListener
         * @return
         */
        fun setOnWillSendMessageListener(willSendMessageListener: OnWillSendMessageListener?): Builder {
            this.willSendMessageListener = willSendMessageListener
            return this
        }

        /**
         * Set touch event listener during recording
         *
         * @param recordTouchListener
         * @return
         */
        fun setOnChatRecordTouchListener(recordTouchListener: OnChatRecordTouchListener?): Builder {
            this.recordTouchListener = recordTouchListener
            return this
        }

        /**
         * Set message forward callback.
         */
        fun setOnMessageForwardCallback(callback: OnMessageForwardCallback?): Builder {
            this.messageForwardCallback = callback
            return this
        }

        /**
         * Set combine message send callback.
         */
        fun setOnSendCombineMessageCallback(callback: OnSendCombineMessageCallback?): Builder {
            this.sendCombineMessageCallback = callback
            return this
        }

        /**
         * Set reaction listener
         *
         * @param reactionMessageListener
         * @return
         */
        private fun setOnReactionMessageListener(reactionMessageListener: OnReactionMessageListener?): Builder {
            this.reactionMessageListener = reactionMessageListener
            return this
        }

        /**
         * Set the text color of message item time
         *
         * @param color
         * @return
         */
        fun setMsgTimeTextColor(@ColorInt color: Int): Builder {
            bundle.putInt(Constant.KEY_MSG_TIME_COLOR, color)
            return this
        }

        /**
         * Set the text size of message item time, unit is px
         *
         * @param size
         * @return
         */
        fun setMsgTimeTextSize(size: Int): Builder {
            bundle.putInt(Constant.KEY_MSG_TIME_SIZE, size)
            return this
        }

        /**
         * Set the bubble background of the received message
         *
         * @param bgDrawable
         * @return
         */
        fun setReceivedMsgBubbleBackground(@DrawableRes bgDrawable: Int): Builder {
            bundle.putInt(Constant.KEY_MSG_LEFT_BUBBLE, bgDrawable)
            return this
        }

        /**
         * Set the bubble background of the sent message
         *
         * @param bgDrawable
         * @return
         */
        fun setSentBubbleBackground(@DrawableRes bgDrawable: Int): Builder {
            bundle.putInt(Constant.KEY_MSG_RIGHT_BUBBLE, bgDrawable)
            return this
        }

        /**
         * Whether to show nickname in message item
         *
         * @param showNickname
         * @return
         */
        fun showNickname(showNickname: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_SHOW_NICKNAME, showNickname)
            return this
        }

        /**
         * Set message list show style, including normal and all_start style
         *
         * @param showType
         * @return
         */
        internal fun setMessageListShowStyle(showType: ChatUIKitMessageListLayout.ShowType): Builder {
            bundle.putString(Constant.KEY_MESSAGE_LIST_SHOW_STYLE, showType.name)
            return this
        }

        /**
         * Set the message modification listener.
         * @param listener
         * @return
         */
        fun setOnModifyMessageListener(listener: OnModifyMessageListener?): Builder {
            modifyMessageListener = listener
            return this
        }

        /**
         * Set the message report listener.
         * @param listener
         * @return
         */
        fun setOnReportMessageListener(listener: OnReportMessageListener?): Builder {
            reportMessageListener = listener
            return this
        }

        /**
         * Set the message translation listener.
         * @param listener
         * @return
         */
        fun setOnTranslationMessageListener(listener: OnTranslationMessageListener?): Builder {
            translationMessageListener = listener
            return this
        }

        /**
         * Whether to hide receiver's avatar
         *
         * @param hide
         * @return
         */
        fun hideReceiverAvatar(hide: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_HIDE_RECEIVE_AVATAR, hide)
            return this
        }

        /**
         * Whether to hide sender's avatar
         *
         * @param hide
         * @return
         */
        fun hideSenderAvatar(hide: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_HIDE_SEND_AVATAR, hide)
            return this
        }

        /**
         * Set the background of the chat list region
         *
         * @param bgDrawable
         * @return
         */
        fun setChatBackground(@DrawableRes bgDrawable: Int): Builder {
            bundle.putInt(Constant.KEY_CHAT_BACKGROUND, bgDrawable)
            return this
        }

        /**
         * Set chat input menu style, including voice input, text input,
         * emoji input and extended function input
         *
         * @param style
         * @return
         */
        internal fun setChatInputMenuStyle(style: ChatUIKitInputMenuStyle): Builder {
            bundle.putString(Constant.KEY_CHAT_MENU_STYLE, style.name)
            return this
        }

        /**
         * Set chat input menu background
         *
         * @param bgDrawable
         * @return
         */
        fun setChatInputMenuBackground(@DrawableRes bgDrawable: Int): Builder {
            bundle.putInt(Constant.KEY_CHAT_MENU_INPUT_BG, bgDrawable)
            return this
        }

        /**
         * Set chat input menu's hint text
         *
         * @param inputHint
         * @return
         */
        fun setChatInputMenuHint(inputHint: String?): Builder {
            bundle.putString(Constant.KEY_CHAT_MENU_INPUT_HINT, inputHint)
            return this
        }

        /**
         * Set whether to use original file to send image message
         *
         * @param sendOriginalImage
         * @return
         */
        fun sendMessageByOriginalImage(sendOriginalImage: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_SEND_ORIGINAL_IMAGE_MESSAGE, sendOriginalImage)
            return this
        }

        /**
         * Marks whether the chat is a chat thread.
         *
         * @param isThread
         * @return
         */
        internal fun setThreadMessage(isThread: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_THREAD_MESSAGE_FLAG, isThread)
            return this
        }

        /**
         * Set chat list's empty layout if you want replace the default
         *
         * @param emptyLayout
         * @return
         */
        fun setEmptyLayout(@LayoutRes emptyLayout: Int): Builder {
            bundle.putInt(Constant.KEY_EMPTY_LAYOUT, emptyLayout)
            return this
        }

        /**
         * Set custom fragment which should extends EaseMessageFragment
         *
         * @param fragment
         * @param <T>
         * @return
        </T> */
        fun <T : UIKitChatFragment?> setCustomFragment(fragment: T): Builder {
            customFragment = fragment
            return this
        }

        /**
         * Set custom adapter which should extends EaseMessageAdapter
         *
         * @param adapter
         * @return
         */
        fun setCustomAdapter(adapter: ChatUIKitMessagesAdapter?): Builder {
            this.adapter = adapter
            return this
        }

        open fun build(): UIKitChatFragment? {
            val fragment = if (customFragment != null) customFragment else UIKitChatFragment()
            fragment?.let {
                it.arguments = bundle
                it.setHeaderBackPressListener(backPressListener)
                it.setOnChatExtendMenuItemClickListener(extendMenuItemClickListener)
                it.setOnChatInputChangeListener(chatInputChangeListener)
                it.setOnMessageItemClickListener(messageItemClickListener)
                it.setOnMessageSendCallback(messageSendCallback)
                it.setOnPeerTypingListener(peerTypingListener)
                it.setOnWillSendMessageListener(willSendMessageListener)
                it.setOnChatRecordTouchListener(recordTouchListener)
                it.setCustomAdapter(adapter)
                it.setOnReactionMessageListener(reactionMessageListener)
                it.setOnModifyMessageListener(modifyMessageListener)
                it.setOnReportMessageListener(reportMessageListener)
                it.setOnTranslationMessageListener(translationMessageListener)
                it.setOnMessageForwardCallback(messageForwardCallback)
                it.setOnSendCombineMessageCallback(sendCombineMessageCallback)
            }
            return fragment
        }
    }

    private object Constant {
        const val KEY_USE_TITLE = "key_use_title"
        const val KEY_USE_TITLE_REPLACE = "key_use_replace_action_bar"
        const val KEY_SET_TITLE = "key_set_title"
        const val KEY_SET_SUB_TITLE = "key_set_sub_title"
        const val KEY_EMPTY_LAYOUT = "key_empty_layout"
        const val KEY_ENABLE_BACK = "key_enable_back"
        const val KEY_MSG_TIME_COLOR = "key_msg_time_color"
        const val KEY_MSG_TIME_SIZE = "key_msg_time_size"
        const val KEY_MSG_LEFT_BUBBLE = "key_msg_left_bubble"
        const val KEY_MSG_RIGHT_BUBBLE = "key_msg_right_bubble"
        const val KEY_SHOW_NICKNAME = "key_show_nickname"
        const val KEY_MESSAGE_LIST_SHOW_STYLE = "key_message_list_show_type"
        const val KEY_HIDE_RECEIVE_AVATAR = "key_hide_left_avatar"
        const val KEY_HIDE_SEND_AVATAR = "key_hide_right_avatar"
        const val KEY_CHAT_BACKGROUND = "key_chat_background"
        const val KEY_CHAT_MENU_STYLE = "key_chat_menu_style"
        const val KEY_CHAT_MENU_INPUT_BG = "key_chat_menu_input_bg"
        const val KEY_CHAT_MENU_INPUT_HINT = "key_chat_menu_input_hint"
        const val KEY_TURN_ON_TYPING_MONITOR = "key_turn_on_typing_monitor"
        const val KEY_SEND_ORIGINAL_IMAGE_MESSAGE = "key_send_original_image_message"
        const val KEY_THREAD_MESSAGE_FLAG = "key_thread_message_flag"
    }

    companion object {
        const val TAG = "UIKitChatFragment"
        const val REQUEST_CODE_CAMERA = 2
        const val REQUEST_CODE_LOCAL = 3
        const val REQUEST_CODE_DING_MSG = 4
        const val REQUEST_CODE_SELECT_VIDEO = 11
        const val REQUEST_CODE_SELECT_FILE = 12
    }

}