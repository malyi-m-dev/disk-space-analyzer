plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm("desktop")
    jvmToolchain(21)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":feature_disk_analyzer:api"))
                implementation(project(":core_base_feature"))
                implementation(project(":core_platform_windows"))
                implementation(project(":core_utils"))
                implementation(project(":data_filesystem:api"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")
                implementation("io.insert-koin:koin-core:4.1.1")
                implementation("io.insert-koin:koin-compose:4.1.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
    }
}
