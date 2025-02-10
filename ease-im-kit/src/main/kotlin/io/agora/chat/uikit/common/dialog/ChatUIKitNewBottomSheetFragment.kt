package io.agora.chat.uikit.common.dialog

import android.view.View
import androidx.fragment.app.Fragment
import io.agora.chat.uikit.R
import io.agora.chat.uikit.feature.chat.activities.UIKitChatActivity
import io.agora.chat.uikit.base.ChatUIKitContainChildBottomSheetFragment
import io.agora.chat.uikit.feature.contact.ChatUIKitContactsListFragment
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitType
import io.agora.chat.uikit.feature.chat.UIKitNewChatFragment
import io.agora.chat.uikit.feature.search.ChatUIKitSearchType
import io.agora.chat.uikit.interfaces.OnUserListItemClickListener
import io.agora.chat.uikit.model.ChatUIKitUser

class ChatUIKitNewBottomSheetFragment: ChatUIKitContainChildBottomSheetFragment() {
    override val childFragment: Fragment
        get() {
            return ChatUIKitContactsListFragment.Builder()
                .useTitleBar(true)
                .setTitleBarTitle(getString(R.string.uikit_chat_new_conversation))
                .useSearchBar(true)
                .setDefaultMenuVisible(false)
                .enableTitleBarPressBack(true)
                .setTitleBarBackPressListener {
                    hide()
                }
                .setHeaderItemVisible(false)
                .setOnUserListItemClickListener(object : OnUserListItemClickListener {
                    override fun onUserListItemClick(v: View?, position: Int, user: ChatUIKitUser?) {
                        user?.let {
                            UIKitChatActivity.actionStart(context!!, it.userId, ChatUIKitType.SINGLE_CHAT)
                        }
                        this@ChatUIKitNewBottomSheetFragment.hide()
                    }
                })
                .setSearchType(ChatUIKitSearchType.USER)
                .setCustomFragment(UIKitNewChatFragment())
                .build()
        }


    override fun showExpandedState(): Boolean {
        return true
    }

    override fun setTitleBar() {
        super.setTitleBar()
        binding?.titleBar?.visibility = View.GONE
    }

}