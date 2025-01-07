package fr.isep.oboo.model

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.isep.oboo.ObooApp
import fr.isep.oboo.ObooDatabase
import fr.isep.oboo.R
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
class Room(val number: String, val name: String, val floorId: Long)
{
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @Composable
    fun getLocalizedName(): String
    {
        when (this.name)
        {
            "Amphitheater" -> return stringResource(R.string.roomName_Amphitheater)
            "Cloister" -> return stringResource(R.string.roomName_Cloister)
            "Classroom" -> return stringResource(R.string.roomName_Classroom)
            "Large classroom" -> return stringResource(R.string.roomName_LargeClassroom)
            "Lab room" -> return stringResource(R.string.roomName_LabRoom)
            "Auditorium" -> return stringResource(R.string.roomName_Auditorium)
        }
        return "<Missing translation>"
    }

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
