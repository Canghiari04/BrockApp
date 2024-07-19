plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.brockapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.brockapp"
        minSdk = 28
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

    implementation ("com.prolificinteractive:material-calendarview:1.4.3") {
        exclude(group = "com.android.support")
    }
    implementation(libs.firebase.auth.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // https://mvnrepository.com/artifact/com.google.android.gms/play-services-location
    implementation(libs.play.services.location)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7") // Use the latest version
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
}
