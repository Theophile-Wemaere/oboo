package fr.isep.oboo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance
{
    val api: ObooAPI by lazy {
        Retrofit.Builder()
            // .baseUrl("http://10.0.2.2:8888/api/")
            .baseUrl("https://api.middle-earth.ovh/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ObooAPI::class.java)
    }
}
