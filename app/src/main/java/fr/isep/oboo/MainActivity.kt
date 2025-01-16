package fr.isep.oboo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class MainActivity: ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val apiKey = ObooDatabase.getInstance(ObooApp.applicationContext()).apiKeyDAO().getAPIKey()

        if (apiKey == null)
        {
            ObooApp.applicationContext().startActivity(Intent(ObooApp.applicationContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            Log.w("Main Activity", "No API key found, redirecting to login page...")
        }
        else
        {
            ObooApp.applicationContext().startActivity(Intent(ObooApp.applicationContext(), DashboardActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            Log.i("Main Activity", "Existing API key found, redirecting to Dashboard...")
        }
    }
}