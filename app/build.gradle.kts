plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.pharmacy"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pharmacy"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":feature-auth"))
    implementation(project(":feature-home"))
    implementation(project(":feature-profile"))
    implementation(project(":feature-map"))
    implementation(project(":core-domain"))
    implementation(project(":core-data"))
    implementation(project(":core-ui"))


    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Compose BOM + Material3
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.compose.foundation:foundation")   // isSystemInDarkTheme()
    implementation("androidx.compose.material3:material3")     // MaterialTheme, Typography, color schemes
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // SIP / Linphone
    implementation("org.linphone:linphone-sdk-android:5.3.1")
}

kotlin { jvmToolchain(17) }
