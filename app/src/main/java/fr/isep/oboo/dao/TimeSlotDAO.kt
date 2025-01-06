package fr.isep.oboo.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import fr.isep.oboo.model.Room
import fr.isep.oboo.model.TimeSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDAO
{
    @Insert
    suspend fun insertTimeSlot(timeSlot: TimeSlot): Long

    @Delete
    fun deleteTimeSlot(timeSlot: TimeSlot)

    @Query("DELETE FROM TimeSlot")
    suspend fun deleteAllTimeSlots()

    @Query("SELECT * FROM TimeSlot ORDER BY roomId ASC")
    fun getAllTimeSlot(): Flow<List<TimeSlot>>

    @Query("SELECT * FROM TimeSlot WHERE roomId = :roomId")
    fun getAllTimeSlotByRoom(roomId: Long): List<TimeSlot>
}
