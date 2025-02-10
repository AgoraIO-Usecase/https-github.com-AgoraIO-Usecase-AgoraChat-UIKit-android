package io.agora.chat.uikit.common.enums

enum class ChatUIKitTranslationLanguageType(
    val value:String
) {
    Chinese("zh"),
    Chinese_traditional("zh-Hant"),
    English("en"),
    Russian("ru"),
    German("de"),
    French("fr"),
    Japanese("ja"),
    Korean("ko"),
    Auto("auto");

    companion object {
        fun from(value: String): ChatUIKitTranslationLanguageType {
            val types = ChatUIKitTranslationLanguageType.values()
            val length = types.size
            for (i in 0 until length) {
                val type = types[i]
                if (type.value == value) {
                    return type
                }
            }
            return Chinese
        }
    }
}
