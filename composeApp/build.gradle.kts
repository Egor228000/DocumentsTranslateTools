import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.regex.Pattern.compile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm("desktop")
    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation("org.apache.pdfbox:pdfbox:2.0.28")
            implementation("org.apache.poi:poi:4.1.2")
            implementation("org.apache.poi:poi-ooxml:4.1.2")
            implementation("org.apache.poi:poi-ooxml-schemas:1.4")
            implementation("io.github.vinceglb:filekit-dialogs-compose:0.10.0-beta04")
            implementation("com.github.therealbush:translator:1.1.1")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation("org.jetbrains.compose.material3:material3-desktop:1.9.0-alpha02")
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {

            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {

    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
        }
    }
}
