package org.mewx.wenku8.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.mewx.wenku8.R
import org.mewx.wenku8.listener.MyItemClickListener
import org.mewx.wenku8.listener.MyItemLongClickListener

/**
 * Search history adapter.
 */
class SearchHistoryAdapter(
    private val history: List<String>
) : RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>() {
    private var itemClickListener: MyItemClickListener? = null
    private var itemLongClickListener: MyItemLongClickListener? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view_search_history_item, viewGroup, false)
        return ViewHolder(view, itemClickListener, itemLongClickListener)
    }

    fun setOnItemClickListener(listener: MyItemClickListener?) {
        itemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: MyItemLongClickListener?) {
        itemLongClickListener = listener
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = history[position]
    }

    override fun getItemCount(): Int = history.size

    class ViewHolder(
        view: View,
        private val clickListener: MyItemClickListener?,
        private val longClickListener: MyItemLongClickListener?
    ) : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {
        val textView: TextView = view.findViewById(R.id.search_history_text)

        init {
            view.findViewById<View>(R.id.item_card).setOnClickListener(this)
            view.findViewById<View>(R.id.item_card).setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            clickListener?.onItemClick(view, bindingAdapterPosition)
        }

        override fun onLongClick(view: View): Boolean {
            longClickListener?.onItemLongClick(view, bindingAdapterPosition)
            return true
        }
    }
}
