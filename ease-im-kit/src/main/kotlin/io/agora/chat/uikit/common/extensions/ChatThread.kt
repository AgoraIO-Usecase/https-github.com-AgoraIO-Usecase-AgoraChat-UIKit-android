package io.agora.chat.uikit.common.extensions

import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatThread


/**
 * Check if the current user is the owner of the thread.
 */
fun ChatThread.isOwner() = owner == ChatClient.getInstance().currentUser