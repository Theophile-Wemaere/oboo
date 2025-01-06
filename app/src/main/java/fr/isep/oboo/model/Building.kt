package fr.isep.oboo.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class Building(val name: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
