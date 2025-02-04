package fr.isep.oboo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import fr.isep.oboo.ui.components.LoginScreen
import fr.isep.oboo.ui.theme.ObooTheme

class LoginActivity: ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ObooTheme {
                LoginScreen(this)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finishAndRemoveTask()
    }
}