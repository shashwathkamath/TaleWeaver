package com.kamath.taleweaver.home.search.domain.model

import com.kamath.taleweaver.home.feed.domain.model.Listing

data class SearchResult(
    val listings: List<Listing>,
    val hasMorePages: Boolean,
    val totalHits: Int,
    val genreFacets: Map<String, Int> = emptyMap()  // BookGenre enum name → count
)
