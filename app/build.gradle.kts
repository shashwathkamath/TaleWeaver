import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

// Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "com.kamath.taleweaver"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kamath.taleweaver"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add your Google Maps API key here or in local.properties
        buildConfigField(
            "String",
            "MAPS_API_KEY",
            "\"${localProperties.getProperty("MAPS_API_KEY") ?: ""}\""
        )
    }

    signingConfigs {
        create("release") {
            storeFile = file("/Users/kamathshashwath/JKS/TaleWeaver.jks")
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD") ?: ""
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS") ?: ""
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }

    packaging {
        jniLibs {
            // Enable 16KB page size alignment for native libraries
            useLegacyPackaging = false
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.compose.material3)
    implementation(libs.play.services.location)
    implementation(libs.volley)
    implementation(libs.androidx.work.runtime.ktx)
    //implementation(libs.identity.jvm)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //firebase
    implementation(libs.firebase.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)

    //hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    //unit testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)

    //navigation support
    implementation(libs.androidx.navigationevent)

    //icons
    implementation(libs.androidx.compose.material.icons.extended)

    //okhttp
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    //logging
    implementation(libs.timber)

    //coil image loader
    implementation(libs.coil.compose)

    //geofire to be used with firestore to query location
    implementation(libs.geofirestore.android)

    // Google Places API for address autocomplete
    implementation(libs.places)

    //datastore for theme preferences
    implementation(libs.firebase.storage)
    //retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    //camera
    implementation(libs.barcode.scanning)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    //room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}

