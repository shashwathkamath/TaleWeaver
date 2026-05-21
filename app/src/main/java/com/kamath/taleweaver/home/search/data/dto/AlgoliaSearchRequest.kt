package com.kamath.taleweaver.home.search.data.dto

data class AlgoliaSearchRequest(
    val query: String = "",
    val aroundLatLng: String? = null,
    val aroundRadius: Int? = null,
    val filters: String = "status:AVAILABLE",
    val facets: List<String> = listOf("genres"),
    val facetFilters: List<List<String>>? = null,
    val hitsPerPage: Int = 20,
    val page: Int = 0,
    val getRankingInfo: Boolean = true
)
