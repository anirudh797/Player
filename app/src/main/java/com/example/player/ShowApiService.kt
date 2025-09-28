package com.example.player

import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

interface ShowApiService {
    @GET("show/watch-rise-and-fall/season-1/{showName}-online-6cc53fd0396e619e756a6bfc48e60754")
    suspend fun getShowMetadata(
        @Path("showName") showName: String,
        @HeaderMap headers: Map<String, String>
    ): String

}
