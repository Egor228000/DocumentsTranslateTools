import org.gradle.kotlin.dsl.commonMain
import org.gradle.kotlin.dsl.commonTest
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.composeCompiler)

}

kotlin {

    jvm("desktop") {
        compilerOptions {
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }

    sourceSets {

        val desktopMain by getting

        commonMain.dependencies {
            implementation(libs.jbr)
            implementation("com.bybutter.compose:compose-jetbrains-expui-theme:2.0.0")
            implementation("org.apache.pdfbox:pdfbox:2.0.28")
            implementation("org.apache.poi:poi:4.1.2")
            implementation("org.apache.poi:poi-ooxml:4.1.2")
            // Для JAXB
            implementation("javax.xml.bind:jaxb-api:2.3.1")
            implementation("org.glassfish.jaxb:jaxb-runtime:2.3.3")

            // Для Ant (если нужно)
            implementation("org.apache.ant:ant:1.10.12")

            implementation("com.github.therealbush:translator:1.1.1")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation("org.jetbrains.compose.material3:material3-desktop:1.9.0-alpha02")
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(compose.desktop.currentOs)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        desktopMain.dependencies {

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
            packageVersion = "1.0.3"
            windows {
                packageVersion = "1.0.3"
                exePackageVersion = "1.0.3"
                iconFile.set(project.file("src/desktopMain/composeResources/drawable/icons.ico"))
            }
            linux {
                packageVersion = "1.0.3"
                debPackageVersion = "1.0.3"
            }
            includeAllModules = true


        }
        nativeDistributions {
            modules("jdk.unsupported")
        }
        buildTypes.release.proguard {
            optimize.set(false)
        }
    }

}
