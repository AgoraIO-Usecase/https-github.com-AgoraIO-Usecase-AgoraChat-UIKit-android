package io.agora.chat.uikit.common.extensions

const val URL_REGEX: String = ("(((https|http)?://)?([a-z0-9]+[.])|(www.))"
        + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)");