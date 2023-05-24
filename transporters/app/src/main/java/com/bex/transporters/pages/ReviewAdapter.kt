package com.bex.transporters.pages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bex.transporters.R
import com.bex.transporters.models.Offer
import com.bex.transporters.models.Review
import com.bex.transporters.pages.driver.OffersAdapter

class ReviewAdapter(private var reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ReviewViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.review_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews[position]
       holder.reviewusername.text = review.user_username
        holder.reviewcontent.text = review.review_content
    }

    override fun getItemCount() = reviews.size


    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val reviewusername = itemView.findViewById<TextView>(R.id.reviewuser)
        val reviewcontent = itemView.findViewById<TextView>(R.id.reviewcontent)


    }
    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
