package io.agora.chat.uikit.common.extensions

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope

val ViewGroup.lifecycleScope: LifecycleCoroutineScope
    get() = (context as? AppCompatActivity)?.lifecycleScope ?: throw IllegalStateException("ViewGroup's context is not an AppCompatActivity")

fun ViewGroup.containsChild(child: View?): Boolean {
    if (child == null) {
        return false
    }
    return isChildTypeExist(child::class.java)
}

fun ViewGroup.indexOfSameTypeChild(child: View?): Int {
    if (child == null) {
        return -1
    }
    for (i in 0 until childCount) {
        val view = getChildAt(i)
        if (view::class.java.isInstance(child)) {
            return i
        }
    }
    return -1
}

internal fun ViewGroup.addChildView(child: View?,childIndex:Int? = null) {
    if (child == null) {
        return
    }
    if (containsChild(child)) {
        val index = indexOfSameTypeChild(child)
        if (index != -1) {
            removeViewAt(index)
            addView(child, index)
        }
    } else {
        if (childIndex != null && childIndex >= 0){
            addView(child,childIndex)
        }else{
            addView(child)
        }
    }
}

internal fun <T: View> ViewGroup.getTheSameTypeChild(type: Class<T>): View? {
    for (i in 0 until childCount) {
        val view = getChildAt(i)
        if (type.isInstance(view)) {
            return view
        }
    }
    return null
}

private fun <T : View> ViewGroup.isChildTypeExist(type: Class<T>): Boolean {
    for (i in 0 until childCount) {
        val child = getChildAt(i)
        if (type.isInstance(child)) {
            return true
        }
    }
    return false
}