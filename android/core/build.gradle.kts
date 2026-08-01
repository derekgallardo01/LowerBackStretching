// Pure-JVM Kotlin library — no Android dependency. Holds the
// platform-agnostic code that both :app and :wear consume so neither
// module's copy drifts from the other.
plugins {
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    jvmToolchain(17)
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
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

// Coverage. :core holds the pure logic that carries the real risk (streak
// math, gamification, session-completion decisions) and has no Android
// dependency, so it is the one module where a meaningful threshold is both
// achievable and worth enforcing.
kover {
    reports {
        verify {
            rule {
                minBound(80)
            }
        }
    }
}
