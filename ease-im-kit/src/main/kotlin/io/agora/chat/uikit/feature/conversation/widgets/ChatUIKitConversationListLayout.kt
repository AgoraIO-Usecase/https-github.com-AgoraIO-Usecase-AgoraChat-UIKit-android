package io.agora.chat.uikit.feature.conversation.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatRecallMessageInfo
import io.agora.chat.uikit.common.RefreshHeader
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.lifecycleScope
import io.agora.chat.uikit.common.impl.OnItemClickListenerImpl
import io.agora.chat.uikit.common.impl.OnItemLongClickListenerImpl
import io.agora.chat.uikit.common.impl.OnMenuItemClickListenerImpl
import io.agora.chat.uikit.databinding.UikitConversationListBinding
import io.agora.chat.uikit.feature.conversation.interfaces.OnLoadConversationListener
import io.agora.chat.uikit.feature.conversation.adapter.ChatUIKitConversationListAdapter
import io.agora.chat.uikit.feature.conversation.config.ChatUIKitConvItemConfig
import io.agora.chat.uikit.feature.conversation.interfaces.IConvItemStyle
import io.agora.chat.uikit.feature.conversation.interfaces.IConversationListLayout
import io.agora.chat.uikit.feature.conversation.interfaces.IUIKitConvListResultView
import io.agora.chat.uikit.feature.conversation.interfaces.OnConversationListChangeListener
import io.agora.chat.uikit.feature.conversation.interfaces.UnreadDotPosition
import io.agora.chat.uikit.feature.conversation.interfaces.UnreadStyle
import io.agora.chat.uikit.feature.conversation.interfaces.IConvMenu
import io.agora.chat.uikit.interfaces.ChatUIKitMessageListener
import io.agora.chat.uikit.interfaces.OnItemClickListener
import io.agora.chat.uikit.interfaces.OnItemLongClickListener
import io.agora.chat.uikit.interfaces.OnMenuDismissListener
import io.agora.chat.uikit.interfaces.OnMenuItemClickListener
import io.agora.chat.uikit.interfaces.OnMenuPreShowListener
import io.agora.chat.uikit.menu.ChatUIKitMenuHelper
import io.agora.chat.uikit.model.ChatUIKitConversation
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.chatConversation
import io.agora.chat.uikit.viewmodel.conversations.ChatUIKitConversationListViewModel
import io.agora.chat.uikit.viewmodel.conversations.IConversationListRequest
import io.agora.chat.uikit.widget.ChatUIKitImageView

