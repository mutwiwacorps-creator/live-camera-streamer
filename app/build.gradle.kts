
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation("io.livekit:livekit-android:2.29.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
}
