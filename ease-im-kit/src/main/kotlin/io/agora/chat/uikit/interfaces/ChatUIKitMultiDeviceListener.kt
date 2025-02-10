package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.ChatMultiDeviceListener

open class ChatUIKitMultiDeviceListener: ChatMultiDeviceListener {
    /**
     * The multi-device contact event.
     * @param event     The contact event. See [ChatMultiDeviceListener].
     * @param target    The user ID of the contact.
     * @param ext       The extension information.
     */
    override fun onContactEvent(event: Int, target: String?, ext: String?) {}

    /**
     * The multi-device group event.
     * @param event         The group event. See [ChatMultiDeviceListener].
     * @param target        The group ID.
     * @param usernames     The operation target ID(s).
     */
    override fun onGroupEvent(event: Int, target: String?, usernames: MutableList<String>?) { }

    /**
     * The multi-device message thread event.
     * @param event         The message thread event. See [ChatMultiDeviceListener].
     * @param target        The message thread ID.
     * @param usernames     The operation target ID(s).
     */
    override fun onChatThreadEvent(event: Int, target: String?, usernames: List<String?>?) {}

    /**
     * The multi-device event of historical message deletion from the server.
     *
     * @param conversationId    The conversation ID.
     * @param deviceId          The device ID.
     */
    override fun onMessageRemoved(conversationId: String?, deviceId: String?) {}


    /**
     * The multi-device conversation event.
     *
     * @param event             The conversation event. See [.CONVERSATION_PINNED], [.CONVERSATION_UNPINNED], and [.CONVERSATION_DELETED].
     * @param conversationId    The conversation ID.
     * @param type              The conversation type. See [ChatConversation.ChatConversationType].
     */
    override fun onConversationEvent(
        event: Int,
        conversationId: String?,
        type: ChatConversationType?
    ) {
    }

}