package fr.isep.oboo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.isep.oboo.ui.components.LoginScreen
import fr.isep.oboo.ui.components.OTPScreen
import fr.isep.oboo.ui.theme.ObooTheme

class OTPActivity: ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve email
        val extras = intent.extras
        var email: String? = ""
        if (extras != null) {
            email = extras.getString("email")
        }
        else {
            Log.e("OTP Activity", "Email not provided in the Intent, defaulting to empty.")
        }

        setContent {
            ObooTheme {
                OTPScreen(this, email ?: "")
            }
        }
    }
}