package io.agora.chat.uikit.feature.group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.dialog.CustomDialog
import io.agora.chat.uikit.common.enums.ChatUIKitGroupMemberType
import io.agora.chat.uikit.common.extensions.isOwner
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.databinding.UikitActivityGroupMemberLayoutBinding
import io.agora.chat.uikit.feature.contact.ChatUIKitContactCheckActivity
import io.agora.chat.uikit.feature.group.fragments.ChatUIKitGroupAddMemberFragment
import io.agora.chat.uikit.feature.group.fragments.ChatUIKitGroupMemberFragment
import io.agora.chat.uikit.feature.group.fragments.ChatUIKitGroupRemoveMemberFragment
import io.agora.chat.uikit.feature.group.interfaces.IUIKitGroupResultView
import io.agora.chat.uikit.feature.group.interfaces.IGroupMemberEventListener
import io.agora.chat.uikit.interfaces.ChatUIKitContactListener
import io.agora.chat.uikit.interfaces.ChatUIKitGroupListener
import io.agora.chat.uikit.interfaces.OnContactSelectedListener
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.getNickname
import io.agora.chat.uikit.viewmodel.group.ChatUIKitGroupViewModel
import io.agora.chat.uikit.viewmodel.group.IGroupRequest

open class ChatUIKitGroupMembersListActivity:ChatUIKitBaseActivity<UikitActivityGroupMemberLayoutBinding>(),
    View.OnClickListener , IUIKitGroupResultView, IGroupMemberEventListener{
    private var groupId:String? =""
    private var actionType: ChatUIKitGroupMemberType? = ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL
    private var group:ChatGroup?=null
    private var memberFragment: ChatUIKitGroupMemberFragment? = null
    private var addMemberFragment: ChatUIKitGroupAddMemberFragment? = null
    private var removeMemberFragment: ChatUIKitGroupRemoveMemberFragment? = null
    private var mCurrentFragment: Fragment? = null
    private var selectData:MutableList<String> = mutableListOf()
    private var data:MutableList<ChatUIKitUser> = mutableListOf()
    private var groupViewModel: IGroupRequest? = null
    private var oldType: ChatUIKitGroupMemberType = ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL

    private val groupChangeListener = object : ChatUIKitGroupListener() {

        override fun onGroupDestroyed(groupId: String?, groupName: String?) {
            finish()
        }

        override fun onMemberJoined(groupId: String?, member: String?) {
            ChatLog.e(TAG,"onMemberJoined $groupId $actionType $member")
            when(actionType){
                ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL -> {
                    if (this@ChatUIKitGroupMembersListActivity.groupId == groupId ){
                        memberFragment?.loadLocalData()
                    }

                }
                ChatUIKitGroupMemberType.GROUP_MEMBER_REMOVE -> {
                    if (this@ChatUIKitGroupMembersListActivity.groupId == groupId ){
                        removeMemberFragment?.resetSelect()
                        removeMemberFragment?.loadData()
                    }
                }
                else -> {}
            }

        }

        override fun onMemberExited(groupId: String?, member: String?) {
            ChatLog.e(TAG,"onMemberExited $groupId $actionType $member")
            when(actionType){
                ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL -> {
                    if (this@ChatUIKitGroupMembersListActivity.groupId == groupId ){
                        memberFragment?.loadLocalData()
                    }
                }
                ChatUIKitGroupMemberType.GROUP_MEMBER_REMOVE -> {
                    if (this@ChatUIKitGroupMembersListActivity.groupId == groupId ){
                        removeMemberFragment?.resetSelect()
                        removeMemberFragment?.loadData()
                    }
                }
                else -> {}
            }
        }
    }

    private val contactListener = object : ChatUIKitContactListener() {

        override fun onContactDeleted(username: String?) {
           if (
               actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_ADD ||
               actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_REMOVE
           ){
               selectData.clear()
               addMemberFragment?.resetSelect()
               addMemberFragment?.loadLocalData()
           }
        }

        override fun onContactAdded(username: String?) {
            if (actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_ADD){
                selectData.clear()
                addMemberFragment?.resetSelect()
                addMemberFragment?.loadLocalData()
            }
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): UikitActivityGroupMemberLayoutBinding? {
       return UikitActivityGroupMemberLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkIfShowSavedFragment(savedInstanceState)
        groupId = intent.getStringExtra(ChatUIKitConstant.EXTRA_CONVERSATION_ID)
        val code = intent.getIntExtra(KEY_ACTION_TYPE, ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL.ordinal)
        actionType = ChatUIKitGroupMemberType.values()[code]
        group = ChatClient.getInstance().groupManager().getGroup(groupId)
        initView()
        initViewModel()
        initListener()
    }


    open fun initView(){
        changeToMemberList()
        if (actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_CHANGE_OWNER){
            updateChangeOwnerLayout()
        }else{
            updateNormalLayout()
        }
    }

    private fun initViewModel(){
        groupViewModel = ViewModelProvider(this)[ChatUIKitGroupViewModel::class.java]
        groupViewModel?.attachView(this)
    }

    private fun initListener(){
        binding.titleBack.setOnClickListener(this)
        binding.itemAdd.setOnClickListener(this)
        binding.itemRemove.setOnClickListener(this)
        binding.tvRightAdd.setOnClickListener(this)
        binding.tvRightRemove.setOnClickListener(this)
        ChatUIKitClient.addGroupChangeListener(groupChangeListener)
        ChatUIKitClient.addContactListener(contactListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatUIKitClient.removeGroupChangeListener(groupChangeListener)
        ChatUIKitClient.removeContactListener(contactListener)
    }

    open fun changeToMemberList(){
        binding.sideBarContact.visibility = View.GONE
        if (memberFragment == null){
            memberFragment = ChatUIKitGroupMemberFragment()
        }
        val bundle = Bundle()
        bundle.putString(ChatUIKitConstant.EXTRA_CONVERSATION_ID, groupId)
        memberFragment?.arguments = bundle
        memberFragment?.setOnGroupMemberItemClickListener(this)
        replace(memberFragment, "normal_member")
    }

    open fun changeToAddMemberList(){
        val memberList = data.map { it.userId }.toMutableList()
        if (addMemberFragment == null){
            addMemberFragment = ChatUIKitGroupAddMemberFragment()
        }
        addMemberFragment?.setAddSelectListener(object : OnContactSelectedListener{
            override fun onContactSelectedChanged(v: View, selectedMembers: MutableList<String>) {
                this@ChatUIKitGroupMembersListActivity.selectData = selectedMembers
                updateCount()
            }

            override fun onSearchSelectedResult(selectedMembers: MutableList<String>) {
                selectedMembers.forEach { id->
                    if (!this@ChatUIKitGroupMembersListActivity.selectData.contains(id)){
                        this@ChatUIKitGroupMembersListActivity.selectData.add(id)
                    }
                }
                updateCount()
            }
        })
        val bundle = Bundle()
        bundle.putString(ChatUIKitConstant.EXTRA_CONVERSATION_ID, groupId)
        addMemberFragment?.arguments = bundle
        addMemberFragment?.setMemberList(memberList)
        addMemberFragment?.addSelectMember(this@ChatUIKitGroupMembersListActivity.selectData)
        addMemberFragment?.setSideBar(binding.sideBarContact)
        replace(addMemberFragment, "add_member")
    }

    open fun changeToRemoveMemberList(){
        val memberList = mutableListOf<String>()
        group?.owner?.let {
            memberList.add(it)
        }
        if (removeMemberFragment == null){
            removeMemberFragment = ChatUIKitGroupRemoveMemberFragment()
        }
        removeMemberFragment?.setRemoveSelectListener(object : OnContactSelectedListener{
            override fun onContactSelectedChanged(v: View, selectedMembers: MutableList<String>) {
                this@ChatUIKitGroupMembersListActivity.selectData = selectedMembers
                updateCount()
            }

            override fun onSearchSelectedResult(selectedMembers: MutableList<String>) {
                selectedMembers.forEach { id->
                    if (!this@ChatUIKitGroupMembersListActivity.selectData.contains(id)){
                        this@ChatUIKitGroupMembersListActivity.selectData.add(id)
                    }
                }
                updateCount()
            }
        })
        val bundle = Bundle()
        bundle.putString(ChatUIKitConstant.EXTRA_CONVERSATION_ID, groupId)
        removeMemberFragment?.arguments = bundle
        removeMemberFragment?.setMemberList(memberList)
        removeMemberFragment?.addSelectList(this@ChatUIKitGroupMembersListActivity.selectData)
        removeMemberFragment?.setSideBar(binding.sideBarContact)
        replace(removeMemberFragment, "remove_member")
    }

    private fun replace(fragment: Fragment?, tag: String) {
        if (fragment != null && mCurrentFragment !== fragment) {
            val t = supportFragmentManager.beginTransaction()
            mCurrentFragment?.let {
                t.hide(it)
            }
            mCurrentFragment = fragment
            if (!fragment.isAdded) {
                t.add(R.id.fl_fragment, fragment, tag).show(fragment).commit()
            } else {
                t.show(fragment).commit()
            }
        }
    }

    /**
     * Check if have fragment exited
     * @param savedInstanceState
     */
    private fun checkIfShowSavedFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            val tag = savedInstanceState.getString("tag")
            if (!TextUtils.isEmpty(tag)) {
                val fragment = supportFragmentManager.findFragmentByTag(tag)
                replace(fragment, tag!!)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mCurrentFragment?.let {
            outState.putString("tag", it.tag)
        }
    }

    companion object {
        private const val TAG = "EaseGroupMemberListActivity"
        private const val KEY_ACTION_TYPE = "action_type"
        fun createIntent(
            context: Context,
            groupId: String,
            type: ChatUIKitGroupMemberType? = ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL
        ): Intent {
            val intent = Intent(context, ChatUIKitGroupMembersListActivity::class.java)
            intent.putExtra(ChatUIKitConstant.EXTRA_CONVERSATION_ID, groupId)
            intent.putExtra(KEY_ACTION_TYPE,type?.ordinal)
            return intent
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.title_back -> {
                if (actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_ADD ||
                    actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_REMOVE){
                    if (actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_ADD){
                        addMemberFragment?.resetSelect()
                    }else if (actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_REMOVE){
                        removeMemberFragment?.resetSelect()
                    }
                    actionType = ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL
                    changeToMemberList()
                    updateNormalLayout()
                    updateCount()
                }else{
                    finish()
                }
            }
            R.id.item_add -> {
                if (oldType != ChatUIKitGroupMemberType.GROUP_MEMBER_ADD){
                    selectData.clear()
                }
                oldType = ChatUIKitGroupMemberType.GROUP_MEMBER_ADD
                actionType = ChatUIKitGroupMemberType.GROUP_MEMBER_ADD
                changeToAddMemberList()
                updateAddLayout()
                updateCount()
            }
            R.id.item_remove -> {
                if (oldType != ChatUIKitGroupMemberType.GROUP_MEMBER_REMOVE){
                    selectData.clear()
                }
                oldType = ChatUIKitGroupMemberType.GROUP_MEMBER_REMOVE
                actionType = ChatUIKitGroupMemberType.GROUP_MEMBER_REMOVE
                changeToRemoveMemberList()
                updateRemoveLayout()
                updateCount()
            }
            R.id.tv_right_add -> {
                actionType = ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL
                submitAddSelection()
            }
            R.id.tv_right_remove -> {
                if (selectData.size > 0){
                    showDeleteMemberDialog()
                }
            }
            else ->{}
        }
    }

    open fun updateNormalLayout(){
        binding.tvRightAdd.visibility = View.GONE
        binding.tvRightRemove.visibility = View.GONE
        group?.let {g->
            if (g.isOwner()){
                binding.itemAdd.visibility = View.VISIBLE
                binding.itemRemove.visibility = View.VISIBLE
            }else{
                binding.itemAdd.visibility = View.GONE
                binding.itemRemove.visibility = View.GONE
            }
        }
    }

    open fun updateAddLayout(){
        binding.itemAdd.visibility = View.GONE
        binding.itemRemove.visibility = View.GONE
        binding.tvRightRemove.visibility = View.GONE
        binding.tvTitle.text = resources.getString(R.string.uikit_add_group_member_title)
        binding.tvRightAdd.visibility = View.VISIBLE
    }

    open fun updateRemoveLayout(){
        binding.itemAdd.visibility = View.GONE
        binding.itemRemove.visibility = View.GONE
        binding.tvRightAdd.visibility = View.GONE
        binding.tvTitle.text = resources.getString(R.string.uikit_remove_group_member_title)
        binding.tvRightRemove.visibility = View.VISIBLE
    }

    open fun updateChangeOwnerLayout(){
        binding.itemAdd.visibility = View.GONE
        binding.itemRemove.visibility = View.GONE
        binding.tvRightAdd.visibility = View.GONE
        binding.itemRemove.visibility = View.GONE
        binding.tvTitle.text = resources.getString(R.string.uikit_group_change_owner_title)
    }

    open fun updateCount(){
        when(actionType){
            ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL -> {
                binding.tvTitle.text = resources.getString(R.string.uikit_group_member_count,data.size)
            }
            ChatUIKitGroupMemberType.GROUP_MEMBER_ADD -> {
                binding.tvRightAdd.isSelected = selectData.size > 0
                binding.tvRightAdd.text = resources.getString(R.string.uikit_add_group_member_select_count,selectData.size)
            }
            ChatUIKitGroupMemberType.GROUP_MEMBER_REMOVE -> {
                binding.tvRightRemove.isSelected = selectData.size > 0
                binding.tvRightRemove.text = resources.getString(R.string.uikit_remove_group_member_select_count,selectData.size)
            }
            else -> {}
        }
    }


    private fun submitAddSelection(){
        groupId?.let {
            if (selectData.size > 0){
                groupViewModel?.addGroupMember(it,selectData)
            }
        }
    }

    private fun submitRemoveSelection(){
        groupId?.let {
            if (selectData.size > 0){
                groupViewModel?.removeGroupMember(it,selectData)
            }
        }
    }

    open fun showChangeOwnerDialog(userId:String){
        var title = userId
        ChatUIKitProfile.getGroupMember(groupId,userId)?.let {
            title = it.getRemarkOrName()
        }
        val dialog = CustomDialog(
            context = this@ChatUIKitGroupMembersListActivity,
            title = resources.getString(R.string.uikit_group_change_owner,title),
            isEditTextMode = false,
            onLeftButtonClickListener = {
                changeOwnerDialogLeftClick()
            },
            onRightButtonClickListener = {
                changeOwnerDialogRightClick(userId)
            }
        )
        dialog.show()
    }

    open fun showDeleteMemberDialog(){
        var title = "" ; var example = ""
        if (selectData.size == 1){
            example = getString(R.string.uikit_group_remove_members)
            title = String.format(example, getGroupNameRule())
        }else if (selectData.size == 2){
            example = getString(R.string.uikit_group_remove_members_2)
            title = String.format(example, getGroupNameRule())
        }else if (selectData.size >= 3){
            example = getString(R.string.uikit_group_remove_members_more_than_3)
            title = String.format(example, getGroupNameRule(), selectData.size)
        }
        val dialogDelete = CustomDialog(
            context = this@ChatUIKitGroupMembersListActivity,
            title = title,
            isEditTextMode = false,
            onLeftButtonClickListener = {

            },
            onRightButtonClickListener = {
                actionType = ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL
                submitRemoveSelection()
            }
        )
        dialogDelete.show()
    }

    private fun getGroupNameRule():String{
        val data = selectData
        data.map {
            ChatUIKitClient.getUserProvider()?.getUser(it)?.toUser()?.getNickname()?:it
        }.let {
            return if (it.size >= 3){
                it.take(3).joinToString(",")
            }else{
                if (it.isEmpty()){
                    ChatUIKitClient.getCurrentUser()?.name ?: ChatUIKitClient.getCurrentUser()?.id.toString()
                }else{
                    it.take(it.size).joinToString(",")
                }
            }
        }
    }

    open fun changeOwnerDialogRightClick(userId:String){
        groupId?.let {
            groupViewModel?.changeChatGroupOwner(it,userId)
        }
    }

    open fun changeOwnerDialogLeftClick(){

    }

    override fun addGroupMemberSuccess() {
        selectData.clear()
        addMemberFragment?.resetSelect()
        removeMemberFragment?.loadData()
        memberFragment?.loadLocalData()
        changeToMemberList()
        updateNormalLayout()
    }

    override fun addGroupMemberFail(code: Int, error: String) {
        addMemberFragment?.resetSelect()
        changeToMemberList()
        updateNormalLayout()
    }

    override fun removeChatGroupMemberSuccess() {
        selectData.clear()
        removeMemberFragment?.resetSelect()
        removeMemberFragment?.loadData()
        memberFragment?.loadLocalData()
        changeToMemberList()
        updateNormalLayout()
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE + ChatUIKitEvent.TYPE.GROUP + ChatUIKitEvent.TYPE.CONTACT)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitConstant.EVENT_REMOVE_GROUP_MEMBER, ChatUIKitEvent.TYPE.GROUP, groupId))
    }

    override fun removeChatGroupMemberFail(code: Int, error: String) {
        ChatLog.e(TAG,"removeChatGroupMemberSuccess $code $error")
        removeMemberFragment?.resetSelect()
        changeToMemberList()
        updateNormalLayout()
    }

    override fun onGroupMemberListItemClick(view: View?, user: ChatUIKitUser) {
        if (actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL ){
            startActivity(
                ChatUIKitContactCheckActivity.createIntent(this@ChatUIKitGroupMembersListActivity,user)
            )
        }else if (actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_CHANGE_OWNER){
            showChangeOwnerDialog(user.userId)
        }
    }

    override fun onGroupMemberLoadSuccess(memberList: MutableList<ChatUIKitUser>) {
        data = memberList
        addMemberFragment?.setMemberList(data.map { it.userId }.toMutableList())
        if (actionType == ChatUIKitGroupMemberType.GROUP_MEMBER_NORMAL){
            updateNormalLayout()
            updateCount()
        }
    }

    override fun changeChatGroupOwnerSuccess() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.GROUP)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitConstant.EVENT_UPDATE_GROUP_OWNER, ChatUIKitEvent.TYPE.GROUP, groupId))
        finish()
    }

    override fun changeChatGroupOwnerFail(code: Int, error: String) {
        finish()
    }

}