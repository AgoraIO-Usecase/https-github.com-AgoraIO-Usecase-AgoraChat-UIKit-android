package io.agora.chat.uikit.feature.chat

import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.dialog.ChatUIKitBottomSheetChildHelper
import io.agora.chat.uikit.feature.contact.ChatUIKitContactsListFragment

class ChatUIKitAddUserCardFragment: ChatUIKitContactsListFragment(), ChatUIKitBottomSheetChildHelper {

    override val titleBarTitle: Int
        get() = R.string.uikit_chat_message_user_card_select_title

    override val isShowTitleBarLeftLayout: Boolean
        get() = true
}