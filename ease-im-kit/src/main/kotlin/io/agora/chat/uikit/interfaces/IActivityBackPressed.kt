package io.agora.chat.uikit.interfaces

/**
 * Fragment implement this interface to handle back press event.
 */
interface IActivityBackPressed {
    /**
     * Called when the activity has detected the user's press of the back key.
     */
    fun onBackPressed(): Boolean
}