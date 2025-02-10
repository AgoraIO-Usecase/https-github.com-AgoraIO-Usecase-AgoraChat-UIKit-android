package io.agora.chat.uikit.feature.conversation.interfaces

import androidx.annotation.ColorInt

interface IConvItemTextStyle {
    /**
     * Set name's text size
     * @param textSize px
     */
    fun setNameTextSize(textSize: Int)

    /**
     * Set name's text color
     * @param textColor
     */
    fun setNameTextColor(@ColorInt textColor: Int)

    /**
     * Set message's text size
     * @param textSize px
     */
    fun setMessageTextSize(textSize: Int)

    /**
     * Set message's text color
     * @param textColor
     */
    fun setMessageTextColor(@ColorInt textColor: Int)

    /**
     * Set date's text size
     * @param textSize
     */
    fun setDateTextSize(textSize: Int)

    /**
     * Set date's text color
     * @param textColor
     */
    fun setDateTextColor(@ColorInt textColor: Int)
}