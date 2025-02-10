package io.agora.chat.uikit.model

import androidx.fragment.app.Fragment

/**
 * Pager for ViewPager.
 */
data class ChatUIKitPager(
    /**
     * Title of the pager.
     */
    val title: String,
    /**
     * Fragment of the pager.
     */
    val fragment: Fragment
)