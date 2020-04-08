package com.liempo.letran.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.liempo.letran.R

class TimelineAdapter(private val items: ArrayList<String>):
    RecyclerView.Adapter<TimelineAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(
            parent.context).inflate(
            R.layout.item_timeline,
            parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val media: ImageView = view.findViewById(R.id.media_image)
        val title: TextView = view.findViewById(R.id.title_text)
        val subtitle: TextView = view.findViewById(R.id.subtitle_text)
        val like: ImageButton = view.findViewById(R.id.like_button)
        val share: ImageButton = view.findViewById(R.id.share_button)
        val content: TextView = view.findViewById(R.id.content_text)
    }
}