package io.agora.chat.uikit.common.impl

import io.agora.chat.uikit.common.ChatCallback

class ChatCallbackWrapper: ChatCallback {
    private val callbacks by lazy { mutableListOf<ChatCallback>() }

    @Synchronized
    fun addCallback(callback: ChatCallback) {
        callbacks.add(callback)
    }

    @Synchronized
    fun removeCallback(callback: ChatCallback) {
        callbacks.remove(callback)
    }

    @Synchronized
    fun clear() {
        callbacks.clear()
    }

    fun hasCallback(): Boolean {
        return callbacks.size > 0
    }

    override fun onSuccess() {
        callbacks.iterator().forEach { it.onSuccess() }
    }

    override fun onError(code: Int, error: String?) {
       callbacks.iterator().forEach { it.onError(code, error) }
    }

    override fun onProgress(progress: Int, status: String?) {
        callbacks.iterator().forEach { it.onProgress(progress, status) }
    }
}