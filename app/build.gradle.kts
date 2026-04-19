plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.chowking.smartdtr"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.chowking.smartdtr"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // ViewModel and LiveData (MVVM architecture)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // RecyclerView (for attendance lists)
    implementation(libs.recyclerview)

    // QR Code scanner — ZXing (Java-friendly)
    implementation(libs.zxing.android.embedded)

    // Password hashing — BCrypt (never store plain text!)
    implementation(libs.jbcrypt)
}