package fr.isep.oboo.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
import fr.isep.oboo.model.TimeSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDAO
{
    @Insert
    suspend fun insertRoom(room: Room): Long

    @Delete
    fun deleteRoom(room: Room)

    @Query("DELETE FROM Room")
    suspend fun deleteAllRooms()

    @Query("SELECT * FROM Room WHERE id = :id")
    fun getRoomById(id: Long): Room

    @Query("SELECT * FROM Room ORDER BY number ASC")
    fun getAllRooms(): Flow<List<Room>>

    @Query("SELECT * FROM TimeSlot WHERE roomId = :roomId ORDER BY startTime ASC")
    fun getTimeSlots(roomId: Long): List<TimeSlot>
}
