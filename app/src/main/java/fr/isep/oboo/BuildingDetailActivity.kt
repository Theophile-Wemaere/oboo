package fr.isep.oboo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.isep.oboo.model.Building
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
import fr.isep.oboo.ui.components.BuildingDetailScreen
import fr.isep.oboo.ui.theme.ObooTheme
import kotlinx.coroutines.flow.Flow

class BuildingDetailActivity: ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve menu index and building ID
        val extras = intent.extras
        var menuIndex = 0
        var buildingId: Long = -1
        if (extras != null) {
            menuIndex = extras.getInt("menuIndex")
            buildingId = extras.getLong("buildingId")
        }
        else {
            Log.e("BuildingDetail Activity", "No extras provided in the Intent, setting menuIndex to 0 and buildingId to 1.")
        }

        val db: ObooDatabase = ObooDatabase.getInstance(applicationContext)
        val building: Building = db.buildingDAO().getBuildingById(buildingId)
        val floors: Flow<List<Floor>> = db.buildingDAO().getAllFloors(building.id)
        val rooms: Flow<List<Room>> = db.buildingDAO().getAllRooms(building.id)

        setContent {
            ObooTheme {
                BuildingDetailScreen(this, menuIndex, building, floors, rooms, onReturn = { this.onBackPressed() })
            }
        }
    }
}