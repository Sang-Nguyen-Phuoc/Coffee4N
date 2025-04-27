import java.io.FileInputStream
import java.util.Properties

// Function to read the API key from local.properties
fun getApiKeyFromLocalProperties(): String {
    val properties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use { fis ->
            properties.load(fis)
            return properties.getProperty("GEMINI_API_KEY") ?: ""
        }
    }
    return "" // Fallback for CI or other environments
}

plugins {
    id("com.android.application") version "8.8.1"
    id("org.jetbrains.kotlin.android") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.coffee4n"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.coffee4n"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${project.property("CLOUDINARY_CLOUD_NAME")}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${project.property("CLOUDINARY_API_KEY")}\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${project.property("CLOUDINARY_API_SECRET")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${getApiKeyFromLocalProperties()}\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = false
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"
    }
}

dependencies {
    implementation(libs.firebase.auth.ktx)

    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose.v190)
    implementation(platform(libs.compose.bom.v20240300))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.database)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.android.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.ai.client.generativeai:generativeai:0.2.0")
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.27.0")

    // For Google Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom.v20240300))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}