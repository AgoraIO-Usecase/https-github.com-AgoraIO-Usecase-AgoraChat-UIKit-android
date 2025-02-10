package io.agora.chat.uikit.configs

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R

class ChatUIKitMultiDeviceEventConfig {
    var useDefaultMultiDeviceContactEvent: Boolean? = null
        get() {
            if (field != null) return field
            if (ChatUIKitClient.isInited()) {
                return ChatUIKitClient.getContext()?.resources?.getBoolean(R.bool.ease_default_multi_device_contact_event) ?: false
            }
            return false
        }

    var useDefaultMultiDeviceGroupEvent: Boolean? = null
        get() {
            if (field != null) return field
            if (ChatUIKitClient.isInited()) {
                return ChatUIKitClient.getContext()?.resources?.getBoolean(R.bool.ease_default_multi_device_group_event) ?: false
            }
            return false
        }
}