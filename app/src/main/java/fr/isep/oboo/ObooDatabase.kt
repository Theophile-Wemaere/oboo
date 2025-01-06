package fr.isep.oboo

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import fr.isep.oboo.dao.BuildingDAO
import fr.isep.oboo.dao.FloorDAO
import fr.isep.oboo.dao.RoomDAO
import fr.isep.oboo.dao.TimeSlotDAO
import fr.isep.oboo.model.Building
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
import fr.isep.oboo.model.TimeSlot

@Database(
    entities = [Building::class, Floor::class, Room::class, TimeSlot::class],
    version = 1
)
abstract class ObooDatabase: RoomDatabase()
{
    abstract fun buildingDAO(): BuildingDAO

    abstract fun floorDAO(): FloorDAO

    abstract fun roomDAO(): RoomDAO

    abstract fun timeSlotDAO(): TimeSlotDAO

    // Singleton instance of the database object that can be accessed in the entire project
    companion object {
        private var instance: ObooDatabase? = null

        fun getInstance(context: Context): ObooDatabase
        {
            if (instance == null)
                instance = androidx.room.Room.databaseBuilder(context, ObooDatabase::class.java, "oboo-db").allowMainThreadQueries().build()

            return instance as ObooDatabase
        }
    }
}
