package io.agora.chat.uikit.feature.thread.widgets

enum class ChatUIKitThreadRole{
    /**
     * thread member
     */
    MEMBER,

    /**
     * thread creator
     */
    CREATOR,

    /**
     * group admin
     */
    GROUP_ADMIN,

    /**
     * Unknown identity
     */
    UNKNOWN;

    companion object {
        fun getThreadRole(role: Int): ChatUIKitThreadRole {
            var threadRole = UNKNOWN
            when (role) {
                0 -> threadRole = MEMBER
                1 -> threadRole = CREATOR
                2 -> threadRole = GROUP_ADMIN
            }
            return threadRole
        }
    }
}
