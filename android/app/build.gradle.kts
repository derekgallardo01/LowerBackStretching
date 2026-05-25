import com.android.build.api.dsl.ManagedVirtualDevice
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.lowerbackstretching"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lowerbackstretching"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    // Release signing — keystore details live in `keystore.properties` at
    // the repo root (gitignored). Gradle's root project is the `android/`
    // subdirectory (settings.gradle.kts lives there), so the props file is
    // one level up. When the file is absent (CI without secrets, fresh
    // clone) we skip the signing config; the release build won't be signed
    // and Play will reject it — exactly what we want as a guard. See
    // PLAY_STORE_SUBMISSION.md for the exact format.
    val keystorePropsFile = rootProject.file("../keystore.properties")
    signingConfigs {
        create("release") {
            if (keystorePropsFile.exists()) {
                val props = Properties().apply { load(FileInputStream(keystorePropsFile)) }
                storeFile = file(props["storeFile"] as String)
                storePassword = props["storePassword"] as String
                keyAlias = props["keyAlias"] as String
                keyPassword = props["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures { compose = true }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    // Gradle Managed Devices: declarative phone + tablet AVDs that Gradle
    // downloads, boots, runs tests on, and tears down.
    //
    //   ./gradlew :app:pixel6Api34DebugAndroidTest        — phone only
    //   ./gradlew :app:pixelTabletApi34DebugAndroidTest   — tablet only
    //   ./gradlew :app:phoneAndTabletGroupDebugAndroidTest — both, in parallel
    testOptions {
        managedDevices {
            devices {
                maybeCreate<ManagedVirtualDevice>("pixel6Api34").apply {
                    device = "Pixel 6"
                    apiLevel = 34
                    systemImageSource = "aosp-atd"
                }
                maybeCreate<ManagedVirtualDevice>("pixelTabletApi34").apply {
                    device = "Pixel Tablet"
                    apiLevel = 34
                    systemImageSource = "aosp-atd"
                }
            }
            groups {
                maybeCreate("phoneAndTablet").apply {
                    targetDevices.add(devices["pixel6Api34"])
                    targetDevices.add(devices["pixelTabletApi34"])
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.health.connect.client)
    implementation(libs.zxing.core)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.truth)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
