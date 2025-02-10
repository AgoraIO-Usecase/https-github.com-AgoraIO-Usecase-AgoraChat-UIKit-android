package io.agora.chat.uikit.common

/**
 * Refer to：https://blog.csdn.net/Y_sunny_U/article/details/89500464
 */
interface PageDecorationLastJudge {
    /**
     * Is the last row in one page
     *
     * @param position
     * @return
     */
    fun isLastRow(position: Int): Boolean

    /**
     * Is the last Colum in one row;
     *
     * @param position
     * @return
     */
    fun isLastColumn(position: Int): Boolean
    fun isPageLast(position: Int): Boolean
}