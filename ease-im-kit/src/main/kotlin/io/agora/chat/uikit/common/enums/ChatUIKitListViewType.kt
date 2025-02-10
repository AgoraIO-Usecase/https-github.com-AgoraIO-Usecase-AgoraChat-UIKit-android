package io.agora.chat.uikit.common.enums

enum class ChatUIKitListViewType(val code: Int) {
    LIST_CONTACT(1001),
    LIST_SELECT_CONTACT(1002),
    LIST_GROUP_MEMBER(1003);

    companion object {
        fun fromCode(code: Int): ChatUIKitListViewType? {
            return ChatUIKitListViewType.values().find { it.code == code }
        }
    }
}