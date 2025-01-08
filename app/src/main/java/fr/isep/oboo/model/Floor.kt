package fr.isep.oboo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.isep.oboo.ObooApp
import fr.isep.oboo.ObooDatabase


@Entity
class Floor(val number: Int, val name: String, val buildingId: Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    fun getBuilding(): Building
    {
        return ObooDatabase.getInstance(ObooApp.applicationContext()).buildingDAO().getBuildingById(this.buildingId)
    }
}
