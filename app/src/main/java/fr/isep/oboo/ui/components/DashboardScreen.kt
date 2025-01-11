package fr.isep.oboo.ui.components

import android.app.Activity
import android.util.Log
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isep.oboo.R
import fr.isep.oboo.model.Room
import fr.isep.oboo.ui.theme.ObooTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.round

//region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(sourceActivity: Activity, menuIndex: Int, rooms: Flow<List<Room>>, onReturn: () -> Unit? = {}, preview: Boolean = false)
{
    // pinnedScrollBehavior makes the top bar always visible but it changes color when scrolling
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        // nestedScrollConnection connects the scroll behavior of the top bar with the scroll behavior of the lazy column in the RoomList composable
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { TopAppBar(stringResource(R.string.screenTitle_Dashboard), scrollBehavior, false, onReturn)},
        bottomBar = { BottomNavigationBar(sourceActivity, menuIndex) }
    )
    {
        // Passing the modifier to the content composable allows it to be shifted downwards
        // with a padding that is the size of the top app bar itself (so that there's no overlap)
        contentPadding -> DashboardContent(rooms, modifier = Modifier.padding(contentPadding), preview = preview)
    }
}

@Composable
fun DashboardContent(roomsFlow: Flow<List<Room>>, modifier: Modifier = Modifier, preview: Boolean = false)
{
    val rooms by roomsFlow.collectAsState(initial = emptyList())

    LazyColumn(modifier = modifier.fillMaxSize())
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
            InfoCard(stringResource(R.string.infoCard_RoomAvailability), "$roomAvailabilityPercentage%", Modifier.fillMaxWidth())

            // TODO: Add graph to visualize room availability over the day
        }
    }
}

//endregion Composables

//region Previews

@PreviewLightDark
@Composable
fun DashboardScreenPreview()
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
        DashboardScreen(Activity(), 0, rooms, preview = true)
    }
}

@PreviewLightDark
@Composable
fun DashboardContentPreview()
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
        DashboardContent(rooms, preview = true)
    }
}

@PreviewLightDark
@Composable
fun DoubleInfoCardPreview()
{
    ObooTheme {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly)
        {
            InfoCard("Available rooms", "14", Modifier.padding(horizontal = 16.dp))
            InfoCard("Occupied rooms", "19", Modifier.padding(horizontal = 16.dp))
        }
    }
}

@PreviewLightDark
@Composable
fun InfoCardPreview()
{
    ObooTheme {
        InfoCard("Room availability", "42%", Modifier.fillMaxWidth())
    }
}

@PreviewLightDark
@Composable
fun FixedInfoCardPreview()
{
    ObooTheme {
        FixedSizeInfoCard("Room availability", "42%")
    }
}

//ednregion Previews
