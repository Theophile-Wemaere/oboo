package fr.isep.oboo.ui.components

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.isep.oboo.R
import fr.isep.oboo.model.Building
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
import fr.isep.oboo.ui.theme.ObooTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.round

//region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorDetailScreen(sourceActivity: Activity, menuIndex: Int, building: Building, floor: Floor, rooms: Flow<List<Room>>, preview: Boolean = false, onReturn: () -> Unit? = {})
{
    // pinnedScrollBehavior makes the top bar always visible but it changes color when scrolling
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        // nestedScrollConnection connects the scroll behavior of the top bar with the scroll behavior of the lazy column in the RoomList composable
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { TopAppBar("${building.name} - ${if (floor.number == 0) stringResource(R.string.floorName_GroundFloor) else stringResource(R.string.floorName_Floor)} ${if (floor.number != 0) floor.number else ""}", scrollBehavior, onReturn = onReturn)},
        bottomBar = { BottomNavigationBar(sourceActivity, menuIndex) }
    )
    {
        // Passing the modifier to the content composable allows it to be shifted downwards
        // with a padding that is the size of the top app bar itself (so that there's no overlap)
        contentPadding -> FloorDetailTabs(sourceActivity, building, floor, rooms, Modifier.padding(contentPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorDetailTabs(sourceActivity: Activity, building: Building, floor: Floor, rooms: Flow<List<Room>>, modifier: Modifier = Modifier, preview: Boolean = false)
{
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(R.string.tabInformation, R.string.tabRooms)

    Column(modifier)
    {
        PrimaryTabRow(selectedTabIndex = selectedTabIndex)
        {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = stringResource(title), maxLines = 2, overflow = TextOverflow.Ellipsis) }
                )
            }
        }
        // Tabs: Information, Rooms
        when (selectedTabIndex)
        {
            0 -> FloorDetailTabInformation(building, floor, rooms, preview)
            1 -> FloorDetailTabRooms(sourceActivity, building, floor, rooms)
        }
    }
}

@Composable
fun FloorDetailTabInformation(building: Building, floor: Floor, roomsFlow: Flow<List<Room>>, preview: Boolean = false)
{
    val rooms by roomsFlow.collectAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.fillMaxSize())
    {
        var availableRooms = 0f
        var unavailableRooms = 0f
        var roomAvailabilityPercentage = 0

        if (!preview)
        {
            items(rooms)
            {
                if (it.isAvailable())
                    availableRooms++
                else
                    unavailableRooms++

                roomAvailabilityPercentage = try {
                    round((availableRooms / (availableRooms + unavailableRooms)) * 100f).toInt()
                } catch (e: ArithmeticException) {
                    0
                }
            }
        }
        else
        {
            availableRooms = 4f
            unavailableRooms = 8f
            roomAvailabilityPercentage = 33
        }

        items(1)
        {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween)
            {
                FixedSizeInfoCard(stringResource(R.string.infoCard_AvailableRooms), availableRooms.toInt().toString())
                FixedSizeInfoCard(stringResource(R.string.infoCard_UnavailableRooms), unavailableRooms.toInt().toString())
            }
            Spacer(Modifier.size(10.dp))
            InfoCard(stringResource(R.string.infoCard_RoomAvailability), "$roomAvailabilityPercentage%", modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun FloorDetailTabRooms(sourceActivity: Activity, building: Building, floor: Floor, rooms: Flow<List<Room>>)
{
    val items by rooms.collectAsState(initial = emptyList())

    // Chip filters states (selected by default)
    val showAvailableRooms = remember { mutableStateOf(true) }
    val showUnavailableRooms = remember { mutableStateOf(true) }

    LazyColumn()
    {
        items(1) {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp))
            {
                RoomFilterChip(stringResource(R.string.filterChip_Available), showAvailableRooms)
                Spacer(Modifier.size(32.dp))
                RoomFilterChip(stringResource(R.string.filterChip_Unavailable), showUnavailableRooms, MaterialTheme.colorScheme.error)
            }
        }

        items(items)
        {
            if ((showAvailableRooms.value && it.isAvailable()) || (showUnavailableRooms.value && !it.isAvailable()))
            {
                RoomCard(sourceActivity, it)
                Spacer(Modifier.size(10.dp))
            }
        }
    }
}

//endregion Composables

//region Previews

@PreviewLightDark
@Composable
fun FloorDetailScreenPreview()
{

    val rooms: Flow<List<Room>> = flowOf(
        listOf(
            Room("N16A", "Large classroom", 0),
            Room("L012", "Amphitheater", 0),
            Room("L213", "Lab room", 0),
            Room("L306", "Classroom", 0),
            Room("L416", "Auditorium", 0),
        )
    )

    ObooTheme {
        FloorDetailScreen(Activity(), 1, Building("NDL", "Notre-Dame-de-Lorette", "Issy-les-Moulineaux"), Floor(0, "Ground floor", 0), rooms, preview = true)
    }
}

@PreviewLightDark
@Composable
fun FloorDetailTabsPreview()
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

    val rooms: Flow<List<Room>> = flowOf(
        listOf(
            Room("N16A", "Large classroom", 0),
            Room("L012", "Amphitheater", 0),
            Room("L213", "Lab room", 0),
            Room("L306", "Classroom", 0),
            Room("L416", "Auditorium", 0),
        )
    )

    ObooTheme {
        FloorDetailTabs(Activity(), Building("NDL", "Notre-Dame-de-Lorette", "Issy-les-Moulineaux"), Floor(0, "Ground floor", 0), rooms, preview = true)
    }
}

//endregion Previews
