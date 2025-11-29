package org.example.project

import androidx.compose.ui.awt.ComposePanel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.bush.translator.Language
import me.bush.translator.Translator
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.awt.Toolkit
import java.io.File
import java.io.FileOutputStream
import javax.swing.JFrame

class AppViewModel : ViewModel() {
    private val translator = Translator()

    private val _selectedFile = MutableStateFlow<File?>(null)
    val selectedFile: StateFlow<File?> = _selectedFile.asStateFlow()

    private val _translationStatus = MutableStateFlow<String?>(null)
    val translationStatus: StateFlow<String?> = _translationStatus.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _translationProgress = MutableStateFlow(0f)
    val translationProgress: StateFlow<Float> = _translationProgress.asStateFlow()

    private val _totalCells = MutableStateFlow(0)
    val totalCells: StateFlow<Int> = _totalCells.asStateFlow()

    private val _translatedCells = MutableStateFlow(0)
    val translatedCells: StateFlow<Int> = _translatedCells.asStateFlow()

    private val _outOpen = MutableStateFlow<File?>(null)
    val outOpen: StateFlow<File?> = _outOpen.asStateFlow()

    private val _removeEmpty = MutableStateFlow(false)
    val removeEmpty: StateFlow<Boolean> = _removeEmpty.asStateFlow()

    private val _removeDuplicates = MutableStateFlow(false)
    val removeDuplicates: StateFlow<Boolean> = _removeDuplicates.asStateFlow()

    private val _targetLanguage = MutableStateFlow("RUSSIAN")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    fun setTargetLanguage(language: String) {
        _targetLanguage.value = language
    }

    fun addFile(file: File) {
        _selectedFile.value = file
    }

    fun setRemoveEmpty(value: Boolean) {
        _removeEmpty.value = value
    }

    fun setRemoveDuplicates(value: Boolean) {
        _removeDuplicates.value = value
    }

    fun clearFile() {
        _selectedFile.value = null
        _translationStatus.value = null
        _isTranslating.value = false
        _translationProgress.value = 0f
        _totalCells.value = 0
        _translatedCells.value = 0
        _outOpen.value = null
    }

    fun clearSelectedFile() {
        _selectedFile.value = null
    }

    fun setTranslationStatus(message: String?) {
        _translationStatus.value = message
    }

    fun setTranslating(isTranslating: Boolean) {
        _isTranslating.value = isTranslating
    }

