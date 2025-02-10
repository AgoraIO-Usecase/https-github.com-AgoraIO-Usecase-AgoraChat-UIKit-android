package io.agora.chat.uikit.common.dialog

import android.view.View
import androidx.fragment.app.Fragment
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitContainChildBottomSheetFragment
import io.agora.chat.uikit.feature.chat.ChatUIKitAddUserCardFragment
import io.agora.chat.uikit.feature.contact.ChatUIKitContactsListFragment
import io.agora.chat.uikit.feature.search.ChatUIKitSearchType
import io.agora.chat.uikit.interfaces.OnUserListItemClickListener
import io.agora.chat.uikit.model.ChatUIKitUser

class ChatUIKitContactBottomSheetFragment: ChatUIKitContainChildBottomSheetFragment() {
    private var userListItemClickListener: OnUserListItemClickListener? = null
    override val childFragment: Fragment
        get() {
            return ChatUIKitContactsListFragment.Builder()
                .useTitleBar(true)
                .setTitleBarTitle(getString(R.string.uikit_chat_message_user_card_select_title))
                .useSearchBar(true)
                .enableTitleBarPressBack(true)
                .setTitleBarBackPressListener {
                    hide()
                }
                .setDefaultMenuVisible(false)
                .setHeaderItemVisible(false)
                .setSideBarVisible(true)
                .setSearchType(ChatUIKitSearchType.USER)
                .setOnUserListItemClickListener(object : OnUserListItemClickListener {
                    override fun onUserListItemClick(v: View?, position: Int, user: ChatUIKitUser?) {
                        userListItemClickListener?.onUserListItemClick(v, position, user)
                    }

                    override fun onAvatarClick(v: View, position: Int) {

                    }
                })
                .setCustomFragment(ChatUIKitAddUserCardFragment())
                .build()
        }


    override fun showExpandedState(): Boolean {
        return true
    }

    override fun setTitleBar() {
        super.setTitleBar()
        binding?.titleBar?.visibility = View.GONE
    }

    fun setOnUserListItemClickListener(listener:OnUserListItemClickListener?) {
        this.userListItemClickListener = listener
    }

}