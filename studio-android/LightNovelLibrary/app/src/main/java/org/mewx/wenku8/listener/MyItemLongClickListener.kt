package org.mewx.wenku8.listener

import android.view.View

@FunctionalInterface
fun interface MyItemLongClickListener {
    fun onItemLongClick(view: View?, position: Int)
}
