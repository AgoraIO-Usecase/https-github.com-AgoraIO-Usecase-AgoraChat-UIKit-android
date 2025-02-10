package io.agora.chat.uikit.feature.chat.interfaces

internal interface OnMultipleSelectChangeListener {

    /**
     * Callback when the selected data is changed.
     */
    fun onMultipleSelectDataChange(key: String)

    /**
     * Callback when the selected model is changed.
     */
    fun onMultipleSelectModelChange(key: String, isMultiStyle: Boolean)
}