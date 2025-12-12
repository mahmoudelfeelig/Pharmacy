package com.example.pharmacy
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.example.pharmacy.core.data.FirebaseAuthRepository
import com.example.core_data.FirestoreUserRepository
import com.example.core_domain.UserProfile
import com.example.feature_auth.LoginScreen
import com.example.feature_auth.RegisterScreen
import com.example.feature_home.PatientHomeScreen
import com.example.feature_home.PharmacistHomeScreen
import com.example.feature_home.PatientCartScreen
import com.example.feature_home.PatientConsultationScreen
import com.example.feature_home.PatientMedicationsScreen
import com.example.feature_home.PharmacistConsultationScreen
import com.example.feature_home.PharmacistMedicationsScreen
import com.example.pharmacy.feature.map.MapScreen
import com.example.feature_profile.ProfileScreen
import com.example.core_ui.theme.PharmacyTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.example.pharmacy.sip.SipConfig
import com.example.pharmacy.sip.SipManager
import androidx.core.content.ContextCompat
import org.linphone.core.tools.service.CoreService
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.example.pharmacy.ui.CallScreen
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.collectLatest

private object Routes {
    const val Login = "auth_login"
    const val Register = "auth_register"
    const val PatientHome = "patient_home"
    const val PharmacistHome = "pharmacist_home"
    const val Profile = "profile"
    const val Map = "map"
    const val PatientMeds = "patient_meds"
    const val PatientCart = "patient_cart"
    const val PatientConsultation = "patient_consultation"
    const val PharmacistMeds = "pharmacist_meds"
    const val PharmacistConsultation = "pharmacist_consultation"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val auth = FirebaseAuthRepository()
        setContent {
            // Start Linphone core early; it registers once we have credentials
            LaunchedEffect(Unit) {
                SipManager.start(applicationContext)
                // Keep Linphone core alive; use startService to avoid foreground start timeout crashes
                kotlin.runCatching {
                    applicationContext.startService(
                        Intent(applicationContext, CoreService::class.java)
                    )
                }
            }
            val userRepo = remember { FirestoreUserRepository() }
            var currentUser by remember { mutableStateOf<UserProfile?>(null) }
            var loadingProfile by remember { mutableStateOf(true) }
            PharmacyTheme {
                val composeContext = LocalContext.current
                val nav = rememberNavController()
                val scope = rememberCoroutineScope()
                val callUiState by SipManager.callState.collectAsState()

                LaunchedEffect(Unit) {
                    auth.currentUserId()?.let { uid ->
                        val fallbackEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
                        userRepo.get(uid)
                            .onSuccess { currentUser = it ?: UserProfile(uid = uid, email = fallbackEmail) }
                            .onFailure { currentUser = UserProfile(uid = uid, email = fallbackEmail) }
                        currentUser?.let { registerSipForUser(it) }
                    }
                    loadingProfile = false
                }

                LaunchedEffect(currentUser, loadingProfile) {
                    if (!loadingProfile && currentUser != null &&
                        nav.currentDestination?.route in listOf(Routes.Login, Routes.Register)
                    ) {
                        nav.navigate(homeRouteFor(currentUser)) {
                            popUpTo(Routes.Login) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                if (loadingProfile) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Box(Modifier.fillMaxSize()) {
                        NavHost(nav, startDestination = Routes.Login) {
                            composable(Routes.Login) {
                                LoginScreen(
                                    onRegister = { nav.navigate(Routes.Register) },
                                    onLoggedIn = { uid ->
                                        scope.launch {
                                            val fallbackEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
                                            userRepo.get(uid)
                                                .onSuccess { profile ->
                                                    currentUser = profile ?: UserProfile(uid = uid, email = fallbackEmail)
                                                }
                                                .onFailure { currentUser = UserProfile(uid = uid, email = fallbackEmail) }
                                            currentUser?.let { registerSipForUser(it) }
                                            nav.navigate(homeRouteFor(currentUser)) {
                                                popUpTo(Routes.Login) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                )
                            }
                            composable(Routes.Register) {
                                RegisterScreen { profile ->
                                    currentUser = profile
                                    nav.navigate(homeRouteFor(profile)) {
                                        popUpTo(Routes.Login) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                            composable(Routes.PatientHome) {
                                currentUser?.let { user ->
                                    PatientHomeScreen(
                                        profile = user,
                                        onMedications = { nav.navigate(Routes.PatientMeds) },
                                        onCart = { nav.navigate(Routes.PatientCart) },
                                        onConsultation = { nav.navigate(Routes.PatientConsultation) },
                                        onMap = { nav.navigate(Routes.Map) },
                                        onProfile = { nav.navigate(Routes.Profile) },
                                        onLogout = {
                                            auth.signOut(); currentUser = null
                                            nav.navigate(Routes.Login) {
                                                popUpTo(0) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                            composable(Routes.PharmacistHome) {
                                currentUser?.let { user ->
                                    PharmacistHomeScreen(
                                        profile = user,
                                        onManageMeds = { nav.navigate(Routes.PharmacistMeds) },
                                        onConsultation = { nav.navigate(Routes.PharmacistConsultation) },
                                        onMap = { nav.navigate(Routes.Map) },
                                        onProfile = { nav.navigate(Routes.Profile) },
                                        onLogout = {
                                            auth.signOut(); currentUser = null
                                            nav.navigate(Routes.Login) {
                                                popUpTo(0) { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                            composable(Routes.PatientMeds) {
                                currentUser?.let { user ->
                                    PatientMedicationsScreen(
                                        userId = user.uid,
                                        onCart = { nav.navigate(Routes.PatientCart) },
                                        onBack = { nav.popBackStack() }
                                    )
                                }
                            }
                            composable(Routes.PatientCart) {
                                currentUser?.let { user ->
                                    PatientCartScreen(
                                        userId = user.uid,
                                        onBack = { nav.popBackStack() }
                                    )
                                }
                            }
                            composable(Routes.PatientConsultation) {
                                currentUser?.let { user ->
                                    PatientConsultationScreen(
                                        userProfile = user,
                                        onBack = { nav.popBackStack() },
                                        onCallPharmacist = { sipAddress ->
                                            val ok = SipManager.call(sipAddress)
                                            if (!ok) {
                                                Toast.makeText(
                                                    composeContext,
                                                    "SIP is not ready yet. Try again in a moment.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        },
                                        sipDomainOrHost = SipConfig.PBX_DOMAIN
                                    )
                                }
                            }
                            composable(Routes.PharmacistMeds) {
                                currentUser?.let { user ->
                                    PharmacistMedicationsScreen(
                                        onBack = { nav.popBackStack() }
                                    )
                                }
                            }
                            composable(Routes.PharmacistConsultation) {
                                currentUser?.let { user ->
                                    PharmacistConsultationScreen(
                                        userProfile = user,
                                        onAvailabilityChanged = { isOnline ->
                                            currentUser = user.copy(online = isOnline)
                                        },
                                        onBack = { nav.popBackStack() }
                                    )
                                }
                            }
                            composable(Routes.Profile) {
                                ProfileScreen(
                                    onBack = { nav.popBackStack() },
                                    providedProfile = currentUser
                                )
                            }
                            composable(Routes.Map) { MapScreen(onBack = { nav.popBackStack() }) }
                        }

                        if (callUiState.status != com.example.pharmacy.sip.CallStatus.Idle) {
                            CallScreen(
                                state = callUiState,
                                onHangUp = { SipManager.hangUpCurrentCall() },
                                onToggleMute = { SipManager.toggleMute() },
                                onToggleSpeaker = { SipManager.toggleSpeaker() }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun homeRouteFor(user: UserProfile?) =
    if (user?.role == "pharmacist") Routes.PharmacistHome else Routes.PatientHome

private fun registerSipForUser(user: UserProfile) {
    val creds = SipConfig.credentialsFor(user.role, user.email, user.uid)
    SipManager.register(creds.username, creds.domain, creds.password, SipConfig.DEFAULT_TRANSPORT)
}
