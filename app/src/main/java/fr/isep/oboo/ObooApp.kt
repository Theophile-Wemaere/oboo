package fr.isep.oboo

import android.app.Application
import android.content.Context

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
    }
}
