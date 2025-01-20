package fr.isep.oboo.model

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

        if (timeSlots.isEmpty())
            return true

        val timeSlotDay: LocalDateTime = LocalDateTime.parse(timeSlots.first().startTime.dropLast(1))
        val instant: LocalDateTime = LocalDateTime.now(ZoneId.of("Z"))
        val now: LocalDateTime = LocalDateTime.of(timeSlotDay.year, timeSlotDay.month, timeSlotDay.dayOfMonth, instant.hour, instant.minute, instant.second)

        for (timeSlot in timeSlots)
        {
            val startTime = LocalDateTime.parse(timeSlot.startTime.dropLast(1))
            val endTime = LocalDateTime.parse(timeSlot.endTime.dropLast(1))

            // If the current time fits into a time slot, then the room is not available
            if (now.isAfter(startTime) && now.isBefore(endTime))
                return false
        }

        return true
    }

    fun isAvailableAt(hourUTC: Int): Boolean
    {
        val timeSlots: List<TimeSlot> = ObooDatabase.getInstance(ObooApp.applicationContext()).timeSlotDAO().getAllTimeSlotByRoom(this.id)

        if (timeSlots.isEmpty())
            return true

        val now: LocalDateTime = LocalDateTime.parse(timeSlots.first().startTime.dropLast(1))
        val specificTime: LocalDateTime = LocalDateTime.of(now.year, now.month, now.dayOfMonth, hourUTC, 0, 0)

        for (timeSlot in timeSlots)
        {
            val startTime = LocalDateTime.parse(timeSlot.startTime.dropLast(1))
            val endTime = LocalDateTime.parse(timeSlot.endTime.dropLast(1))

            // If the specific time fits into a time slot, then the room is not available at that specific time
            if (specificTime.isAfter(startTime) && specificTime.isBefore(endTime))
                return false
        }

        return true
    }
}
