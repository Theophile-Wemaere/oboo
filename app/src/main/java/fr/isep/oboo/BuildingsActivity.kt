package fr.isep.oboo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.isep.oboo.ui.components.BuildingsScreen
import fr.isep.oboo.ui.theme.ObooTheme

class BuildingsActivity: ComponentActivity()
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
            Log.e("Buildings Activity", "Menu index not provided in the Intent, defaulting to 0.")
        }

        setContent {
            ObooTheme {
                BuildingsScreen(this, menuIndex, ObooDatabase.getInstance(applicationContext).buildingDAO().getAllBuildings(), { this.onBackPressed() })
            }
        }
    }
}
