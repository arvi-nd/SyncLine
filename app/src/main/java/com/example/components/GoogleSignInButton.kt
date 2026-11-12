package com.example.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.ui.theme.GlobalChatTheme
import com.example.viewmodel.GlobalChatViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * "Continue with Google" button. Opens the Google account chooser through Credential Manager and
 * signs the returned ID token in to Firebase Auth.
 */
@Composable
fun GoogleSignInButton(
    viewModel: GlobalChatViewModel?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val colors = GlobalChatTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isBusy by remember { mutableStateOf(false) }

    Surface(
        onClick = {
            if (isBusy || viewModel == null) return@Surface
            isBusy = true
            scope.launch {
                try {
                    val option = GetSignInWithGoogleOption.Builder(context.getString(R.string.default_web_client_id)).build()
                    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                    val credential = CredentialManager.create(context).getCredential(context, request).credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                        viewModel.signInWithGoogle(
                            idToken = token,
                            onSuccess = { isBusy = false; onSuccess() },
                            onError = { isBusy = false; onError(it) }
                        )
                    } else {
                        isBusy = false
                        onError("Google sign-in returned an unsupported credential.")
                    }
                } catch (e: GetCredentialCancellationException) {
                    isBusy = false
                } catch (e: GetCredentialException) {
                    isBusy = false
                    onError(e.message ?: "Google sign-in failed.")
                } catch (e: Exception) {
                    isBusy = false
                    onError(e.localizedMessage ?: "Google sign-in failed.")
                }
            }
        },
        shape = RoundedCornerShape(9999.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, colors.cardBorder),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "G", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFFEA4335))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isBusy) "Signing in..." else "Continue with Google",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colors.textPrimary
            )
        }
    }
}
