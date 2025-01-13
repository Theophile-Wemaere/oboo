package fr.isep.oboo

import android.app.Application
import android.content.Context
import android.util.Log
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ObooApp: Application()
{
    init {
        instance = this
    }

    companion object {
        private var instance: ObooApp? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        val context: Context = ObooApp.applicationContext()

        // Rebuild the local database on app startup by querying the API
        MainScope().launch {
            refreshDatabase()
        }
    }
}
