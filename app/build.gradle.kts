plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // hilt & ksp
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    // Firebase
    alias(libs.plugins.services)
    alias(libs.plugins.crashlytics)
}

android {
    namespace = "net.pop.projectpilot"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "net.pop.projectpilot"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // for coil online images
    implementation(libs.coil.compose)
    // viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // default icons
    implementation(libs.androidx.compose.material.icons.extended)
    // firebase remote config & Services & Crashlytics & Messaging & Auth
    implementation(libs.firebase.config.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    // supabase storage
    implementation(libs.storage.kt)
    // ktor client
    implementation(libs.ktor.client.android)
    // navigation
    implementation(libs.androidx.navigation.compose)
    // Security & Biometrics
    implementation(libs.androidx.biometric)
    // room db
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    // data store
    implementation(libs.androidx.datastore.preferences)
    // gson
    implementation(libs.gson)
    // material 3
    implementation(libs.androidx.material3)
}