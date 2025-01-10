package fr.isep.oboo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.isep.oboo.model.Building
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
import fr.isep.oboo.ui.components.FloorDetailScreen
import fr.isep.oboo.ui.theme.ObooTheme
import kotlinx.coroutines.flow.Flow

class FloorDetailActivity: ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve menu index and floor ID
        val extras = intent.extras
        var menuIndex = 0
        var floorId: Long = -1
        if (extras != null) {
            menuIndex = extras.getInt("menuIndex")
            floorId = extras.getLong("floorId")
        }
        else {
            Log.e("FloorDetail Activity", "No extras provided in the Intent, setting menuIndex to 0 and floorId to 1.")
        }

        val db: ObooDatabase = ObooDatabase.getInstance(applicationContext)
        val floor: Floor = db.floorDAO().getFloorById(floorId)
        val building: Building = db.buildingDAO().getBuildingById(floor.buildingId)
        val rooms: Flow<List<Room>> = db.floorDAO().getAllRooms(floor.id)

        setContent {
            ObooTheme {
                FloorDetailScreen(this, menuIndex, building, floor, rooms, onReturn = { this.onBackPressed() })
            }
        }
    }
}
