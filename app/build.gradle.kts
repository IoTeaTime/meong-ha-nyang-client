plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.mhnfe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mhnfe"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val nav_version = "2.8.0"

    //화면 이동 의존성 추가
    implementation("androidx.navigation:navigation-compose:$nav_version")
    //QR 생성, 리더기 의존성 추가
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")
    //Add CameraX dependency
    val cameraxVersion = "1.4.0-alpha02"
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-video:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")

    //Add awsKVS dependency
    val kvsVersion = "2.77.0"
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo:${kvsVersion}")
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo-signaling:${kvsVersion}")
    implementation("com.amazonaws:aws-android-sdk-mobile-client:${kvsVersion}")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    implementation(kotlin("script-runtime"))

}