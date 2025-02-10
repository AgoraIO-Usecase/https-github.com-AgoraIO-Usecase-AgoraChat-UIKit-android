package io.agora.chat.uikit.feature.contact

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.lifecycle.ViewModelProvider
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseFragment
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.feature.contact.adapter.ChatUIKitContactListAdapter
import io.agora.chat.uikit.common.enums.ChatUIKitListViewType
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.configs.ChatUIKitHeaderItemConfig
import io.agora.chat.uikit.databinding.FragmentContactListLayoutBinding
import io.agora.chat.uikit.feature.contact.adapter.ChatUIKitCustomHeaderAdapter
import io.agora.chat.uikit.feature.contact.interfaces.OnContactEventListener
import io.agora.chat.uikit.feature.contact.interfaces.OnHeaderItemClickListener
import io.agora.chat.uikit.feature.conversation.controllers.ChatUIKitConvDialogController
import io.agora.chat.uikit.feature.group.ChatUIKitGroupListActivity
import io.agora.chat.uikit.feature.invitation.ChatUIKitNewRequestsActivity
import io.agora.chat.uikit.feature.invitation.helper.ChatUIKitNotificationMsgManager
import io.agora.chat.uikit.feature.search.ChatUIKitSearchActivity
import io.agora.chat.uikit.feature.search.ChatUIKitSearchType
import io.agora.chat.uikit.feature.search.interfaces.OnContactSelectListener
import io.agora.chat.uikit.interfaces.ChatUIKitContactListener
import io.agora.chat.uikit.interfaces.OnContactSelectedListener
import io.agora.chat.uikit.interfaces.OnItemLongClickListener
import io.agora.chat.uikit.interfaces.OnUserListItemClickListener
import io.agora.chat.uikit.model.ChatUIKitCustomHeaderItem
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.viewmodel.contacts.ChatUIKitContactListViewModel
import kotlinx.coroutines.launch

