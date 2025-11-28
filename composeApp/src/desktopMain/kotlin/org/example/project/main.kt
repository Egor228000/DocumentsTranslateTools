package org.example.project

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import exceltranslate.composeapp.generated.resources.Res
import exceltranslate.composeapp.generated.resources.icons
import io.github.kdroidfilter.platformtools.darkmodedetector.windows.setWindowsAdaptiveTitleBar
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
        App(addViewModel)
    }
}