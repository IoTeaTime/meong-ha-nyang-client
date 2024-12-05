plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version libs.versions.kotlin
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
    alias(libs.plugins.compose.compiler)

}

android {
    namespace = "com.example.mhnfe"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mhnfe"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "AWS_ACCESS_KEY", properties["AWS_ACCESS_KEY"].toString())
        buildConfigField("String", "AWS_PRIVATE_KEY", properties["AWS_PRIVATE_KEY"].toString())
        buildConfigField("String", "AWS_REGION", properties["AWS_REGION"].toString())
        buildConfigField("String", "MQTT_END_POINT", properties["MQTT_END_POINT"].toString())
        buildConfigField("String", "AWS_KEYSTORE_PW", properties["AWS_KEYSTORE_PW"].toString())
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    //webRTC 공식 지원 종료
    // 로컬 WebRTC.aar 파일 사용
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.datastore.core.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val nav_version = "2.8.0"

    // Retrofit library for making HTTP requests to the server
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson converter library
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

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

    val awsVersion = "2.77.0"
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo:${awsVersion}@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo-signaling:${awsVersion}@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-kinesisvideo-webrtcstorage:${awsVersion}@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-mobile-client:${awsVersion}@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-auth-userpools:${awsVersion}@aar") { isTransitive = true }
    implementation("com.amazonaws:aws-android-sdk-auth-ui:${awsVersion}@aar") { isTransitive = true }

    implementation("org.awaitility:awaitility:4.2.0")
    implementation("org.json:json:20190722")
    implementation("com.google.guava:guava:28.1-android")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.commons:commons-lang3:3.9")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.1.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.1.1")
    implementation("androidx.media3:media3-ui:1.1.1")


    //Add OkHttp dependency
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    //Add Awaitility dependency
    implementation("org.awaitility:awaitility:4.2.0")

    implementation("com.amazonaws:aws-android-sdk-iot:2.77.0")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("com.amazonaws:aws-android-sdk-mobile-client:2.77.0")
    implementation("software.amazon.awssdk.iotdevicesdk:aws-iot-device-sdk:1.21.0")

    // FCM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging-ktx")

    val work_version = "2.9.1"
    // (Java only)
    implementation("androidx.work:work-runtime:$work_version")

    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // optional - RxJava2 support
    implementation("androidx.work:work-rxjava2:$work_version")

    // optional - GCMNetworkManager support
    implementation("androidx.work:work-gcm:$work_version")

    // optional - Test helpers
    androidTestImplementation("androidx.work:work-testing:$work_version")

    // optional - Multiprocess support
    implementation("androidx.work:work-multiprocess:$work_version")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Hilt dependencies
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")


    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    //hilt life
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
}

kapt {
    correctErrorTypes = true
}