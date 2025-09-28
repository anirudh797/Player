package com.example.player

import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query

interface ShowApiService {

    /* used to fetch metaData for the episodes */
    @GET("show/watch-rise-and-fall/season-1/{showName}-online-{contentId}")
    suspend fun getShowMetadata(
        @Path("showName") showName: String,
        @Path("contentId") contentId: String,
        @HeaderMap headers: Map<String, String>
    ): String


    /* used to fetch contentIds for the episodes */
    @GET("v1/web/detail/tab/aroundcurrentepisodes")
    suspend fun getAroundCurrentEpisodes(
        @Query("type") type: String,
        @Query("id") id: String,
        @Query("filterId") filterId: String,
        @Query("device-density") deviceDensity: Int,
        @Query("userid") userId: String,
        @Query("platform") platform: String,
        @Query("content-languages") contentLanguages: String,
        @Query("kids-mode-enabled") kidsModeEnabled: Boolean
    ): String

}
