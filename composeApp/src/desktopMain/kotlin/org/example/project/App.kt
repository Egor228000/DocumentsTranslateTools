package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import exceltranslate.composeapp.generated.resources.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.bush.translator.Language
import me.bush.translator.Translator
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.swing.JFrame

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
        var lang by remember { mutableStateOf("RUSSIAN") }
        var searchQuery by remember { mutableStateOf("") }
        var outOpen by remember { mutableStateOf<File?>(null) }
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
        val file = selectedFile
        val ext = file?.extension?.lowercase(Locale.getDefault())
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
                                totalCells = 0

                                // Определяем тип файла по расширению
                                val ext = file.extension.lowercase(Locale.getDefault())

                                when (ext) {
                                    in listOf("xls", "xlsx", "csv") -> {
                                        // Excel
                                        val workbook = XSSFWorkbook(file.inputStream())
                                        val sheet = workbook.getSheetAt(0)

                                        for (rowNum in 0..sheet.lastRowNum) {
                                            val row = sheet.getRow(rowNum) ?: continue
                                            for (cellNum in 0 until row.lastCellNum) {
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

                                            for (cellNum in 0 until row.lastCellNum) {
                                                val cell = row.getCell(cellNum) ?: continue
                                                val translatedCell = translatedRow.createCell(cellNum)

                                                if (cell.cellType == CellType.STRING && cell.stringCellValue.isNotBlank()) {
                                                    val text = cell.stringCellValue
                                                    val translation = try {
                                                        translator.translateBlocking(
                                                            text,
                                                            Language(lang),
                                                            Language.AUTO
                                                        )
                                                    } catch (e: Exception) {
                                                        null
                                                    }
                                                    translatedCell.setCellValue(
                                                        translation?.translatedText
                                                            ?: "Ошибка перевода"
                                                    )
                                                    translatedCells++
                                                    translationProgress = translatedCells.toFloat() / totalCells
                                                } else {
                                                    when (cell.cellType) {
                                                        CellType.NUMERIC -> translatedCell.setCellValue(cell.numericCellValue)
                                                        CellType.BOOLEAN -> translatedCell.setCellValue(cell.booleanCellValue)
                                                        else -> translatedCell.setCellValue(cell.toString())
                                                    }
                                                }
                                            }
                                        }

                                        val outputFile =
                                            File(file.parentFile, "${file.nameWithoutExtension}_translated.xlsx")
                                        FileOutputStream(outputFile).use { fos ->
                                            translatedWorkbook.write(fos)
                                        }
                                        outOpen = outputFile

                                        translationStatus = "Excel переведён и сохранён как ${outputFile.name}"
                                    }

                                    in listOf("doc", "docx", "odt") -> {
                                        // Word (.docx)
                                        val doc = XWPFDocument(file.inputStream())

                                        doc.paragraphs.forEach { p ->
                                            totalCells++

                                            val original = p.text
                                            if (original.isNotBlank()) {
                                                val tr = try {
                                                    translatedCells++
                                                    translator.translateBlocking(
                                                        original,
                                                        Language(lang),
                                                        Language.AUTO
                                                    )
                                                } catch (e: Exception) {
                                                    null
                                                }
                                                p.runs.forEach { run -> run.setText(tr?.translatedText ?: original, 0) }
                                                translationProgress =
                                                    translatedCells.toFloat() / totalCells  // Обновляем прогресс

                                                println("Перевод параграфа: ${(translationProgress * 100).toInt()}%")
                                            }
                                        }

                                        doc.tables.forEach { table ->
                                            table.rows.forEach { row ->
                                                row.tableCells.forEach { cell ->
                                                    totalCells++

                                                    val original = cell.text
                                                    if (original.isNotBlank()) {
                                                        val tr = try {
                                                            translator.translateBlocking(
                                                                original,
                                                                Language(lang),
                                                                Language.AUTO
                                                            )
                                                        } catch (e: Exception) {
                                                            null
                                                        }
                                                        cell.removeParagraph(0)
                                                        cell.setText(tr?.translatedText ?: original)
                                                    }
                                                }
                                            }
                                        }

                                        val outFile =
                                            File(file.parentFile, "${file.nameWithoutExtension}_translated.docx")
                                        FileOutputStream(outFile).use { fos ->
                                            doc.write(fos)
                                        }
                                        doc.close()
                                        outOpen = outFile

                                        translationStatus = "Word документ переведён и сохранён как ${outFile.name}"
                                        println("Перевод завершён. Прогресс: 100%")
                                    }

                                    "pdf" -> {
                                        val pdDoc = PDDocument.load(file)
                                        val stripper = PDFTextStripper()
                                        val originalText = stripper.getText(pdDoc)
                                        pdDoc.close()

                                        val translatedText = try {
                                            translator.translateBlocking(
                                                originalText,
                                                Language(lang),
                                                Language.AUTO
                                            ).translatedText
                                        } catch (e: Exception) {
                                            "Ошибка перевода: ${e.message}"
                                        }

                                        val newPdf = PDDocument()
                                        val page = PDPage()
                                        newPdf.addPage(page)

                                        val fontFile = javaClass.getResourceAsStream("/arialmt.ttf")
                                        val font = PDType0Font.load(newPdf, fontFile)

                                        PDPageContentStream(newPdf, page).use { stream ->
                                            stream.beginText()
                                            stream.setFont(font, 12f)
                                            stream.newLineAtOffset(50f, 750f)
                                            translatedText.lines().forEach { line ->
                                                stream.showText(line)
                                                stream.newLineAtOffset(0f, -15f)
                                            }
                                            stream.endText()
                                        }

                                        val outPdf =
                                            File(file.parentFile, "${file.nameWithoutExtension}_translated.pdf")
                                        newPdf.save(outPdf)
                                        newPdf.close()

                                        outOpen = outPdf
                                        translationStatus = "PDF переведён и сохранён как ${outPdf.name}"
                                    }
                                }
                            } catch (e: Exception) {
                                translationStatus = "Ошибка: ${e.message}"
                                print(e.message)
                            } finally {
                                isTranslating = false
                                translationProgress = 1f
                            }
                        }
                    }
                },
                isTranslating = isTranslating,
                ext = ext

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
                if (!isTranslating) {
                    Button(
                        onClick = {
                            outOpen?.let { file ->
                                openFile(file)
                            } ?: run {

                                println("Файл не найден")
                            }
                        }
                    ) {
                        Text("Открыть файл")
                    }
                } else {

                }
            }
            var visible by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    visible = true
                }
            ) {
                Text("asd")
            }

            if (visible) {
                showCustomNotification(
                    "DocumentsTranslate",
                    "Перевод успешно завершен!",
                    1500
                )
                visible = false

            }

        }
    }
}

fun showCustomNotification(title: String, message: String, durationMillis: Long) {
    val window = JFrame()

    window.isUndecorated = true
    window.isAlwaysOnTop = true
    window.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE

    val panel = ComposePanel().apply {
        preferredSize = java.awt.Dimension(300, 100)
        setContent {
            NotificationCard(title, message)
        }
    }

    window.contentPane = panel
    window.pack()

    // Position bottom right
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val width = 300
    val height = 100
    val startY = screenSize.height
    val endY = screenSize.height - height - 60
    val x = screenSize.width - width - 30

    // Initial off-screen position
    window.setLocation(x, startY)
    window.isVisible = true

    // Slide up animation (optional)
    CoroutineScope(Dispatchers.IO).launch {
        for (y in startY downTo endY step 5) {
            delay(5)
            window.setLocation(x, y)
        }
        delay(durationMillis)
        for (y in endY..startY step 5) {
            delay(5)
            window.setLocation(x, y)
        }
        window.isVisible = false
        window.dispose()
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


fun openFile(file: File) {
    try {
        java.awt.Desktop.getDesktop().open(file)
    } catch (e: Exception) {
        println(" ${e.message}")
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