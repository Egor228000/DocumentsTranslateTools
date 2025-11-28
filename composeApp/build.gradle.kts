import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeHotReload)

    alias(libs.plugins.composeCompiler)

}

kotlin {

    jvm("desktop")

    sourceSets {

        val desktopMain by getting

        commonMain.dependencies {

            implementation(libs.platformtools.core)
            implementation(libs.platformtools.darkmodedetector)
            implementation(libs.jbr)
            implementation("org.apache.pdfbox:pdfbox:2.0.28")
            implementation("org.apache.poi:poi:4.1.2")
            implementation("org.apache.poi:poi-ooxml:4.1.2")
            // Для JAXB
            implementation("javax.xml.bind:jaxb-api:2.3.1")
            implementation("org.glassfish.jaxb:jaxb-runtime:2.3.3")


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
tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    exclude(
        "META-INF/LICENSE.md",
        "META-INF/LICENSE.txt",
        "META-INF/LICENSE-LGPL-2.1.txt",
        "META-INF/LICENSE-LGPL-3.txt",
        "META-INF/NOTICE.txt",
        "META-INF/DEPENDENCIES",
        "META-INF/ASL2.0",
        "META-INF/NOTICE.md",
        "META-INF/LICENSE-W3C-TEST"
    )
}

compose.desktop {

    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            modules("java.sql")

            targetFormats(TargetFormat.Exe, TargetFormat.Msi,  TargetFormat.Deb)
            packageName = "DocumentTools"
            packageVersion = "1.0.5"
            windows {
                packageVersion = "1.0.5"
                exePackageVersion = "1.0.5"
                msiPackageVersion = "1.0.5"
                iconFile.set(project.file("src/desktopMain/composeResources/drawable/icons.ico"))
            }
            linux {
                packageVersion = "1.0.5"
                debPackageVersion = "1.0.5"
                iconFile.set(project.file("src/desktopMain/composeResources/drawable/icon.png"))

            }

        }

        buildTypes.release.proguard {
            isEnabled.set(false)
            optimize.set(true)
            obfuscate.set(false)
        }
    }

}
