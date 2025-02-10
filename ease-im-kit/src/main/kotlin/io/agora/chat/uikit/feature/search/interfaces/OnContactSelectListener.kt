package io.agora.chat.uikit.feature.search.interfaces

import android.view.View

interface OnContactSelectListener {
    fun onContactSelectedChanged(v: View,userId:String,isSelected:Boolean)
}