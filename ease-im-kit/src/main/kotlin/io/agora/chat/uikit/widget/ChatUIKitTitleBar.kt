package io.agora.chat.uikit.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import coil.load
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.extensions.dpToPx
import io.agora.chat.uikit.common.extensions.getScreenInfo
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.databinding.UikitWidgetTitleBarBinding
import kotlinx.coroutines.launch
import kotlin.math.abs

class ChatUIKitTitleBar @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = androidx.appcompat.R.attr.toolbarStyle
): RelativeLayout(context, attrs, defStyle) {

    private val binding: UikitWidgetTitleBarBinding by lazy {
        UikitWidgetTitleBarBinding.inflate(LayoutInflater.from(context), this, true)
    }
    private val toolbar = binding.toolbar
    private var menuIconTint: Int = -1
    private var menuTitleColor: Int = -1
    private var isCentered = false

    init {
        init(context, attrs, defStyle)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyle: Int) {
        parseStyle(context, attrs, defStyle)
    }

    private fun parseStyle(context: Context, attrs: AttributeSet?, defStyle: Int) {
        context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitTitleBar, defStyle, 0)?.let { a ->
            val toolBarDisplayHomeAsUpEnabled = a.getBoolean(R.styleable.ChatUIKitTitleBar_titleBarDisplayHomeAsUpEnabled, true)
            setDisplayHomeAsUpEnabled(toolBarDisplayHomeAsUpEnabled, a.getBoolean(R.styleable.ChatUIKitTitleBar_titleBarReplaceActionBar, false))
            a.getDimensionPixelSize(R.styleable.ChatUIKitTitleBar_titleBarLogoSize, -1).let {
                if (it != -1) {
                    setLogoSize(it)
                }
            }
            a.getDrawable(R.styleable.ChatUIKitTitleBar_titleBarLogo)?.let {
                setLogo(it)
            }
            if (a.hasValue(R.styleable.ChatUIKitTitleBar_titleBarMenuTitleColor)) {
                setMenuTitleColor(a.getColor(R.styleable.ChatUIKitTitleBar_titleBarMenuTitleColor, ContextCompat.getColor(context, R.color.ease_title_bar_menu_text_color)))
            }
            if (a.hasValue(R.styleable.ChatUIKitTitleBar_titleBarMenuIconTint)) {
                setMenuIconTint(a.getColor(R.styleable.ChatUIKitTitleBar_titleBarMenuIconTint, ContextCompat.getColor(context, R.color.ease_title_bar_menu_icon_tint_color)))
            }
            setTitleTextAppearance(context, a.getResourceId(R.styleable.ChatUIKitTitleBar_titleBarTitleTextAppearance, 0))
            setSubtitleTextAppearance(context, a.getResourceId(R.styleable.ChatUIKitTitleBar_titleBarSubtitleTextAppearance, 0))

            val title = a.getText(R.styleable.ChatUIKitTitleBar_titleBarTitle)
            if (!TextUtils.isEmpty(title)) {
                setTitle(title)
            }

            val subtitle = a.getText(R.styleable.ChatUIKitTitleBar_titleBarSubtitle)
            if (!TextUtils.isEmpty(subtitle)) {
                setSubtitle(subtitle)
            }

            setTitleCentered(a.getBoolean(R.styleable.ChatUIKitTitleBar_titleBarTitleCenter, false))

            toolbar.popupTheme = a.getResourceId(R.styleable.ChatUIKitTitleBar_titleBarPopupTheme, 0)

            val navIcon = a.getDrawable(R.styleable.ChatUIKitTitleBar_titleBarNavigationIcon)
            navIcon?.let {
                setNavigationIcon(it)
            }

            if (a.hasValue(R.styleable.ChatUIKitTitleBar_titleBarNavigationIconTint)) {
                setNavigationIconTint(a.getColor(R.styleable.ChatUIKitTitleBar_titleBarNavigationIconTint, -1))
            }

            val navDesc = a.getText(R.styleable.ChatUIKitTitleBar_titleBarNavigationContentDescription)
            if (!TextUtils.isEmpty(navDesc)) {
                toolbar.navigationContentDescription = navDesc
            }

            val logo = a.getDrawable(R.styleable.ChatUIKitTitleBar_titleBarLogo)
            logo?.let { setLogo(it) }

            val logoDesc = a.getText(R.styleable.ChatUIKitTitleBar_titleBarLogoDescription)
            if (!TextUtils.isEmpty(logoDesc)) {
                setLogoDescription(logoDesc)
            }

            if (a.hasValue(R.styleable.ChatUIKitTitleBar_titleBarTitleTextColor)) {
                setTitleTextColor(a.getColorStateList(R.styleable.ChatUIKitTitleBar_titleBarTitleTextColor))
            }

            if (a.hasValue(R.styleable.ChatUIKitTitleBar_titleBarSubtitleTextColor)) {
                setSubtitleTextColor(a.getColorStateList(R.styleable.ChatUIKitTitleBar_titleBarSubtitleTextColor))
            }

            if (a.hasValue(R.styleable.ChatUIKitTitleBar_titleBarMenu)) {
                inflateMenu(a.getResourceId(R.styleable.ChatUIKitTitleBar_titleBarMenu, 0))
            }

            a.recycle()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        binding.markLayer.let {
            val layoutParams = it.layoutParams as MarginLayoutParams
            if ((it.layoutParams as MarginLayoutParams).marginStart != calculateMarginStart() ||
                (it.layoutParams as MarginLayoutParams).marginEnd != calculateMarginEnd() ){
                layoutParams.marginStart = calculateMarginStart()
                layoutParams.marginEnd = calculateMarginEnd()
                it.layoutParams = layoutParams
            }

            if (isCentered) {
                binding.tvTitle.let { tv ->
                    val lp = (tv.layoutParams as ConstraintLayout.LayoutParams)
                    if ((tv.layoutParams as ConstraintLayout.LayoutParams).marginStart != lp.marginStart ||
                        (tv.layoutParams as ConstraintLayout.LayoutParams).marginEnd != lp.marginEnd){
                        lp.apply {
                            val margin = getTitleMargin()
                            if (margin > 0) {
                                this.marginEnd = abs(margin)
                            } else {
                                this.marginStart = abs(margin)
                            }
                        }
                        tv.layoutParams = lp
                    }

                }
                binding.tvSubtitle.let { tv ->
                    val lp = (tv.layoutParams as ConstraintLayout.LayoutParams)
                    if ((tv.layoutParams as ConstraintLayout.LayoutParams).marginStart != lp.marginStart ||
                        (tv.layoutParams as ConstraintLayout.LayoutParams).marginEnd != lp.marginEnd){
                        lp.apply {
                            val margin = getTitleMargin()
                            if (margin > 0) {
                                this.marginEnd = abs(margin)
                            } else {
                                this.marginStart = abs(margin)
                            }
                        }
                        tv.layoutParams = lp
                    }
                }
            }
        }

        toolbar.let {
            ToolbarUtils.getTitleTextView(it)?.visibility = GONE
            ToolbarUtils.getSubtitleTextView(it)?.visibility = GONE
            ToolbarUtils.getLogoImageView(it)?.visibility = GONE
        }
    }


    /**
     * Set whether to display the return button.
     */
    fun setDisplayHomeAsUpEnabled(enableDisplayHomeAsUp: Boolean, replaceActionBar: Boolean = false) {
        if (replaceActionBar) {
            if (context is AppCompatActivity) {
                if (context.supportActionBar == null) {
                    context.setSupportActionBar(toolbar)
                }
                context.supportActionBar?.setDisplayHomeAsUpEnabled(enableDisplayHomeAsUp)
            }
        } else {
            if (enableDisplayHomeAsUp) {
                toolbar.setNavigationIcon(R.drawable.uikit_default_navigation_icon)
            } else {
                toolbar.navigationIcon = null
            }
        }
    }

    /**
     * Set the icon to use for the toolbar's navigation button.
     *
     *
     * The navigation button appears at the start of the toolbar if present. Setting an icon
     * will make the navigation button visible.
     *
     *
     * If you use a navigation icon you should also set a description for its action using
     * [.setNavigationContentDescription]. This is used for accessibility and
     * tooltips.
     *
     * @param resId Resource ID of a drawable to set
     *
     * [androidx.appcompat.R.attr.navigationIcon]
     */
    fun setNavigationIcon(@DrawableRes resId: Int) {
        setNavigationIcon(AppCompatResources.getDrawable(getContext(), resId))
    }

    /**
     * Set the icon to use for the toolbar's navigation button.
     *
     *
     * The navigation button appears at the start of the toolbar if present. Setting an icon
     * will make the navigation button visible.
     *
     *
     * If you use a navigation icon you should also set a description for its action using
     * [.setNavigationContentDescription]. This is used for accessibility and
     * tooltips.
     *
     * @param icon Drawable to set, may be null to clear the icon
     *
     * [androidx.appcompat.R.attr.navigationIcon]
     */
    fun setNavigationIcon(icon: Drawable?) {
        toolbar.navigationIcon = icon
    }

    /**
     * Sets the color of the toolbar's navigation icon.
     *
     * @see .setNavigationIcon
     */
    fun setNavigationIconTint(@ColorInt navigationIconTint: Int) {
        toolbar.setNavigationIconTint(navigationIconTint)
    }

    /**
     * Set logo icon size.
     */
    fun setLogoSize(size: Int) {
        binding.ivIcon.let {
            it.layoutParams.width = size
            it.layoutParams.height = size
        }
    }

    fun setLogo(@DrawableRes resId: Int) {
        setLogo(AppCompatResources.getDrawable(getContext(), resId))
    }

    fun setLogo(drawable: Drawable?) {
        binding.ivIcon.let {
            it.setImageDrawable(drawable)
        }
    }

    /**
     * Set remote url.
     */
    fun setLogo(data: Any?, @DrawableRes placeResource: Int,
                size: Int = context.resources.getDimensionPixelSize(R.dimen.ease_toolbar_height)) {
        setLogoSize(size)
        context.mainScope().launch {
            binding.ivIcon.load(data) {
                placeholder(placeResource)
                error(placeResource)
                size(size)
            }
        }
    }

    fun setLogoStatus(@DrawableRes resId: Int){
        setLogoStatus(AppCompatResources.getDrawable(getContext(), resId))
    }

    fun setLogoStatus(drawable: Drawable?){
        drawable?.let {
            binding.ivStatus.setImageDrawable(it)
        }
    }

    fun setLogoStatusSize(size: Int) {
        binding.ivStatus.let {
            it.layoutParams.width = size
            it.layoutParams.height = size
        }
    }

    fun setLogoStatusMargin(
        start:Int? = 0,
        top:Int? = 0,
        end:Int? = 0,
        bottom:Int? = 0
    ){
        val layoutParams = binding.ivStatus.layoutParams as MarginLayoutParams
        start?.let {
            layoutParams.marginStart = it.dpToPx(context)
        }
        top?.let {
            layoutParams.topMargin = it.dpToPx(context)
        }
        end?.let {
            layoutParams.marginEnd = it.dpToPx(context)
        }
        bottom?.let {
            layoutParams.bottomMargin = it.dpToPx(context)
        }
        binding.ivStatus.layoutParams = layoutParams
    }

    fun setLogoDescription(@StringRes resId: Int) {
        setLogoDescription(getContext().getText(resId))
    }

    /**
     * Set a description of the toolbar's logo.
     *
     *
     * This description will be used for accessibility or other similar descriptions
     * of the UI.
     *
     * @param description Description to set
     */
    fun setLogoDescription(description: CharSequence?) {
        binding.ivIcon.contentDescription = description
    }

    /**
     * Set menu title color.
     */
    fun setMenuTitleColor(@ColorInt colorInt: Int) {
        if (colorInt == -1) {
            return
        }
        menuTitleColor = colorInt
        toolbar.menu?.forEach { item ->
            item.title?.let {
                SpannableStringBuilder(it).let { span ->
                    span.setSpan(ForegroundColorSpan(colorInt), 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    item.title = span
                }
            }
        }
    }

    /**
     * Set menu icon tint.
     */
    fun setMenuIconTint(@ColorInt colorInt: Int) {
        if (colorInt == -1) {
            return
        }
        menuIconTint = colorInt
        toolbar.menu?.forEach { item ->
            item.icon?.let {
                it.setTint(colorInt)
            }
        }
    }

    /**
     * Set menu icon visible.
     */
    fun setMenuIconVisible(id:Int,visible:Boolean){
        toolbar.menu?.forEach { item ->
            if (item.itemId == id){
                item.isVisible = visible
            }
        }
    }

    fun setTitleCentered(centered: Boolean) {
        isCentered = centered
        toolbar.isTitleCentered = centered
        if (centered) {
            val set = ConstraintSet()
            set.clone(binding.markLayer)
            set.clear(binding.ivIcon.id, ConstraintSet.END)

            // reset subtitle
            set.clear(binding.tvSubtitle.id, ConstraintSet.START)
            set.connect(binding.tvSubtitle.id, ConstraintSet.START, binding.ivIcon.id, ConstraintSet.END)
            set.setHorizontalBias(binding.tvSubtitle.id, 0.5f)
            set.applyTo(binding.markLayer)
        } else {
            val set = ConstraintSet()
            set.clone(binding.markLayer)
            set.connect(binding.ivIcon.id, ConstraintSet.END, binding.tvTitle.id, ConstraintSet.START)
            set.applyTo(binding.markLayer)
        }
    }

    /**
     * Returns the title of this toolbar.
     *
     * @return The current title.
     */
    fun getTitle(): CharSequence? {
        return binding.tvTitle.text
    }

    fun getTitleView():TextView{
        return binding.tvTitle
    }

    /**
     * Set the title of this toolbar.
     *
     *
     * A title should be used as the anchor for a section of content. It should
     * describe or name the content being viewed.
     *
     * @param resId Resource ID of a string to set as the title
     */
    fun setTitle(@StringRes resId: Int) {
        setTitle(getContext().getText(resId))
    }

    /**
     * Set the title of this toolbar.
     *
     *
     * A title should be used as the anchor for a section of content. It should
     * describe or name the content being viewed.
     *
     * @param title Title to set
     */
    fun setTitle(title: CharSequence?) {
        binding.tvTitle.text = title
    }

    fun setTitleEndDrawable(@DrawableRes resId: Int?,spacing:Int? = 0){
        if (resId != null){
            setTitleEndDrawable(AppCompatResources.getDrawable(getContext(), resId),spacing)
        }else{
            setTitleEndDrawable()
        }
    }

    fun setTitleEndDrawable(drawable: Drawable? = null,spacing:Int? = 0){
        binding.tvTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
        spacing?.let {
            binding.tvTitle.compoundDrawablePadding = it.dpToPx(context)
        }
    }

    /**
     * Return the subtitle of this toolbar.
     *
     * @return The current subtitle
     */
    fun getSubtitle(): CharSequence? {
        return binding.tvSubtitle.text
    }

    /**
     * Set the subtitle of this toolbar.
     *
     *
     * Subtitles should express extended information about the current content.
     *
     * @param resId String resource ID
     */
    fun setSubtitle(@StringRes resId: Int) {
        setSubtitle(getContext().getText(resId))
    }

    /**
     * Set the subtitle of this toolbar.
     *
     *
     * Subtitles should express extended information about the current content.
     *
     * @param subtitle Subtitle to set
     */
    fun setSubtitle(subtitle: CharSequence?) {
        binding.tvSubtitle.text = subtitle
        if (binding.tvSubtitle.visibility != VISIBLE) {
            binding.tvSubtitle.visibility = VISIBLE
        }
    }

    /**
     * Sets the text color, size, style, hint color, and highlight color
     * from the specified TextAppearance resource.
     */
    fun setTitleTextAppearance(context: Context?, @StyleRes resId: Int) {
        if (resId != 0) {
            binding.tvTitle.setTextAppearance(context, resId)
        }
    }

    /**
     * Sets the text color, size, style, hint color, and highlight color
     * from the specified TextAppearance resource.
     */
    fun setSubtitleTextAppearance(context: Context?, @StyleRes resId: Int) {
        if (resId != 0) {
            binding.tvSubtitle.setTextAppearance(context, resId)
        }
    }

    /**
     * Sets the text color of the title, if present.
     *
     * @param color The new text color in 0xAARRGGBB format
     */
    fun setTitleTextColor(@ColorInt color: Int) {
        if (color != 0) {
            setTitleTextColor(ColorStateList.valueOf(color))
        }
    }

    /**
     * Sets the text color of the title, if present.
     *
     * @param color The new text color
     */
    fun setTitleTextColor(color: ColorStateList?) {
        if (color != null) {
            binding.tvTitle.setTextColor(color)
        }
    }

    /**
     * Sets the text color of the subtitle, if present.
     *
     * @param color The new text color in 0xAARRGGBB format
     */
    fun setSubtitleTextColor(@ColorInt color: Int) {
        if (color != 0) {
            setSubtitleTextColor(ColorStateList.valueOf(color))
        }
    }

    /**
     * Sets the text color of the subtitle, if present.
     *
     * @param color The new text color
     */
    fun setSubtitleTextColor(color: ColorStateList?) {
        if (color != null) {
            binding.tvSubtitle.setTextColor(color)
        }
    }

    /**
     * Get the toolbar.
     */
    fun getToolBar(): Toolbar {
        return toolbar
    }

    private fun calculateMarginStart(): Int {
        return ToolbarUtils.getNavigationIconButton(binding.toolbar)?.right ?: 0
    }

    private fun calculateMarginEnd(): Int {
        return ToolbarUtils.getActionMenuView(binding.toolbar)?.let {
            (context.getScreenInfo()[0] - it.left).toInt()
        } ?: 0
    }

    /**
     * If result > 0 , means start margin is bigger than end margin.
     */
    private fun getTitleMargin(): Int {
        val navigationRight = ToolbarUtils.getNavigationIconButton(binding.toolbar)?.right ?: 0
        val iconRight = binding.ivIcon.right
        val leftMargin = navigationRight + iconRight
        return leftMargin - calculateMarginEnd()
    }

    fun getLogoView(): ChatUIKitImageView? {
        return binding.ivIcon
    }

    fun getStatusView(): ChatUIKitImageView {
        return binding.ivStatus
    }

    /**
     * Set logo click listener.
     */
    fun setLogoClickListener(listener: OnClickListener?) {
        binding.ivIcon.setOnClickListener(listener)
    }

    /**
     * Set title click listener.
     */
    fun setTitleClickListener(listener: OnClickListener?) {
        binding.tvTitle.setOnClickListener(listener)
    }

    fun setNavigationOnClickListener(listener: OnClickListener?) {
        binding.toolbar.setNavigationOnClickListener(listener)
    }

    /**
     * Set a listener to respond to menu item click events.
     *
     *
     * This listener will be invoked whenever a user selects a menu item from
     * the action buttons presented at the end of the toolbar or the associated overflow.
     *
     * @param listener Listener to set
     */
    fun setOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener) {
        binding.toolbar.setOnMenuItemClickListener(listener)
    }

    /**
     * Inflate a menu resource into this toolbar.
     *
     *
     * Inflate an XML menu resource into this toolbar. Existing items in the menu will not
     * be modified or removed.
     *
     * @param resId ID of a menu resource to inflate
     * [androidx.appcompat.R.attr.menu]
     */
    fun inflateMenu(@MenuRes resId: Int) {
        binding.toolbar.inflateMenu(resId)
        setMenuIconTint(menuIconTint)
        setMenuTitleColor(menuTitleColor)
    }

    fun hideDefaultMenu(){
        toolbar.menu?.forEach { item ->
            item.icon = null
            item.isEnabled = false
        }
    }

}