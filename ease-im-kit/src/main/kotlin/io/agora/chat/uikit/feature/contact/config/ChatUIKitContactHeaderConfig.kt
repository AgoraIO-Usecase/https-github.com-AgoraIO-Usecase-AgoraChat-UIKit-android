package io.agora.chat.uikit.feature.contact.config

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class ChatUIKitContactHeaderConfig(
    var arrowItemTitleSize:Int = -1,
    var arrowItemTitleColor:Int = -1,
    var arrowItemContentSize:Int = -1,
    var arrowItemContentColor:Int = -1,
    var arrowItemTitleStyle:Int = -1,
    var arrowItemUnReadCountTextSize:Int = -1,
    @ColorInt var arrowItemUnReadCountColor : Int = -1,
    @DrawableRes var arrowItemUnReadCountLayoutDrawable:Int = -1,
    var itemHeight: Float = -1f,

)
