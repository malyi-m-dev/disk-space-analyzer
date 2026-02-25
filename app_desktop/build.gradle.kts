plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    implementation(project(":feature_disk_analyzer:api"))
    implementation(project(":feature_disk_analyzer:impl"))
    implementation(project(":data_filesystem:api"))
    implementation(project(":data_filesystem:impl"))
    implementation(project(":core_platform_windows"))
    implementation(project(":core_utils"))
    implementation("io.insert-koin:koin-core:4.1.1")
    implementation("io.insert-koin:koin-compose:4.1.1")
    implementation(compose.material3)
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "st.service.app_desktop.MainKt"
    }
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    main {
        kotlin.srcDirs("src/desktopMain/kotlin")
        resources.srcDirs("src/desktopMain/resources")
    }
}
