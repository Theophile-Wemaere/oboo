package fr.isep.oboo.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.isep.oboo.model.Building
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDAO
{
    @Insert
    suspend fun insertBuilding(building: Building): Long

    @Delete
    fun deleteBuilding(building: Building)

    @Query("DELETE FROM Building")
    suspend fun deleteAllBuildings()

    @Query("SELECT * FROM Building ORDER BY name ASC")
    fun getAllBuildings(): Flow<List<Building>>
}
