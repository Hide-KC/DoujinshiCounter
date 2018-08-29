package com.kcpoipoi.doujinshicounter

import java.text.SimpleDateFormat
import java.util.*

data class DetectedBook(
        val book: Book,
        val detectedTime: Date = Date()
) {
    fun getDetectedTime(): String{
        return SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(detectedTime)
    }

    fun getBookId(): Int{
        return book.id
    }
}