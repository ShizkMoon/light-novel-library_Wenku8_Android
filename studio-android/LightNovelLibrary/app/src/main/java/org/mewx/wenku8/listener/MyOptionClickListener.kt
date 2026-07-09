package org.mewx.wenku8.listener

import android.view.View

@FunctionalInterface
fun interface MyOptionClickListener {
    fun onOptionButtonClick(view: View?, position: Int)
}
