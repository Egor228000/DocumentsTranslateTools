package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import exceltranslate.composeapp.generated.resources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(addViewModel: AppViewModel) {
    MaterialTheme {
        val scope = rememberCoroutineScope()

        // Состояния из ViewModel
        val selectedFile by addViewModel.selectedFile.collectAsStateWithLifecycle()
        val translationStatus by addViewModel.translationStatus.collectAsStateWithLifecycle()
        val isTranslating by addViewModel.isTranslating.collectAsStateWithLifecycle()
        val translationProgress by addViewModel.translationProgress.collectAsStateWithLifecycle()
        val totalCells by addViewModel.totalCells.collectAsStateWithLifecycle()
        val translatedCells by addViewModel.translatedCells.collectAsStateWithLifecycle()
        val outOpen by addViewModel.outOpen.collectAsStateWithLifecycle()
        // Состояние языка и поиска
        var searchQuery by remember { mutableStateOf("") }
        var expandedLanguage by remember { mutableStateOf(false) }
        var lang by remember { mutableStateOf("RUSSIAN") }
        val langList = listOf(
            "AFRIKAANS",
            "ALBANIAN",
            "AMHARIC",
            "ARABIC",
            "ARMENIAN",
            "AZERBAIJANI",
            "BASQUE",
            "BELARUSIAN",
            "BENGALI",
            "BOSNIAN",
            "BULGARIAN",
            "CATALAN",
            "CEBUANO",
            "CHICHEWA",
            "CHINESE_SIMPLIFIED",
            "CHINESE_TRADITIONAL",
            "CORSICAN",
            "CROATIAN",
            "CZECH",
            "DANISH",
            "DUTCH",
            "ENGLISH",
            "ESPERANTO",
            "ESTONIAN",
            "FILIPINO",
            "FINNISH",
            "FRENCH",
            "FRISIAN",
            "GALICIAN",
            "GEORGIAN",
            "GERMAN",
            "GREEK",
            "GUJARATI",
            "HATIAN_CREOLE",
            "HAUSA",
            "HAWAIIAN",
            "HEBREW_IW",
            "HEBREW_HE",
            "HINDI",
            "HMONG",
            "HUNGARIAN",
            "ICELANDIC",
            "IGBO",
            "INDONESIAN",
            "IRISH",
            "ITALIAN",
            "JAPANESE",
            "JAVANESE",
            "KANNADA",
            "KAZAKH",
            "KHMER",
            "KOREAN",
            "KURDISH_KURMANJI",
            "KYRGYZ",
            "LAO",
            "LATIN",
            "LATVIAN",
            "LITHUANIAN",
            "LUXEMBOURGISH",
            "MACEDONIAN",
            "MALAGASY",
            "MALAY",
            "MALAYALAM",
            "MALTESE",
            "MAORI",
            "MARATHI",
            "MONGOLIAN",
            "MYANMAR_BURMESE",
            "NEPALI",
            "NORWEGIAN",
            "ODIA",
            "PASHTO",
            "PERSIAN",
            "POLISH",
            "PORTUGUESE",
            "PUNJABI",
            "ROMANIAN",
            "RUSSIAN",
            "SAMOAN",
            "SCOTS_GAELIC",
            "SERBIAN",
            "SESOTHO",
            "SHONA",
            "SINDHI",
            "SINHALA",
            "SLOVAK",
            "SLOVENIAN",
            "SOMALI",
            "SPANISH",
            "SUDANESE",
            "SWAHILI",
            "SWEDISH",
            "TAJIK",
            "TAMIL",
            "TELUGU",
            "THAI",
            "TURKISH",
            "UKRAINIAN",
            "URDU",
            "UYGHUR",
            "UZBEK",
            "VIETNAMESE",
            "WELSH",
            "XHOSA",
            "YIDDISH",
            "YORUBA",
            "ZULU"
        )

        // UI-компоненты
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val filteredLangList = remember(langList, searchQuery) {
                if (searchQuery.isEmpty()) {
                    langList
                } else {
                    langList.filter { it.contains(searchQuery, ignoreCase = true) }
                }
            }


            ExposedDropdownMenuBox(
                expanded = expandedLanguage,
                onExpandedChange = { newExpanded ->
                    expandedLanguage = newExpanded
                    searchQuery = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                OutlinedTextField(
                    value = if (expandedLanguage) searchQuery else lang,
                    onValueChange = { newValue ->
                        searchQuery = newValue
                        if (!expandedLanguage) expandedLanguage = true
                    },
                    label = { Text("Язык") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguage)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                        .fillMaxWidth(),
                    placeholder = { Text("Начните вводить язык...") },
                    singleLine = true
                )


                ExposedDropdownMenu(
                    expanded = expandedLanguage,
                    onDismissRequest = {
                        expandedLanguage = false
                        searchQuery = ""
                    },
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {

                    if (filteredLangList.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Язык не найден") },
                            onClick = { /*…*/ },
                            enabled = false
                        )
                    } else {

                        filteredLangList.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    lang = option
                                    searchQuery = ""
                                    expandedLanguage = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Загрузка файлов
            FileDropZone(
                onFileDropped = { file ->
                    addViewModel.addFile(file)
                    addViewModel.addMessage("")
                },
                onClickClear = {
                    addViewModel.clearFile()
                    addViewModel.addMessage("")
                },
                selectedFile = selectedFile,
                onTranslateClick = {
                    selectedFile?.let { file ->
                        scope.launch(Dispatchers.IO) {
                            try {
                                addViewModel.addBooolean(true)
                                addViewModel.addMessage("")

                                // Обновление состояния для начала перевода
                                addViewModel.startTranslation(file, lang)
                            } catch (e: Exception) {
                                addViewModel.addMessage("Ошибка: ${e.message}")
                            }
                        }
                    }
                },
                isTranslating = isTranslating,
                ext = selectedFile?.extension?.lowercase(Locale.getDefault())
            )

            // Прогресс перевода
            if (isTranslating) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Прогресс перевода: ${(translationProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = translationProgress,
                        onValueChange = {},
                        modifier = Modifier.width(350.dp),
                        enabled = false,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Переведено $translatedCells из $totalCells ячеек",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Статус перевода
            translationStatus?.let { status ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    color = if (status.startsWith("Ошибка")) Color.Red else Color.Unspecified
                )
                outOpen?.let { file ->
                    Button(
                        onClick = {
                            outOpen?.let { file ->
                                addViewModel.openFile(file)
                            } ?: run {
                                println("Файл не найден")
                            }
                        }
                    ) {
                        Text("Открыть файл .${file.extension}")
                    }
                }
            }
        }
    }
}


@Composable
fun NotificationCard(title: String, message: String) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
            .padding(16.dp)


    ) {
        Image(
            painterResource(Res.drawable.icons),
            null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.padding(start = 16.dp))

        Column(
        ) {
            Text(title, color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, color = Color.LightGray, fontSize = 14.sp)
        }
    }

}





@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FileDropZone(
    onFileDropped: (File) -> Unit,
    onClickClear: () -> Unit,
    selectedFile: File?,
    onTranslateClick: () -> Unit,
    isTranslating: Boolean = false,
    ext: String?,
) {

    var isHovering by remember { mutableStateOf(false) }

    val dropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isHovering = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isHovering = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isHovering = false
                val dropped = (event.awtTransferable
                    .getTransferData(DataFlavor.javaFileListFlavor) as? List<*>)
                    ?.filterIsInstance<File>()
                    ?: emptyList()

                val validFiles = dropped.filter { file ->
                    file.extension.lowercase() in listOf("xls", "xlsx", "csv", "doc", "odt", "docx", "pdf")
                }

                if (validFiles.isNotEmpty()) {
                    onFileDropped(validFiles.first())
                    return true
                }
                return false
            }
        }
    }

    Card(
        modifier = Modifier
            .size(400.dp)
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dropTarget
            ),
        border = BorderStroke(
            width = if (isHovering) 2.dp else 1.dp,
            color = if (isHovering) Color.Blue else Color.Gray
        )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(if (isHovering) Color(0xFFE0E0E0) else Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            if (selectedFile == null) {
                Text(
                    text = "Перетащите файл сюда",
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        painterResource(
                            when (ext) {
                                in listOf("xls", "xlsx", "csv") -> {
                                    Res.drawable.gsheet_document_svgrepo_com
                                }

                                in listOf("doc", "docx", "odt") -> {
                                    Res.drawable.word_document_svgrepo_com
                                }

                                "pdf" -> {
                                    Res.drawable.pdf_document_svgrepo_com
                                }


                                else -> {}
                            } as DrawableResource
                        ),
                        null,
                        modifier = Modifier
                            .size(100.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    Text(
                        text = selectedFile.name,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.width(300.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onTranslateClick,
                        modifier = Modifier.fillMaxWidth(1f),
                        enabled = !isTranslating
                    ) {
                        Text(if (isTranslating) "Перевод..." else "Начать перевод")
                    }
                    if (selectedFile != null) {
                        Button(
                            onClick = onClickClear,
                            enabled = !isTranslating,
                            modifier = Modifier.fillMaxWidth(1f),

                            ) {
                            Text("Очитсить")
                        }
                    }
                }
            }
        }


    }
}