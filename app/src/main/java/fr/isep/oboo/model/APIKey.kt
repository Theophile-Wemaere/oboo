package fr.isep.oboo.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class APIKey(val email: String, val key: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
