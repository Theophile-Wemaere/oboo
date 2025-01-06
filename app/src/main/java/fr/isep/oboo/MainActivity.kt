package fr.isep.oboo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import fr.isep.oboo.dao.BuildingDAO
import fr.isep.oboo.dao.FloorDAO
import fr.isep.oboo.dao.RoomDAO
import fr.isep.oboo.dao.TimeSlotDAO
import fr.isep.oboo.dto.BuildingDTO
import fr.isep.oboo.dto.FloorDTO
import fr.isep.oboo.dto.RoomDTO
import fr.isep.oboo.dto.TimeSlotDTO
import fr.isep.oboo.model.Building
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
import fr.isep.oboo.model.TimeSlot
import fr.isep.oboo.ui.components.RoomsScreen
import fr.isep.oboo.ui.theme.ObooTheme
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.LocalDateTime
import kotlin.math.floor

private lateinit var db: ObooDatabase
private lateinit var buildingDAO: BuildingDAO
private lateinit var floorDAO: FloorDAO
private lateinit var roomDAO: RoomDAO
private lateinit var timeSlotDAO: TimeSlotDAO

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Database instantiation
        db = ObooDatabase.getInstance(applicationContext)
        buildingDAO = db.buildingDAO()
        floorDAO = db.floorDAO()
        roomDAO = db.roomDAO()
        timeSlotDAO = db.timeSlotDAO()

        lifecycleScope.launch {
            refreshDatabase()
        }

        setContent {
            ObooTheme {
                RoomsScreen(roomDAO.getAllRooms())
            }
        }
    }
}

suspend fun refreshDatabase()
{
    val buildingsResponse: Response<List<BuildingDTO>> = try {
        RetrofitInstance.api.getBuildings()
    }
    catch (e: IOException) {
        throw e
    }
    catch (e: HttpException) {
        throw e
    }

    val floorsResponse: Response<List<FloorDTO>> = try {
        RetrofitInstance.api.getFloors()
    }
    catch (e: IOException) {
        throw e
    }
    catch (e: HttpException) {
        throw e
    }

    val roomsResponse: Response<List<RoomDTO>> = try {
        RetrofitInstance.api.getRooms()
    }
    catch (e: IOException) {
        throw e
    }
    catch (e: HttpException) {
        throw e
    }

    val timeSlotsResponse: Response<List<TimeSlotDTO>> = try {
        RetrofitInstance.api.getTimeSlots()
    }
    catch (e: IOException) {
        throw e
    }
    catch (e: HttpException) {
        throw e
    }

    // Override local database data with data fetched from the API
    if (
        (buildingsResponse.isSuccessful && buildingsResponse.body() != null)
        && (floorsResponse.isSuccessful && floorsResponse.body() != null)
        && (roomsResponse.isSuccessful && roomsResponse.body() != null)
        && (timeSlotsResponse.isSuccessful && timeSlotsResponse.body() != null)
    )
    {
        Log.d("Oboo API", "API calls successful, rebuilding local database...")
        timeSlotDAO.deleteAllTimeSlots()
        roomDAO.deleteAllRooms()
        floorDAO.deleteAllFloors()
        buildingDAO.deleteAllBuildings()

        for (buildingDTO: BuildingDTO in buildingsResponse.body()!!)
        {
            val buildingId = buildingDAO.insertBuilding(Building(buildingDTO.name))

            for (floorDTO: FloorDTO in floorsResponse.body()!!)
            {
                var floorId: Long = 0;
                if (floorDTO.building == buildingDTO.id)
                    floorId = floorDAO.insertFloor(Floor(floorDTO.number, floorDTO.name, buildingId))
                else
                    continue

                for (roomDTO: RoomDTO in roomsResponse.body()!!)
                {
                    var roomId: Long = 0;
                    if (roomDTO.floor == floorDTO.id)
                        roomId = roomDAO.insertRoom(Room(roomDTO.number, roomDTO.name, floorId))
                    else
                        continue

                    for (timeSlotDTO: TimeSlotDTO in timeSlotsResponse.body()!!)
                    {
                        if (timeSlotDTO.room == roomDTO.id)
                            timeSlotDAO.insertTimeSlot(TimeSlot(timeSlotDTO.subject, timeSlotDTO.start_time, timeSlotDTO.end_time, roomId))
                    }
                }
            }
        }
        Log.d("Oboo API", "Local database rebuilt !")
    }
    else
    {
        Log.d("Oboo API", "API calls failed")
    }

//    if (response.isSuccessful && response.body() != null)
//    {
//        Log.d("Oboo API", "API call to /rooms: OK")
//        Log.d("Oboo API", response.body().toString())
//
//        // Override the database data with data fetched from the API
//        timeSlotDAO.deleteAllTimeSlots()
//        roomDAO.deleteAllRooms()
//        floorDAO.deleteAllFloors()
//        buildingDAO.deleteAllBuildings()
//
//        for (roomDTO: RoomDTO in response.body()!!)
//            roomDAO.insertRoom(Room(roomDTO.number, roomDTO.name))
//
//    }
//    else
//    {
//        Log.d("Oboo API", "Response not successful: ${response.isSuccessful} (code: ${response.code()})")
//        Log.d("Oboo API", "Response body: ${response.body().toString()}")
//    }
}
