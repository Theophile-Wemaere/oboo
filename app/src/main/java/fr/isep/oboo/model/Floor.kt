package fr.isep.oboo.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class Floor(val number: Int, val name: String, val buildingId: Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
