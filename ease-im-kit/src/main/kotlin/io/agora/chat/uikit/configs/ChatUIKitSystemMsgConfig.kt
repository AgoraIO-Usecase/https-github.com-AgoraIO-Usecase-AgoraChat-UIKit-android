package io.agora.chat.uikit.configs

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R

class ChatUIKitSystemMsgConfig {

    var useDefaultContactSystemMsg: Boolean? = null
        get() {
            if (field != null) return field
            if (ChatUIKitClient.isInited()) {
                return ChatUIKitClient.getContext()?.resources?.getBoolean(R.bool.ease_use_default_contact_system_msg) ?: false
            }
            return false
        }

}