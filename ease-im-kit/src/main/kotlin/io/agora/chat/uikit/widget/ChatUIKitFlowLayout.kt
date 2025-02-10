package io.agora.chat.uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import io.agora.chat.uikit.R
import kotlin.math.max

/**
 * A custom view group that can display child views in a single row.
 */
class ChatUIKitFlowLayout @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): ViewGroup(context, attrs, defStyleAttr) {

    private val mObserver: ViewDataObserver = ViewDataObserver()
    private var mAdapter: Adapter<*>? = null
    private var horizontalSpace: Int = 0
    private var alignToRight: Boolean = false
    private var maxSingleRowChildCount = 0

    init {
        initAttr(context, attrs, defStyleAttr)
    }

    private fun initAttr(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitFlowLayout, defStyleAttr, 0)
        alignToRight = a.getBoolean(R.styleable.ChatUIKitFlowLayout_alignRight, false)
        horizontalSpace = a.getDimensionPixelSize(R.styleable.ChatUIKitFlowLayout_horizontalMargin, 0)
        a.recycle()
    }

    fun setAlignToRight(alignToRight: Boolean) {
        this.alignToRight = alignToRight
        requestLayout()
    }

    private fun isAlignToRight(): Boolean {
        return alignToRight
    }

    fun setHorizontalSpace(space: Int) {
        horizontalSpace = space
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val isExactly = widthMode == MeasureSpec.EXACTLY

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        var totalWidth = 0
        var maxHeight = 0
        maxSingleRowChildCount = 0
        //Measure children width, margin and padding
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            val layoutParams = child.layoutParams as MarginLayoutParams
            val tempTotalWidth = totalWidth + child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin

            // If the total width of the child (exclude horizontalSpace) is greater than the width of the parent, then break
            if (tempTotalWidth > widthSize) {
                // The last not add horizontalSpace
                totalWidth -= horizontalSpace
                break
            } else {
                totalWidth = tempTotalWidth + horizontalSpace

                maxSingleRowChildCount = i + 1
            }

            maxHeight = max(maxHeight, child.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin)
        }

        val height = if (heightMode == MeasureSpec.EXACTLY) heightSize else maxHeight
        if (isExactly) {
            setMeasuredDimension(widthSize, height)
        } else {
            setMeasuredDimension(totalWidth, height)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = 0
        var right = r - l
        var top = 0
        var isRowLast = false
        for (i in 0 until maxSingleRowChildCount) {
            isRowLast = if (isAlignToRight()) maxSingleRowChildCount - 1 - i == 0 else i == maxSingleRowChildCount - 1
            val child = if (isAlignToRight()) getChildAt(maxSingleRowChildCount - 1 - i) else getChildAt(i)
            val layoutParams = child.layoutParams as MarginLayoutParams
            val width = child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin

            if(isAlignToRight()) {
                if (right - width < 0)
                    break
                child.layout(right - width + layoutParams.leftMargin, top + layoutParams.topMargin,
                    right - layoutParams.rightMargin, top + layoutParams.topMargin + child.measuredHeight)
                right -= (width + if (isRowLast) 0 else horizontalSpace)
            } else {
                if (left + width > r)
                    break
                child.layout(left + layoutParams.leftMargin, top + layoutParams.topMargin
                    , left + layoutParams.leftMargin + child.measuredWidth
                    , top + layoutParams.topMargin + child.measuredHeight)
                left += (width + if (isRowLast) 0 else horizontalSpace)
            }
        }
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

    fun <VH: ViewHolder> setAdapter(adapter: Adapter<VH>) {
        if (mAdapter != null) {
            mAdapter?.unregisterAdapterDataObserver(mObserver)
            mAdapter?.onDetachedFromRecyclerView(this)
        }
        removeAllViews()
        mAdapter = adapter
        adapter.registerAdapterDataObserver(mObserver)
        adapter.onAttachedToRecyclerView(this)
        for (i in 0 until adapter.getItemCount()) {
            val viewHolder = adapter.onCreateViewHolder(this, adapter.getItemViewType(i))
            adapter.onBindViewHolder(viewHolder, i)
            addView(viewHolder.itemView)
        }
    }

    private fun <VH: ViewHolder> reloadViews() {
        removeAllViews()
        (mAdapter as? Adapter<VH>)?.let {
            for (i in 0 until it.getItemCount()) {
                val viewHolder = it.onCreateViewHolder(this, it.getItemViewType(i))
                it.onBindViewHolder(viewHolder, i)
                addView(viewHolder.itemView)
            }
        }
    }

    abstract class ViewHolder(val itemView: View)

    abstract class Adapter<VH: ViewHolder> {

        private var mObservable: AdapterDataObserver? = null
        abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
        abstract fun onBindViewHolder(holder: VH, position: Int)
        abstract fun getItemCount(): Int

        open fun getItemViewType(position: Int): Int {
            return 0
        }

        fun notifyDataSetChanged() {
            mObservable?.onChanged()
        }

        open fun onAttachedToRecyclerView(view: ChatUIKitFlowLayout) {}
        open fun onDetachedFromRecyclerView(view: ChatUIKitFlowLayout) {}

        fun registerAdapterDataObserver(observer: AdapterDataObserver) {
            mObservable = observer
        }

        fun unregisterAdapterDataObserver(observer: AdapterDataObserver) {
            mObservable = null
        }
    }

    open abstract class ChatUIKitFlowAdapter<T, VH: ViewHolder>: Adapter<VH>() {
        private var mOnItemClickListener: ((View, Int) -> Unit)? = null
        private var mOnItemLongClickListener: ((View, Int) -> Boolean)? = null
        private var mData: List<T>? = null

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.itemView.setOnClickListener {
                mOnItemClickListener?.invoke(it, position)
            }
            holder.itemView.setOnLongClickListener {
                mOnItemLongClickListener?.invoke(it, position) ?: false
            }

        }

        override fun getItemCount(): Int {
            return mData?.size ?: 0
        }

        fun setData(data: List<T>) {
            mData = data
            notifyDataSetChanged()
        }

        fun getData(): List<T>? {
            return mData
        }

        fun setOnItemClickListener(listener: (View, Int) -> Unit) {
            mOnItemClickListener = listener
        }

        fun setOnItemLongClickListener(listener: (View, Int) -> Boolean) {
            mOnItemLongClickListener = listener
        }
    }

    private inner class ViewDataObserver: AdapterDataObserver() {
        override fun onChanged() {
            reloadViews<ViewHolder>()
        }
    }

    abstract class AdapterDataObserver {
        open fun onChanged() {
            // Do nothing
        }
    }

}