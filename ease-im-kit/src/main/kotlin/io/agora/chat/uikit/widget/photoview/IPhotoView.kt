package io.agora.chat.uikit.widget.photoview

import android.graphics.RectF
import android.view.View.OnLongClickListener
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import io.agora.chat.uikit.widget.photoview.PhotoViewAttacher.OnMatrixChangedListener

interface IPhotoView {
    /**
     * Returns true if the PhotoView is set to allow zooming of Photos.
     *
     * @return true if the PhotoView allows zooming.
     */
    fun canZoom(): Boolean?

    /**
     * Gets the Display Rectangle of the currently displayed Drawable. The
     * Rectangle is relative to this View and includes all scaling and
     * translations.
     *
     * @return - RectF of Displayed Drawable
     */
    fun getDisplayRect(): RectF?

    /**
     * @return The current minimum scale level. What this value represents depends on the current [ImageView.ScaleType].
     */
    fun getMinScale(): Float?

    /**
     * @return The current middle scale level. What this value represents depends on the current [ImageView.ScaleType].
     */
    fun getMidScale(): Float?

    /**
     * @return The current maximum scale level. What this value represents depends on the current [ImageView.ScaleType].
     */
    fun getMaxScale(): Float?

    /**
     * Returns the current scale value
     *
     * @return float - current scale value
     */
    fun getScale(): Float?

    /**
     * Return the current scale type in use by the ImageView.
     */
    fun getScaleType(): ScaleType?

    /**
     * Whether to allow the ImageView's parent to intercept the touch event when the photo is scroll to it's horizontal edge.
     */
    fun setAllowParentInterceptOnEdge(allow: Boolean)

    /**
     * Sets the minimum scale level. What this value represents depends on the current [ImageView.ScaleType].
     */
    fun setMinScale(minScale: Float)

    /**
     * Sets the middle scale level. What this value represents depends on the current [ImageView.ScaleType].
     */
    fun setMidScale(midScale: Float)

    /**
     * Sets the maximum scale level. What this value represents depends on the current [ImageView.ScaleType].
     */
    fun setMaxScale(maxScale: Float)

    /**
     * Register a callback to be invoked when the Photo displayed by this view is long-pressed.
     *
     * @param listener - Listener to be registered.
     */
    fun setOnLongClickListener(listener: OnLongClickListener?)

    /**
     * Register a callback to be invoked when the Matrix has changed for this
     * View. An example would be the user panning or scaling the Photo.
     *
     * @param listener - Listener to be registered.
     */
    fun setOnMatrixChangeListener(listener: OnMatrixChangedListener?)

    /**
     * Register a callback to be invoked when the Photo displayed by this View
     * is tapped with a single tap.
     *
     * @param listener - Listener to be registered.
     */
    fun setOnPhotoTapListener(listener: OnPhotoTapListener?)

    /**
     * Register a callback to be invoked when the View is tapped with a single
     * tap.
     *
     * @param listener - Listener to be registered.
     */
    fun setOnViewTapListener(listener: OnViewTapListener?)

    /**
     * Controls how the image should be resized or moved to match the size of
     * the ImageView. Any scaling or panning will happen within the confines of
     * this [ImageView.ScaleType].
     *
     * @param scaleType - The desired scaling mode.
     */
    fun setScaleType(scaleType: ScaleType?)

    /**
     * Allows you to enable/disable the zoom functionality on the ImageView.
     * When disable the ImageView reverts to using the FIT_CENTER matrix.
     *
     * @param zoomable - Whether the zoom functionality is enabled.
     */
    fun setZoomable(zoomable: Boolean)

    /**
     * Zooms to the specified scale, around the focal point given.
     *
     * @param scale  - Scale to zoom to
     * @param focalX - X Focus Point
     * @param focalY - Y Focus Point
     */
    fun zoomTo(scale: Float, focalX: Float, focalY: Float)
}