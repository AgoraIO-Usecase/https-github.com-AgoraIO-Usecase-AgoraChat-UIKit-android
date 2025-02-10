package io.agora.chat.uikit.feature.group.config

import android.util.TypedValue
import android.view.View
import io.agora.chat.uikit.databinding.UikitLayoutGroupListItemBinding
import io.agora.chat.uikit.widget.ChatUIKitImageView


fun ChatUIKitGroupListConfig.bindView(binding: UikitLayoutGroupListItemBinding){
    with(binding) {
        if (itemNameTextSize != -1) groupName.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemNameTextSize.toFloat())
        if (itemNameTextColor != -1) groupName.setTextColor(itemNameTextColor)
        if (avatarSize != -1) {
            val layoutParams = groupAvatar.layoutParams
            layoutParams.height = avatarSize
            layoutParams.width = avatarSize
        }
        if (avatarShape != ChatUIKitImageView.ShapeType.NONE) {
            groupAvatar.setShapeType(avatarShape)
        }
        if (avatarBorderWidth != -1) {
            groupAvatar.setBorderWidth(avatarBorderWidth)
        }
        if (avatarBorderColor != -1) {
            groupAvatar.setBorderColor(avatarBorderColor)
        }
        if (itemHeight != -1f) {
            val layoutParams = root.layoutParams
            layoutParams.height = itemHeight.toInt()
        }
        if (!isShowDivider) divider.visibility = View.GONE
    }
}