package fr.isep.oboo.ui.components

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isep.oboo.OTPActivity
import fr.isep.oboo.R
import fr.isep.oboo.RetrofitInstance
import fr.isep.oboo.dto.OneTimePasswordDTO
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

//region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(sourceActivity: Activity)
{
    // pinnedScrollBehavior makes the top bar always visible but it changes color when scrolling
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        // nestedScrollConnection connects the scroll behavior of the top bar with the scroll behavior of the lazy column in the RoomList composable
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    )
    {
        // Passing the modifier to the content composable allows it to be shifted downwards
        // with a padding that is the size of the top app bar itself (so that there's no overlap)
        contentPadding -> LoginScreenContent(sourceActivity, modifier = Modifier.padding(contentPadding))
    }
}

@Composable
fun LoginScreenContent(sourceActivity: Activity, modifier: Modifier = Modifier)
{
    Column(modifier = modifier.fillMaxSize().padding(top = 150.dp), horizontalAlignment = Alignment.CenterHorizontally)
    {
        Column(horizontalAlignment = Alignment.CenterHorizontally)
        {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Oboo logo",
                modifier = Modifier.size(250.dp)
            )
            Text(text = stringResource(R.string.app_name), fontSize = 48.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.size(48.dp))

        var enabled by remember { mutableStateOf(false) }
        var email by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                enabled = (email.endsWith("@isep.fr") || email.endsWith("@eleve.isep.fr") || email.endsWith("@ext.isep.fr"))
            },
            label = { Text(stringResource(R.string.inputField_ISEPEmail)) },
            maxLines = 1,
            leadingIcon = { Icon(imageVector = Icons.Outlined.Mail, contentDescription = stringResource(R.string.inputField_ISEPEmail)) }
        )
        Spacer(Modifier.size(16.dp))
        ElevatedButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            enabled = enabled,
            onClick = {
                scope.launch {
                    val OTPResponse: Response<OneTimePasswordDTO> = try {
                        RetrofitInstance.api.generateOTP(email = email)
                    }
                    catch (e: IOException) {
                        throw e
                    }
                    catch (e: HttpException) {
                        throw e
                    }

                    if (OTPResponse.isSuccessful && OTPResponse.body() != null)
                    {
                        // If the OTP has been sent, proceed to the OTP Activity
                        if (OTPResponse.body()!!.status == "success")
                        {
                            Log.i("OTP Request", "OTP Request sent successfully.")
                            val intent = Intent(sourceActivity, OTPActivity::class.java)
                            intent.putExtra("email", email)
                            sourceActivity.startActivity(intent)
                        }
                        else
                            Log.e("OTP Request", "OTP Request was rejected by the API with status: ${OTPResponse.body()!!.status}")
                    }
                }
            },
        )
        {
            Row(verticalAlignment = Alignment.CenterVertically)
            {
                Text(stringResource(R.string.button_Next), fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                Spacer(Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.button_Next)
                )
            }
        }
    }
}

//endregion Composables
