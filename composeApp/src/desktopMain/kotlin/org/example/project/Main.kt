package org.example.project

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import exceltranslate.composeapp.generated.resources.Res
import exceltranslate.composeapp.generated.resources.icons
import org.jetbrains.compose.resources.painterResource


fun main() = application {

    Window(
        state = rememberWindowState(),
        onCloseRequest = ::exitApplication,
        title = "ExcelTranslate",
        icon = painterResource(Res.drawable.icons),

    ) {
        val addViewModel = remember { AppViewModel() }
        App(addViewModel)
    }
}
