package fr.isep.oboo.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.isep.oboo.model.Building
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
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

    @Query("SELECT * FROM Building WHERE id = :id")
    fun getBuildingById(id: Long): Building

    @Query("SELECT * FROM Building ORDER BY name ASC")
    fun getAllBuildings(): Flow<List<Building>>

    @Query("SELECT * FROM Floor WHERE buildingId = :buildingId ORDER BY number ASC")
    fun getAllFloors(buildingId: Long): Flow<List<Floor>>

    @Query("SELECT * FROM Room WHERE floorId IN (SELECT id FROM Floor WHERE buildingId = :buildingId) ORDER BY number ASC")
    fun getAllRooms(buildingId: Long): Flow<List<Room>>
}
