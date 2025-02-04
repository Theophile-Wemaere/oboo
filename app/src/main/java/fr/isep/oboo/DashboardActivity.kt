package fr.isep.oboo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.isep.oboo.ui.components.DashboardScreen
import fr.isep.oboo.ui.theme.ObooTheme

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

        val db = ObooDatabase.getInstance(applicationContext)

        setContent {
            ObooTheme {
                DashboardScreen(this, menuIndex, db.roomDAO().getAllRooms(), db.roomDAO().getAllRoomsStatic())
            }
        }
    }
}
