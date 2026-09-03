package com.veltrion.vyrox.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.veltrion.vyrox.R
import com.veltrion.vyrox.data.model.LoginRequest
import com.veltrion.vyrox.data.repository.AuthRepository
import com.veltrion.vyrox.ui.theme.VyroxNavy
import com.veltrion.vyrox.ui.theme.VyroxOrange
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGuestContinue: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isOtpMode by remember { mutableStateOf(false) }
    var identifier by remember { mutableStateOf("customer@vyrox.com") }
    var password by remember { mutableStateOf("Customer@123") }
    var mobileNumber by remember { mutableStateOf("9876543210") }
    var otpCode by remember { mutableStateOf("123456") }
    var otpSent by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var oauthNotice by remember { mutableStateOf<String?>(null) }
    var showConfigModal by remember { mutableStateOf(false) }

    fun performGoogleSignIn() {
        coroutineScope.launch {
            try {
                loading = true
                errorMessage = null
                oauthNotice = null
                
                // Use Android Credential Manager for Google Sign-In
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("dummy-client-id.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    val authRes = AuthRepository.googleOAuth(idToken)
                    if (authRes.isSuccess) {
                        onLoginSuccess()
                    } else {
                        errorMessage = authRes.exceptionOrNull()?.message ?: "Google token validation failed"
                    }
                }
            } catch (e: GetCredentialException) {
                // When client ID is not configured or user cancels
                oauthNotice = "Google Sign-In: CONFIGURATION REQUIRED (Set GOOGLE_CLIENT_ID in backend environment & SHA-1 in Google Cloud Console)."
                showConfigModal = true
            } catch (e: Exception) {
                oauthNotice = "Google Sign-In: CONFIGURATION REQUIRED (Set GOOGLE_CLIENT_ID in backend environment & SHA-1 in Google Cloud Console)."
                showConfigModal = true
            } finally {
                loading = false
            }
        }
    }

    fun performFacebookSignIn() {
        // Facebook Android OAuth Integration
        oauthNotice = "Facebook Login: CONFIGURATION REQUIRED (Set FACEBOOK_APP_ID & FACEBOOK_CLIENT_TOKEN in backend / Android configuration)."
        showConfigModal = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_vyrox_logo),
                contentDescription = "VYROX Logo",
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Welcome to VYROX",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = VyroxNavy
            )

            Text(
                text = "SHOP SMART. COMPARE BETTER. LIVE BETTER.",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = VyroxOrange,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // OAuth status notice
            oauthNotice?.let { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notice,
                            color = Color(0xFF92400E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showConfigModal = true }) {
                            Text("Guide", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        }
                    }
                }
            }

            errorMessage?.let { err ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                ) {
                    Text(
                        text = err,
                        color = Color(0xFFB91C1C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Google OAuth Button
            OutlinedButton(
                onClick = { performGoogleSignIn() },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Continue with Google", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Facebook Button
            OutlinedButton(
                onClick = { performFacebookSignIn() },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Continue with Facebook", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(text = " OR ", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isOtpMode) {
                // Email / Password Form
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("Email or Mobile") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            loading = true
                            errorMessage = null
                            val res = AuthRepository.login(LoginRequest(identifier, password))
                            loading = false
                            if (res.isSuccess) {
                                onLoginSuccess()
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "Login failed"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VyroxNavy)
                ) {
                    Text(
                        text = if (loading) "Signing In..." else "Login",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(onClick = { isOtpMode = true }) {
                    Text("Use Mobile OTP instead", color = Color(0xFF2B6CB0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                // Mobile OTP Form
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Mobile Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (otpSent) {
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("Enter OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                loading = true
                                val res = AuthRepository.verifyOtp(mobileNumber, otpCode)
                                loading = false
                                if (res.isSuccess) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Invalid OTP entered"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyroxNavy)
                    ) {
                        Text("Verify OTP & Login", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                loading = true
                                AuthRepository.sendOtp(mobileNumber)
                                otpSent = true
                                loading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VyroxOrange)
                    ) {
                        Text("Send OTP", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(onClick = { isOtpMode = false }) {
                    Text("Back to Email Login", color = Color(0xFF2B6CB0), fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mandated Guest Mode button
            TextButton(
                onClick = {
                    AuthRepository.setGuestMode()
                    onGuestContinue()
                }
            ) {
                Text(
                    text = "Continue as Guest →",
                    color = VyroxOrange,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }

        // Integration Details Dialog
        if (showConfigModal) {
            AlertDialog(
                onDismissRequest = { showConfigModal = false },
                title = {
                    Text("OAuth Credentials Guide", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        Text(
                            "VYROX native Android application credentials configuration:",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Package: com.veltrion.vyrox", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Debug SHA-1:\n36:C9:D3:61:54:EA:19:86:86:2A:D5:15:AB:EA:A4:C2:BF:E4:97:6F", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Release SHA-1:\nDB:08:25:AA:1C:61:FC:96:37:7D:01:01:85:88:29:55:7B:3E:B4:CC", fontSize = 10.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Facebook Debug Key Hash:\nNsnTYVTqGYaGKtUVq+qkwr/kl28=", fontSize = 10.sp, color = Color.Gray)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showConfigModal = false }) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
