package com.example.pharmacy
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.*
import com.example.pharmacy.feature.auth.LoginScreen
import com.example.pharmacy.feature.auth.RegisterScreen
import com.example.pharmacy.feature.home.HomeScreen
import com.example.pharmacy.feature.profile.ProfileScreen
import com.example.pharmacy.feature.map.MapScreen
import com.example.pharmacy.core.data.FirebaseAuthRepository

class MainActivity: ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val auth = FirebaseAuthRepository()
        setContent {
            val nav = rememberNavController()
            val start = if (auth.currentUserId()!=null) "home" else "auth/login"
            NavHost(nav, startDestination = start) {
                composable("auth/login"){ LoginScreen(
                    onRegister = { nav.navigate("auth/register") },
                    onLoggedIn = { nav.navigate("home"){ popUpTo("auth/login"){inclusive=true} } })
                }
                composable("auth/register"){ RegisterScreen(onDone = { nav.popBackStack() }) }
                composable("home"){ HomeScreen(
                    onProfile = { nav.navigate("profile") },
                    onMap = { nav.navigate("map") },
                    onLogout = { auth.signOut(); nav.navigate("auth/login"){ popUpTo(0) } })
                }
                composable("profile"){ ProfileScreen(onBack = { nav.popBackStack() }) }
                composable("map"){ MapScreen(onBack = { nav.popBackStack() }) }
            }
        }
    }
}
