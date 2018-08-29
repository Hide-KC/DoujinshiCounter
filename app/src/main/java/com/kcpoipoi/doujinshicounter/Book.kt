package com.kcpoipoi.doujinshicounter

import java.util.*

data class Book(
        val id: Int,
        val author: String = "kcpoipoi",
        val bookTitle: String,
        val publishedDate: Calendar,
        val cost: Int
){}
