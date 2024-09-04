package org.technoserve.farmcollector.database.remote

import org.technoserve.farmcollector.database.DeviceFarmDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class FarmRequest(
    val device_id: String,
    val email: String = "",
    val phone_number: String = ""
)
interface ApiService {

    @POST("/api/farm/sync/")
    suspend fun syncFarms(@Body farms: List<DeviceFarmDto>): Response<Any>

    @POST("/api/farm/restore/")
    suspend fun getFarmsByDeviceId(@Body request: FarmRequest): List<Any>
}
