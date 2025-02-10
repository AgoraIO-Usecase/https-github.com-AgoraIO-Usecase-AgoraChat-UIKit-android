package io.agora.chat.uikit.viewmodel.messages

import io.agora.chat.uikit.common.ChatConversation
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatSearchDirection
import io.agora.chat.uikit.viewmodel.IAttachView

interface IChatMessageListRequest: IAttachView {

    /**
     * The number of messages loaded each time.
     */
    var pageSize: Int

    /**
     * The message id to search more messages.
     */
    var messageCursor: String?

    /**
     * Setup with conversation.
     * @param conversation The conversation of this chat.
     */
    fun setupWithConversation(conversation: ChatConversation?)

    /**
     * Join chatroom.
     * When you want to open a chatroom, you need to call this method to join the chatroom first.
     * @param roomId The room id of the chatroom.
     */
    fun joinChatroom(roomId: String)

    /**
     * Leave chatroom.
     * When you want to close a chatroom, you need to call this method to leave the chatroom first.
     * @param roomId The room id of the chatroom.
     */
    fun leaveChatroom(roomId: String)

    /**
     * Get all cache messages. It refers to the api [ChatConversation.getAllMessages].
     */
    fun getAllCacheMessages()

    /**
     * Load local messages from database.
     * The method only works when the load data type is [ChatUIKitMessageListLayout.ChatUIKitLoadDataType.LOCAL].
     * @param direction The direction of loading. [ChatSearchDirection.UP] or [ChatSearchDirection.DOWN].
     */
    fun loadLocalMessages(direction: ChatSearchDirection = ChatSearchDirection.UP)

    /**
     * Load more local messages from database.
     * The method only works when the load data type is [ChatUIKitMessageListLayout.ChatUIKitLoadDataType.LOCAL].
     * @param startMsgId The message id of the first message loaded last time.
     *                   If this parameter is null or "", it will load the first page of messages.
     * @param direction The direction of loading. [ChatSearchDirection.UP] or [ChatSearchDirection.DOWN].
     */
    fun loadMoreLocalMessages(startMsgId: String? = "", direction: ChatSearchDirection = ChatSearchDirection.UP)

    /**
     * Load remote messages from chat server.
     * The method only works when the load data type is [ChatUIKitMessageListLayout.ChatUIKitLoadDataType.ROAM].
     * @param direction The direction of loading. [ChatSearchDirection.UP] or [ChatSearchDirection.DOWN].
     */
    fun fetchRoamMessages(direction: ChatSearchDirection = ChatSearchDirection.UP)

    /**
     * Load more remote messages from chat server.
     * The method only works when the load data type is [ChatUIKitMessageListLayout.ChatUIKitLoadDataType.ROAM].
     * @param startMsgId The message id of the first message loaded last time.
     *                   If this parameter is null or "", it will load the first page of messages.
     * @param direction The direction of loading. [ChatSearchDirection.UP] or [ChatSearchDirection.DOWN].
     */
    fun fetchMoreRoamMessages(startMsgId: String? = "", direction: ChatSearchDirection = ChatSearchDirection.UP)

    /**
     * Load local history messages from database.
     * The method only works when the load data type is [ChatUIKitMessageListLayout.ChatUIKitLoadDataType.HISTORY].
     * @param startMsgId The message id of the first message loaded last time.
     *                   If this parameter is null or "", it will load the first page of messages.
     * @param direction The direction of loading. [ChatSearchDirection.UP] or [ChatSearchDirection.DOWN].
     * @param isFirst Whether it is the first time to load history messages.
     */
    fun loadLocalHistoryMessages(startMsgId: String?, direction: ChatSearchDirection, isFirst: Boolean = false)

    /**
     * Load more local messages from the database for retrieval target messages.
     * @param msgId
     * @param pageSize
     */
    fun loadMoreRetrievalsMessages(msgId: String?, pageSize: Int)

    /**
     * Remove the target message.
     */
    fun removeMessage(message: ChatMessage?, isDeleteServerMessage: Boolean = false)

}