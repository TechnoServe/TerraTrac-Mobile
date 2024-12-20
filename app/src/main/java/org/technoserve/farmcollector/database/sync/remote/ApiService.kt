package org.technoserve.farmcollector.database.sync.remote

import org.technoserve.farmcollector.database.models.DeviceFarmDto
import org.technoserve.farmcollector.database.models.FarmRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *  this is the  implementation of the API interface that will be used to connect to the device farm server TO sync the farm plots to remote server
 *  and restore them to the device using either device_id or email or phoneNUmber.
 *  The GET method is used to fetch farms based on the deviceId or email or phone number
 *  The POST method is used to sync the farms to the remote server
 *  The suspend keyword is used to make the function suspending, allowing it to be called from a coroutine
 *  The Response<Any> is used to handle the response from the server, which can be any type of data, in this case, it's any type of data.
 *  The @Body annotation is used to pass the data to the API endpoint as JSON.
 *  The @POST annotation is used to indicate that this method should be called with a POST request.
 *  The @Path annotation is used to specify the path parameter in the API endpoint.
 *  The @Query annotation is used to specify query parameters in the API endpoint.
 *  The @GET annotation is used to indicate that this method should be called with a GET request.
 */
interface ApiService {

    @POST("/api/farm/sync/")
    suspend fun syncFarms(@Body farms: List<DeviceFarmDto>): Response<Any>

    @POST("/api/farm/restore/")
    suspend fun getFarmsByDeviceId(@Body request: FarmRequest): List<Any>
}
