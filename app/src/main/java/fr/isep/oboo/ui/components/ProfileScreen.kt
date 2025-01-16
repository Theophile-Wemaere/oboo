package fr.isep.oboo.ui.components

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isep.oboo.LoginActivity
import fr.isep.oboo.OTPActivity
import fr.isep.oboo.ObooApp
import fr.isep.oboo.ObooDatabase
import fr.isep.oboo.R
import fr.isep.oboo.RetrofitInstance
import fr.isep.oboo.dto.OneTimePasswordDTO
import fr.isep.oboo.model.Building
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.Locale

//region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(sourceActivity: Activity, menuIndex: Int, email: String, onReturn: () -> Unit? = {})
{
    // pinnedScrollBehavior makes the top bar always visible but it changes color when scrolling
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        // nestedScrollConnection connects the scroll behavior of the top bar with the scroll behavior of the lazy column in the RoomList composable
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { TopAppBar(stringResource(R.string.screenTitle_Profile), scrollBehavior, onReturn = onReturn)},
        bottomBar = { BottomNavigationBar(sourceActivity, menuIndex) }
    )
    {
        // Passing the modifier to the BuildingList composable allows the LazyColumn to be shifted downwards
        // with a padding that is the size of the top app bar itself (so that there's no overlap)
        contentPadding -> ProfileContent(sourceActivity, email, modifier = Modifier.padding(contentPadding))
    }
}

@Composable
fun ProfileContent(sourceActivity: Activity, email: String, modifier: Modifier = Modifier)
{
    Column(modifier = modifier.fillMaxSize().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally)
    {
        val firstName = email.split("@")[0].split(".")[0]
        val lastName = email.split("@")[0].split(".")[1]
        val scope = rememberCoroutineScope()

        Text(
            text = "${firstName.first().uppercase()}${lastName.first().uppercase()}",
            fontSize = 96.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight(500),
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(32.dp)
        )
        Spacer(Modifier.size(16.dp))
        Text(text = "${firstName.capitalize()} ${lastName.uppercase()}", fontSize = MaterialTheme.typography.displaySmall.fontSize, fontWeight = FontWeight.Bold)
        Text("($email)")
        Spacer(Modifier.size(48.dp))
        ElevatedButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            onClick = {
                scope.launch {
                    ObooDatabase.getInstance(ObooApp.applicationContext()).apiKeyDAO().deleteAPIKeys()
                    Log.i("Profile Activity", "API key deleted by the user, logging out...")
                    sourceActivity.startActivity(Intent(ObooApp.applicationContext(), LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                }
            }
        )
        {
            Row(verticalAlignment = Alignment.CenterVertically)
            {
                Text(stringResource(R.string.button_LogOut), fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                Spacer(Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = stringResource(R.string.button_LogOut)
                )
            }
        }
    }
}

//endregion Composables
