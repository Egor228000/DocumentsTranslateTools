import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
            modules("java.sql")

            targetFormats(TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "DocumentTools"
            packageVersion = "1.0.0"
            windows {
                packageVersion = "1.0.0"
                exePackageVersion = "1.0.0"
            }
        }

        buildTypes.release.proguard {
            optimize.set(false)
        }
    }

}
