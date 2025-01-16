package fr.isep.oboo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.isep.oboo.ui.components.ProfileScreen
import fr.isep.oboo.ui.theme.ObooTheme

class ProfileActivity: ComponentActivity()
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
            Log.e("Profile Activity", "Menu index not provided in the Intent, defaulting to 0.")
        }

        setContent {
            ObooTheme {
                ProfileScreen(this, menuIndex, ObooDatabase.getInstance(ObooApp.applicationContext()).apiKeyDAO().getAPIKey()!!.email, { this.onBackPressed() })
            }
        }
    }
}
