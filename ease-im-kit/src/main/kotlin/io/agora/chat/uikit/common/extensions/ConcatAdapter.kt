package io.agora.chat.uikit.common.extensions

import androidx.recyclerview.widget.ConcatAdapter
import io.agora.chat.uikit.feature.chat.adapter.ChatUIKitMessagesAdapter

/**
 * Get the total item count before the target adapter.
 */
internal fun ConcatAdapter.getItemCountBeforeTarget(adapter: ChatUIKitMessagesAdapter): Int {
    var count = 0
    for (i in 0 until adapters.size) {
        val currentAdapter = adapters[i]
        if (currentAdapter == adapter) {
            break
        }
        count += currentAdapter.itemCount
    }
    return count
}