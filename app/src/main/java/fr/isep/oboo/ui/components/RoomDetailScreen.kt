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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import fr.isep.oboo.R
import fr.isep.oboo.model.Room
import fr.isep.oboo.ui.theme.ObooTheme

//region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(sourceActivity: Activity, menuIndex: Int, room: Room, onReturn: () -> Unit? = {})
{
    // pinnedScrollBehavior makes the top bar always visible but it changes color when scrolling
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        // nestedScrollConnection connects the scroll behavior of the top bar with the scroll behavior of the lazy column in the RoomList composable
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { TopAppBar(room.number, scrollBehavior, onReturn = onReturn)},
        bottomBar = { BottomNavigationBar(sourceActivity, menuIndex) }
    )
    {
        // Passing the modifier to the content composable allows it to be shifted downwards
        // with a padding that is the size of the top app bar itself (so that there's no overlap)
        contentPadding -> RoomDetailTabs(room, Modifier.padding(contentPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailTabs(room: Room, modifier: Modifier = Modifier)
{
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(R.string.tabInformation, R.string.tabTimetable)

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
        // Information tab
        if (selectedTabIndex == 0)
            RoomDetailTabInformation(room)
        // Timetable tab
        else if (selectedTabIndex == 1)
            RoomDetailTabTimetable(room)
    }
}

@Composable
fun RoomDetailTabInformation(room: Room)
{
    Text("Information tab content placeholder about room ${room.number}")
}

@Composable
fun RoomDetailTabTimetable(room: Room)
{
    Text("Timetable tab content placeholder about room ${room.number}")
}

//endregion Composables

//region Previews

@PreviewLightDark
@Composable
fun PreviewRoomDetailScreen()
{
    ObooTheme {
        RoomDetailScreen(Activity(), 3, Room("N16A", "Large classroom", 0))
    }
}

@PreviewLightDark
@Composable
fun PreviewRoomDetailTabs()
{
    ObooTheme {
        RoomDetailTabs(Room("N16A", "Large classroom", 0))
    }
}

//endregion Previews
