package io.agora.chat.uikit.widget.photoview

import android.content.Context
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import io.agora.chat.uikit.widget.photoview.PhotoViewAttacher.OnMatrixChangedListener

class ChatUIKitPhotoView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attr, defStyle), IPhotoView {
    private var mAttacher: PhotoViewAttacher?

    private var mPendingScaleType: ScaleType? = null

    init {
        super.setScaleType(ScaleType.MATRIX)
        mAttacher = PhotoViewAttacher(this)
        if (null != mPendingScaleType) {
            scaleType = mPendingScaleType
            mPendingScaleType = null
        }
    }

    override fun canZoom(): Boolean? {
        return mAttacher?.canZoom()
    }

    override fun getDisplayRect(): RectF? {
        return mAttacher?.getDisplayRect()
    }

    override fun getMinScale(): Float? {
        return mAttacher?.getMinScale()
    }

    override fun getMidScale(): Float? {
        return mAttacher?.getMidScale()
    }

    override fun getMaxScale(): Float? {
        return mAttacher?.getMaxScale()
    }

    override fun getScale(): Float? {
        return mAttacher?.getScale()
    }

    override fun getScaleType(): ScaleType? {
        return mAttacher?.getScaleType()
    }

    override fun setAllowParentInterceptOnEdge(allow: Boolean) {
        mAttacher?.setAllowParentInterceptOnEdge(allow)
    }

    override fun setMinScale(minScale: Float) {
        mAttacher?.setMinScale(minScale)
    }

    override fun setMidScale(midScale: Float) {
        mAttacher?.setMidScale(midScale)
    }

    override fun setMaxScale(maxScale: Float) {
        mAttacher?.setMaxScale(maxScale)
    }

    // setImageBitmap calls through to this method
    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        mAttacher?.update()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        mAttacher?.update()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        mAttacher?.update()
    }

    override fun setOnMatrixChangeListener(listener: OnMatrixChangedListener?) {
        mAttacher?.setOnMatrixChangeListener(listener)
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {
        mAttacher?.setOnLongClickListener(l)
    }

    override fun setOnPhotoTapListener(listener: OnPhotoTapListener?) {
        mAttacher?.setOnPhotoTapListener(listener)
    }

    override fun setOnViewTapListener(listener: OnViewTapListener?) {
        mAttacher?.setOnViewTapListener(listener)
    }

    override fun setScaleType(scaleType: ScaleType?) {
        mAttacher?.setScaleType(scaleType)?:kotlin.run {
            mPendingScaleType = scaleType
        }
    }

    override fun setZoomable(zoomable: Boolean) {
        mAttacher?.setZoomable(zoomable)
    }

    override fun zoomTo(scale: Float, focalX: Float, focalY: Float) {
        mAttacher?.zoomTo(scale, focalX, focalY)
    }

    override fun onDetachedFromWindow() {
        mAttacher?.cleanup()
        super.onDetachedFromWindow()
    }
}