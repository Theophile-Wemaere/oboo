package fr.isep.oboo.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.isep.oboo.model.Floor
import kotlinx.coroutines.flow.Flow

@Dao
interface FloorDAO
{
    @Insert
    suspend fun insertFloor(floor: Floor): Long

    @Delete
    fun deleteFloor(floor: Floor)

    @Query("DELETE FROM Floor")
    suspend fun deleteAllFloors()

    @Query("SELECT * FROM Floor ORDER BY name ASC")
    fun getAllFloors(): Flow<List<Floor>>
}