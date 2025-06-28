package org.example.project

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import exceltranslate.composeapp.generated.resources.Res
import exceltranslate.composeapp.generated.resources.icons
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ExcelTranslate",
        icon = painterResource(Res.drawable.icons)
    ) {
        window.minimumSize = Dimension(900, 700)
        val addViewModel = rememberSaveable { AppViewModel() }

        App(addViewModel)
    }
}