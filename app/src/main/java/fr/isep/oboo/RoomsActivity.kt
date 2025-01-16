package fr.isep.oboo

import android.app.Activity
import android.content.Intent
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
