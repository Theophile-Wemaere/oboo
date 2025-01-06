package fr.isep.oboo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime


@Entity
class TimeSlot(val subject: String, val startTime: String, val endTime: String, val roomId: Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
