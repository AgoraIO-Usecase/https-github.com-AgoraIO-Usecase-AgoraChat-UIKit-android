package io.agora.chat.uikit.feature.thread.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseFragment
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatThread
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.extensions.getMessageDigest
import io.agora.chat.uikit.databinding.UikitFragmentThreadCreateBinding
import io.agora.chat.uikit.feature.chat.UIKitChatFragment
import io.agora.chat.uikit.feature.chat.chathistory.ChatUIKitHistoryAdapter
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitType
import io.agora.chat.uikit.feature.chat.interfaces.ChatInputMenuListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatExtendMenuItemClickListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatRecordTouchListener
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitInputMenu
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitVoiceRecorderDialog
import io.agora.chat.uikit.feature.thread.ChatUIKitThreadActivity
import io.agora.chat.uikit.feature.thread.controllers.ChatUIKitThreadAttachmentController
import io.agora.chat.uikit.feature.thread.controllers.ChatUIKitThreadController
import io.agora.chat.uikit.feature.thread.interfaces.IChatThreadResultView
import io.agora.chat.uikit.interfaces.OnVoiceRecorderClickListener
import io.agora.chat.uikit.menu.chat.ChatUIKitExtendMenuDialog
import io.agora.chat.uikit.model.ChatUIKitEmojicon
import io.agora.chat.uikit.viewmodel.thread.ChatUIKitThreadViewModel
import io.agora.chat.uikit.viewmodel.thread.IChatThreadRequest


