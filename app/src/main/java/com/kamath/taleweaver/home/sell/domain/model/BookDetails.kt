package com.kamath.taleweaver.home.sell.domain.model

data class BookDetails(
    val title: String,
    val authors: List<String>,
    val description: String,
    val genres: List<String>,
    val publisher: String?,
    val publishedDate: String?,
    val pageCount: Int?,
    val coverImageUrl: String?,
    val language: String?
)