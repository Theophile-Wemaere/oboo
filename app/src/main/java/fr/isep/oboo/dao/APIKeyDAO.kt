package fr.isep.oboo.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import fr.isep.oboo.model.APIKey

@Dao
interface APIKeyDAO
{
    @Query("SELECT * FROM APIKey LIMIT 1")
    fun getAPIKey(): APIKey?

    @Upsert
    fun setAPIKey(apiKey: APIKey)

    @Query("DELETE FROM APIKey")
    fun deleteAPIKeys()
}
