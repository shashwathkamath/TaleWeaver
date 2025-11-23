package com.kamath.taleweaver.home.sell.data.remote

import com.kamath.taleweaver.home.sell.domain.model.GoogleBooksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {

    @GET("volumes")
    suspend fun searchByIsbn(
        @Query("q") query: String
    ): GoogleBooksResponse

    companion object {
        const val BASE_URL = "https://www.googleapis.com/books/v1/"
    }
}