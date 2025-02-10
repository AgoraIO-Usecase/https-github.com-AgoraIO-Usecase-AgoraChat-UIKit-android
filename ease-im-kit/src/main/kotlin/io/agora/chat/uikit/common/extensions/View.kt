package io.agora.chat.uikit.common.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Show soft keyboard.
 */
fun View.showSoftKeyboard() {
    this.postDelayed({
        requestFocus()
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).let {
            it.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }, 200)
}

/**
 * Hide soft keyboard.
 */
fun View.hideSoftKeyboard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).let {
        it.hideSoftInputFromWindow(windowToken, 0)
    }
}

