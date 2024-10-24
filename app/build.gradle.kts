import org.jetbrains.kotlin.fir.resolve.dfa.cfg.NormalPath.label
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt") version "1.8.0"
}

android {
    namespace = "com.example.brockapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.brockapp"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val fileProjectProperties = project.rootProject.file("project.properties")
        val properties = Properties()
        properties.load(fileProjectProperties.inputStream())

        val baseUrl = properties.getProperty("BASE_URL") ?: ""
        val mapApiKey = properties.getProperty("MAPS_API_KEY") ?: ""
        val bucketName = properties.getProperty("BUCKET_NAME") ?: ""
        val geoApiKey = properties.getProperty("GEO_DB_API_KEY") ?: ""
        val identityPoolId = properties.getProperty("IDENTITY_POOL_ID") ?: ""

        addManifestPlaceholders(
            mapOf(
                "MAPS_API_KEY" to mapApiKey
            )
        )

        buildConfigField(type = "String", name = "BASE_URL", value = baseUrl)
        buildConfigField(type = "String", name = "BUCKET_NAME", value = bucketName)
        buildConfigField(type = "String", name = "GEO_DB_API_KEY", value = geoApiKey)
        buildConfigField(type = "String", name = "IDENTITY_POOL_ID", value = identityPoolId)
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.androidx.constraintlayout)

    // Material Design
    implementation(libs.material)
    implementation(libs.material.v180)

    // Google Play Services
    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)
    implementation(libs.play.services.location.v1800)
    implementation(libs.play.services.maps)

    // Room (Persistence)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    kapt("androidx.room:room-compiler:2.6.1")

    // ViewModel
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Work Manager
    implementation(libs.androidx.work.runtime.ktx)

    // Charts
    implementation(libs.mpandroidchart)

    // AWS
    implementation(libs.aws.android.sdk.s3)
    implementation(libs.aws.android.sdk.core)

    // Image View
    implementation(libs.circleimageview)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Gson
    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}