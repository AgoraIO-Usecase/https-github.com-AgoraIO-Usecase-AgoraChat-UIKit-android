package io.agora.chat.uikit.feature.contact.config

import android.util.TypedValue
import io.agora.chat.uikit.databinding.UikitLayoutItemHeaderBinding


fun ChatUIKitContactHeaderConfig.bindView(binding: UikitLayoutItemHeaderBinding){
    with(binding) {

        if (arrowItemTitleSize != -1) headerItem.tvTitle?.setTextSize(TypedValue.COMPLEX_UNIT_PX, arrowItemTitleSize.toFloat())
        if (arrowItemTitleColor != -1) headerItem.tvTitle?.setTextColor(arrowItemTitleColor)
        if (arrowItemContentSize != -1) headerItem.tvContent?.setTextSize(TypedValue.COMPLEX_UNIT_PX, arrowItemContentSize.toFloat())
        if (arrowItemContentColor != -1) headerItem.tvContent?.setTextColor(arrowItemContentColor)
        if (arrowItemTitleStyle != -1) headerItem.setTvStyle(arrowItemTitleStyle)
        if (arrowItemContentSize != -1) unreadCount.setTextSize(TypedValue.COMPLEX_UNIT_PX, arrowItemContentSize.toFloat())
        if (arrowItemUnReadCountColor != -1)unreadCount.setTextColor(arrowItemUnReadCountColor)
        if (arrowItemUnReadCountLayoutDrawable != -1) unreadCount.setBackgroundResource(arrowItemUnReadCountLayoutDrawable)

        if (itemHeight != -1f) {
            val layoutParams = root.layoutParams
            layoutParams.height = itemHeight.toInt()
        }
    }
}