class ChatUIKitConversationListLayout @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr), IConversationListLayout, 
    IConvItemStyle, IUIKitConvListResultView, IConvMenu {

    private val binding: UikitConversationListBinding by lazy {
        UikitConversationListBinding.inflate(LayoutInflater.from(context), this, true)
    }
    /**
     * Conversation item configuration
     */
    private lateinit var itemConfig: ChatUIKitConvItemConfig

    private val menuHelper: ChatUIKitMenuHelper by lazy { ChatUIKitMenuHelper() }

    /**
     * Item click listener set by user.
     */
    private var itemClickListener: OnItemClickListener? = null

    /**
     * Item long click listener set by user.
     */
    private var itemLongClickListener: OnItemLongClickListener? = null

    /**
     * Menu pre show listener.
     */
    private var menuPreShowListener: OnMenuPreShowListener? = null

    /**
     * Menu item click listener.
     */
    private var itemMenuClickListener: OnMenuItemClickListener? = null

    /**
     * Conversation list change listener.
     */
    private var conversationChangeListener: OnConversationListChangeListener? = null

    private var listViewModel: IConversationListRequest? = null

    /**
     * Conversation list load listener.
     */
    private var conversationLoadListener: OnLoadConversationListener? = null

    val conversationList: RecyclerView get() = binding.rvList

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
     * Conversation list adapter
     */
    private var listAdapter: ChatUIKitConversationListAdapter? = null

    private val chatMessageListener = object : ChatUIKitMessageListener() {
        override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
            listViewModel?.loadData()
        }

        override fun onMessageRecalledWithExt(recallMessageInfo: MutableList<ChatRecallMessageInfo>?) {
            super.onMessageRecalledWithExt(recallMessageInfo)
            listViewModel?.loadData()
        }
    }
    
    init {
        initAttrs(context, attrs)
        initViews()
        initListener()
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        itemConfig = ChatUIKitConvItemConfig(context, attrs)
    }

    private fun initViews() {
        binding.rvList.layoutManager = LinearLayoutManager(context)
        listAdapter = ChatUIKitConversationListAdapter(itemConfig)
        listAdapter?.setHasStableIds(true)
        concatAdapter.addAdapter(listAdapter!!)
        binding.rvList.adapter = concatAdapter

        // Set refresh layout
        // Can not load more
        binding.refreshLayout.setEnableLoadMore(false)

        if (binding.refreshLayout.refreshHeader == null) {
            binding.refreshLayout.setRefreshHeader(RefreshHeader(context))
        }

        // init view model
        listViewModel = ViewModelProvider(context as AppCompatActivity)[ChatUIKitConversationListViewModel::class.java]
        listViewModel?.attachView(this)
    }

    private fun initListener() {
        binding.refreshLayout.setOnRefreshListener {
            listViewModel?.loadData()
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
                    if (!visibleList.isNullOrEmpty()) {
                        listViewModel?.fetchConvGroupInfo(visibleList)
                        listViewModel?.fetchConvUserInfo(visibleList)
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
            if (itemLongClickListener != null && itemLongClickListener?.onItemLongClick(view, position) == true) {
                return@OnItemLongClickListenerImpl true
            }
            showDefaultMenu(view, position)
            return@OnItemLongClickListenerImpl true
        })

        ChatUIKitClient.addChatMessageListener(chatMessageListener)
    }

    override fun onDetachedFromWindow() {
        menuHelper.setOnMenuDismissListener(null)
        menuHelper.dismiss()
        menuHelper.clear()
        menuHelper.release()
        ChatUIKitClient.removeChatMessageListener(chatMessageListener)
        super.onDetachedFromWindow()
    }
    private fun showDefaultMenu(view: View?, position: Int) {
        val conv = listAdapter?.getItem(position)
        conv?.setSelected(true)
        menuHelper.initMenu(view)
        menuHelper.clear()

        menuHelper.addItemMenu(R.id.ease_action_conv_menu_silent, 0, context.getString(R.string.uikit_conv_menu_item_silent))
        menuHelper.addItemMenu(R.id.ease_action_conv_menu_unsilent, 1, context.getString(R.string.uikit_conv_menu_item_unsilent))
        menuHelper.addItemMenu(R.id.ease_action_conv_menu_pin, 2, context.getString(R.string.uikit_conv_menu_item_pin))
        menuHelper.addItemMenu(R.id.ease_action_conv_menu_unpin, 3, context.getString(R.string.uikit_conv_menu_item_unpin))
        menuHelper.addItemMenu(R.id.ease_action_conv_menu_read, 4, context.getString(R.string.uikit_conv_menu_item_read))
        menuHelper.addItemMenu(R.id.ease_action_conv_menu_delete, 5, context.getString(R.string.uikit_conv_menu_item_delete),
            titleColor = ContextCompat.getColor(context, R.color.ease_color_error))

        menuHelper.findItemVisible(R.id.ease_action_conv_menu_read, false)
        conv?.run {
            chatConversation()?.let {
                if (it.unreadMsgCount > 0) {
                    menuHelper.findItemVisible(R.id.ease_action_conv_menu_read, true)
                }
            }
            if (isSilent()) {
                menuHelper.findItemVisible(R.id.ease_action_conv_menu_silent, false)
                menuHelper.findItemVisible(R.id.ease_action_conv_menu_unsilent, true)
            } else {
                menuHelper.findItemVisible(R.id.ease_action_conv_menu_silent, true)
                menuHelper.findItemVisible(R.id.ease_action_conv_menu_unsilent, false)
            }
            if (isPinned) {
                menuHelper.findItemVisible(R.id.ease_action_conv_menu_pin, false)
                menuHelper.findItemVisible(R.id.ease_action_conv_menu_unpin, true)
            } else {
                menuHelper.findItemVisible(R.id.ease_action_conv_menu_pin, true)
                menuHelper.findItemVisible(R.id.ease_action_conv_menu_unpin, false)
            }
        }

        menuPreShowListener?.let {
            it.onMenuPreShow(menuHelper, position)
        }

        menuHelper.setOnMenuItemClickListener(OnMenuItemClickListenerImpl {
            item, _ ->
            itemMenuClickListener?.let {
                if (it.onMenuItemClick(item, position)) {
                    return@OnMenuItemClickListenerImpl true
                }
            }
            item?.run {
                when (menuId) {
                    R.id.ease_action_conv_menu_silent -> {
                        conv?.let {
                            listViewModel?.makeSilentForConversation(position, conv)
                            return@OnMenuItemClickListenerImpl true
                        }
                    }
                    R.id.ease_action_conv_menu_unsilent -> {
                        conv?.let {
                            listViewModel?.cancelSilentForConversation(position, conv)
                            return@OnMenuItemClickListenerImpl true
                        }
                    }
                    R.id.ease_action_conv_menu_pin -> {
                        conv?.let {
                            listViewModel?.pinConversation(position, it)
                            return@OnMenuItemClickListenerImpl true
                        }
                    }
                    R.id.ease_action_conv_menu_unpin -> {
                        conv?.let {
                            listViewModel?.unpinConversation(position, it)
                            return@OnMenuItemClickListenerImpl true
                        }
                    }
                    R.id.ease_action_conv_menu_read -> {
                        conv?.let {
                            listViewModel?.makeConversionRead(position, it)
                            return@OnMenuItemClickListenerImpl true
                        }
                    }
                    R.id.ease_action_conv_menu_delete -> {
                        conv?.let {
                            listViewModel?.deleteConversation(position, it)
                            return@OnMenuItemClickListenerImpl true
                        }
                    }
                    else -> {
                        return@OnMenuItemClickListenerImpl false
                    }
                }
            }

            return@OnMenuItemClickListenerImpl false
        })

        menuHelper.setOnMenuDismissListener(object : OnMenuDismissListener {
            override fun onDismiss() {
                conv?.setSelected(false)
            }
        })

        menuHelper.show()
    }

    fun loadData() {
        listViewModel?.loadData()
    }

    fun fetchConvUserInfo(visibleList:List<ChatUIKitConversation>){
        listViewModel?.fetchConvUserInfo(visibleList)
    }

    override fun setLoadConversationListener(listener: OnLoadConversationListener) {
       this.conversationLoadListener = listener
    }

    /**
     * Notify data changed
     */
    override fun notifyDataSetChanged() {
        listAdapter?.setConversationItemConfig(itemConfig)
    }

    override fun setItemBackGround(backGround: Drawable?) {
        
    }

    override fun setItemHeight(height: Int) {
        itemConfig.itemHeight = height.toFloat()
        notifyDataSetChanged()
    }

    override fun showUnreadDotPosition(position: UnreadDotPosition) {
        itemConfig.unreadDotPosition = position
        notifyDataSetChanged()
    }

    override fun setUnreadStyle(style: UnreadStyle) {
        itemConfig.unreadStyle = style
        notifyDataSetChanged()
    }

    override fun setAvatarSize(avatarSize: Float) {
        itemConfig.avatarSize = avatarSize.toInt()
        notifyDataSetChanged()
    }

    override fun setAvatarShapeType(shapeType: ChatUIKitImageView.ShapeType) {
        itemConfig.avatarConfig.avatarShape = shapeType
        notifyDataSetChanged()
    }

    override fun setAvatarRadius(radius: Int) {
        itemConfig.avatarConfig.avatarRadius = radius
        notifyDataSetChanged()
    }

    override fun setAvatarBorderWidth(borderWidth: Int) {
        itemConfig.avatarConfig.avatarBorderWidth = borderWidth
        notifyDataSetChanged()
    }

    override fun setAvatarBorderColor(borderColor: Int) {
        itemConfig.avatarConfig.avatarBorderColor = borderColor
        notifyDataSetChanged()
    }

    override fun setNameTextSize(textSize: Int) {
        itemConfig.itemNameTextSize = textSize
        notifyDataSetChanged()
    }

    override fun setNameTextColor(textColor: Int) {
        itemConfig.itemNameTextColor = textColor
        notifyDataSetChanged()
    }

    override fun setMessageTextSize(textSize: Int) {
        itemConfig.itemMessageTextSize = textSize
        notifyDataSetChanged()
    }

    override fun setMessageTextColor(textColor: Int) {
        itemConfig.itemMessageTextColor = textColor
        notifyDataSetChanged()
    }

    override fun setDateTextSize(textSize: Int) {
        itemConfig.itemDateTextSize = textSize
        notifyDataSetChanged()
    }

    override fun setDateTextColor(textColor: Int) {
        itemConfig.itemDateTextColor = textColor
        notifyDataSetChanged()
    }

    override fun setViewModel(viewModel: IConversationListRequest?) {
        this.listViewModel = viewModel
        this.listViewModel?.attachView(this)
    }

    override fun setListAdapter(adapter: ChatUIKitConversationListAdapter?) {
        adapter?.run {
            setHasStableIds(true)
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
            listAdapter!!.setConversationItemConfig(itemConfig)
        }
    }

    override fun getListAdapter(): ChatUIKitConversationListAdapter? {
        return listAdapter
    }

    override fun getItem(position: Int): ChatUIKitConversation? {
        return listAdapter?.getItem(position)
    }

    override fun makeConversionRead(position: Int, info: ChatUIKitConversation?) {
        listViewModel?.makeConversionRead(position, info!!)
    }

    override fun makeConversationTop(position: Int, info: ChatUIKitConversation?) {
        listViewModel?.pinConversation(position, info!!)
    }

    override fun cancelConversationTop(position: Int, info: ChatUIKitConversation?) {
        listViewModel?.unpinConversation(position, info!!)
    }

    override fun deleteConversation(position: Int, info: ChatUIKitConversation?) {
        listViewModel?.deleteConversation(position, info!!)
    }

    override fun setOnConversationChangeListener(listener: OnConversationListChangeListener?) {
        conversationChangeListener = listener
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
        binding.rvList.addItemDecoration(decor)
    }

    override fun removeItemDecoration(decor: RecyclerView.ItemDecoration) {
        binding.rvList.removeItemDecoration(decor)
    }

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemClickListener = listener
    }

    override fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        this.itemLongClickListener = listener
    }

    override fun loadConversationListSuccess(list: List<ChatUIKitConversation>) {
        binding.refreshLayout.finishRefresh()
        listAdapter?.setData(list.toMutableList())
        conversationChangeListener?.notifyAllChange()
        conversationLoadListener?.loadConversationListSuccess(list)
        // Notify to load conversation successfully
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD + ChatUIKitEvent.TYPE.CONVERSATION)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitEvent.EVENT.ADD + ChatUIKitEvent.TYPE.CONVERSATION, ChatUIKitEvent.TYPE.CONVERSATION))

    }

    override fun loadConversationListFail(code: Int, error: String) {
        binding.refreshLayout.finishRefresh()
        conversationLoadListener?.loadConversationListFail(code,error)
    }

    override fun sortConversationListFinish(conversations: List<ChatUIKitConversation>) {
        listAdapter?.setData(conversations.toMutableList())
        conversationLoadListener?.loadConversationListSuccess(conversations)
    }

    override fun makeConversionReadSuccess(position: Int, conversation: ChatUIKitConversation) {
        listViewModel?.loadData()
        conversationChangeListener?.notifyItemChange(position, conversation.conversationId)
    }

    override fun pinConversationSuccess(position: Int, conversation: ChatUIKitConversation) {
        listViewModel?.loadData()
        conversationChangeListener?.notifyAllChange()
    }

    override fun pinConversationFail(conversation: ChatUIKitConversation, code: Int, error: String) {

    }

    override fun unpinConversationSuccess(position: Int, conversation: ChatUIKitConversation) {
        listViewModel?.loadData()
        conversationChangeListener?.notifyAllChange()
    }

    override fun unpinConversationFail(conversation: ChatUIKitConversation, code: Int, error: String) {

    }

    override fun deleteConversationSuccess(position: Int, conversation: ChatUIKitConversation) {
        conversationChangeListener?.notifyItemRemove(position, conversation.conversationId)
        listViewModel?.loadData()
    }

    override fun deleteConversationFail(conversation: ChatUIKitConversation, code: Int, error: String) {

    }

    override fun makeSilentForConversationSuccess(position: Int, conversation: ChatUIKitConversation) {
        conversationChangeListener?.notifyItemRemove(position, conversation.conversationId)
        listAdapter?.notifyItemChanged(position)
    }

    override fun makeSilentForConversationFail(
        conversation: ChatUIKitConversation,
        errorCode: Int,
        description: String?
    ) {

    }

    override fun cancelSilentForConversationSuccess(position: Int, conversation: ChatUIKitConversation) {
        conversationChangeListener?.notifyItemRemove(position, conversation.conversationId)
        listAdapter?.notifyItemChanged(position)
    }

    override fun cancelSilentForConversationFail(
        conversation: ChatUIKitConversation,
        errorCode: Int,
        description: String?
    ) {

    }

    override fun fetchConversationInfoByUserSuccess(profiles: List<ChatUIKitProfile>?) {
        if (!profiles.isNullOrEmpty()) notifyDataSetChanged()
    }

    override fun clearMenu() {
        menuHelper.clear()
    }

    override fun addItemMenu(itemId: Int, order: Int, title: String, groupId: Int) {
        menuHelper.addItemMenu(itemId, order, title, groupId)
    }

    override fun findItemVisible(id: Int, visible: Boolean) {
        menuHelper.findItemVisible(id, visible)
    }

    override fun setOnMenuPreShowListener(preShowListener: OnMenuPreShowListener?) {
        menuPreShowListener = preShowListener
    }

    override fun setOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
        itemMenuClickListener = listener
    }

    override fun getConvMenuHelper(): ChatUIKitMenuHelper {
        return menuHelper
    }

}