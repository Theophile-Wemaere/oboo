package fr.isep.oboo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.RoomDatabase
import fr.isep.oboo.dao.BuildingDAO
import fr.isep.oboo.dao.FloorDAO
import fr.isep.oboo.dao.RoomDAO
import fr.isep.oboo.dao.TimeSlotDAO
import fr.isep.oboo.ui.components.DashboardScreen
import fr.isep.oboo.ui.theme.ObooTheme

private lateinit var db: ObooDatabase
private lateinit var buildingDAO: BuildingDAO
private lateinit var floorDAO: FloorDAO
private lateinit var roomDAO: RoomDAO
private lateinit var timeSlotDAO: TimeSlotDAO

class DashboardActivity : ComponentActivity()
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

        setContent {
            ObooTheme {
                DashboardScreen(this, menuIndex, ObooDatabase.getInstance(applicationContext).roomDAO().getAllRooms())
            }
        }
    }
}
