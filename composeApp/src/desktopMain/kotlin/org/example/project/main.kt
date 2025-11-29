package org.example.project

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import exceltranslate.composeapp.generated.resources.Res
import exceltranslate.composeapp.generated.resources.icons
import io.github.kdroidfilter.platformtools.darkmodedetector.isSystemInDarkMode
import io.github.kdroidfilter.platformtools.darkmodedetector.windows.setWindowsAdaptiveTitleBar
import org.example.project.Theme.DarkColors
import org.example.project.Theme.LightColors
import org.example.project.Theme.MyAppTheme
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ExcelTranslate",
        icon = painterResource(Res.drawable.icons)
    ) {
        window.minimumSize = Dimension(500, 700)
        window.setWindowsAdaptiveTitleBar()
        val addViewModel = rememberSaveable { AppViewModel() }
        MyAppTheme(
            colorScheme = if (isSystemInDarkMode()) DarkColors else LightColors
        ) {
            App(addViewModel)
        }
    }
}