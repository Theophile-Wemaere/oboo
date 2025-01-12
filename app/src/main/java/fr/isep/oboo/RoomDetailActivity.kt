package fr.isep.oboo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.isep.oboo.model.Building
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
import fr.isep.oboo.model.TimeSlot
import fr.isep.oboo.ui.components.RoomDetailScreen
import fr.isep.oboo.ui.theme.ObooTheme

class RoomDetailActivity: ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve menu index and room ID
        val extras = intent.extras
        var menuIndex = 0
        var roomId: Long = -1
        if (extras != null) {
            menuIndex = extras.getInt("menuIndex")
            roomId = extras.getLong("roomId")
        }
        else {
            Log.e("RoomDetail Activity", "No extras provided in the Intent, setting menuIndex to 0 and roomId to 1.")
        }

        val db: ObooDatabase = ObooDatabase.getInstance(applicationContext)
        val room: Room = db.roomDAO().getRoomById(roomId)
        val timeSlots: List<TimeSlot> = db.roomDAO().getTimeSlots(roomId)
        val floor: Floor = db.floorDAO().getFloorById(room.floorId)
        val building: Building = db.buildingDAO().getBuildingById(floor.buildingId)

        setContent {
            ObooTheme {
                RoomDetailScreen(this, menuIndex, building, floor, room, timeSlots, onReturn = { this.onBackPressed() })
            }
        }
    }
}