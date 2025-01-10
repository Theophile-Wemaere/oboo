package fr.isep.oboo.ui.components

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isep.oboo.FloorDetailActivity
import fr.isep.oboo.R
import fr.isep.oboo.model.Floor
import fr.isep.oboo.refreshDatabase
import fr.isep.oboo.ui.theme.ObooTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorsScreen(sourceActivity: Activity, menuIndex: Int, floors: Flow<List<Floor>>, onReturn: () -> Unit? = {})
{
    // pinnedScrollBehavior makes the top bar always visible but it changes color when scrolling
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        // nestedScrollConnection connects the scroll behavior of the top bar with the scroll behavior of the lazy column in the RoomList composable
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { TopAppBar(stringResource(R.string.screenTitle_Floors), scrollBehavior, onReturn = onReturn)},
        bottomBar = { BottomNavigationBar(sourceActivity, menuIndex) }
    )
    {
        // Passing the modifier to the FloorList composable allows the LazyColumn to be shifted downwards
        // with a padding that is the size of the top app bar itself (so that there's no overlap)
        contentPadding -> FloorList(sourceActivity, floors, modifier = Modifier.padding(contentPadding))
    }
}

@Composable
fun FloorList(sourceActivity: Activity, floors: Flow<List<Floor>>, modifier: Modifier = Modifier)
{
    Column(modifier = modifier)
    {
        var isRefreshing by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        PullToRefreshLazyColumn(
            itemsFlow = floors,
            content = { floor: Floor ->
                FloorCard(sourceActivity, floor)
                Spacer(Modifier.size(10.dp))
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
fun FloorCard(sourceActivity: Activity, floor: Floor, preview: Boolean = false)
{
    ElevatedCard(
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        onClick = {
            val intent = Intent(sourceActivity, FloorDetailActivity::class.java)
            intent.putExtra("menuIndex", 2)
            intent.putExtra("floorId", floor.id)
            sourceActivity.startActivity(intent)
        }
    )
    {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 12.dp))
        {
            Text(
                text = floor.number.toString(),
                fontSize = 32.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight(500)
            )
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f))
            {
                Text(
                    text = if (!preview) floor.getBuilding().name.uppercase() else "NDC",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight(500),
                )
                if (floor.number == 0)
                {
                    Text(
                        text = stringResource(R.string.floorName_GroundFloor),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400),
                    )
                }
                else
                {
                    Text(
                        text = "${stringResource(R.string.floorName_Floor)} ${floor.number}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight(400),
                    )
                }
            }
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

//endregion Composables

//region Previews

@PreviewLightDark
@Composable
fun FloorScreenPreview()
{
    val floors: Flow<List<Floor>> = flowOf(
        listOf(
            Floor(0, "Ground floor", 0),
            Floor(1, "Floor 1", 0),
            Floor(2, "Floor 2", 0),
            Floor(3, "Floor 3", 0),
            Floor(4, "Floor 4", 0),
        )
    )

    ObooTheme {
        FloorsScreen(Activity(), 2, floors)
    }
}

@PreviewLightDark
@Composable
fun FloorListPreview()
{
    val floors: Flow<List<Floor>> = flowOf(
        listOf(
            Floor(0, "Ground floor", 0),
            Floor(1, "Floor 1", 0),
            Floor(2, "Floor 2", 0),
            Floor(3, "Floor 3", 0),
            Floor(4, "Floor 4", 0),
        )
    )

    ObooTheme()
    {
        FloorList(Activity(), floors)
    }
}

@PreviewLightDark
@Composable
fun FloorCardPreview()
{
    ObooTheme {
        FloorCard(Activity(), Floor(0, "Ground floor", 0), true)
    }
}

//endregion Previews
