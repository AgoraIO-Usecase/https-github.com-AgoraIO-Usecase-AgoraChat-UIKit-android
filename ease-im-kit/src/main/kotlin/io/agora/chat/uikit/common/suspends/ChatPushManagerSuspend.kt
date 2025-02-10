package io.agora.chat.uikit.common.suspends

import io.agora.chat.uikit.common.ChatConversation
import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatPushManager
import io.agora.chat.uikit.common.ChatSilentModeParam
import io.agora.chat.uikit.common.ChatSilentModeResult
import io.agora.chat.uikit.common.impl.CallbackImpl
import io.agora.chat.uikit.common.impl.ValueCallbackImpl
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Set the silent mode for the conversation.
 * @param conversationId The conversation id.
 * @param chatConversationType The conversation type.
 * @param silentModeParam The silent mode param, see [ChatSilentModeParam].
 * @return The result of setting silent mode. See [ChatSilentModeResult].
 */
suspend fun ChatPushManager.setSilentModeForConversation(conversationId: String,
                                                         chatConversationType: ChatConversationType,
                                                         silentModeParam: ChatSilentModeParam): ChatSilentModeResult {
    return suspendCoroutine { continuation ->
        setSilentModeForConversation(conversationId, chatConversationType, silentModeParam, ValueCallbackImpl<ChatSilentModeResult>(
            onSuccess = {
                continuation.resume(it)
            },
            onError = { error, errorDescription ->
                continuation.resumeWithException(ChatException(error, errorDescription))
            }
        ))
    }
}

/**
 * Clear the setting of offline push notification type for the conversation.
 * After clearing, the conversation follows the Settings of the current logged-in user
 * setSilentModeForAll(SilentModeParam, ValueCallBack).
 * @param conversationId The conversation id.
 * @param chatConversationType The conversation type.
 * @return The error code. See [ChatError].
 */
suspend fun ChatPushManager.clearSilentModeForConversation(conversationId: String,
                                                           chatConversationType: ChatConversationType): Int {
    return suspendCoroutine { continuation ->
        clearRemindTypeForConversation(conversationId, chatConversationType, CallbackImpl(
                onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                },
                onError = { error, errorDescription ->
                    continuation.resumeWithException(ChatException(error, errorDescription))
                }
            )
        )
    }
}

/**
 * Obtain the DND settings of specified conversations.
 * @param list The conversation list.
 * @return The result of getting silent mode. See [ChatSilentModeResult].
 */
suspend fun ChatPushManager.getSilentModeOfConversations(list: List<ChatConversation>): Map<String, ChatSilentModeResult> {
    return suspendCoroutine { continuation ->
        getSilentModeForConversations(list.toMutableList(), ValueCallbackImpl(
                onSuccess = {
                    continuation.resume(it)
                },
                onError = { error, errorDescription ->
                    continuation.resumeWithException(ChatException(error, errorDescription))
                }
            )
        )
    }
}