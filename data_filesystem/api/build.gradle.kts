plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm("desktop")
    jvmToolchain(21)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
    }
}
