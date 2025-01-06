package fr.isep.oboo.model

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.isep.oboo.ObooApp
import fr.isep.oboo.ObooDatabase
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
class Room(val number: String, val name: String, val floorId: Long)
{
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    fun isAvailable(): Boolean
    {
        // Time comparisons are done in UTC. Time is only localized when being displayed to the user.
        val timeSlots: List<TimeSlot> = ObooDatabase.getInstance(ObooApp.applicationContext()).timeSlotDAO().getAllTimeSlotByRoom(this.id)
        val now: LocalDateTime = LocalDateTime.now(ZoneId.of("Z"))

        for (timeSlot in timeSlots)
        {
            val startTime = LocalDateTime.parse(timeSlot.startTime.dropLast(1))
            val endTime = LocalDateTime.parse(timeSlot.endTime.dropLast(1))

            Log.d("Room Model", "[${this.number}] Checking if now ($now) is between $startTime and $endTime")

            // If the current time fits into a time slot, then the room is not available
            if (now.isAfter(startTime) && now.isBefore(endTime))
                return false
        }

        return true
    }
}
