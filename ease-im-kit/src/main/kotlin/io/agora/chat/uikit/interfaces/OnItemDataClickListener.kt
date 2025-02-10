package io.agora.chat.uikit.interfaces

interface OnItemDataClickListener {
    /**
     * Item click.
     * @param position
     * @param data
     */
    fun onItemClick(data: Any?, position: Int)
}