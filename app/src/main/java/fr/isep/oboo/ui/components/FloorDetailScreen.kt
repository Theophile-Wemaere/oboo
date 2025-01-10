package fr.isep.oboo.ui.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import fr.isep.oboo.R
import fr.isep.oboo.model.Building
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
import fr.isep.oboo.ui.theme.ObooTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

//region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorDetailScreen(sourceActivity: Activity, menuIndex: Int, building: Building, floor: Floor, rooms: Flow<List<Room>>, onReturn: () -> Unit? = {})
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
        contentPadding -> FloorDetailTabs(building, floor, rooms, Modifier.padding(contentPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorDetailTabs(building: Building, floor: Floor, rooms: Flow<List<Room>>, modifier: Modifier = Modifier)
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
            0 -> FloorDetailTabInformation(building, floor)
            1 -> FloorDetailTabRooms(building, floor, rooms)
        }
    }
}

@Composable
fun FloorDetailTabInformation(building: Building, floor: Floor)
{
    Text("Information tab content placeholder about ${floor.name} (${building.name})")
}

@Composable
fun FloorDetailTabRooms(building: Building, floor: Floor, rooms: Flow<List<Room>>)
{
    Text("Tab content placeholder about rooms of ${floor.name} (${building.name})")
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
        FloorDetailScreen(Activity(), 1, Building("NDL", "Notre-Dame-de-Lorette", "Issy-les-Moulineaux"), Floor(0, "Ground floor", 0), rooms)
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
        FloorDetailTabs(Building("NDL", "Notre-Dame-de-Lorette", "Issy-les-Moulineaux"), Floor(0, "Ground floor", 0), rooms)
    }
}

//endregion Previews
