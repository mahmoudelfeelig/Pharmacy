package com.example.pharmacy
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.example.pharmacy.core.data.FirebaseAuthRepository
import com.example.pharmacy.feature.auth.LoginScreen
import com.example.pharmacy.feature.auth.RegisterScreen
import com.example.pharmacy.feature.home.HomeScreen
import com.example.pharmacy.feature.map.MapScreen
import com.example.pharmacy.feature.profile.ProfileScreen
import com.example.pharmacy.ui.theme.PharmacyTheme

private object Routes {
    const val Login = "auth_login"
    const val Register = "auth_register"
    const val Home = "home"
    const val Profile = "profile"
    const val Map = "map"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val auth = FirebaseAuthRepository()
        setContent {
            PharmacyTheme {
                val nav = rememberNavController()
                val start = if (auth.currentUserId() != null) Routes.Home else Routes.Login
                NavHost(nav, startDestination = start) {
                    composable(Routes.Login) {
                        LoginScreen(
                            onRegister = { nav.navigate(Routes.Register) },
                            onLoggedIn = {
                                nav.navigate(Routes.Home) {
                                    popUpTo(Routes.Login) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(Routes.Register) { RegisterScreen(onDone = { nav.popBackStack() }) }
                    composable(Routes.Home) {
                        HomeScreen(
                            onProfile = { nav.navigate(Routes.Profile) },
                            onMap = { nav.navigate(Routes.Map) },
                            onLogout = {
                                auth.signOut()
                                nav.navigate(Routes.Login) {
                                    popUpTo(Routes.Home) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(Routes.Profile) { ProfileScreen(onBack = { nav.popBackStack() }) }
                    composable(Routes.Map) { MapScreen(onBack = { nav.popBackStack() }) }
                }
            }
        }
    }
}
