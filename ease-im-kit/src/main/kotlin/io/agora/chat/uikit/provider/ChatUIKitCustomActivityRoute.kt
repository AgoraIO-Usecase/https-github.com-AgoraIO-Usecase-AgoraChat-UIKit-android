package io.agora.chat.uikit.provider

import android.content.Intent

/**
 * Set custom Activity route in UIKit.
 * You can use this interface to customize the route of the activity.
 */
interface ChatUIKitCustomActivityRoute {

    /**
     * Provide the new intent by the old intent.
     * @param intent The old intent.
     * @return The new intent.
     */
    fun getActivityRoute(intent: Intent): Intent?
}
