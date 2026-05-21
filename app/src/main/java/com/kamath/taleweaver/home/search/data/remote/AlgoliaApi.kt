package com.kamath.taleweaver.home.search.data.remote

import com.kamath.taleweaver.home.search.data.dto.AlgoliaSearchRequest
import com.kamath.taleweaver.home.search.data.dto.AlgoliaSearchResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AlgoliaApi {
    @POST("/1/indexes/{indexName}/query")
    suspend fun search(
        @Path("indexName") indexName: String,
        @Body request: AlgoliaSearchRequest
    ): AlgoliaSearchResponse
}
