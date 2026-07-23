import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.lowerbackstretching.wear"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lowerbackstretching"
        // Wear OS 3+ (Android 11 / API 30) is the practical floor.
        minSdk = 30
        targetSdk = 36
        // Standalone Wear app under the same Play listing as the phone app;
        // its versionCode must stay distinct from the phone's.
        versionCode = 2
        versionName = "0.1.1"
    }

    // Release signing — shares the phone app's upload key (same applicationId,
    // same Play App Signing key). Keystore details live in `keystore.properties`
    // at the repo root (gitignored, one level up from Gradle's `android/` root).
    // When absent (CI, fresh clone) signing is skipped and Play would reject the
    // build — the intended guard. See PLAY_STORE_SUBMISSION.md for the format.
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
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)

    implementation(libs.kotlinx.serialization.json)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
