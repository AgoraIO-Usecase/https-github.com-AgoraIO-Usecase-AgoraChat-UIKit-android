package io.agora.chat.uikit.feature.chat.interfaces

interface IChatInputMenu {
    /**
     * Set custom menu
     * @param menu
     */
    fun setCustomPrimaryMenu(menu: IChatPrimaryMenu?)

    /**
     * Set up a custom emoji
     * @param menu
     */
    fun setCustomEmojiconMenu(menu: IChatEmojiconMenu?)

    /**
     * Set up a custom extended menu
     * @param menu
     */
    fun setCustomExtendMenu(menu: IChatExtendMenu?)

    /**
     * Set custom top extension menu.
     * @param menu
     */
    fun setCustomTopExtendMenu(menu: IChatTopExtendMenu?)

    /**
     * Hide extended area (including emoticons and extended menu)
     */
    fun hideExtendContainer()

    /**
     * Hide input menu exclude top extend menu.
     */
    fun hideInputMenu()

    /**
     * Whether to show the primary menu
     * @param show
     */
    fun showPrimaryMenu(show: Boolean)

    /**
     * Whether to show the emoji menu
     * @param show
     */
    fun showEmojiconMenu(show: Boolean)

    /**
     * Whether to show the extended menu
     * @param show
     */
    fun showExtendMenu(show: Boolean)

    /**
     * Whether to show the top extension menu
     * @param isShow
     */
    fun showTopExtendMenu(isShow: Boolean)

    /**
     * Hide soft keyboard
     */
    fun hideSoftKeyboard()

    /**
     * Set menu listener
     * @param listener
     */
    fun setChatInputMenuListener(listener: ChatInputMenuListener?)

    /**
     * Get menu
     * @return
     */
    val chatPrimaryMenu: IChatPrimaryMenu?

    /**
     * Get emoji menu
     * @return
     */
    val chatEmojiMenu: IChatEmojiconMenu?

    /**
     * Get extended menu
     * @return
     */
    val chatExtendMenu: IChatExtendMenu?

    /**
     * Get the top extension menu
     * @return
     */
    val chatTopExtendMenu: IChatTopExtendMenu?

    /**
     * Click back
     * @return
     */
    fun onBackPressed(): Boolean
}