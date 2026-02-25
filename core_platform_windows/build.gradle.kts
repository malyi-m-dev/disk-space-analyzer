plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm("desktop")
    jvmToolchain(21)

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation("net.java.dev.jna:jna-platform:5.17.0")
            }
        }
    }
}
