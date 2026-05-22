package com.taleweaver.app.home.search.data.remote

import com.taleweaver.app.home.search.data.dto.AlgoliaSearchRequest
import com.taleweaver.app.home.search.data.dto.AlgoliaSearchResponse
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
