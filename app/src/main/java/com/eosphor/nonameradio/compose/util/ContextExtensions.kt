package com.eosphor.nonameradio.compose.util

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        when (current) {
            is FragmentActivity -> return current
            else -> current = current.baseContext
        }
    }
    return null
}
