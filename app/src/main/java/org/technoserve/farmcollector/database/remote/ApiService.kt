package org.technoserve.farmcollector.database.remote

import org.technoserve.farmcollector.database.Farm
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/api/addFarms")
    suspend fun syncFarm(@Body farm: Farm): Response<Unit>

    @GET("/api/getFarms")
    suspend fun getAllFarms(): Response<List<Farm>>
}
