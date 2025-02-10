package io.agora.chat.uikit

import io.agora.chat.uikit.configs.ChatUIKitAvatarConfig
import io.agora.chat.uikit.configs.ChatUIKitDateFormatConfig
import io.agora.chat.uikit.configs.ChatUIKitConfig
import io.agora.chat.uikit.configs.ChatUIKitMultiDeviceEventConfig
import io.agora.chat.uikit.configs.ChatUIKitSystemMsgConfig

class ChatUIKitConfig(
    var avatarConfig: ChatUIKitAvatarConfig? = ChatUIKitAvatarConfig(),
    var chatConfig: ChatUIKitConfig? = ChatUIKitConfig(),
    var dateFormatConfig: ChatUIKitDateFormatConfig? = ChatUIKitDateFormatConfig(),
    var systemMsgConfig: ChatUIKitSystemMsgConfig? = ChatUIKitSystemMsgConfig(),
    var multiDeviceConfig: ChatUIKitMultiDeviceEventConfig? = ChatUIKitMultiDeviceEventConfig(),
)