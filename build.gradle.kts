import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

group = "com.example"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "ComposeScrollSelectionBugRepro"
            packageVersion = "1.0.0"
        }
    }
}
