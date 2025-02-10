package io.agora.chat.uikit.common.dialog

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.lang.ref.WeakReference

internal class DialogViewHelper {
    private val mContext: Context

    /**
     * @return the dialog's view
     */
    val contentView: View?
    private val views =
        SparseArray<WeakReference<View?>>() //Use WeakReference to prevent memory leaks

    constructor(mContext: Context) {
        this.mContext = mContext
        contentView = null
    }

    constructor(mContext: Context, contentView: View?) {
        this.mContext = mContext
        this.contentView = contentView
    }

    constructor(mContext: Context, contentViewId: Int) {
        this.mContext = mContext
        contentView = LayoutInflater.from(mContext).inflate(contentViewId, null)
    }

    /**
     * Set click listener
     * @param viewId
     * @param onClickListener
     */
    fun setOnClickListener(viewId: Int, onClickListener: View.OnClickListener?) {
        val view = getViewById<View>(viewId)
        view?.setOnClickListener(onClickListener)
    }

    fun <T : View?> getViewById(viewId: Int): T? {
        var view: View? = null
        val weakReference = views[viewId]
        if (weakReference != null) {
            view = weakReference.get()
        } else {
            if (contentView != null) {
                view = contentView.findViewById(viewId)
                views.put(viewId, WeakReference(view))
            }
        }
        return view as T?
    }

    /**
     * Set text
     * @param viewId
     * @param text
     */
    fun setText(viewId: Int, text: CharSequence?) {
        val view = getViewById<TextView>(viewId)
        if (view != null) {
            view.text = text
            //Make hyperlink clickable
            view.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    /**
     * Set Image View by resource ID
     * @param viewId
     * @param resId
     */
    fun setImageView(viewId: Int, resId: Int) {
        val imageView = getViewById<ImageView>(viewId)
        imageView?.setImageResource(resId)
    }
}