package org.technoserve.farmcollector.database.remote

import org.technoserve.farmcollector.database.Farm
import org.technoserve.farmcollector.database.FarmDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/api/farm/sync/")
    suspend fun syncFarms(@Body farms: List<FarmDto>): Response<Any>

    @GET("/api/farm/list")
    suspend fun getAllFarms(): Response<List<Farm>>
}
