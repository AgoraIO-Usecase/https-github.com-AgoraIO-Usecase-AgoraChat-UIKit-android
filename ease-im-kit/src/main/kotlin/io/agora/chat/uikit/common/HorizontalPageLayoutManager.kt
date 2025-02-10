package io.agora.chat.uikit.common

import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * This LayoutManager provides pagination effects similar to ViewPager+GridView.
 * Refer to：https://blog.csdn.net/Y_sunny_U/article/details/89500464
 */
class HorizontalPageLayoutManager(rows: Int, columns: Int) : RecyclerView.LayoutManager(),
    PageDecorationLastJudge {
    private var totalHeight = 0
    private var totalWidth = 0
    private var offsetY = 0
    private var offsetX = 0
    private var rows = 0
    private var columns = 0
    private var pageSize = 0
    private var itemWidth = 0
    private var itemHeight = 0
    private var onePageSize = 0
    private var itemWidthUsed = 0
    private var itemHeightUsed = 0
    private var itemSetHeight = 0
    private var isUseSetHeight = false
    private var heightMode = 0
    private val allItemFrames = SparseArray<Rect>()
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    init {
        this.rows = rows
        this.columns = columns
        onePageSize = rows * columns
    }

    fun setItemHeight(height: Int) {
        itemSetHeight = height
        isUseSetHeight = height > 0
    }

    override fun canScrollHorizontally(): Boolean {
        return true
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        detachAndScrapAttachedViews(recycler)
        val newX = offsetX + dx
        var result = dx
        if (newX > totalWidth) {
            result = totalWidth - offsetX
        } else if (newX < 0) {
            result = 0 - offsetX
        }
        offsetX += result
        offsetChildrenHorizontal(-result)
        recycleAndFillItems(recycler, state)
        return result
    }

    private val usableWidth: Int
        get() = width - paddingLeft - paddingRight
    private val usableHeight: Int
        get() = height - paddingTop - paddingBottom

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        var heightSpec = heightSpec
        heightMode = View.MeasureSpec.getMode(heightSpec)
        if (heightMode == View.MeasureSpec.AT_MOST) {
            if (isUseSetHeight) {
                heightSpec =
                    View.MeasureSpec.makeMeasureSpec(itemSetHeight * rows, View.MeasureSpec.EXACTLY)
            }
            totalHeight = View.MeasureSpec.getSize(heightSpec)
        }
        super.onMeasure(recycler, state, widthSpec, heightSpec)
    }

    /**
     * Returns true using recyclerView for automatic measurement
     * @return
     */
    override fun isAutoMeasureEnabled(): Boolean {
        return false
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        if (state.isPreLayout) {
            return
        }
        //Gets the average width and height of each Item
        itemWidth = usableWidth / columns
        itemHeight = usableHeight / rows
        if (itemHeight == 0) {
            wrapItemHeight
        }

        //Calculate the amount of width and height already used, mainly for later measurement
        itemWidthUsed = (columns - 1) * itemWidth
        itemHeightUsed = (rows - 1) * itemHeight
        //Count the total number of pages
        computePageSize(state)
        //Calculate the maximum value that can be scrolled horizontally
        totalWidth = (pageSize - 1) * width
        //Separate views
        detachAndScrapAttachedViews(recycler)
        val count = itemCount
        var p = 0
        while (p < pageSize) {
            var r = 0
            while (r < rows) {
                var c = 0
                while (c < columns) {
                    val index = p * onePageSize + r * columns + c
                    if (index == count) {
                        c = columns
                        r = rows
                        p = pageSize
                        break
                    }
                    val view = recycler.getViewForPosition(index)
                    addView(view)
                    //Measure item
                    measureChildWithMargins(view, itemWidthUsed, itemHeightUsed)
                    val width = getDecoratedMeasuredWidth(view)
                    var height = getDecoratedMeasuredHeight(view)
                    //If the entry height is set, use; If it is not set, the actual itemHeight is used as itemHeight
                    if (isUseSetHeight) {
                        height = wrapItemHeight
                        itemHeight = height
                    } else {
                        if (index == 0 && height != 0) {
                            itemHeight = height
                        }
                    }
                    var rect = allItemFrames[index]
                    if (rect == null) {
                        rect = Rect()
                    }
                    val x = p * usableWidth + c * itemWidth
                    val y = r * itemHeight
                    rect[x, y, width + x] = height + y
                    allItemFrames.put(index, rect)
                    c++
                }
                r++
            }
            //After each page is recycled, one page of View is recycled for the next page
            removeAndRecycleAllViews(recycler)
            p++
        }
        recycleAndFillItems(recycler, state)
        requestLayout()
    }

    private val wrapItemHeight: Int
        private get() {
            if (heightMode == View.MeasureSpec.AT_MOST) {
                itemHeight = if (isUseSetHeight) {
                    if (itemSetHeight * rows <= totalHeight) {
                        itemSetHeight
                    } else {
                        totalHeight / rows
                    }
                } else {
                    totalHeight / rows
                }
                return itemHeight
            }
            return itemHeight
        }

    private fun computePageSize(state: RecyclerView.State) {
        pageSize = state.itemCount / onePageSize + if (state.itemCount % onePageSize == 0) 0 else 1
    }

    override fun onDetachedFromWindow(view: RecyclerView, recycler: RecyclerView.Recycler) {
        super.onDetachedFromWindow(view, recycler)
        offsetX = 0
        offsetY = 0
    }

    private fun recycleAndFillItems(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.isPreLayout) {
            return
        }
        val displayRect = Rect(
            paddingLeft + offsetX,
            paddingTop,
            width - paddingLeft - paddingRight + offsetX,
            height - paddingTop - paddingBottom
        )
        val childRect = Rect()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            childRect.left = getDecoratedLeft(child!!)
            childRect.top = getDecoratedTop(child)
            childRect.right = getDecoratedRight(child)
            childRect.bottom = getDecoratedBottom(child)
            if (!Rect.intersects(displayRect, childRect)) {
                removeAndRecycleView(child, recycler)
            }
        }
        for (i in 0 until itemCount) {
            if (Rect.intersects(displayRect, allItemFrames[i])) {
                val view = recycler.getViewForPosition(i)
                addView(view)
                measureChildWithMargins(view, itemWidthUsed, itemHeightUsed)
                val rect = allItemFrames[i]
                layoutDecorated(
                    view,
                    rect.left - offsetX,
                    rect.top,
                    rect.right - offsetX,
                    rect.bottom
                )
            }
        }
    }

    override fun isLastRow(index: Int): Boolean {
        if (index >= 0 && index < itemCount) {
            var indexOfPage = index % onePageSize
            indexOfPage++
            if (indexOfPage > (rows - 1) * columns && indexOfPage <= onePageSize) {
                return true
            }
        }
        return false
    }

    override fun isLastColumn(position: Int): Boolean {
        var position = position
        if (position >= 0 && position < itemCount) {
            position++
            if (position % columns == 0) {
                return true
            }
        }
        return false
    }

    override fun isPageLast(position: Int): Boolean {
        var index = position
        index++
        return index % onePageSize == 0
    }

    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int {
        computePageSize(state)
        return pageSize * width
    }

    override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int {
        return offsetX
    }

    override fun computeHorizontalScrollExtent(state: RecyclerView.State): Int {
        return width
    }
}