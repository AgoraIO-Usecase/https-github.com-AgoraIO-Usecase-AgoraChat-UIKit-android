package io.agora.chat.uikit.common.extensions

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch

fun <T> Flow<T>.catchChatException(action: suspend FlowCollector<T>.(ChatException) -> Unit): Flow<T> {
    return this.catch { e ->
        if (ChatUIKitClient.DEBUG) {
            ChatLog.e("ChatUIKitClient", "catchChatException: ${e.message}")
        }
        if (e is ChatException) {
            action.invoke(this, e)
        } else {
            ChatLog.e("catchChatException", "catchChatException: ${e.message}")
        }
    }
}
suspend fun <T> SharedFlow<T>.collectWithCheckErrorCode(checked: (T) -> Unit): Nothing {
    collect{
        if (it is Int && it == ChatError.EM_NO_ERROR) {
            checked.invoke(it)
        } else {
            if (ChatUIKitClient.DEBUG) {
                ChatLog.d("flow", "collectWithCheckErrorCode: execute to no check scope")
            }
        }
    }
}
