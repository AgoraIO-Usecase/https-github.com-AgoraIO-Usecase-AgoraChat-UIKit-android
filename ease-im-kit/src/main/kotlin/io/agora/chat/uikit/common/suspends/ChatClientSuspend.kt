package io.agora.chat.uikit.common.suspends

import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.impl.CallbackImpl
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Suspend method for [ChatClient.login(String, String, ChatCallback)]
 * @param username
 * @param password
 * @return [ChatError]
 */
suspend fun ChatClient.loginWithPassword(username: String, password: String): Int {
    return suspendCoroutine { continuation ->
        login(username, password, CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, error->
                continuation.resumeWithException(ChatException(code, error))
            }
        ))
    }
}

/**
 * Suspend method for [ChatClient.loginWithToken(String, String, ChatCallback)]
 * @param username
 * @param token
 * @return Login result, see [ChatError]
 */
suspend fun ChatClient.login(username: String, token: String): Int {
    return suspendCoroutine { continuation ->
        loginWithToken(username, token, CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, error->
                continuation.resumeWithException(ChatException(code, error))
            }
        ))
    }
}

/**
 * Suspended method for [ChatClient.logout(boolean, ChatCallback)]
 * @param unbindToken Whether to unbind the token from the device
 * @return Logout result, see [ChatError]
 */
suspend fun ChatClient.logout(unbindToken: Boolean): Int {
    return suspendCoroutine { continuation ->
        logout(unbindToken, CallbackImpl(
            onSuccess = {
                continuation.resume(ChatError.EM_NO_ERROR)
            },
            onError = { code, error->
                continuation.resumeWithException(ChatException(code, error))
            }
        ))
    }
}