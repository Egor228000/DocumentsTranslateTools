package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import exceltranslate.composeapp.generated.resources.Res
import exceltranslate.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.bush.translator.Language
import me.bush.translator.Translator
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var selectedFile by remember { mutableStateOf<File?>(null) }
        var translationStatus by remember { mutableStateOf<String?>(null) }
        var isTranslating by remember { mutableStateOf(false) }
        var translationProgress by remember { mutableStateOf(0f) }
        var totalCells by remember { mutableStateOf(0) }
        var translatedCells by remember { mutableStateOf(0) }
        val translator = remember { Translator() }
        val scope = rememberCoroutineScope()
        var expandedLanguage by remember { mutableStateOf(false) }
        var lang by remember { mutableStateOf("ENGLISH") }
        var searchQuery by remember { mutableStateOf("") }
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
                    searchQuery = ""   // теперь всегда очищаем при переключении
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
                        .menuAnchor()
                        .fillMaxWidth(),
                    placeholder = { Text("Выберите язык перевода") },
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

            FileDropZone(
                onFileDropped = { file ->
                    selectedFile = file
                    translationStatus = null
                    translationProgress = 0f
                },
                onClickClear = {
                    selectedFile = null
                    translationStatus = null
                    translationProgress = 0f
                },
                selectedFile = selectedFile,
                onTranslateClick = {
                    selectedFile?.let { file ->
                        scope.launch(Dispatchers.IO) {
                            try {
                                isTranslating = true
                                translationStatus = ""
                                translationProgress = 0f
                                translatedCells = 0

                                val workbook = XSSFWorkbook(file.inputStream())
                                val sheet = workbook.getSheetAt(0)

                                totalCells = 0
                                for (rowNum in 0..sheet.lastRowNum) {
                                    val row = sheet.getRow(rowNum) ?: continue
                                    for (cellNum in 0..row.lastCellNum) {
                                        val cell = row.getCell(cellNum) ?: continue
                                        if (cell.cellType == CellType.STRING && cell.stringCellValue.isNotBlank()) {
                                            totalCells++
                                        }
                                    }
                                }

                                val translatedWorkbook = XSSFWorkbook()
                                val translatedSheet = translatedWorkbook.createSheet("Перевод")

                                for (rowNum in 0..sheet.lastRowNum) {
                                    val row = sheet.getRow(rowNum) ?: continue
                                    val translatedRow = translatedSheet.createRow(rowNum)

                                    for (cellNum in 0..row.lastCellNum) {
                                        val cell = row.getCell(cellNum) ?: continue
                                        val translatedCell = translatedRow.createCell(cellNum)

                                        when (cell.cellType) {
                                            CellType.STRING -> {
                                                val text = cell.stringCellValue
                                                if (text.isNotBlank()) {
                                                    try {
                                                        val translation = translator.translateBlocking(
                                                            text,
                                                            Language(lang),
                                                            Language.AUTO
                                                        )
                                                        translatedCell.setCellValue(translation.translatedText)
                                                    } catch (e: Exception) {
                                                        translatedCell.setCellValue("Ошибка перевода: ${e.message}")
                                                    }
                                                    translatedCells++
                                                    translationProgress = translatedCells.toFloat() / totalCells
                                                }
                                            }

                                            else -> {
                                                when (cell.cellType) {
                                                    CellType.NUMERIC -> translatedCell.setCellValue(cell.numericCellValue)
                                                    CellType.BOOLEAN -> translatedCell.setCellValue(cell.booleanCellValue)
                                                    else -> translatedCell.setCellValue(cell.toString())
                                                }
                                            }
                                        }
                                    }
                                }

                                val outputFile =
                                    File(file.parentFile, "${file.nameWithoutExtension}_translated.xlsx")
                                FileOutputStream(outputFile).use { fos ->
                                    translatedWorkbook.write(fos)
                                }

                                translationStatus = "Перевод завершен! Сохранено в: ${outputFile.name}"
                            } catch (e: Exception) {
                                translationStatus = "Ошибка: ${e.message}"
                            } finally {
                                isTranslating = false
                            }
                        }
                    }
                },
                isTranslating = isTranslating
            )
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

            translationStatus?.let { status ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    color = if (status.startsWith("Ошибка")) Color.Red else Color.Unspecified
                )
            }
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
                    file.extension.lowercase() in listOf("xls", "xlsx", "cvs")
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
                    text = "Перетащите Excel файл сюда",
                    textAlign = TextAlign.Center
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        painterResource(Res.drawable.compose_multiplatform),
                        contentDescription = "Файл Excel",
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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
                        modifier = Modifier.width(200.dp),
                        enabled = !isTranslating
                    ) {
                        Text(if (isTranslating) "Перевод..." else "Начать перевод")
                    }
                    if (selectedFile != null) {
                        Button(
                            onClick = onClickClear,
                            enabled = !isTranslating
                        ) {
                            Text("Очитсить")
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.TopEnd
        ) {

        }

    }
}