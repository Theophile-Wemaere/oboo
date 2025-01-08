package fr.isep.oboo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import fr.isep.oboo.ui.components.FloorsScreen
import fr.isep.oboo.ui.theme.ObooTheme
import kotlinx.coroutines.launch

class FloorsActivity: ComponentActivity()
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
            Log.e("Floors Activity", "Menu index not provided in the Intent, defaulting to 0.")
        }

        lifecycleScope.launch {
            refreshDatabase()
        }

        setContent {
            ObooTheme {
                FloorsScreen(this, menuIndex, ObooDatabase.getInstance(applicationContext).floorDAO().getAllFloors(), { this.onBackPressed() })
            }
        }
    }
}
