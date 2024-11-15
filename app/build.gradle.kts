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

}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.play.services.auth)

    // chart
    implementation(libs.mpandroidchart)
    
    implementation ("com.prolificinteractive:material-calendarview:1.4.3") {
        exclude(group = "com.android.support")
    }
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.play.services.location)
    implementation(libs.material)

    // firebase
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)

    // room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    kapt("androidx.room:room-compiler:2.6.1")

    // scope view model
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // maps
    implementation(libs.play.services.maps)

    // aws
    implementation(libs.aws.android.sdk.s3)
    implementation(libs.aws.android.sdk.core)
    implementation (libs.gson)

    // tab layout
    implementation(libs.material.v130)
}