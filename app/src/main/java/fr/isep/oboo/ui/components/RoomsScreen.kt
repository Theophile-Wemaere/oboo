package fr.isep.oboo.ui.components

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isep.oboo.R
import fr.isep.oboo.model.Room
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
fun RoomsScreen(sourceActivity: Activity, menuIndex: Int, rooms: Flow<List<Room>>, onReturn: () -> Unit? = {})
{
    // pinnedScrollBehavior makes the top bar always visible but it changes color when scrolling
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        // nestedScrollConnection connects the scroll behavior of the top bar with the scroll behavior of the lazy column in the RoomList composable
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { TopAppBar(stringResource(R.string.screenTitle_Rooms), scrollBehavior, onReturn = onReturn)},
        bottomBar = { BottomNavigationBar(sourceActivity, menuIndex) }
    )
    {
        // Passing the modifier to the RoomList composable allows the LazyColumn to be shifted downwards
        // with a padding that is the size of the top app bar itself (so that there's no overlap)
            contentPadding -> RoomList(rooms, modifier = Modifier.padding(contentPadding))
    }
}

@Composable
fun RoomFilterChip(text: String, selected: MutableState<Boolean>, selectedContainerColor: Color = MaterialTheme.colorScheme.primary)
{
    FilterChip(
        onClick = {
            selected.value = !selected.value
        },
        label = {
            Text(text)
        },
        selected = selected.value,
        leadingIcon = if (selected.value) {
            { Icon(imageVector = Icons.Filled.Done, contentDescription = "Selected", modifier = Modifier.size(
                FilterChipDefaults.IconSize)) }
        }
        else {
            { Icon(imageVector = Icons.Filled.Close, contentDescription = "Unselected", modifier = Modifier.size(
                FilterChipDefaults.IconSize), tint = MaterialTheme.colorScheme.inverseSurface) }
        },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = selectedContainerColor)
    )
}

@Composable
fun RoomList(rooms: Flow<List<Room>>, modifier: Modifier = Modifier)
{
    Column(modifier = modifier)
    {
        // Chip filters states (selected by default)
        val showAvailableRooms = remember { mutableStateOf(true) }
        val showUnavailableRooms = remember { mutableStateOf(true) }

        var isRefreshing by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        PullToRefreshLazyColumnWithRoomFilterChips(
            itemsFlow = rooms,
            content = { room: Room ->
                if ((showAvailableRooms.value && room.isAvailable()) || (showUnavailableRooms.value && !room.isAvailable()))
                {
                    RoomCard(room)
                    Spacer(Modifier.size(10.dp))
                }
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
            },
            showAvailableRooms = showAvailableRooms,
            showUnavailableRooms = showUnavailableRooms
        )
    }
}

@Composable
fun RoomCard(room: Room, isRoomAvailable: Boolean? = null)
{
    // Allow overriding the availability value using a parameter
    // so that the Composable preview can manually set the availability
    val roomAvailable: Boolean
    if (isRoomAvailable != null)
        roomAvailable = isRoomAvailable
    else
        roomAvailable = room.isAvailable()

    ElevatedCard(
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        onClick = {/* TODO: Navigate to room details */ }
    )
    {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, end = 16.dp))
        {
            // Fetch the room's picture (if the picture does not exist, the returned ID is 0)
            var roomPictureId: Int = LocalContext.current.resources.getIdentifier(room.number.lowercase(), "drawable", LocalContext.current.packageName)
            if (roomPictureId == 0)
            {
                Log.d("Rendering", "Could not find picture for room ${room.number}, using default picture instead.")
                roomPictureId = R.drawable.missing
            }

            Image(
                painter = painterResource(roomPictureId),
                contentDescription = room.number.uppercase(),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.size(10.dp))
            Column(modifier = Modifier.weight(1f))
            {
                Row(verticalAlignment = Alignment.CenterVertically)
                {
                    Text(
                        text = room.number.uppercase(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight(500),
                    )
                    Spacer(Modifier.size(10.dp))
                    AvailabilityBadge(roomAvailable)
                }
                Text(
                    text = room.getLocalizedName(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight(400),
                )
            }
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}

@Composable
fun AvailabilityBadge(available: Boolean)
{
    Text(
        text = if (available) stringResource(R.string.statusBadge_Available) else stringResource(R.string.statusBadge_Unavailable),
        color = if (available) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onError,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color = if (available) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)
            .padding(vertical = 1.dp, horizontal = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PullToRefreshLazyColumnWithRoomFilterChips(
    itemsFlow: Flow<List<T>>,
    content: @Composable (T) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    showAvailableRooms: MutableState<Boolean>,
    showUnavailableRooms: MutableState<Boolean>
)
{
    val pullToRefreshState = rememberPullToRefreshState()
    val items by itemsFlow.collectAsState(initial = emptyList())

    Box(modifier = modifier.nestedScroll(pullToRefreshState.nestedScrollConnection))
    {
        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize())
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

            items(items) {
                content(it)
            }
        }

        if (pullToRefreshState.isRefreshing)
        {
            LaunchedEffect(true)
            {
                onRefresh()
            }
        }

        LaunchedEffect(isRefreshing)
        {
            if (isRefreshing)
                pullToRefreshState.startRefresh()
            else
                pullToRefreshState.endRefresh()
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

//endregion Composables

//region Previews

@PreviewLightDark
@Composable
fun RoomCardAvailablePreview()
{
    ObooTheme()
    {
        Column()
        {
            RoomCard(Room("N16A", "Large classroom", 0), true)
        }
    }
}

@PreviewLightDark
@Composable
fun RoomCardUnavailablePreview()
{
    ObooTheme()
    {
        Column()
        {
            RoomCard(Room("N16A", "Large classroom", 0), false)
        }
    }
}

@PreviewLightDark
@Composable
fun RoomListPreview()
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

    ObooTheme()
    {
        RoomList(rooms)
    }
}

@PreviewLightDark
@Composable
fun RoomScreenPreview()
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
        RoomsScreen(Activity(), 3, rooms)
    }
}

//endregion Previews
