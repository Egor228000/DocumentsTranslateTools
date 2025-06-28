package org.example.project

import androidx.compose.ui.awt.ComposePanel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import java.awt.Toolkit
import java.io.File
import java.io.FileOutputStream
import javax.swing.JFrame

class AppViewModel : ViewModel() {
    // Используем ваш текущий Translator (который вы упомянули в предыдущем коде)
    private val translator = Translator()

    private val _selectedFile = MutableStateFlow<File?>(null)
    val selectedFile: StateFlow<File?> = _selectedFile

    private val _translationStatus = MutableStateFlow<String?>(null)
    val translationStatus: StateFlow<String?> = _translationStatus

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating

    private val _translationProgress = MutableStateFlow(0f)
    val translationProgress: StateFlow<Float> = _translationProgress

    private val _totalCells = MutableStateFlow(0)
    val totalCells: StateFlow<Int> = _totalCells

    private val _translatedCells = MutableStateFlow(0)
    val translatedCells: StateFlow<Int> = _translatedCells

    fun addFile(file: File) {
        _selectedFile.value = file
    }
    // Переменная для хранения переведенного файла
    private val _outOpen = MutableStateFlow<File?>(null)
    val outOpen: StateFlow<File?> = _outOpen


    fun clearFile() {
        _selectedFile.value = null
        _translationStatus.value = null
        _isTranslating.value = false
        _translationProgress.value = 0f
        _totalCells.value = 0
        _translatedCells.value = 0
        _outOpen.value = null
    }

    fun addMessage(message: String) {
        _translationStatus.value = message
    }

    fun addBooolean(isTranslating: Boolean) {
        _isTranslating.value = isTranslating
    }

    fun startTranslation(file: File, lang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                resetTranslationProgress()
                _isTranslating.value = true

                val ext = file.extension.lowercase()
                when (ext) {
                    in listOf("xls", "xlsx", "csv") -> translateExcel(file, lang)
                    in listOf("doc", "docx", "odt") -> translateWord(file, lang)
                    "pdf" -> translatePdf(file, lang)
                    else -> addMessage("Unsupported file format")
                }
            } catch (e: Exception) {
                addMessage("Error: ${e.message}")
            } finally {
                _isTranslating.value = false
            }
        }
    }

    private fun translateExcel(file: File, lang: String) {
        // Excel
        val workbook = XSSFWorkbook(file.inputStream())
        val sheet = workbook.getSheetAt(0)

        for (rowNum in 0..sheet.lastRowNum) {
            val row = sheet.getRow(rowNum) ?: continue
            for (cellNum in 0 until row.lastCellNum) {
                val cell = row.getCell(cellNum) ?: continue
                if (cell.cellType == CellType.STRING && cell.stringCellValue.isNotBlank()) {
                    _totalCells.value++
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
                    _translatedCells.value++
                    _translationProgress.value = _translatedCells.value.toFloat() / _totalCells.value
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
        addMessage("Excel переведён и сохранён как ${outputFile.name}")
        _outOpen.value = outputFile
        viewModelScope.launch {
            showCustomNotification(
                "DocumentsTranslate",
                "Перевод успешно завершен!",
                1500
            )
        }

    }

    private fun translateWord(file: File, lang: String) {
        // Word (.docx)
        val doc = XWPFDocument(file.inputStream())

        doc.paragraphs.forEach { p ->
            _totalCells.value++

            val original = p.text
            if (original.isNotBlank()) {
                val tr = try {
                    _translatedCells.value++
                    translator.translateBlocking(
                        original,
                        Language(lang),
                        Language.AUTO
                    )
                } catch (e: Exception) {
                    null
                }
                p.runs.forEach { run -> run.setText(tr?.translatedText ?: original, 0) }
                _translationProgress.value =
                    _translatedCells.value.toFloat() / _totalCells.value  // Обновляем прогресс

                println("Перевод параграфа: ${(_translationProgress.value * 100).toInt()}%")
            }
        }

        doc.tables.forEach { table ->
            table.rows.forEach { row ->
                row.tableCells.forEach { cell ->
                    _totalCells.value++

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

        addMessage("Word документ переведён и сохранён как ${outFile.name}")
        _outOpen.value = outFile  // Сохранить путь к переведенному файлу
        viewModelScope.launch {
            showCustomNotification(
                "DocumentsTranslate",
                "Перевод успешно завершен!",
                1500
            )
        }
    }

    private fun translatePdf(file: File, lang: String) {
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

        addMessage("PDF переведён и сохранён как ${outPdf.name}")
        _outOpen.value = outPdf  // Сохранить путь к переведенному файлу
        viewModelScope.launch {
            showCustomNotification(
                "DocumentsTranslate",
                "Перевод успешно завершен!",
                1500
            )
        }
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
        val width = 300
        val height = 100
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
}
