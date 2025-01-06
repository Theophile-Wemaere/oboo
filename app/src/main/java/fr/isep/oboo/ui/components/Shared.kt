package fr.isep.oboo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(title: String, scrollBehavior: TopAppBarScrollBehavior)
{
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = {
                /* TODO: Return button for TopAppBar*/
            }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
            }
        },
        scrollBehavior = scrollBehavior
    )
}

data class BottomNavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun BottomNavigationBar()
{
    val navigationItems = listOf(
        BottomNavigationItem("Dashboard", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavigationItem("Buildings", Icons.Filled.Apartment, Icons.Outlined.Apartment),
        BottomNavigationItem("Floors", Icons.Filled.Stairs, Icons.Outlined.Stairs),
        BottomNavigationItem("Rooms", Icons.Filled.DoorFront, Icons.Outlined.DoorFront),
        BottomNavigationItem("Profile", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle),
    )
    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    NavigationBar {
        navigationItems.forEachIndexed { index, bottomNavigationItem ->
            NavigationBarItem(
                selected = (selectedItemIndex == index),
                onClick = {
                    selectedItemIndex = index
                    // TODO: Navigate to screen
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
    items: List<T>,
    content: @Composable (T) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
)
{
    val pullToRefreshState = rememberPullToRefreshState()
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
