package fr.isep.oboo

import android.util.Log
import fr.isep.oboo.dto.BuildingDTO
import fr.isep.oboo.dto.FloorDTO
import fr.isep.oboo.dto.RoomDTO
import fr.isep.oboo.dto.TimeSlotDTO
import fr.isep.oboo.model.Room
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.GET
import java.io.IOException

interface ObooAPI {
    @GET("buildings")
    suspend fun getBuildings(): Response<List<BuildingDTO>>

    @GET("floors")
    suspend fun getFloors(): Response<List<FloorDTO>>

    @GET("rooms")
    suspend fun getRooms(): Response<List<RoomDTO>>

    @GET("timeslots")
    suspend fun getTimeSlots(): Response<List<TimeSlotDTO>>
}
