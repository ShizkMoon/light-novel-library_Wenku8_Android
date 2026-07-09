package org.mewx.wenku8.listener

import android.view.View

@FunctionalInterface
fun interface MyItemClickListener {
    fun onItemClick(view: View?, position: Int)
}
