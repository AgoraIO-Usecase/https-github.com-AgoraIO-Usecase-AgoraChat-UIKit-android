package io.agora.chat.uikit.feature.chat.pin

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ChatUIKitPinItemSpaceDecoration(
    private val space: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = space
        outRect.right = space
        outRect.bottom = space
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = space
        } else {
            outRect.top = 0
        }
    }
}