open class ChatUIKitContactsListFragment: ChatUIKitBaseFragment<FragmentContactListLayoutBinding>(),
    OnItemLongClickListener, OnContactEventListener{
    private var adapter: ChatUIKitContactListAdapter? = null
    private var headerAdapter:ChatUIKitCustomHeaderAdapter?=null
    private var headerItemClickListener: OnHeaderItemClickListener? = null
    private var userListItemClickListener: OnUserListItemClickListener? = null
    private var itemLongClickListener: OnItemLongClickListener? = null
    private var contactSelectedListener:OnContactSelectedListener?=null
    private var backPressListener: View.OnClickListener? = null
    private var viewType: ChatUIKitListViewType? = ChatUIKitListViewType.LIST_CONTACT
    private var headerList:List<ChatUIKitCustomHeaderItem>? = null
    private var searchType: ChatUIKitSearchType? = null
    private var selectedMembers:MutableList<String> = mutableListOf()
    private val contactViewModel by lazy { ViewModelProvider(this)[ChatUIKitContactListViewModel::class.java] }
    val dialogController by lazy { ChatUIKitConvDialogController(mContext, this) }

    private val returnSearchClickResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onClickResult(result) }

    private val contactListener = object : ChatUIKitContactListener() {
        override fun onContactAdded(username: String?) {
            refreshData()
        }

        override fun onContactDeleted(username: String?) {
            refreshData()
        }

        override fun onContactInvited(username: String?, reason: String?) {
            refreshRequest()
        }

        override fun onFriendRequestAccepted(username: String?) {
            refreshData()
            refreshRequest()
        }

        override fun onFriendRequestDeclined(username: String?) {
            refreshRequest()
        }
    }

    companion object{
        const val TAG:String = "ContactsList"
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentContactListLayoutBinding {
        return FragmentContactListLayoutBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        arguments?.run {
            binding?.run {
                titleContact.visibility = if (getBoolean(Constant.KEY_USE_TITLE, false)) View.VISIBLE else View.GONE
                getString(Constant.KEY_SET_TITLE)?.let {
                    if (it.isNotEmpty()) {
                        titleContact.setTitle(it)
                    }
                }
                titleContact.setDisplayHomeAsUpEnabled(getBoolean(Constant.KEY_ENABLE_BACK, false)
                    , getBoolean(Constant.KEY_USE_TITLE_REPLACE, false))
                titleContact.setNavigationOnClickListener {
                    backPressListener?.onClick(it) ?: activity?.finish()
                }
                searchBar.visibility = if (getBoolean(Constant.KEY_USE_SEARCH, false)) View.VISIBLE else View.GONE
                if (getBoolean(Constant.KEY_SHOW_ITEM_HEADER, false)) {
                    if (headerList.isNullOrEmpty()) headerList = ChatUIKitHeaderItemConfig(mContext).getDefaultHeaderItemModels()
                    if (headerAdapter == null) {
                        headerAdapter = ChatUIKitCustomHeaderAdapter()
                    }
                    headerAdapter?.let {
                        it.setHasStableIds(true)
                        listContact.addHeaderAdapter(it)
                        if (headerList?.isNotEmpty() == true){
                            updateRequestCount()
                        }
                    }
                }
                getInt(Constant.KEY_EMPTY_LAYOUT, -1).takeIf { it != -1 }?.let {
                    listContact.getListAdapter()?.setEmptyView(it)
                }
                getString(Constant.KEY_SEARCH_TYPE)?.let {
                    searchType = ChatUIKitSearchType.valueOf(it)
                }

                listContact.setListViewType(viewType)
                val isShowSidebar = getBoolean(Constant.KEY_SIDEBAR_VISIBLE,true)
                listContact.setSideBarVisible(isShowSidebar)

                defaultMenu()

                if (!getBoolean(Constant.KEY_DEFAULT_MENU_VISIBLE,true)){
                    titleContact.hideDefaultMenu()
                }
            }
        }?:kotlin.run {
            refreshData()
        }
    }

    override fun initListener() {

        setMenuItemClickListener()

        binding?.searchBar?.setOnClickListener {
            returnSearchClickResult.launch(
                ChatUIKitSearchActivity.createIntent(
                    context = mContext,
                    searchType = searchType
                )
            )
        }

        headerAdapter?.setOnHeaderItemClickListener(object : OnHeaderItemClickListener {
            override fun onHeaderItemClick(v: View, itemIndex: Int,itemId:Int?) {
                if (headerItemClickListener != null){
                    headerItemClickListener?.onHeaderItemClick(v,itemIndex,itemId)
                    return
                }
                when(itemId){
                    R.id.ease_contact_header_new_request -> {
                        ChatUIKitNotificationMsgManager.getInstance().markAllMessagesAsRead()
                        refreshRequest()
                        startActivity(ChatUIKitNewRequestsActivity.createIntent(mContext))
                    }
                    R.id.uikit_contact_header_group -> {
                        ChatUIKitGroupListActivity.actionStart(mContext)
                    }
                    else -> {}
                }
            }
        })

        binding?.listContact?.getListAdapter()?.setOnUserListItemClickListener(object : OnUserListItemClickListener{
            override fun onUserListItemClick(v: View?, position: Int, user: ChatUIKitUser?) {
                if (userListItemClickListener != null){
                    userListItemClickListener?.onUserListItemClick(v, position, user) ?: defaultAction(user)
                    return
                }
                user?.let {
                    startActivity(
                        ChatUIKitContactDetailsActivity.createIntent(
                            context = mContext,
                            user = it,
                        )
                    )
                }
            }

            override fun onAvatarClick(v: View, position: Int) {
                if (userListItemClickListener != null){
                    userListItemClickListener?.onAvatarClick(v, position)
                    return
                }
            }
        })

        binding?.listContact?.getListAdapter()?.setCheckBoxSelectListener(object : OnContactSelectListener{
            override fun onContactSelectedChanged(v: View, userId: String, isSelected: Boolean) {
                if (isSelected){
                    if (!selectedMembers.contains(userId)){
                        selectedMembers.add(userId)
                    }
                }else{
                    if (selectedMembers.contains(userId)){
                        selectedMembers.remove(userId)
                    }
                }
                contactSelectedListener?.onContactSelectedChanged(v,selectedMembers)
            }
        })

        binding?.listContact?.setOnItemLongClickListener(this)
        binding?.listContact?.setLoadContactListener(this)

        ChatUIKitClient.addContactListener(contactListener)
    }

    private fun defaultAction(user: ChatUIKitUser?) {
        user?.run {
            if (searchType == null) {
                startActivity(ChatUIKitContactDetailsActivity.createIntent(mContext, this))
            }
        }
    }

    open fun defaultMenu(){
        binding?.titleContact?.inflateMenu(R.menu.menu_new_request_add_contact)
    }

    private fun setMenuItemClickListener() {
        binding?.titleContact?.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener setMenuItemClick(it)
        }
    }

    open fun setMenuItemClick(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_add_contact -> {
                dialogController.showAddContactDialog { content ->
                    if (content.isNotEmpty()) {
                        contactViewModel.addContact(content)
                    }
                }
                return true
            }
            else -> return false
        }
    }

    override fun initData() {
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name).register(viewLifecycleOwner) { event ->
            if (event.isContactChange) {
                refreshData()
            }
        }
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name).register(viewLifecycleOwner) { event ->
            if (event.isContactChange) {
                refreshData()
            }
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).register(viewLifecycleOwner) {
            if (it.isNotifyChange) {
                refreshRequest()
            }
        }
    }

    private fun refreshData() {
        binding?.listContact?.loadContactData(false)
    }

    private fun refreshRequest() {
        updateRequestCount()
    }

    private fun onClickResult(result: ActivityResult) {
        if (result.resultCode == RESULT_OK) {
            result.data?.getSerializableExtra(Constant.KEY_USER)?.let {
                if (it is ChatUIKitUser) {
                    userListItemClickListener?.onUserListItemClick(null, -1, it) ?: defaultAction(it)
                }
            }
            result.data?.getStringArrayListExtra(Constant.KEY_SELECT_USER)?.let { selectMembers->
                val adapter = binding?.listContact?.getListAdapter()
                for (selectMember in selectMembers) {
                    if (!selectedMembers.contains(selectMember)){
                        selectedMembers.add(selectMember)
                    }
                }
                adapter?.setSelectedMembers(selectedMembers)
            }
        }
    }

    fun fetchContactInfo(visibleList:List<ChatUIKitUser>?){
        binding?.listContact?.fetchContactInfo(visibleList)
    }

    override fun loadContactListSuccess(userList: MutableList<ChatUIKitUser>) {

    }

    override fun loadContactListFail(code: Int, error: String) {

    }

    override fun addContactFail(code: Int, error: String) {
        ChatLog.e(TAG,"addContactFail $code $error")
    }

    override fun onItemLongClick(view: View?, position: Int): Boolean {
        return itemLongClickListener?.onItemLongClick(view, position) ?: false
    }

    private fun setCustomAdapter(adapter: ChatUIKitContactListAdapter?) {
        this.adapter = adapter
    }

    private fun setOnBackPressListener(backPressListener: View.OnClickListener?) {
        this.backPressListener = backPressListener
    }

    private fun setOnItemLongClickListener(itemLongClickListener: OnItemLongClickListener?) {
        this.itemLongClickListener = itemLongClickListener
    }

    private fun setListViewType(viewType: ChatUIKitListViewType?){
        this.viewType = viewType
    }

    private fun setHeaderAdapter(headerAdapter:ChatUIKitCustomHeaderAdapter?){
        this.headerAdapter = headerAdapter
    }

    private fun setOnHeaderItemClickListener(listener: OnHeaderItemClickListener?){
        this.headerItemClickListener = listener
    }

    private fun setOnUserListItemClickListener(listener:OnUserListItemClickListener?){
        this.userListItemClickListener = listener
    }

    private fun setHeaderItemList(headerList:List<ChatUIKitCustomHeaderItem>?){
        this.headerList = headerList
    }

    private fun setOnContactSelectedListener(listener: OnContactSelectedListener?){
        this.contactSelectedListener = listener
    }

    private fun updateRequestCount(){
        mContext.mainScope().launch {
            headerList?.map {
                if (it.headerTitle == getString(R.string.uikit_contact_header_request)){
                    val systemConversation = ChatUIKitNotificationMsgManager.getInstance().getConversation()
                    systemConversation.let { cv->
                        it.headerUnReadCount = cv.unreadMsgCount
                    }
                }
            }
            headerAdapter?.setData(headerList?.toMutableList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ChatUIKitClient.removeContactListener(contactListener)
    }

    open class Builder {
        private val bundle: Bundle = Bundle()
        protected var customFragment: ChatUIKitContactsListFragment? = null
        protected var adapter: ChatUIKitContactListAdapter? = null
        protected var headerAdapter:ChatUIKitCustomHeaderAdapter? = null
        private var headerItemClickListener: OnHeaderItemClickListener? = null
        private var userListItemClickListener: OnUserListItemClickListener? = null
        private var itemLongClickListener: OnItemLongClickListener? = null
        private var contactSelectedListener:OnContactSelectedListener?=null
        private var backPressListener: View.OnClickListener? = null
        private var viewType: ChatUIKitListViewType? = ChatUIKitListViewType.LIST_CONTACT
        private var headerList:List<ChatUIKitCustomHeaderItem>? = mutableListOf()

        /**
         * Set custom fragment which should extends ChatUIKitContactsListFragment
         * @param fragment
         * @param <T>
         * @return
        </T> */
        fun <T : ChatUIKitContactsListFragment?> setCustomFragment(fragment: T): Builder {
            customFragment = fragment
            return this
        }

        /**
         * Whether to use default titleBar which is [io.agora.chat.uikit.widget.ChatUIKitTitleBar]
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
         * @param title
         * @return
         */
        fun setTitleBarTitle(title: String?): Builder {
            bundle.putString(Constant.KEY_SET_TITLE, title)
            return this
        }

        /**
         * Whether show back icon in titleBar
         * @param canBack
         * @return
         */
        fun enableTitleBarPressBack(canBack: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_ENABLE_BACK, canBack)
            return this
        }

        /**
         * If you have set [ChatUIKitContactsListFragment.Builder.enableTitleBarPressBack], you can set the listener
         * @param listener
         * @return
         */
        fun setTitleBarBackPressListener(listener: View.OnClickListener?): Builder {
            backPressListener = listener
            return this
        }

        /**
         * Whether to use default titleBar which is [io.agora.uikit.widget.ChatUIKitTitleBar]
         * @param isVisible
         * @return
         */
        fun setDefaultMenuVisible(isVisible: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_DEFAULT_MENU_VISIBLE, isVisible)
            return this
        }

        /**
         * Whether to use search bar.
         * @param useSearchBar
         */
        fun useSearchBar(useSearchBar: Boolean): Builder {
            bundle.putBoolean(Constant.KEY_USE_SEARCH, useSearchBar)
            return this
        }

        /**
         * Set the search type for the contact list.
         */
        fun setSearchType(searchType: ChatUIKitSearchType): Builder {
            bundle.putString(Constant.KEY_SEARCH_TYPE, searchType.name)
            return this
        }

        /**
         * Set chat list's empty layout if you want replace the default
         * @param emptyLayout
         * @return
         */
        fun setEmptyLayout(@LayoutRes emptyLayout: Int): Builder {
            bundle.putInt(Constant.KEY_EMPTY_LAYOUT, emptyLayout)
            return this
        }

        /**
         * Set custom adapter which should extends ChatUIKitConversationListAdapter
         * @param adapter
         * @return
         */
        fun setCustomAdapter(adapter: ChatUIKitContactListAdapter?): Builder {
            this.adapter = adapter
            return this
        }

        /**
         * Set list view type
         * @param type
         * @return
         */
        fun setListViewType(type: ChatUIKitListViewType): Builder {
            this.viewType = type
            return this
        }

        /**
         * Set is show side bar
         * @param isVisible
         * @return
         */
        fun setSideBarVisible(isVisible:Boolean):Builder{
            bundle.putBoolean(Constant.KEY_SIDEBAR_VISIBLE,true)
            return this
        }

        /**
         * Set is show list header
         * @param isVisible
         * @return
         */
        fun setHeaderItemVisible(isVisible:Boolean): Builder{
            bundle.putBoolean(Constant.KEY_SHOW_ITEM_HEADER, isVisible)
            return this
        }

        /**
         * Set list header adapter
         * @param headerAdapter
         * @return
         */
        fun setHeaderAdapter(headerAdapter:ChatUIKitCustomHeaderAdapter): Builder{
            this.headerAdapter = headerAdapter
            return this
        }

        /**
         * Set list header item click listener
         * @param listener
         * @return
         */
        fun setOnHeaderItemClickListener(listener: OnHeaderItemClickListener):Builder{
            this.headerItemClickListener = listener
            return this
        }

        /**
         * Set contact list item click listener return ChatUIKitUser
         * @param listener
         * @return
         */
        fun setOnUserListItemClickListener(listener:OnUserListItemClickListener?): Builder{
            this.userListItemClickListener = listener
            return this
        }

        /**
         * Set contact item long click listener
         * @param itemLongClickListener
         */
        fun setOnItemLongClickListener(itemLongClickListener: OnItemLongClickListener?):Builder {
            this.itemLongClickListener = itemLongClickListener
            return this
        }

        /**
         * Set list header item
         * @param headerList
         * @return
         */
        fun setHeaderItemList(headerList:List<ChatUIKitCustomHeaderItem>) : Builder{
            this.headerList = headerList
            return this
        }

        /**
         * Set contact checkbox select listener
         * @param listener
         * @return
         */
        fun setOnContactSelectedListener(listener: OnContactSelectedListener): Builder{
            this.contactSelectedListener = listener
            return this
        }

        open fun build(): ChatUIKitContactsListFragment {
            val fragment =
                if (customFragment != null) customFragment else ChatUIKitContactsListFragment()
            fragment!!.arguments = bundle
            fragment.setCustomAdapter(adapter)
            fragment.setOnBackPressListener(backPressListener)
            fragment.setListViewType(viewType)
            fragment.setHeaderAdapter(headerAdapter)
            fragment.setOnHeaderItemClickListener(headerItemClickListener)
            fragment.setOnUserListItemClickListener(userListItemClickListener)
            fragment.setOnItemLongClickListener(itemLongClickListener)
            fragment.setHeaderItemList(headerList)
            fragment.setOnContactSelectedListener(contactSelectedListener)
            return fragment
        }
    }

    private object Constant {
        const val KEY_USE_TITLE = "key_use_title"
        const val KEY_USE_TITLE_REPLACE = "key_use_replace_action_bar"
        const val KEY_SET_TITLE = "key_set_title"
        const val KEY_USE_SEARCH = "key_use_search"
        const val KEY_SEARCH_TYPE = "key_search_type"
        const val KEY_EMPTY_LAYOUT = "key_empty_layout"
        const val KEY_ENABLE_BACK = "key_enable_back"
        const val KEY_SHOW_ITEM_HEADER = "key_show_item_header"
        const val KEY_SELECT_USER = "select_user"
        const val KEY_USER = "user"
        const val KEY_SIDEBAR_VISIBLE = "key_side_bar_visible"
        const val KEY_DEFAULT_MENU_VISIBLE = "key_default_menu_visible"
    }

}