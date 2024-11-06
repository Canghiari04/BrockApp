import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"

    kotlin("kapt") version "1.9.0"
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

        val bucketName = properties.getProperty("BUCKET_NAME") ?: ""
        val baseUrl = properties.getProperty("GEO_DB_BASE_URL") ?: ""
        val geoApiKey = properties.getProperty("GEO_DB_API_KEY") ?: ""
        val supabaseUrl = properties.getProperty("SUPABASE_BASE_URL") ?: ""
        val supabaseApiKey = properties.getProperty("SUPABASE_API_KEY") ?: ""
        val identityPoolId = properties.getProperty("IDENTITY_POOL_ID") ?: ""

        buildConfigField(type = "String", name = "BUCKET_NAME", value = bucketName)
        buildConfigField(type = "String", name = "GEO_DB_BASE_URL", value = baseUrl)
        buildConfigField(type = "String", name = "GEO_DB_API_KEY", value = geoApiKey)
        buildConfigField(type = "String", name = "SUPABASE_BASE_URL", value = supabaseUrl)
        buildConfigField(type = "String", name = "SUPABASE_API_KEY", value = supabaseApiKey)
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
    // Android libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.coordinatorlayout)

    // Material Design
    implementation(libs.material)
    implementation(libs.material.v180)

    // Google Play Services
    implementation(libs.play.services.location)
    implementation(libs.play.services.location.v1800)

    // Osmdroid
    implementation(libs.osmdroid.wms)
    implementation(libs.osmdroid.android)

    // Room
    implementation(libs.androidx.room.ktx)
    kapt("androidx.room:room-compiler:2.6.1")
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)

    // Supabase
    implementation(libs.postgrest.kt)
    implementation(libs.ktor.client.android)

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