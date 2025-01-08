package fr.isep.oboo.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.isep.oboo.R


@Entity
class Building(val name: String, val longName: String, val city: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @Composable
    fun getLocalizedCity(): String
    {
        when (this.city)
        {
            "Paris, 14th district" -> return stringResource(R.string.buildingCityName_Paris14)
            "Issy-les-Moulineaux" -> return stringResource(R.string.buildingCityName_IssyLesMoulineaux)
        }
        return "<Missing translation>"
    }
}
