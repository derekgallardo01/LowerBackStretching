// Pure-JVM Kotlin library — no Android dependency. Holds the
// platform-agnostic code that both :app and :wear consume so neither
// module's copy drifts from the other.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