open class ChatUIKitCreateThreadFragment: ChatUIKitBaseFragment<UikitFragmentThreadCreateBinding>(),
    IChatThreadResultView {

    private var conversationId: String = ""
    private var topicMsgId: String = ""

    private var sendOriginalImage = false

    /**
     * The view model of the chat thread view.
     */
    private var threadViewModel: IChatThreadRequest? = null

    /**
     * Used to monitor touch events for sending voice
     */
    private var recordTouchListener: OnChatRecordTouchListener? = null

    private var backPressListener: View.OnClickListener? = null

    /**
     * Use to control the create chat thread.
     */
    private val chatThreadController: ChatUIKitThreadController by lazy {
        ChatUIKitThreadController(mContext, threadViewModel) }


    private val attachmentController: ChatUIKitThreadAttachmentController by lazy {
        ChatUIKitThreadAttachmentController(mContext, this, conversationId, sendOriginalImage)
    }

    private val launcherToCamera: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> attachmentController.onActivityResult(result,
        UIKitChatFragment.REQUEST_CODE_CAMERA
    ) }
    private val launcherToAlbum: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> attachmentController.onActivityResult(result, UIKitChatFragment.REQUEST_CODE_LOCAL) }
    private val launcherToVideo: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> attachmentController.onActivityResult(result,
        UIKitChatFragment.REQUEST_CODE_SELECT_VIDEO
    ) }
    private val launcherToFile: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> attachmentController.onActivityResult(result,
        UIKitChatFragment.REQUEST_CODE_SELECT_FILE
    ) }

    private var extendMenuItemClickListener: OnChatExtendMenuItemClickListener? = null

    val chatInputMenu: ChatUIKitInputMenu?
        get() = binding?.layoutMenu

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UikitFragmentThreadCreateBinding {
        return UikitFragmentThreadCreateBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        arguments?.let {
            conversationId = it.getString(ChatUIKitConstant.EXTRA_CONVERSATION_ID,"")
            topicMsgId = it.getString(ChatUIKitConstant.THREAD_TOPIC_MESSAGE_ID,"")
            defaultTitleBar()
            defaultTopicView()
            val title: String = it.getString(Constant.KEY_SET_TITLE, "")
            if (!TextUtils.isEmpty(title)) {
                binding?.titleBar?.setTitle(title)
            }
            val subTitle: String = it.getString(Constant.KEY_SET_SUB_TITLE, "")
            if (!TextUtils.isEmpty(subTitle)) {
                binding?.titleBar?.setSubtitle(subTitle)
            }
            val canBack: Boolean = it.getBoolean(Constant.KEY_ENABLE_BACK, true)
            binding?.titleBar?.setDisplayHomeAsUpEnabled(canBack
                , it.getBoolean(Constant.KEY_USE_TITLE_REPLACE, false))
            binding?.titleBar?.setNavigationOnClickListener {view->
                if (backPressListener != null) {
                    backPressListener?.onClick(view)
                    return@setNavigationOnClickListener
                }
                mContext.onBackPressed()
            }
        }
        setCustomExtendView()
    }

    override fun initViewModel() {
        super.initViewModel()
        threadViewModel = ViewModelProvider(this)[ChatUIKitThreadViewModel::class.java]
        threadViewModel?.attachView(this)

        chatThreadController.setupWithToConversation(conversationId,topicMsgId)
    }

    override fun initListener() {
        super.initListener()

        binding?.run {
            titleBar.setNavigationOnClickListener{
                mContext.onBackPressed()
            }

            layoutMenu.setChatInputMenuListener(object : ChatInputMenuListener{
                override fun onTyping(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {

                }

                override fun editTextOnKeyListener(
                    v: View?,
                    keyCode: Int,
                    event: KeyEvent?
                ): Boolean {
                    return false
                }

                override fun onSendMessage(content: String?) {
                    threadSendTextMessage(content)
                }

                override fun onExpressionClicked(emojiIcon: Any?) {
                    if (emojiIcon is ChatUIKitEmojicon){
                        threadSendBigExpressionMessage(emojiIcon.name, emojiIcon.identityCode)
                    }
                }

                override fun onPressToSpeakBtnTouch(v: View?, event: MotionEvent?): Boolean {
                    return false
                }

                override fun onToggleVoiceBtnClicked() {
                    ChatUIKitVoiceRecorderDialog(mContext, conversationId).apply {
                        if (mContext is AppCompatActivity) {
                            show((mContext as AppCompatActivity).supportFragmentManager, "ease_chat_voice_recorder_dialog")
                            setOnVoiceRecorderClickListener(object : OnVoiceRecorderClickListener {
                                override fun onClick(filePath: String?, length: Int) {
                                    chatInputMenu?.chatPrimaryMenu?.showTextStatus()
                                    sendVoiceMessage(if (filePath.isNullOrEmpty()) null else Uri.parse(filePath), length)
                                }
                            })
                            setOnRecordTouchListener(recordTouchListener)
                            setOnDismissListener { chatInputMenu?.chatPrimaryMenu?.showTextStatus() }
                        }
                    }
                }

                override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
                    onExtendMenuItemClick(view,itemId)
                }

            })
        }
    }

    open fun defaultTitleBar(){
        binding?.run {
            chatHeaderDivider.visibility = View.VISIBLE
            titleBar.getTitleView().let {
                it.maxEms = 64
                it.maxLines = 1
                it.ellipsize = TextUtils.TruncateAt.END
                it.text = getThreadName()
            }
            titleBar.setSubtitle(getString(R.string.uikit_thread_affiliation_group,getParentName()))
        }
    }

    open fun defaultTopicView(){
        binding?.run {
            rvTopicMsg.layoutManager = LinearLayoutManager(mContext)
            val topicAdapter = ChatUIKitHistoryAdapter()
            rvTopicMsg.adapter = topicAdapter
            val topicMsg = ChatClient.getInstance().chatManager().getMessage(topicMsgId)
            topicAdapter.setData(mutableListOf(topicMsg))
        }
    }

    open fun getParentName():String{
        if (conversationId.isNotEmpty()){
            val parentGroup = ChatClient.getInstance().groupManager().getGroup(conversationId)
            return if (parentGroup == null){
                conversationId
            }else{
                parentGroup.groupName
            }
        }
        return conversationId
    }

    open fun setCustomExtendView() {
        val dialog = ChatUIKitExtendMenuDialog(mContext)
        dialog.init()
        binding?.layoutMenu?.setCustomExtendMenu(dialog)
    }

    open fun getThreadName():String{
       val topicMessage = ChatClient.getInstance().chatManager().getMessage(topicMsgId)
       return topicMessage.getMessageDigest(mContext)
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

    private fun setOnChatExtendMenuItemClickListener(listener: OnChatExtendMenuItemClickListener?) {
        this.extendMenuItemClickListener = listener
    }

    fun onExtendMenuItemClick(view: View?, itemId: Int): Boolean {
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
                attachmentController.selectContact(childFragmentManager, ChatUIKitType.GROUP_CHAT)
                return true
            }
        }
        return false
    }

    override fun onCreateChatThreadSuccess(chatThread: ChatThread, message: ChatMessage) {
        ChatLog.e(TAG,"onCreateChatThreadSuccess chatThreadId ${chatThread.chatThreadId}")
        ChatUIKitThreadActivity.actionStart(
            context = mContext,
            conversationId = chatThread.parentId,
            threadId = chatThread.chatThreadId,
            topicMsgId = topicMsgId,
            msgId = message.msgId,
        )
        mContext.finish()
    }

    override fun onCreateChatThreadFail(code: Int, message: String?) {
        ChatLog.e(TAG,"onCreateChatThreadFail $code $message")
    }

    fun setOnChatRecordTouchListener(voiceTouchListener: OnChatRecordTouchListener?) {
        this.recordTouchListener = voiceTouchListener
    }

    fun sendMessage(message: ChatMessage?) {
        message?.let { chatThreadController.setMessage(it) }
    }

    fun sendImageMessage(imageUri: Uri?, sendOriginalImage: Boolean) {
        chatThreadController.sendImageMessage(imageUri, sendOriginalImage)
    }

    fun sendVideoMessage(videoUri: Uri?, videoLength: Int) {
        chatThreadController.sendVideoMessage(videoUri, videoLength)
    }

    fun sendFileMessage(fileUri: Uri?) {
        chatThreadController.sendFileMessage(fileUri)
    }

    fun sendVoiceMessage(filePath: String?, length: Int) {
        chatThreadController.sendVoiceMessage(if (filePath.isNullOrEmpty()) Uri.parse(filePath) else null, length)
    }

    fun sendVoiceMessage(filePath: Uri?, length: Int) {
        chatThreadController.sendVoiceMessage(filePath,length)
    }

    fun threadSendTextMessage(content: String?){
        chatThreadController.sendTextMessage(content)
    }

    fun threadSendBigExpressionMessage(name: String?, identityCode: String?){
        chatThreadController.sendBigExpressionMessage(name,identityCode)
    }

    private fun setHeaderBackPressListener(listener: View.OnClickListener?) {
        this.backPressListener = listener
    }

    override fun onDestroyView() {
        binding?.layoutMenu?.let {
            it.setCustomExtendMenu(null)
            it.setChatInputMenuListener(null)
        }
        super.onDestroyView()
    }

    class Builder(
        private val conversationId: String?,
        private val topicMsgId: String?,
    ) {
        protected val bundle: Bundle = Bundle()
        protected var customFragment: ChatUIKitCreateThreadFragment? = null
        private var recordTouchListener: OnChatRecordTouchListener? = null
        private var backPressListener: View.OnClickListener? = null
        private var extendMenuItemClickListener: OnChatExtendMenuItemClickListener? = null

        init {
            bundle.putString(ChatUIKitConstant.EXTRA_CONVERSATION_ID, conversationId)
            bundle.putString(ChatUIKitConstant.THREAD_TOPIC_MESSAGE_ID, topicMsgId)
        }

        /**
         * Set custom fragment which should extends ChatUIKitCreateThreadFragment
         *
         * @param fragment
         * @param <T>
         * @return
        </T> */
        fun <T : ChatUIKitCreateThreadFragment?> setCustomFragment(fragment: T): Builder {
            customFragment = fragment
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
         * If you have set [Builder.enableTitleBarPressBack], you can set the listener
         *
         * @param listener
         * @return
         */
        fun setTitleBarBackPressListener(listener: View.OnClickListener?):Builder {
            backPressListener = listener
            return this
        }

        /**
         * Set chat extension menu item click listener
         *
         * @param listener
         * @return
         */
        fun setOnChatExtendMenuItemClickListener(listener: OnChatExtendMenuItemClickListener?):Builder {
            extendMenuItemClickListener = listener
            return this
        }

        /**
         * Set touch event listener during recording
         *
         * @param recordTouchListener
         * @return
         */
        fun setOnChatRecordTouchListener(recordTouchListener: OnChatRecordTouchListener?):Builder {
            this.recordTouchListener = recordTouchListener
            return this
        }

        fun build(): ChatUIKitCreateThreadFragment? {
            val fragment = if (customFragment != null) customFragment else ChatUIKitCreateThreadFragment()
            fragment?.let {
                it.arguments = bundle

                it.setOnChatExtendMenuItemClickListener(extendMenuItemClickListener)
                it.setOnChatRecordTouchListener(recordTouchListener)
                it.setHeaderBackPressListener(backPressListener)

            }
            return fragment
        }
    }

    private object Constant {
        const val KEY_USE_TITLE_REPLACE = "key_use_replace_action_bar"
        const val KEY_SET_TITLE = "key_set_title"
        const val KEY_SET_SUB_TITLE = "key_set_sub_title"
        const val KEY_ENABLE_BACK = "key_enable_back"
    }

    companion object{
        private val TAG = ChatUIKitCreateThreadFragment::class.java.simpleName
    }

}