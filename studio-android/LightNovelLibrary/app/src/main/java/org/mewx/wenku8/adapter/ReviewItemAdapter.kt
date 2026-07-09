package org.mewx.wenku8.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import org.mewx.wenku8.R
import org.mewx.wenku8.global.api.ReviewList
import org.mewx.wenku8.listener.MyItemClickListener

/**
 * Review list item adapter.
 */
class ReviewItemAdapter(
    private val reviewList: ReviewList
) : RecyclerView.Adapter<ReviewItemAdapter.ViewHolder>() {
    private var itemClickListener: MyItemClickListener? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.view_review_post_item, viewGroup, false)
        return ViewHolder(view, itemClickListener)
    }

    fun setOnItemClickListener(listener: MyItemClickListener?) {
        itemClickListener = listener
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val review = reviewList.list[position]
        viewHolder.reviewTitle.text = review.title
        viewHolder.postTime.text = DATE_FORMATTER.format(review.postTime)
        viewHolder.reviewAuthor.text = review.userName
        viewHolder.numberOfReplies.text = String.format(Locale.CHINA, "%d", review.noReplies)
    }

    override fun getItemCount(): Int = reviewList.list.size

    class ViewHolder(
        view: View,
        private val clickListener: MyItemClickListener?
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val reviewTitle: TextView = view.findViewById(R.id.review_title)
        val postTime: TextView = view.findViewById(R.id.review_item_post_time)
        val reviewAuthor: TextView = view.findViewById(R.id.review_item_author)
        val numberOfReplies: TextView = view.findViewById(R.id.review_item_number_of_posts)

        init {
            view.findViewById<View>(R.id.item_card).setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                clickListener?.onItemClick(view, bindingAdapterPosition)
            }
        }
    }

    companion object {
        private val DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    }
}
