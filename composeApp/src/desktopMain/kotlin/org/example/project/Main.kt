package org.example.project

import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mayakapps.compose.windowstyler.WindowBackdrop
import com.mayakapps.compose.windowstyler.WindowCornerPreference
import com.mayakapps.compose.windowstyler.WindowFrameStyle
import com.mayakapps.compose.windowstyler.WindowStyle
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
        window.minimumSize = Dimension(500, 700)
        WindowStyle(
            isDarkTheme = false,
            backdropType = WindowBackdrop.Mica,
            frameStyle = WindowFrameStyle(cornerPreference = WindowCornerPreference.SMALL_ROUNDED),
        )
        val addViewModel = rememberSaveable { AppViewModel() }
        App(addViewModel)
    }
}