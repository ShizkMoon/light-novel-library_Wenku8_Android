package org.mewx.wenku8.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import org.mewx.wenku8.R
import org.mewx.wenku8.global.api.ReviewReplyList
import org.mewx.wenku8.listener.MyItemLongClickListener

/**
 * Review reply list item adapter.
 */
class ReviewReplyItemAdapter(
    private val reviewReplyList: ReviewReplyList
) : RecyclerView.Adapter<ReviewReplyItemAdapter.ViewHolder>() {
    private var itemLongClickListener: MyItemLongClickListener? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view_review_reply_item, viewGroup, false)
        return ViewHolder(view, itemLongClickListener)
    }

    fun setOnItemLongClickListener(listener: MyItemLongClickListener?) {
        itemLongClickListener = listener
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val reviewReply = reviewReplyList.list[position]
        viewHolder.userName.text = String.format("[%s]", reviewReply.userName)
        viewHolder.replyTime.text = DATE_FORMATTER.format(reviewReply.replyTime)
        viewHolder.numberedId.text = String.format(Locale.CHINA, "%d", position + 1)
        viewHolder.content.text = reviewReply.content
    }

    override fun getItemCount(): Int = reviewReplyList.list.size

    class ViewHolder(
        view: View,
        private val longClickListener: MyItemLongClickListener?
    ) : RecyclerView.ViewHolder(view), View.OnLongClickListener {
        val userName: TextView = view.findViewById(R.id.review_reply_item_user)
        val replyTime: TextView = view.findViewById(R.id.review_reply_item_time)
        val numberedId: TextView = view.findViewById(R.id.review_reply_item_numbered_id)
        val content: TextView = view.findViewById(R.id.review_reply_content)

        init {
            view.findViewById<View>(R.id.review_reply_item).setOnLongClickListener(this)
        }

        override fun onLongClick(view: View): Boolean {
            longClickListener?.onItemLongClick(view, bindingAdapterPosition)
            return true
        }
    }

    companion object {
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    }
}
