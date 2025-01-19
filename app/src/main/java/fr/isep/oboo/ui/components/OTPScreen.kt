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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Password
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isep.oboo.DashboardActivity
import fr.isep.oboo.ObooApp
import fr.isep.oboo.ObooDatabase
import fr.isep.oboo.R
import fr.isep.oboo.RetrofitInstance
import fr.isep.oboo.dto.APIKeyDTO
import fr.isep.oboo.model.APIKey
import fr.isep.oboo.refreshDatabase
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

//region Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPScreen(sourceActivity: Activity, email: String)
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
        contentPadding -> OTPScreenContent(sourceActivity, email, modifier = Modifier.padding(contentPadding))
    }
}

@Composable
fun OTPScreenContent(sourceActivity: Activity, email: String, modifier: Modifier = Modifier)
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
            Text(text = stringResource(R.string.app_name), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.size(48.dp))

        var enabled by remember { mutableStateOf(false) }
        var isError by remember { mutableStateOf(false) }
        var otp by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()

        OutlinedTextField(
            value = otp,
            onValueChange = {
                otp = it
                enabled = (otp.length == 6)
                isError = false
            },
            label = { Text(stringResource(R.string.inputField_VerificationCode)) },
            maxLines = 1,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(imageVector = Icons.Outlined.Password, contentDescription = stringResource(R.string.inputField_VerificationCode)) },
        )
        Spacer(Modifier.size(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally)
        {
            Text(stringResource(R.string.text_PleaseEnterOTP), color = MaterialTheme.colorScheme.onPrimary)
            Text(text = email, textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.onPrimary)
        }
        Spacer(Modifier.size(16.dp))
        ElevatedButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            enabled = enabled,
            onClick = {
                scope.launch {
                    val APIKeyResponse: Response<APIKeyDTO> = try {
                        RetrofitInstance.api.authenticate(email, otp)
                    }
                    catch (e: IOException) {
                        throw e
                    }
                    catch (e: HttpException) {
                        throw e
                    }

                    if (APIKeyResponse.isSuccessful && APIKeyResponse.body() != null)
                    {
                        // If the OTP is valid, save the API key and proceed to the Dashboard Activity
                        if (APIKeyResponse.body()!!.status == "success")
                        {
                            Log.i("OTP Submit", "OTP Submit successful. Saving the received API key !")
                            // Save the received API key
                            val db = ObooDatabase.getInstance(ObooApp.applicationContext())
                            db.apiKeyDAO().deleteAPIKeys()
                            db.apiKeyDAO().setAPIKey(APIKey(email, APIKeyResponse.body()!!.key))
                            // Launch the Dashboard Activity
                            refreshDatabase()
                            val intent = Intent(sourceActivity, DashboardActivity::class.java)
                            intent.putExtra("menuIndex", 0)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            sourceActivity.startActivity(intent)
                        }
                        else
                        {
                            Log.e("OTP Submit", "OTP Submit was rejected by the API with status: ${APIKeyResponse.body()!!.status}")
                            otp = ""
                            isError = true
                            enabled = false
                        }
                    }
                }
            },
            )
        {
            Row(verticalAlignment = Alignment.CenterVertically)
            {
                Text(stringResource(R.string.button_LogIn), fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                Spacer(Modifier.size(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.button_LogIn)
                )
            }
        }
    }
}

//endregion Composables
