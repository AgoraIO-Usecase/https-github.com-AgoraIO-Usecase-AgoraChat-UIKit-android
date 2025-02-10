package io.agora.chat.uikit.feature.thread.interfaces

import io.agora.chat.uikit.feature.thread.widgets.ChatUIKitThreadRole

interface OnChatThreadRoleResultCallback {
    fun onThreadRole(role:ChatUIKitThreadRole)
}