    fun startTranslation(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                resetTranslationProgress()
                setTranslating(true)

                val ext = file.extension.lowercase()
                when (ext) {
                    in EXCEL_EXTENSIONS -> processExcel(
                        file,
                        targetLanguage.value,
                        removeEmpty.value,
                        removeDuplicates.value
                    )
                    in WORD_EXTENSIONS -> translateWord(file, targetLanguage.value)
                    "pdf" -> translatePdf(file, targetLanguage.value)
                    else -> setTranslationStatus("Unsupported file format")
                }
            } catch (e: Exception) {
                setTranslationStatus("Error: ${e.message}")
            } finally {
                setTranslating(false)
            }
        }
    }

    private suspend fun processExcel(
        file: File,
        lang: String,
        removeEmpty: Boolean,
        removeDuplicates: Boolean,
    ) {
        withContext(Dispatchers.IO) {
            val workbook = XSSFWorkbook(file.inputStream()).apply {
                val sheet = getSheetAt(0)
                if (removeEmpty) removeEmptyRows(sheet)
                if (removeDuplicates) removeDuplicateRows(sheet)
            }
            translateExcel(workbook.getSheetAt(0), file, lang)
        }
        withContext(Dispatchers.Main) {
            clearSelectedFile()
        }
    }

    private fun removeEmptyRows(sheet: Sheet) {
        val rowsToRemove = mutableListOf<Int>()
        val lastRow = sheet.lastRowNum

        for (i in 0..lastRow) {
            val row = sheet.getRow(i)
            val isEmpty = when {
                row == null -> true
                row.physicalNumberOfCells == 0 -> true
                else -> {
                    val first = row.firstCellNum.takeIf { it >= 0 } ?: 0
                    val last = row.lastCellNum.takeIf { it >= 0 } ?: -1
                    (first..last).all { ci ->
                        row.getCell(ci)?.let {
                            it.cellType == CellType.BLANK || it.toString().trim().isEmpty()
                        } ?: true
                    }
                }
            }
            if (isEmpty) rowsToRemove.add(i)
        }

        rowsToRemove
            .sortedDescending()
            .forEach { idx ->
                if (idx < sheet.lastRowNum) {
                    sheet.shiftRows(idx + 1, sheet.lastRowNum, -1)
                } else {
                    sheet.getRow(idx)?.let(sheet::removeRow)
                }
            }
    }

    private fun removeDuplicateRows(sheet: Sheet) {
        val seen = mutableSetOf<String>()
        val rowsToRemove = mutableListOf<Int>()
        val lastRow = sheet.lastRowNum

        for (i in 0..lastRow) {
            val row = sheet.getRow(i) ?: continue
            val first = row.firstCellNum.takeIf { it >= 0 } ?: 0
            val last = row.lastCellNum.takeIf { it >= 0 } ?: -1

            val rowText = (first..last).joinToString("|") { ci ->
                row.getCell(ci)?.toString()?.trim() ?: ""
            }

            if (!seen.add(rowText)) {
                rowsToRemove.add(i)
            }
        }

        rowsToRemove
            .sortedDescending()
            .forEach { idx ->
                if (idx < sheet.lastRowNum) {
                    sheet.shiftRows(idx + 1, sheet.lastRowNum, -1)
                } else {
                    sheet.getRow(idx)?.let(sheet::removeRow)
                }
            }
    }

    private suspend fun translateExcel(sheet: Sheet, originalFile: File, lang: String) {
        val uniqueTexts = mutableSetOf<String>()
        for (rowNum in 0..sheet.lastRowNum) {
            val row = sheet.getRow(rowNum) ?: continue
            for (ci in 0 until row.lastCellNum) {
                val cell = row.getCell(ci) ?: continue
                if (cell.cellType == CellType.STRING && cell.stringCellValue.isNotBlank()) {
                    uniqueTexts += cell.stringCellValue
                }
            }
        }

        _totalCells.value = uniqueTexts.size
        val translations = mutableMapOf<String, String>()

        coroutineScope {
            uniqueTexts.chunked(10).forEach { chunk ->
                chunk.map { text ->
                    async {
                        val result = try {
                            translator.translateBlocking(text, Language(lang), Language.AUTO)
                        } catch (e: Exception) {
                            null
                        }
                        text to (result?.translatedText ?: "Translation Error")
                    }
                }.awaitAll().forEach { (original, translated) ->
                    translations[original] = translated
                    _translatedCells.value++
                    _translationProgress.value = _translatedCells.value.toFloat() / _totalCells.value
                }
            }
        }

        val translatedWorkbook = XSSFWorkbook()
        val translatedSheet = translatedWorkbook.createSheet("Translated")

        for (rowNum in 0..sheet.lastRowNum) {
            val row = sheet.getRow(rowNum) ?: continue
            val tRow = translatedSheet.createRow(rowNum)

            for (ci in 0 until row.lastCellNum) {
                val cell = row.getCell(ci) ?: continue
                val tCell = tRow.createCell(ci)

                if (cell.cellType == CellType.STRING && cell.stringCellValue.isNotBlank()) {
                    tCell.setCellValue(translations[cell.stringCellValue] ?: "Translation Error")
                } else {
                    when (cell.cellType) {
                        CellType.NUMERIC -> tCell.setCellValue(cell.numericCellValue)
                        CellType.BOOLEAN -> tCell.setCellValue(cell.booleanCellValue)
                        else -> tCell.setCellValue(cell.toString())
                    }
                }
            }
        }

        val outputFile = File(originalFile.parentFile, "${originalFile.nameWithoutExtension}_processed.xlsx")
        FileOutputStream(outputFile).use { fos -> translatedWorkbook.write(fos) }

        withContext(Dispatchers.Main) {
            setTranslationStatus("Excel переведен и сохранен как ${outputFile.name}")
            _outOpen.value = outputFile
            showCustomNotification("DocumentsTranslate", "Translation successful!", 1500)
            clearSelectedFile()
        }
    }

    private fun translateWord(file: File, lang: String) {
        val doc = XWPFDocument(file.inputStream())
        val paragraphs = doc.paragraphs
        _totalCells.value = paragraphs.size + doc.tables.sumOf { it.rows.size }
        _translatedCells.value = 0

        paragraphs.forEach { p ->
            val original = p.text
            if (original.isNotBlank()) {
                val tr = try {
                    translator.translateBlocking(original, Language(lang), Language.AUTO)
                } catch (e: Exception) { null }
                p.runs.forEach { it.setText(tr?.translatedText ?: original, 0) }
            }
            _translatedCells.value++
            _translationProgress.value = _translatedCells.value.toFloat() / _totalCells.value
        }

        doc.tables.forEach { table ->
            table.rows.forEach { row ->
                row.tableCells.forEach { cell ->
                    val original = cell.text
                    if (original.isNotBlank()) {
                        val tr = try {
                            translator.translateBlocking(original, Language(lang), Language.AUTO)
                        } catch (e: Exception) { null }
                        cell.removeParagraph(0)
                        cell.setText(tr?.translatedText ?: original)
                    }
                }
                _translatedCells.value++
                _translationProgress.value = _translatedCells.value.toFloat() / _totalCells.value
            }
        }

        val outFile = File(file.parentFile, "${file.nameWithoutExtension}_translated.docx")
        FileOutputStream(outFile).use { fos -> doc.write(fos) }
        doc.close()

        setTranslationStatus("Документ Word переведен и сохранен как ${outFile.name}")
        _outOpen.value = outFile
        viewModelScope.launch { showCustomNotification("DocumentsTranslate", "Translation successful!", 1500) }
        clearSelectedFile()
    }

    private fun translatePdf(file: File, lang: String) {
        val pdDoc = PDDocument.load(file)
        val stripper = PDFTextStripper()
        val originalText = stripper.getText(pdDoc)
        pdDoc.close()

        _totalCells.value = originalText.lines().count { it.isNotBlank() }
        _translatedCells.value = 0

        val translatedText = originalText.lines().joinToString("\n") { line ->
            if (line.isNotBlank()) {
                val translated = try {
                    translator.translateBlocking(line, Language(lang), Language.AUTO).translatedText
                } catch (e: Exception) { "Translation Error" }
                _translatedCells.value++
                _translationProgress.value = _translatedCells.value.toFloat() / _totalCells.value
                translated
            } else {
                line
            }
        }

        val newPdf = PDDocument()
        val page = PDPage()
        newPdf.addPage(page)

        val fontFile = javaClass.getResourceAsStream("/arialmt.ttf")
        val font = PDType0Font.load(newPdf, fontFile)

        PDPageContentStream(newPdf, page).use { stream ->
            stream.beginText()
            stream.setFont(font, 12f)
            stream.setLeading(14.5f)
            stream.newLineAtOffset(50f, 750f)
            translatedText.lines().forEach { line ->
                stream.showText(line)
                stream.newLine()
            }
            stream.endText()
        }

        val outPdf = File(file.parentFile, "${file.nameWithoutExtension}_translated.pdf")
        newPdf.save(outPdf)
        newPdf.close()

        setTranslationStatus("PDF переведен и сохранен как ${outPdf.name}")
        _outOpen.value = outPdf
        viewModelScope.launch { showCustomNotification("DocumentsTranslate", "Translation successful!", 1500) }
        clearSelectedFile()
    }

    private fun resetTranslationProgress() {
        _translationProgress.value = 0f
        _translatedCells.value = 0
        _totalCells.value = 0
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

        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val (width, height) = 300 to 100
        val startY = screenSize.height
        val endY = screenSize.height - height - 60
        val x = screenSize.width - width - 30

        window.setLocation(x, startY)
        window.isVisible = true

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

    fun openFile(file: File) {
        try {
            java.awt.Desktop.getDesktop().open(file)
        } catch (e: Exception) {
            println("Error opening file: ${e.message}")
        }
    }

    companion object {
        val SUPPORTED_LANGUAGES = listOf(
            "AFRIKAANS", "ALBANIAN", "AMHARIC", "ARABIC", "ARMENIAN", "AZERBAIJANI", "BASQUE",
            "BELARUSIAN", "BENGALI", "BOSNIAN", "BULGARIAN", "CATALAN", "CEBUANO", "CHICHEWA",
            "CHINESE_SIMPLIFIED", "CHINESE_TRADITIONAL", "CORSICAN", "CROATIAN", "CZECH", "DANISH",
            "DUTCH", "ENGLISH", "ESPERANTO", "ESTONIAN", "FILIPINO", "FINNISH", "FRENCH", "FRISIAN",
            "GALICIAN", "GEORGIAN", "GERMAN", "GREEK", "GUJARATI", "HATIAN_CREOLE", "HAUSA",
            "HAWAIIAN", "HEBREW_IW", "HEBREW_HE", "HINDI", "HMONG", "HUNGARIAN", "ICELANDIC", "IGBO",
            "INDONESIAN", "IRISH", "ITALIAN", "JAPANESE", "JAVANESE", "KANNADA", "KAZAKH", "KHMER",
            "KOREAN", "KURDISH_KURMANJI", "KYRGYZ", "LAO", "LATIN", "LATVIAN", "LITHUANIAN",
            "LUXEMBOURGISH", "MACEDONIAN", "MALAGASY", "MALAY", "MALAYALAM", "MALTESE", "MAORI",
            "MARATHI", "MONGOLIAN", "MYANMAR_BURMESE", "NEPALI", "NORWEGIAN", "ODIA", "PASHTO",
            "PERSIAN", "POLISH", "PORTUGUESE", "PUNJABI", "ROMANIAN", "RUSSIAN", "SAMOAN",
            "SCOTS_GAELIC", "SERBIAN", "SESOTHO", "SHONA", "SINDHI", "SINHALA", "SLOVAK", "SLOVENIAN",
            "SOMALI", "SPANISH", "SUDANESE", "SWAHILI", "SWEDISH", "TAJIK", "TAMIL", "TELUGU", "THAI",
            "TURKISH", "UKRAINIAN", "URDU", "UYGHUR", "UZBEK", "VIETNAMESE", "WELSH", "XHOSA",
            "YIDDISH", "YORUBA", "ZULU"
        )

        val EXCEL_EXTENSIONS = listOf("xls", "xlsx", "csv")
        val WORD_EXTENSIONS = listOf("doc", "docx", "odt")
        val PDF_EXTENSIONS = listOf("pdf")
        val SUPPORTED_EXTENSIONS = EXCEL_EXTENSIONS + WORD_EXTENSIONS + PDF_EXTENSIONS
    }
}
