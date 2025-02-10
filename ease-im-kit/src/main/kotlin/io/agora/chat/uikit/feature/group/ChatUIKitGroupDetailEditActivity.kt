package io.agora.chat.uikit.feature.group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
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
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.databinding.UikitLayoutGroupDetailEditBinding
import io.agora.chat.uikit.feature.group.interfaces.IUIKitGroupResultView
import io.agora.chat.uikit.interfaces.ChatUIKitGroupListener
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitGroupProfile
import io.agora.chat.uikit.viewmodel.group.ChatUIKitGroupViewModel
import io.agora.chat.uikit.viewmodel.group.IGroupRequest

open class ChatUIKitGroupDetailEditActivity:ChatUIKitBaseActivity<UikitLayoutGroupDetailEditBinding>(),
    IUIKitGroupResultView {
    private var type:EditType = EditType.ACTION_EDIT_GROUP_UN_KNOW
    private var groupViewModel: IGroupRequest? = null
    private var groupId:String? = ""
    private var groupNickName:String?=""
    private var threadName:String?=""
    private var threadId:String?=""
    private var group: ChatGroup? = null

    private val groupChangeListener = object : ChatUIKitGroupListener() {

        override fun onGroupDestroyed(groupId: String?, groupName: String?) {
            finish()
        }

    }

    override fun getViewBinding(inflater: LayoutInflater): UikitLayoutGroupDetailEditBinding? {
        return UikitLayoutGroupDetailEditBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val index = intent.getIntExtra(KEY_GROUP_EDIT_TYPE, -1)
        groupId = intent.getStringExtra(ChatUIKitConstant.EXTRA_CONVERSATION_ID)
        groupId?.let {
            group = ChatClient.getInstance().groupManager().getGroup(it)
        }
        threadId = intent.getStringExtra(KEY_THREAD_ID)
        threadName = intent.getStringExtra(KEY_THREAD_NAME)
        type = EditType.values()[index]
        if (type == EditType.ACTION_EDIT_GROUP_ATTRIBUTE){
            groupNickName = intent.getStringExtra(KEY_GROUP_NICKNAME)
        }else if (type == EditType.ACTION_EDIT_THREAD_NAME){
            threadName = intent.getStringExtra(KEY_THREAD_NAME)
        }
        groupViewModel = ViewModelProvider(this)[ChatUIKitGroupViewModel::class.java]
        groupViewModel?.attachView(this)
        initView()
        initListener()
    }

    private fun initView(){
        when(type){
            EditType.ACTION_EDIT_GROUP_NAME -> {
                binding.editNameLayout.visibility = View.VISIBLE
                binding.etName.setText(group?.groupName ?: "")
                binding.etName.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(32))
                binding.inputNameCount.text = resources.getString(R.string.uikit_group_change_name_count,group?.groupName?.length ?: 0)
                binding.titleBar.setTitle(resources.getString(R.string.uikit_group_change_name))
                showSoftInput(binding.etName)
                updateSaveView(binding.etName.text.length)
            }
            EditType.ACTION_EDIT_THREAD_NAME -> {
                binding.editNameLayout.visibility = View.VISIBLE
                binding.etName.setText(threadName?:"")
                binding.etName.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(64))
                binding.inputNameCount.text = getString(R.string.uikit_thread_edit_name_count,threadName?.length ?: 0)
                binding.titleBar.setTitle(getString(R.string.uikit_thread_edit_title))
                showSoftInput(binding.etName)
                updateSaveView(binding.etName.text.length)
            }
            EditType.ACTION_EDIT_GROUP_DESCRIBE -> {
                binding.editDescribeLayout.visibility = View.VISIBLE
                binding.etDescribe.setText(group?.description ?: "")
                binding.inputDescribeCount.text = resources.getString(R.string.uikit_group_change_describe_count,group?.description?.length ?: 0)
                binding.titleBar.setTitle(resources.getString(R.string.uikit_group_change_describe))
                showSoftInput(binding.etDescribe)
                updateSaveView(binding.etDescribe.text.length)
            }
            EditType.ACTION_EDIT_GROUP_ATTRIBUTE -> {
                binding.editAttributeLayout.visibility = View.VISIBLE
                binding.inputAttributeCount.text = resources.getString(R.string.uikit_group_change_name_count,groupNickName?.length ?: 0)
                binding.etAttribute.setText(groupNickName ?: "")
                binding.titleBar.setTitle(resources.getString(R.string.uikit_group_detail_my_notes))
                groupNickName?.let {
                    binding.etAttribute.setText(it)
                }
                updateSaveView(binding.etAttribute.text.length)
            }
            else -> {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatUIKitClient.removeGroupChangeListener(groupChangeListener)
    }

    private fun initListener(){
        ChatUIKitClient.addGroupChangeListener(groupChangeListener)
        binding.titleBar.setNavigationOnClickListener { mContext.onBackPressed() }
        binding.titleBar.setOnMenuItemClickListener { item ->
            when(item.itemId) {
                R.id.action_save -> {
                    when(type){
                        EditType.ACTION_EDIT_GROUP_NAME -> {
                            groupId?.let {
                                if (binding.etName.text.isNotEmpty()){
                                    groupViewModel?.changeChatGroupName(
                                        groupId = it,
                                        newName = binding.etName.text.trim().toString()
                                    )
                                }
                            }
                        }
                        EditType.ACTION_EDIT_THREAD_NAME -> {
                            val changeName = binding.etName.text.trim().toString()
                            threadId?.let {
                                if (changeName.isNotEmpty()){
                                    groupViewModel?.updateChatThreadName(
                                        chatThreadId = it,
                                        chatThreadName = changeName
                                    )
                                }
                            }
                        }
                        EditType.ACTION_EDIT_GROUP_DESCRIBE -> {
                            groupId?.let {
                                groupViewModel?.changeChatGroupDescription(
                                    groupId = it,
                                    description = binding.etDescribe.text.trim().toString()
                                )
                            }
                        }
                        EditType.ACTION_EDIT_GROUP_ATTRIBUTE -> {
                            val map = mutableMapOf<String,String>()
                            map[ChatUIKitConstant.GROUP_MEMBER_ATTRIBUTE_NICKNAME] = binding.etAttribute.text?.trim().toString()

                            groupId?.let {
                                groupViewModel?.setGroupMemberAttributes(
                                    groupId = it,
                                    userId= ChatClient.getInstance().currentUser,
                                    attribute = map
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
            return@setOnMenuItemClickListener true
        }
        binding.etName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                val length = s.toString().trim().length
                if (length == 0){
                    if (type == EditType.ACTION_EDIT_GROUP_NAME){
                        binding.inputNameCount.text =
                            resources.getString(R.string.uikit_group_change_name_count, 0)
                    }else if (type == EditType.ACTION_EDIT_THREAD_NAME){
                        binding.inputNameCount.text =
                            resources.getString(R.string.uikit_thread_edit_name_count, 0)
                    }
                }else{
                    if (type == EditType.ACTION_EDIT_GROUP_NAME){
                        binding.inputNameCount.text =
                            resources.getString(R.string.uikit_group_change_name_count,length)
                    }else if (type == EditType.ACTION_EDIT_THREAD_NAME){
                        binding.inputNameCount.text =
                            resources.getString(R.string.uikit_thread_edit_name_count, length)
                    }

                }
                updateSaveView(binding.etName.text.length)
            }
        })

        binding.etDescribe.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val length = s.toString().trim().length
                if (length == 0){
                    binding.inputDescribeCount.text =
                        resources.getString(R.string.uikit_group_change_describe_count, 0)
                }else{
                    binding.inputDescribeCount.text =
                        resources.getString(R.string.uikit_group_change_describe_count,length)
                }

                updateSaveView(binding.etDescribe.text.length)
            }
        })

        binding.etAttribute.addTextChangedListener (object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val length = s.toString().trim().length
                if (length == 0){
                    binding.inputAttributeCount.text =
                        resources.getString(R.string.uikit_group_change_name_count,0)
                }else{
                    binding.inputAttributeCount.text =
                        resources.getString(R.string.uikit_group_change_name_count,length)
                }
                updateSaveView(binding.etAttribute.text.length)
            }
        })
    }

    open fun updateSaveView(length: Int){
        binding.titleBar.setMenuTitleColor(ContextCompat.getColor(mContext,
            if (length != 0) R.color.ease_color_primary else R.color.ease_color_on_background_high))
    }

    open fun showSoftInput(editText:EditText){
        editText.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun changeChatGroupNameSuccess() {
        val group = ChatClient.getInstance().groupManager().getGroup(groupId)
        val convInfo = ChatUIKitClient.getCache().getGroup(groupId)
        convInfo?.let {
            it.name = group.groupName
        }
        groupId?.let {
            ChatUIKitClient.getCache().insertGroup(it,convInfo?: ChatUIKitGroupProfile(it,group.groupName))
        }
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.GROUP)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitConstant.EVENT_UPDATE_GROUP_NAME, ChatUIKitEvent.TYPE.GROUP, groupId))
        finish()
    }

    override fun changeChatGroupNameFail(code: Int, error: String) {
        ChatLog.e(TAG,"changeChatGroupNameFail $code $error")
    }

    override fun changeChatGroupDescriptionSuccess() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.GROUP)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitConstant.EVENT_UPDATE_GROUP_DESCRIPTION, ChatUIKitEvent.TYPE.GROUP, groupId))
        finish()
    }

    override fun changeChatGroupDescriptionFail(code: Int, error: String) {
        ChatLog.e(TAG,"changeChatGroupDescriptionFail $code $error")
    }

    override fun setGroupMemberAttributesSuccess() {
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.ATTRIBUTE)
            .post(lifecycleScope, ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE + ChatUIKitEvent.TYPE.ATTRIBUTE, ChatUIKitEvent.TYPE.ATTRIBUTE, groupId))
        // update cache data
        finish()
    }

    override fun setGroupMemberAttributesFail(code: Int, error: String) {
        ChatLog.e(TAG,"setGroupMemberAttributesFail $code $error")
    }

    override fun changeThreadNameSuccess() {
        threadName = binding.etName.text.trim().toString()
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name)
            .post(mContext.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name, ChatUIKitEvent.TYPE.THREAD,threadName))
        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name)
            .post(mContext.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name, ChatUIKitEvent.TYPE.THREAD))
        finish()
    }

    override fun changeThreadNameFail(code: Int, error: String?) {
        ChatLog.e(TAG,"changeThreadNameFail $code $error")
    }

    companion object {
        private const val TAG = "ChatUIKitGroupDetailEditActivity"
        private const val KEY_GROUP_EDIT_TYPE = "edit_type"
        private const val KEY_GROUP_NICKNAME = "group_nick_name"
        private const val KEY_THREAD_ID = "thread_id"
        private const val KEY_THREAD_NAME = "thread_name"

        fun createIntent(
            context: Context,
            groupId:String?,
            type: EditType,
            nickname:String?="",
            threadId:String?="",
            threadName:String?="",
        ): Intent {
            val intent = Intent(context, ChatUIKitGroupDetailEditActivity::class.java)
            intent.putExtra(KEY_GROUP_EDIT_TYPE, type.ordinal)
            intent.putExtra(ChatUIKitConstant.EXTRA_CONVERSATION_ID, groupId)
            nickname?.let {
                intent.putExtra(KEY_GROUP_NICKNAME, it)
            }
            threadId?.let {
                intent.putExtra(KEY_THREAD_ID, it)
            }
            threadName?.let {
                intent.putExtra(KEY_THREAD_NAME, it)
            }
            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    return it
                }
            }
            return intent
        }
    }

}

enum class EditType(var code:Int){
    ACTION_EDIT_GROUP_UN_KNOW(-1),
    ACTION_EDIT_GROUP_NAME(0),
    ACTION_EDIT_GROUP_DESCRIBE(1),
    ACTION_EDIT_GROUP_ATTRIBUTE(2),
    ACTION_EDIT_THREAD_NAME(3)
}