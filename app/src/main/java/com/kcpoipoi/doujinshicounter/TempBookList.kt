package com.kcpoipoi.doujinshicounter

import java.util.*

object TempBookList {
    val books = listOf(
            Book(1, bookTitle = "Twitter Application on Kotlin", cost = 1000, author = "kcpoipoi",
                    publishedDate = Calendar.getInstance().also { it.set(2018, 10, 8)})
    )

    fun getBook(bookId: Int): Book?{
        for (book in books){
            if (book.id == bookId){
                return book
            }
        }
        return null
    }
}