package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import exceltranslate.composeapp.generated.resources.Res
import exceltranslate.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ExcelTranslate",
        icon = painterResource(Res.drawable.compose_multiplatform)
    ) {
        window.minimumSize = Dimension(900, 700)
        App()
    }
}