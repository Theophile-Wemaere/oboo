package fr.isep.oboo.ui.components

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isep.oboo.BuildingDetailActivity
import fr.isep.oboo.R
import fr.isep.oboo.model.Building
import fr.isep.oboo.refreshDatabase
import fr.isep.oboo.ui.theme.ObooTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

//region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingsScreen(sourceActivity: Activity, menuIndex: Int, buildings: Flow<List<Building>>, onReturn: () -> Unit? = {})
{
    // pinnedScrollBehavior makes the top bar always visible but it changes color when scrolling
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        // nestedScrollConnection connects the scroll behavior of the top bar with the scroll behavior of the lazy column in the RoomList composable
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { TopAppBar(stringResource(R.string.screenTitle_Buildings), scrollBehavior, onReturn = onReturn)},
        bottomBar = { BottomNavigationBar(sourceActivity, menuIndex) }
    )
    {
        // Passing the modifier to the BuildingList composable allows the LazyColumn to be shifted downwards
        // with a padding that is the size of the top app bar itself (so that there's no overlap)
        contentPadding -> BuildingList(sourceActivity, buildings, modifier = Modifier.padding(contentPadding))
    }
}

@Composable
fun BuildingList(sourceActivity: Activity, buildings: Flow<List<Building>>, modifier: Modifier = Modifier)
{
    Column(modifier = modifier)
    {
        var isRefreshing by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        PullToRefreshLazyColumn(
            itemsFlow = buildings,
            content = { building: Building ->
                BuildingCard(sourceActivity, building)
                Spacer(Modifier.size(24.dp))
            },
            isRefreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true

                    try {
                        refreshDatabase()
                    }
                    catch (e: IOException) {
                        e.printStackTrace()
                        isRefreshing = false
                        return@launch
                    }
                    catch (e: HttpException) {
                        e.printStackTrace()
                        isRefreshing = false
                        return@launch
                    }
                    Log.i("Oboo API", "Successfully refreshed the local database using API data.")
                    isRefreshing = false
                }
            }
        )
    }
}

@Composable
fun BuildingCard(sourceActivity: Activity, building: Building)
{
    ElevatedCard(
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .size(height = 300.dp, width = 100.dp)
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        onClick = {
            val intent = Intent(sourceActivity, BuildingDetailActivity::class.java)
            intent.putExtra("menuIndex", 1)
            intent.putExtra("buildingId", building.id)
            sourceActivity.startActivity(intent)
        }
    )
    {
        Column()
        {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 12.dp))
            {
                Text(
                    text = building.name,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight(500),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(10.dp)

                )
                Spacer(Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f))
                {
                    Text(
                        text = building.longName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight(500),
                    )
                    Text(
                        text = building.getLocalizedCity(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400),
                    )
                }
            }
            // Fetch the building's picture (if the picture does not exist, the returned ID is 0)
            var buildingPictureId: Int = LocalContext.current.resources.getIdentifier(building.name.lowercase(), "drawable", LocalContext.current.packageName)
            if (buildingPictureId == 0)
            {
                Log.d("Rendering", "Could not find picture for building ${building.name}, using default picture instead.")
                buildingPictureId = R.drawable.missing
            }
            Image(
                painter = painterResource(buildingPictureId),
                contentDescription = building.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            )
        }
    }
}

//endregion Composables

//region Previews

@PreviewLightDark
@Composable
fun BuildingScreenPreview()
{
    val buildings: Flow<List<Building>> = flowOf(
        listOf(
            Building("NDC", "Notre-Dame-des-Champs", "Paris, 14th district"),
            Building("NDL", "Notre-Dame-de-Lorette", "Issy-les-Moulineaux")
        )
    )

    ObooTheme {
        BuildingsScreen(Activity(), 1, buildings)
    }
}

@PreviewLightDark
@Composable
fun BuildingListPreview()
{
    val buildings: Flow<List<Building>> = flowOf(
        listOf(
            Building("NDC", "Notre-Dame-des-Champs", "Paris, 14th district"),
            Building("NDL", "Notre-Dame-de-Lorette", "Issy-les-Moulineaux")
        )
    )

    ObooTheme()
    {
        BuildingList(Activity(), buildings)
    }
}

@PreviewLightDark
@Composable
fun BuildingCardPreview()
{
    ObooTheme {
        BuildingCard(Activity(), Building("NDC", "Notre-Dame-des-Champs", "Paris, 14th district"))
    }
}

//endregion Previews
