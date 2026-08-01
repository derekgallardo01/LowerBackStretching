import com.android.build.api.dsl.ManagedVirtualDevice
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.lowerbackstretching"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lowerbackstretching"
        minSdk = 26
        targetSdk = 36
        versionCode = 11
        versionName = "1.0.8"

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
            // R8 + resource shrinking. material-icons-extended in particular is a
            // very large artifact of which the app uses a handful of icons, so
            // shipping it whole was a meaningful chunk of the download.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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

    // buildConfig: AboutCard shows BuildConfig.VERSION_NAME so the version in
    // Settings can't drift from the version we actually ship.
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }

    lint {
        // CI runs :app:lintDebug. Fail the build on anything Lint considers an
        // error so regressions can't merge, and promote the checks that matter
        // most for this app: accessibility (TalkBack support is a real gap here)
        // and hardcoded UI text (there is no localization path yet).
        abortOnError = true
        warningsAsErrors = false
        checkDependencies = true
        sarifReport = true
        htmlReport = true
        // No baseline on purpose: the backlog was cleared rather than frozen, so
        // any new finding is genuinely new. Per-issue exemptions that need to
        // survive live in app/lint.xml with the reason written down.
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
    // collectAsStateWithLifecycle — stops Room/DataStore flows collecting while backgrounded.
    implementation(libs.androidx.lifecycle.runtime.compose)
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

// ktlint. The codebase was formatted rather than baselined, so there is no
// exemption file — `ktlintCheck` runs clean and CI fails on any new deviation.
// Compose-specific carve-outs (PascalCase composables, multiline-expression
// wrapping) live in android/.editorconfig with the reasoning.
ktlint {
    version.set("1.3.1")
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }
    filter {
        // Generated sources are not ours to format.
        exclude { it.file.path.contains("${File.separator}build${File.separator}") }
    }
}

// Coverage is reported for :app but not gated. Most of what is left here is
// Compose UI and Android framework glue, which JVM unit tests can't reach —
// a threshold would measure how much untestable code exists rather than how
// well the testable parts are covered. :core is where the bound lives.
kover {
    reports {
        filters {
            excludes {
                // Generated, and Compose UI that only the instrumented suite exercises.
                classes(
                    "*.BuildConfig",
                    "*ComposableSingletons*",
                    "*_Factory*",
                    "com.lowerbackstretching.ui.theme.*",
                )
            }
        }
    }
}
