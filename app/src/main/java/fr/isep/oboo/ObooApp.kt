package fr.isep.oboo

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import fr.isep.oboo.dto.BuildingDTO
import fr.isep.oboo.dto.FloorDTO
import fr.isep.oboo.dto.RoomDTO
import fr.isep.oboo.dto.TimeSlotDTO
import fr.isep.oboo.model.Building
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
import fr.isep.oboo.model.TimeSlot
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ObooApp: Application()
{
    init {
        instance = this
    }

    companion object {
        private var instance: ObooApp? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        val context: Context = ObooApp.applicationContext()

        // Rebuild the local database on app startup by querying the API
        MainScope().launch {
            refreshDatabase()
        }
    }
}

suspend fun refreshDatabase()
{
    // To manually add a key for testing
    // ObooDatabase.getInstance(ObooApp.applicationContext()).apiKeyDAO().setAPIKey(APIKey("quentin.laurent@eleve.isep.fr", "ujOI59AIrRb76tLs0-2gI1hsEPHjlKk5q0IKhy7iwMireM55yINMff_DmHV9Hl7w"))
    // ObooDatabase.getInstance(ObooApp.applicationContext()).apiKeyDAO().setAPIKey(APIKey("quentin.laurent@eleve.isep.fr", "Pzz7UMnY8IzjHHR_UIdJbxsxyJQkKc3SKuur90GrbiJoUKmRoQK-uD_8zoC4PI-J"))

    val apiKey = ObooDatabase.getInstance(ObooApp.applicationContext()).apiKeyDAO().getAPIKey()

    if (apiKey == null)
    {
        ObooApp.applicationContext().startActivity(Intent(ObooApp.applicationContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        Log.w("Oboo API", "No API key found, redirecting to login page...")
        return
    }

    val buildingsResponse: Response<List<BuildingDTO>> = try {
        RetrofitInstance.api.getBuildings(apiKey.email, apiKey.key)
    }
    catch (e: IOException) {
        throw e
    }
    catch (e: HttpException) {
        if (e.code() == 403)
        {
            ObooApp.applicationContext().startActivity(Intent(ObooApp.applicationContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            Log.w("Oboo API", "API key rejected by the API, redirecting to login page to request new API key...")
            return
        }
        else
            throw e
    }

    val floorsResponse: Response<List<FloorDTO>> = try {
        RetrofitInstance.api.getFloors(apiKey.email, apiKey.key)
    }
    catch (e: IOException) {
        throw e
    }
    catch (e: HttpException) {
        if (e.code() == 403)
        {
            ObooApp.applicationContext().startActivity(Intent(ObooApp.applicationContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            Log.w("Oboo API", "API key rejected by the API, redirecting to login page to request new API key...")
            return
        }
        else
            throw e
    }

    val roomsResponse: Response<List<RoomDTO>> = try {
        RetrofitInstance.api.getRooms(apiKey.email, apiKey.key)
    }
    catch (e: IOException) {
        throw e
    }
    catch (e: HttpException) {
        if (e.code() == 403)
        {
            ObooApp.applicationContext().startActivity(Intent(ObooApp.applicationContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            Log.w("Oboo API", "API key rejected by the API, redirecting to login page to request new API key...")
            return
        }
        else
            throw e
    }

    val timeSlotsResponse: Response<List<TimeSlotDTO>> = try {
        RetrofitInstance.api.getTimeSlots(apiKey.email, apiKey.key)
    }
    catch (e: IOException) {
        throw e
    }
    catch (e: HttpException) {
        if (e.code() == 403)
        {
            ObooApp.applicationContext().startActivity(Intent(ObooApp.applicationContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            Log.w("Oboo API", "API key rejected by the API, redirecting to login page to request new API key...")
            return
        }
        else
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
        val returnCodes = listOf(buildingsResponse.code(), floorsResponse.code(), roomsResponse.code(), timeSlotsResponse.code())
        Log.e("Oboo API", "API calls failed: return codes: $returnCodes")

        if (returnCodes.contains(403))
        {
            ObooApp.applicationContext().startActivity(Intent(ObooApp.applicationContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            Log.w("Oboo API", "API key rejected by the API, redirecting to login page to request new API key...")
        }
    }
}

