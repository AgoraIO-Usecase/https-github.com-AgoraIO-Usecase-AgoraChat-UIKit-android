package io.agora.chat.uikit.interfaces

import android.view.View

interface OnContactSelectedListener {
    fun onContactSelectedChanged(v: View, selectedMembers: MutableList<String>){}
    fun onSearchSelectedResult(selectedMembers: MutableList<String>){}
}
