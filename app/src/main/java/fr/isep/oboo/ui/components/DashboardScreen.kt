package fr.isep.oboo.ui.components

import android.app.Activity
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isep.oboo.R
import fr.isep.oboo.model.Room
import fr.isep.oboo.ui.theme.ObooTheme
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.round

//region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(sourceActivity: Activity, menuIndex: Int, rooms: Flow<List<Room>>, roomsList: List<Room>, onReturn: () -> Unit? = {}, preview: Boolean = false)
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
        contentPadding ->

        val primaryColor: Color = MaterialTheme.colorScheme.primary
        val errorColor: Color = MaterialTheme.colorScheme.error

        val barsData: MutableList<Bars> = mutableListOf()

        // 08:00 and 18:00 in Europe/Paris timezone
        val start = ZonedDateTime.of(LocalDateTime.of(2025, 1, 1, 8, 0, 0), ZoneId.of("Europe/Paris"))
        val end = ZonedDateTime.of(LocalDateTime.of(2025, 1, 1, 18, 0, 0), ZoneId.of("Europe/Paris"))

        val startHourUTC = start.withZoneSameInstant(ZoneId.of("UTC")).hour
        val endHourUTC = end.withZoneSameInstant(ZoneId.of("UTC")).hour
        var hourLocal = start.hour

        for (hour in startHourUTC..endHourUTC)
        {
            var availableRooms = 0.0
            var unavailableRooms = 0.0

            for (room in roomsList)
            {
                if (room.isAvailableAt(hour))
                    availableRooms++
                else
                {
                    Log.i("Dashboard data", "Room ${room.number} is unavailable at $hour UTC (Local $hourLocal)")
                    unavailableRooms++
                }
            }

            barsData.add(Bars(
                label = if (hourLocal < 10) "0$hourLocal:00" else "$hourLocal:00",
                values = listOf(
                    Bars.Data(label = stringResource(R.string.dashboardChartLabel_Available), value = availableRooms, color = SolidColor(primaryColor)),
                    Bars.Data(label = stringResource(R.string.dashboardChartLabel_Unavailable), value = -unavailableRooms, color = SolidColor(errorColor)),
                ),
            ))

            hourLocal++
        }

        DashboardContent(rooms, barsData, roomsList.count().toDouble(), modifier = Modifier.padding(contentPadding), preview = preview)
    }
}

@Composable
fun DashboardContent(roomsFlow: Flow<List<Room>>, graphData: List<Bars>, totalRooms: Double, modifier: Modifier = Modifier, preview: Boolean = false) {
    val rooms by roomsFlow.collectAsState(initial = emptyList())

    Column(modifier = modifier.fillMaxSize())
    {
        LazyColumn(Modifier.padding(bottom = 16.dp))
        {
            var availableRooms = 0f
            var unavailableRooms = 0f
            var roomAvailabilityPercentage = 0

            if (!preview) {
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
            } else {
                availableRooms = 4f
                unavailableRooms = 8f
                roomAvailabilityPercentage = 33
            }

            items(1)
            {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                )
                {
                    FixedSizeInfoCard(
                        stringResource(R.string.infoCard_AvailableRooms),
                        availableRooms.toInt().toString()
                    )
                    FixedSizeInfoCard(
                        stringResource(R.string.infoCard_UnavailableRooms),
                        unavailableRooms.toInt().toString()
                    )
                }
                Spacer(Modifier.size(10.dp))
                Column(Modifier.padding(bottom = 16.dp))
                {
                    InfoCard(
                        stringResource(R.string.infoCard_RoomAvailability),
                        "$roomAvailabilityPercentage%",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        // TODO: Add graph to visualize room availability over the day

        DashboardChart(totalRooms, graphData)
    }
}

@Composable
fun DashboardChart(totalRooms: Double, barsData: List<Bars>)
{
    Column(Modifier.padding(bottom = 32.dp, start = 10.dp, end = 10.dp))
    {
        Text(stringResource(R.string.dashboardChartTitle), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        Spacer(Modifier.size(10.dp))
        ColumnChart(
            modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
            data = remember { barsData },
            maxValue = totalRooms,
            minValue = -totalRooms,
            barProperties = BarProperties(
                cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
                spacing = 1.dp,
                thickness = 6.dp
            ),
            gridProperties = GridProperties(
                enabled = false
            ),
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = false,
            ),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimary),
                rotation = LabelProperties.Rotation(
                    mode = LabelProperties.Rotation.Mode.Force,
                    degree = -45f
                )
            ),
            labelHelperProperties = LabelHelperProperties(
                enabled = true,
                textStyle = TextStyle(MaterialTheme.colorScheme.onPrimary)
            ),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
        )
    }
}

//endregion Composables

//region Previews

@PreviewLightDark
@Composable
fun DashboardScreenPreview()
{
    val roomsList = listOf(
        Room("N16A", "Large classroom", 0),
        Room("L012", "Amphitheater", 0),
        Room("L213", "Lab room", 0),
        Room("L306", "Classroom", 0),
        Room("L416", "Auditorium", 0),
    )

    val rooms: Flow<List<Room>> = flowOf(roomsList)

    ObooTheme {
        DashboardScreen(Activity(), 0, rooms, roomsList, preview = true)
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
        DashboardContent(rooms, emptyList(), 5.0, preview = true)
    }
}

@PreviewLightDark
@Composable
fun DoubleInfoCardPreview()
{
    ObooTheme {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly)
        {
            InfoCard("Available rooms", "14", modifier = Modifier.padding(horizontal = 16.dp))
            InfoCard("Occupied rooms", "19", modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@PreviewLightDark
@Composable
fun InfoCardPreview()
{
    ObooTheme {
        InfoCard("Room availability", "42%", modifier = Modifier.fillMaxWidth())
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
