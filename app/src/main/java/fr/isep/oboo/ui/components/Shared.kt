package fr.isep.oboo.ui.components

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Stairs
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isep.oboo.BuildingsActivity
import fr.isep.oboo.DashboardActivity
import fr.isep.oboo.FloorsActivity
import fr.isep.oboo.R
import fr.isep.oboo.RoomsActivity
import kotlinx.coroutines.flow.Flow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(title: String, scrollBehavior: TopAppBarScrollBehavior, returnButton: Boolean = true, onReturn: () -> Unit? = {})
{
    if (returnButton)
    {
        CenterAlignedTopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = {
                    onReturn()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                }
            },
            scrollBehavior = scrollBehavior
        )
    }
    else
        CenterAlignedTopAppBar(title = { Text(title) }, scrollBehavior = scrollBehavior)
}

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val destinationActivity: Class<*>
)

@Composable
fun BottomNavigationBar(sourceActivity: Activity, selectedItemIndex: Int)
{
    val navigationItems = listOf(
        BottomNavigationItem(stringResource(R.string.navMenu_Dashboard), Icons.Filled.Home, Icons.Outlined.Home, DashboardActivity::class.java),
        BottomNavigationItem(stringResource(R.string.navMenu_Buildings), Icons.Filled.Apartment, Icons.Outlined.Apartment, BuildingsActivity::class.java),
        BottomNavigationItem(stringResource(R.string.navMenu_Floors), Icons.Filled.Stairs, Icons.Outlined.Stairs, FloorsActivity::class.java),
        BottomNavigationItem(stringResource(R.string.navMenu_Rooms), Icons.Filled.DoorFront, Icons.Outlined.DoorFront, RoomsActivity::class.java),
        BottomNavigationItem(stringResource(R.string.navMenu_Profile), Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle, DashboardActivity::class.java),
    )

    NavigationBar {
        navigationItems.forEachIndexed { index, bottomNavigationItem ->
            NavigationBarItem(
                selected = (selectedItemIndex == index),
                onClick = {
                    // selectedItemIndex = index
                    val intent = Intent(sourceActivity, bottomNavigationItem.destinationActivity)
                    intent.putExtra("menuIndex", index)
                    sourceActivity.startActivity(intent)
                },
                label = {
                    Text(bottomNavigationItem.title)
                },
                icon = {
                    Icon(
                        imageVector = if (index == selectedItemIndex) bottomNavigationItem.selectedIcon else bottomNavigationItem.unselectedIcon,
                        contentDescription = bottomNavigationItem.title
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> PullToRefreshLazyColumn(
    itemsFlow: Flow<List<T>>,
    content: @Composable (T) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
)
{
    val pullToRefreshState = rememberPullToRefreshState()
    val items by itemsFlow.collectAsState(initial = emptyList())

    Box(modifier = modifier.nestedScroll(pullToRefreshState.nestedScrollConnection))
    {
        LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize())
        {
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

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier)
{
    ElevatedCard(
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    )
    {
        Column(modifier = modifier)
        {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
            )
            Text(
                text = value,
                fontSize = 40.sp,
                fontWeight = FontWeight(500),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun FixedSizeInfoCard(title: String, value: String, width: Int = 150, height: Int = 100)
{
    ElevatedCard(
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.padding(horizontal = 20.dp).size(width = width.dp, height = height.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    )
    {
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize())
        {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight(500),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            )
            Text(
                text = value,
                fontSize = 40.sp,
                fontWeight = FontWeight(500),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
            )
        }
    }
}
