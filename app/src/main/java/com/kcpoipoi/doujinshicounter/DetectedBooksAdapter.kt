package com.kcpoipoi.doujinshicounter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class DetectedBooksAdapter(context: Context): ArrayAdapter<DetectedBook>(context, android.R.layout.simple_list_item_1) {
    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val cv = convertView?: inflater.inflate(R.layout.adapter_detected, null)
        cv.tag = ItemViewHolder(cv)
        val holder = cv.tag as ItemViewHolder

        holder.title.text = getItem(position).book.bookTitle
        holder.detectedDate.text = getItem(position).getDetectedTime()

        return cv
    }

    private class ItemViewHolder(view: View){
        val title = view.findViewById<TextView>(R.id.book_title)
        val detectedDate = view.findViewById<TextView>(R.id.detected_date)
    }
}