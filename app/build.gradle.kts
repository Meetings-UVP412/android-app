plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.meetings"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.meetings"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.constraintlayout)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ViewModel & LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Retrofit + Gson
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // OkHttp logging
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.okhttp)

    // RecyclerView
    implementation(libs.androidx.recyclerview)

    implementation(libs.circleimageview)

    implementation(libs.androidx.media)

    implementation(libs.androidx.swiperefreshlayout)
}