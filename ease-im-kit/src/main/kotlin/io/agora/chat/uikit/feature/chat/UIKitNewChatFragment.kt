package io.agora.chat.uikit.feature.chat

import android.os.Bundle
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.dialog.ChatUIKitBottomSheetChildHelper
import io.agora.chat.uikit.feature.contact.ChatUIKitContactsListFragment

class UIKitNewChatFragment: ChatUIKitContactsListFragment(), ChatUIKitBottomSheetChildHelper {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.searchBar?.setText(getString(R.string.uikit_search_conversation_hint))
    }
    override val titleBarTitle: Int
        get() = R.string.uikit_chat_new_conversation

    override val isShowTitleBarLeftLayout: Boolean
        get() = true
}