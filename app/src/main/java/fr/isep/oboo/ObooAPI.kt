package fr.isep.oboo

import fr.isep.oboo.dto.APIKeyDTO
import fr.isep.oboo.dto.BuildingDTO
import fr.isep.oboo.dto.FloorDTO
import fr.isep.oboo.dto.OneTimePasswordDTO
import fr.isep.oboo.dto.RoomDTO
import fr.isep.oboo.dto.TimeSlotDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ObooAPI {
    @GET("sendotp")
    suspend fun generateOTP(@Query("email") email: String): Response<OneTimePasswordDTO>

    @GET("auth")
    suspend fun authenticate(@Query("email") email: String, @Query("otp") otp: String): Response<APIKeyDTO>

    @GET("buildings")
    suspend fun getBuildings(@Query("email") email: String = "", @Query("api_key") apiKey: String = ""): Response<List<BuildingDTO>>

    @GET("floors")
    suspend fun getFloors(@Query("email") email: String = "", @Query("api_key") apiKey: String = ""): Response<List<FloorDTO>>

    @GET("rooms")
    suspend fun getRooms(@Query("email") email: String = "", @Query("api_key") apiKey: String = ""): Response<List<RoomDTO>>

    @GET("timeslots")
    suspend fun getTimeSlots(@Query("email") email: String = "", @Query("api_key") apiKey: String = ""): Response<List<TimeSlotDTO>>
}
