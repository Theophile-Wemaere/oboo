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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import fr.isep.oboo.R
import fr.isep.oboo.model.Building
import fr.isep.oboo.model.Floor
import fr.isep.oboo.model.Room
import fr.isep.oboo.model.TimeSlot
import fr.isep.oboo.ui.theme.ObooTheme
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

//region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(sourceActivity: Activity, menuIndex: Int, building: Building, floor: Floor, room: Room, timeSlots: List<TimeSlot>, onReturn: () -> Unit? = {}, preview: Boolean = false)
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
        contentPadding -> RoomDetailTabs(building, floor, room, timeSlots, Modifier.padding(contentPadding), preview)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailTabs(building: Building, floor: Floor, room: Room, timeSlots: List<TimeSlot>, modifier: Modifier = Modifier, preview: Boolean = false)
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
            RoomDetailTabInformation(building, floor, room, preview)
        // Timetable tab
        else if (selectedTabIndex == 1)
            RoomDetailTabTimetable(room, timeSlots)
    }
}

@Composable
fun RoomDetailTabInformation(building: Building, floor: Floor, room: Room, preview: Boolean = false)
{
    // Fetch the room's picture (if the picture does not exist, the returned ID is 0)
    var roomPictureId: Int = LocalContext.current.resources.getIdentifier(room.number.lowercase(), "drawable", LocalContext.current.packageName)
    if (roomPictureId == 0)
    {
        Log.d("Rendering", "Could not find picture for room ${room.number}, using default picture instead.")
        roomPictureId = R.drawable.missing
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(vertical = 16.dp))
    {
        items(1)
        {
            if (!preview && room.isAvailable())
                InfoCard(stringResource(R.string.infoCard_Status), "Available", valueColor = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth())
            else
                InfoCard(stringResource(R.string.infoCard_Status), "Unavailable", valueColor = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.size(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween)
            {
                FixedSizeInfoCard(stringResource(R.string.infoCard_Building), building.name)
                FixedSizeInfoCard(stringResource(R.string.infoCard_Floor), floor.number.toString())
            }
            Spacer(Modifier.size(10.dp))
            Image(
                painter = painterResource(roomPictureId),
                contentDescription = building.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .size(height = 250.dp, width = 100.dp)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
fun RoomDetailTabTimetable(room: Room, timeSlots: List<TimeSlot>)
{
    val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    var previousEndTime: LocalDateTime? by remember { mutableStateOf(null) }
    
    LazyColumn(modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp))
    {
        items(timeSlots) {   timeSlot ->

            if (previousEndTime != null)
            {
                val start = LocalDateTime.parse(timeSlot.endTime.dropLast(1))
                // If the previous TimeSlot and the current TimeSlot are NOT consecutive,
                // separate both with a TimeSlot with the subject "Available" (to "fill-in" the hole)
                if (ChronoUnit.MINUTES.between(previousEndTime, start) > 0)
                {
                    TimeSlotCard(TimeSlot(stringResource(R.string.timeSlotSubject_Available), previousEndTime!!.format(dtf), timeSlot.startTime, -1))
                    Spacer(Modifier.size(10.dp))
                }

                TimeSlotCard(timeSlot)
                Spacer(Modifier.size(10.dp))
                // Set the current TimeSlot endTime as the previousEndTime for the next iteration
                previousEndTime = LocalDateTime.parse(timeSlot.endTime.dropLast(1))
            }
            else
            {
                TimeSlotCard(timeSlot)
                Spacer(Modifier.size(10.dp))
                previousEndTime = LocalDateTime.parse(timeSlot.endTime.dropLast(1))
            }
        }
        if (previousEndTime != null)
        {
            items(1)
            {
                // Add an available slot to fill-in the gap to 20:00 (Europe/Paris TZ)
                val lastTimeSlotEndTime = ZonedDateTime.of(previousEndTime, ZoneId.of("Europe/Paris"))
                if (lastTimeSlotEndTime.hour < 20)
                    TimeSlotCard(TimeSlot(stringResource(R.string.timeSlotSubject_Available), previousEndTime!!.format(dtf), LocalDateTime.of(previousEndTime!!.year, previousEndTime!!.month, previousEndTime!!.dayOfMonth, 20, 0,0).format(dtf), -1))
            }
        }
    }
}

@Composable
fun TimeSlotCard(timeSlot: TimeSlot, modifier: Modifier = Modifier)
{
    // Size for a 30min event
    val baseSize = 40

    val dtf = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Europe/Paris"))

    val start = LocalDateTime.parse(timeSlot.startTime.dropLast(1))
    val end = LocalDateTime.parse(timeSlot.endTime.dropLast(1))
    val now = LocalDateTime.now(ZoneOffset.UTC)

    val startLocal = ZonedDateTime.of(start, ZoneId.of("UTC"))
    val endLocal = ZonedDateTime.of(end, ZoneId.of("UTC"))

    // Duration of the event in blocks of 30 minutes (a duration of 5 means 2h30)
    var duration = ChronoUnit.MINUTES.between(start, end).toInt() / 30

    if (duration < 2)
        duration = 2

    Column(modifier = modifier
        .fillMaxWidth()
        .sizeIn(maxHeight = 400.dp)
        .size(width = 100.dp, height = (baseSize * duration).dp)
        .padding(end = 2.dp, bottom = 2.dp)
        .background(if (timeSlot.subject == stringResource(R.string.timeSlotSubject_Available)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(4.dp))
        .padding(8.dp),
    )
    {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp))
        {
            Text(
                text = "${startLocal.format(dtf)} - ${endLocal.format(dtf)}",
                color = if (timeSlot.subject == stringResource(R.string.timeSlotSubject_Available)) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onError
            )
            if (now.isAfter(start) && now.isBefore(end))
            {
                Badge(
                    content = { Text(text = stringResource(R.string.badge_Now), modifier = Modifier.padding(1.dp)) },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            }
        }
        HorizontalDivider(
            color = if (timeSlot.subject == stringResource(R.string.timeSlotSubject_Available)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 2.dp)
        )
        Text(
            text = timeSlot.subject,
            color = if (timeSlot.subject == stringResource(R.string.timeSlotSubject_Available)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
        )
    }
}

//endregion Composables

//region Previews

@PreviewLightDark
@Composable
fun PreviewRoomDetailScreen()
{
    ObooTheme {

        val timeSlots: List<TimeSlot> = listOf(
            TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T08:00:00", "2025-01-01T08:30:00", 0),
            TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T11:00:00", "2025-01-01T12:30:00", 0),
            TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T14:00:00", "2025-01-01T16:30:00", 0),
            TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T17:00:00", "2025-01-01T18:00:00", 0),
        )

        RoomDetailScreen(
            Activity(),
            3,
            Building("NDL", "Notre-Dame-de-Lorette", "Issy-les-Moulineaux"),
            Floor(0, "Ground floor", 0), Room("N16A", "Large classroom", 0),
            timeSlots,
            preview = true
        )
    }
}

@PreviewLightDark
@Composable
fun PreviewRoomDetailTabs()
{
    ObooTheme {

        val timeSlots: List<TimeSlot> = listOf(
            TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T08:00:00", "2025-01-01T08:30:00", 0),
            TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T11:00:00", "2025-01-01T12:30:00", 0),
            TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T14:00:00", "2025-01-01T16:30:00", 0),
            TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T17:00:00", "2025-01-01T18:00:00", 0),
        )

        RoomDetailTabs(
            Building("NDL", "Notre-Dame-de-Lorette", "Issy-les-Moulineaux"),
            Floor(0, "Ground floor", 0),
            Room("N16A", "Large classroom", 0),
            timeSlots,
            preview = true
        )
    }
}

@PreviewLightDark
@Composable
fun RoomDetailTabTimetablePreview()
{
    val timeSlots: List<TimeSlot> = listOf(
        TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T08:00:00Z", "2025-01-01T08:30:00Z", 0),
        TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T11:00:00Z", "2025-01-01T12:30:00Z", 0),
        TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T14:00:00Z", "2025-01-01T16:30:00Z", 0),
        TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-01T17:00:00Z", "2025-01-01T18:00:00Z", 0),
    )

    ObooTheme {
        RoomDetailTabTimetable(Room("L012", "Amphitheater", 0), timeSlots)
    }
}

@PreviewLightDark
@Composable
fun TimeSlotCardAvailablePreview()
{
    ObooTheme {
        TimeSlotCard(TimeSlot("Available", "2025-01-18T08:00:00Z", "2025-01-18T10:30:00Z", 0))
    }
}

@PreviewLightDark
@Composable
fun TimeSlotCardUnavailablePreview()
{
    ObooTheme {
        TimeSlotCard(TimeSlot("FC - MS Protection Données - FC (MS) - MS MPDCP Octobre 2024-Octobre 2025", "2025-01-18T08:00:00Z", "2025-01-18T08:30:00Z", 0))
    }
}

//endregion Previews
