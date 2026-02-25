plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm("desktop")
    jvmToolchain(21)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":data_filesystem:api"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
        val desktopMain by getting
    }
}
