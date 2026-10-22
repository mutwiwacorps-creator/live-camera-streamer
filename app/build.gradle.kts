plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.livecamerastreamer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.livecamerastreamer"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("io.livekit:livekit-android:2.29.0")
}
