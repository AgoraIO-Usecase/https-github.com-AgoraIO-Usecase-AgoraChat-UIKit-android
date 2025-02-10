package io.agora.chat.uikit.feature.contact.interfaces

import io.agora.chat.uikit.feature.conversation.interfaces.IConvItemTextStyle
import io.agora.chat.uikit.common.interfaces.IAvatarStyle

interface IContactItemStyle : IAvatarStyle, IConvItemTextStyle {
    fun setItemHeight(height: Int)
}