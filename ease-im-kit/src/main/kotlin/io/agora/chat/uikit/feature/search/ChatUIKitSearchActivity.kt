package io.agora.chat.uikit.feature.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.databinding.UikitActivitySearchLayoutBinding
import io.agora.chat.uikit.feature.chat.activities.ChatUIKitMessageSearchResultActivity
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitType
import io.agora.chat.uikit.feature.search.interfaces.OnSearchMsgItemClickListener
import io.agora.chat.uikit.feature.search.interfaces.OnSearchUserItemClickListener
import io.agora.chat.uikit.interfaces.OnContactSelectedListener
import io.agora.chat.uikit.model.ChatUIKitUser

class ChatUIKitSearchActivity:ChatUIKitBaseActivity<UikitActivitySearchLayoutBinding>(),
    ChatUIKitSearchUserFragment.OnCancelClickListener {

    private var searchType: ChatUIKitSearchType = ChatUIKitSearchType.USER
    private var conversationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            searchType = it.getIntExtra(Constant.KEY_SEARCH_TYPE, ChatUIKitSearchType.USER.ordinal).let { type->
                when(type){
                    ChatUIKitSearchType.USER.ordinal -> { ChatUIKitSearchType.USER }
                    ChatUIKitSearchType.SELECT_USER.ordinal -> { ChatUIKitSearchType.SELECT_USER }
                    ChatUIKitSearchType.CONVERSATION.ordinal -> { ChatUIKitSearchType.CONVERSATION }
                    ChatUIKitSearchType.MESSAGE.ordinal -> { ChatUIKitSearchType.MESSAGE}
                    ChatUIKitSearchType.BLOCK_USER.ordinal -> { ChatUIKitSearchType.BLOCK_USER}
                    else -> { ChatUIKitSearchType.CONVERSATION}
                }
            }

            if (it.hasExtra(Constant.KEY_CONVERSATION_ID)){
                conversationId = it.getStringExtra(Constant.KEY_CONVERSATION_ID)
            }
        }
        replaceSearchUserFragment()
    }

    override fun getViewBinding(inflater: LayoutInflater): UikitActivitySearchLayoutBinding {
        return UikitActivitySearchLayoutBinding.inflate(inflater)
    }

    private fun replaceSearchUserFragment() {
        val fragment = when (searchType) {
            ChatUIKitSearchType.USER -> {
                ChatUIKitSearchUserFragment.Builder()
                    .setItemClickListener(object : OnSearchUserItemClickListener{
                        override fun onSearchItemClick(view: View?, position: Int, user: ChatUIKitUser) {
                            Intent().apply {
                                putExtra(Constant.KEY_USER, user)
                                setResult(RESULT_OK, this)
                                finish()
                            }
                        }
                    })
                    .showRightCancel(true)
                    .setOnCancelListener(object : ChatUIKitSearchUserFragment.OnCancelClickListener{
                        override fun onCancelClick(view: View) {
                            finish()
                        }
                    }).build()
            }
            ChatUIKitSearchType.SELECT_USER -> {
                ChatUIKitSearchSelectUserFragment.Builder()
                    .setOnSelectListener(object : OnContactSelectedListener{
                        override fun onContactSelectedChanged(
                            v: View,
                            selectedMembers: MutableList<String>
                        ) {
                            Intent().apply {
                                putStringArrayListExtra(Constant.KEY_SELECT_USER,ArrayList(selectedMembers))
                                setResult(RESULT_OK, this)
                                finish()
                            }
                        }
                    })
                    .showRightCancel(true)
                    .build()
            }
            ChatUIKitSearchType.MESSAGE ->{
                ChatUIKitSearchMessageFragment.Builder()
                    .setConversationId(conversationId)
                    .setItemClickListener(object : OnSearchMsgItemClickListener{
                        override fun onSearchItemClick(
                            view: View?,
                            position: Int,
                            msg: ChatMessage
                        ) {
                            msg.chatType?.let {
                                val chatType = when(it) {
                                    ChatType.Chat ->  ChatUIKitType.SINGLE_CHAT
                                    ChatType.GroupChat -> ChatUIKitType.GROUP_CHAT
                                    ChatType.ChatRoom -> ChatUIKitType.CHATROOM
                                }
                                ChatUIKitMessageSearchResultActivity.actionStart(mContext, conversationId, chatType, msg.msgId)
                            }
                        }
                    })
                    .setOnCancelListener(object : ChatUIKitSearchMessageFragment.OnCancelClickListener{
                        override fun onCancelClick(view: View) {
                            finish()
                        }
                    })
                    .showRightCancel(true)
                    .build()
            }
            ChatUIKitSearchType.BLOCK_USER -> {
                ChatUIKitSearchUserFragment.Builder()
                    .setSearchBlockUser(true)
                    .setItemClickListener(object : OnSearchUserItemClickListener{
                        override fun onSearchItemClick(view: View?, position: Int, user: ChatUIKitUser) {
                            Intent().apply {
                                putExtra(Constant.KEY_USER, user)
                                setResult(RESULT_OK, this)
                                finish()
                            }
                        }
                    })
                    .showRightCancel(true)
                    .setOnCancelListener(object : ChatUIKitSearchUserFragment.OnCancelClickListener{
                        override fun onCancelClick(view: View) {
                            finish()
                        }
                    }).build()
            }
            else -> {
                ChatUIKitSearchConversationFragment()
            }
        }

        val t = supportFragmentManager.beginTransaction()
        t.add(R.id.fl_fragment, fragment, fragment::javaClass.name).show(fragment).commit()

    }

    override fun onCancelClick(view: View) {
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            for (fragment in supportFragmentManager.fragments) {
                if (fragment is ChatUIKitSearchSelectUserFragment){
                    fragment.resetSelectList()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private object Constant {
        const val KEY_SELECT_USER = "select_user"
        const val KEY_USER = "user"
        const val KEY_SEARCH_TYPE = "searchType"
        const val KEY_CONVERSATION_ID = "searchConversationId"
    }

    companion object {
        fun actionStart(context: Context, searchType: ChatUIKitSearchType) {
            Intent(context, ChatUIKitSearchActivity::class.java).apply {
                putExtra(Constant.KEY_SEARCH_TYPE, searchType.ordinal)
                context.startActivity(this)
            }
        }

        fun createIntent(context: Context, searchType: ChatUIKitSearchType? = null,conversationId:String? = null): Intent {
            val intent = Intent(context, ChatUIKitSearchActivity::class.java)
            if (searchType != null) {
                intent.putExtra(Constant.KEY_SEARCH_TYPE, searchType.ordinal)
            }
            if (conversationId != null){
                intent.putExtra(Constant.KEY_CONVERSATION_ID, conversationId)
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

enum class ChatUIKitSearchType{
    USER,
    SELECT_USER,
    CONVERSATION,
    MESSAGE,
    BLOCK_USER,
}