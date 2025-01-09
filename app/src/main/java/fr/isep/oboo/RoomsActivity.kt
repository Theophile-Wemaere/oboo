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

class RoomsActivity: ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve menu index
        val extras = intent.extras
        var menuIndex: Int = 0
        if (extras != null) {
            menuIndex = extras.getInt("menuIndex")
        }
        else {
            Log.e("Rooms Activity", "Menu index not provided in the Intent, defaulting to 0.")
        }

        setContent {
            ObooTheme {
                RoomsScreen(this, menuIndex, rooms = ObooDatabase.getInstance(applicationContext).roomDAO().getAllRooms(), onReturn = { this.onBackPressed() })
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
        val db = ObooDatabase.getInstance(ObooApp.applicationContext())
        db.timeSlotDAO().deleteAllTimeSlots()
        db.roomDAO().deleteAllRooms()
        db.floorDAO().deleteAllFloors()
        db.buildingDAO().deleteAllBuildings()

        for (buildingDTO: BuildingDTO in buildingsResponse.body()!!)
        {
            val buildingId = db.buildingDAO().insertBuilding(Building(buildingDTO.name, buildingDTO.long_name, buildingDTO.city))

            for (floorDTO: FloorDTO in floorsResponse.body()!!)
            {
                var floorId: Long = 0;
                if (floorDTO.building == buildingDTO.id)
                    floorId = db.floorDAO().insertFloor(Floor(floorDTO.number, floorDTO.name, buildingId))
                else
                    continue

                for (roomDTO: RoomDTO in roomsResponse.body()!!)
                {
                    var roomId: Long = 0;
                    if (roomDTO.floor == floorDTO.id)
                        roomId = db.roomDAO().insertRoom(Room(roomDTO.number, roomDTO.name, floorId))
                    else
                        continue

                    for (timeSlotDTO: TimeSlotDTO in timeSlotsResponse.body()!!)
                    {
                        if (timeSlotDTO.room == roomDTO.id)
                            db.timeSlotDAO().insertTimeSlot(TimeSlot(timeSlotDTO.subject, timeSlotDTO.start_time, timeSlotDTO.end_time, roomId))
                    }
                }
            }
        }
        Log.d("Oboo API", "Local database rebuilt !")
    }
    else
    {
        Log.e("Oboo API", "API calls failed")
    }
}
