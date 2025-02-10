package io.agora.chat.uikit.configs

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R

/**
 * Date format configs.
 */
class ChatUIKitDateFormatConfig {

    /**
     * Whether use the default locale for conversation and chat date format.
     */
    var useDefaultLocale: Boolean? = null
        get() {
            if (field != null) return field
            if (ChatUIKitClient.isInited()) {
                return ChatUIKitClient.getContext()?.resources?.getBoolean(R.bool.ease_date_use_default_locale) ?: false
            }
            return false
        }

    /**
     * The format of today's date in the conversation.
     */
    var convTodayFormat: String = ""
        get() {
            if (field.isNotEmpty()) return field
            if (ChatUIKitClient.isInited()) {
                ChatUIKitClient.getContext()?.resources?.getStringArray(R.array.ease_conversation_date_format_array)?.let {
                    return if (it.isNotEmpty()) it[0] else DEFAULT_CONV_TODAY_FORMAT
                } ?: return DEFAULT_CONV_TODAY_FORMAT
            }
            return DEFAULT_CONV_TODAY_FORMAT
        }

    /**
     * The format of other day's date in the conversation.
     */
    var convOtherDayFormat: String = ""
        get() {
            if (field.isNotEmpty()) return field
            if (ChatUIKitClient.isInited()) {
                ChatUIKitClient.getContext()?.resources?.getStringArray(R.array.ease_conversation_date_format_array)?.let {
                    return if (it.size > 1) it[1] else DEFAULT_CONV_OTHER_DAY_FORMAT
                } ?: return DEFAULT_CONV_OTHER_DAY_FORMAT
            }
            return DEFAULT_CONV_OTHER_DAY_FORMAT
        }

    /**
     * The format of other year's date in the conversation.
     */
    var convOtherYearFormat: String = ""
        get() {
            if (field.isNotEmpty()) return field
            if (ChatUIKitClient.isInited()) {
                ChatUIKitClient.getContext()?.resources?.getStringArray(R.array.ease_conversation_date_format_array)?.let {
                    return if (it.size > 2) it[2] else DEFAULT_CONV_OTHER_YEAR_FORMAT
                } ?: return DEFAULT_CONV_OTHER_YEAR_FORMAT
            }
            return DEFAULT_CONV_OTHER_YEAR_FORMAT
        }

    /**
     * The format of today's date in the chat.
     */
    var chatTodayFormat: String = ""
        get() {
            if (field.isNotEmpty()) return field
            if (ChatUIKitClient.isInited()) {
                ChatUIKitClient.getContext()?.resources?.getStringArray(R.array.ease_chat_date_format_array)?.let {
                    return if (it.isNotEmpty()) it[0] else DEFAULT_CHAT_TODAY_FORMAT
                } ?: return DEFAULT_CHAT_TODAY_FORMAT
            }
            return DEFAULT_CHAT_TODAY_FORMAT
        }

    /**
     * The format of other day's date in the chat.
     */
    var chatOtherDayFormat: String = ""
        get() {
            if (field.isNotEmpty()) return field
            if (ChatUIKitClient.isInited()) {
                ChatUIKitClient.getContext()?.resources?.getStringArray(R.array.ease_chat_date_format_array)?.let {
                    return if (it.size > 1) it[1] else DEFAULT_CHAT_OTHER_DAY_FORMAT
                } ?: return DEFAULT_CHAT_OTHER_DAY_FORMAT
            }
            return DEFAULT_CHAT_OTHER_DAY_FORMAT
        }

    /**
     * The format of other year's date in the chat.
     */
    var chatOtherYearFormat: String = ""
        get() {
            if (field.isNotEmpty()) return field
            if (ChatUIKitClient.isInited()) {
                ChatUIKitClient.getContext()?.resources?.getStringArray(R.array.ease_chat_date_format_array)?.let {
                    return if (it.size > 2) it[2] else DEFAULT_CHAT_OTHER_YEAR_FORMAT
                } ?: return DEFAULT_CHAT_OTHER_YEAR_FORMAT
            }
            return DEFAULT_CHAT_OTHER_YEAR_FORMAT
        }

    companion object {
        const val DEFAULT_CONV_TODAY_FORMAT = "HH:mm"
        const val DEFAULT_CONV_OTHER_DAY_FORMAT = "MMM dd"
        const val DEFAULT_CONV_OTHER_YEAR_FORMAT = "MMM dd, yyyy"
        const val DEFAULT_CHAT_TODAY_FORMAT = "HH:mm"
        const val DEFAULT_CHAT_OTHER_DAY_FORMAT = "MMM dd, HH:mm"
        const val DEFAULT_CHAT_OTHER_YEAR_FORMAT = "MMM dd, yyyy HH:mm"
    }
}