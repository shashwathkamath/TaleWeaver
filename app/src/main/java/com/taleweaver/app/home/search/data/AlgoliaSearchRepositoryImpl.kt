package com.taleweaver.app.home.search.data

import com.taleweaver.app.core.util.ApiResult
import com.taleweaver.app.home.search.data.dto.AlgoliaSearchRequest
import com.taleweaver.app.home.search.data.dto.toListing
import com.taleweaver.app.home.search.data.remote.AlgoliaApi
import com.taleweaver.app.home.search.domain.model.SearchResult
import com.taleweaver.app.home.search.domain.repository.SearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

private const val INDEX_NAME = "listings"
private const val HITS_PER_PAGE = 20

class AlgoliaSearchRepositoryImpl @Inject constructor(
    private val algoliaApi: AlgoliaApi
) : SearchRepository {

    override fun searchListings(
        latitude: Double,
        longitude: Double,
        radiusInKm: Double,
        query: String,
        expandedGenreIds: Set<String>,
        page: Int
    ): Flow<ApiResult<SearchResult>> = flow {
        emit(ApiResult.Loading())
        try {
            // Each genre enum name becomes an OR alternative: ["genres:FICTION", "genres:SCI_FI"]
            val facetFilters = if (expandedGenreIds.isNotEmpty()) {
                listOf(expandedGenreIds.map { "genres:$it" })
            } else null

            val response = algoliaApi.search(
                indexName = INDEX_NAME,
                request = AlgoliaSearchRequest(
                    query = query,
                    aroundLatLng = "$latitude,$longitude",
                    aroundRadius = (radiusInKm * 1000).toInt(),
                    facetFilters = facetFilters,
                    hitsPerPage = HITS_PER_PAGE,
                    page = page
                )
            )

            Timber.d("Algolia: ${response.nbHits} total hits, page ${response.page}/${response.nbPages - 1}")

            emit(
                ApiResult.Success(
                    SearchResult(
                        listings = response.hits.map { it.toListing() },
                        hasMorePages = response.page < response.nbPages - 1,
                        totalHits = response.nbHits,
                        genreFacets = response.facets?.get("genres") ?: emptyMap()
                    )
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Algolia search failed")
            emit(ApiResult.Error(e.message ?: "Search failed"))
        }
    }
}
