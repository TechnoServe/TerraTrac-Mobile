package org.technoserve.farmcollector.database.remote

import org.technoserve.farmcollector.database.DeviceFarmDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *  this is the  implementation of the API interface that will be used to connect to the device farm server TO sync the farm plots to remote server
 *  and restore them to the device using either device_id or email or phoneNUmber.
 *
 */

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
