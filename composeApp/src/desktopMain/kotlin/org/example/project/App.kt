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
import org.example.project.AppViewModel.Companion.EXCEL_EXTENSIONS
import org.example.project.AppViewModel.Companion.PDF_EXTENSIONS
import org.example.project.AppViewModel.Companion.SUPPORTED_EXTENSIONS
import org.example.project.AppViewModel.Companion.SUPPORTED_LANGUAGES
import org.example.project.AppViewModel.Companion.WORD_EXTENSIONS
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(viewModel: AppViewModel) {
    val selectedFile by viewModel.selectedFile.collectAsStateWithLifecycle()
    val translationStatus by viewModel.translationStatus.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()
    val translationProgress by viewModel.translationProgress.collectAsStateWithLifecycle()
    val totalCells by viewModel.totalCells.collectAsStateWithLifecycle()
    val translatedCells by viewModel.translatedCells.collectAsStateWithLifecycle()
    val outOpen by viewModel.outOpen.collectAsStateWithLifecycle()
    val removeEmpty by viewModel.removeEmpty.collectAsStateWithLifecycle()
    val removeDuplicates by viewModel.removeDuplicates.collectAsStateWithLifecycle()
    val targetLanguage by viewModel.targetLanguage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var expandedLanguage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LanguageSelector(
            expanded = expandedLanguage,
            onExpandedChange = { expandedLanguage = it },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            targetLanguage = targetLanguage,
            onLanguageSelected = {
                viewModel.setTargetLanguage(it)
                expandedLanguage = false
                searchQuery = ""
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FileDropZone(
            onFileDropped = { file ->
                viewModel.addFile(file)
                viewModel.setTranslationStatus(null)
            },
            onClickClear = {
                viewModel.clearFile()
            },
            selectedFile = selectedFile,
            onTranslateClick = {
                selectedFile?.let { viewModel.startTranslation(it) }
            },
            isTranslating = isTranslating
        )

        if (!isTranslating && selectedFile?.extension?.lowercase() in EXCEL_EXTENSIONS) {
            ExcelOptions(
                removeEmpty = removeEmpty,
                onRemoveEmptyChange = viewModel::setRemoveEmpty,
                removeDuplicates = removeDuplicates,
                onRemoveDuplicatesChange = viewModel::setRemoveDuplicates
            )
        }

        if (isTranslating) {
            TranslationProgress(
                translationProgress = translationProgress,
                translatedCells = translatedCells,
                totalCells = totalCells
            )
        }

        translationStatus?.let { status ->
            TranslationStatus(
                status = status,
                outFile = outOpen,
                onOpenFile = { file -> viewModel.openFile(file) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    targetLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val filteredLangList = remember(SUPPORTED_LANGUAGES, searchQuery) {
        if (searchQuery.isEmpty()) {
            SUPPORTED_LANGUAGES
        } else {
            SUPPORTED_LANGUAGES.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            onExpandedChange(it)
            if (!it) onSearchQueryChange("")
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = if (expanded) searchQuery else targetLanguage,
            onValueChange = {
                onSearchQueryChange(it)
                if (!expanded) onExpandedChange(true)
            },
            label = { Text("Язык") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
            placeholder = { Text("Поиск языка") },
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.heightIn(max = 300.dp)
        ) {
            if (filteredLangList.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Язык не найден") },
                    onClick = {},
                    enabled = false
                )
            } else {
                filteredLangList.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onLanguageSelected(option) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExcelOptions(
    removeEmpty: Boolean,
    onRemoveEmptyChange: (Boolean) -> Unit,
    removeDuplicates: Boolean,
    onRemoveDuplicatesChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = removeEmpty,
                onCheckedChange = onRemoveEmptyChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text("Remove empty rows", style = MaterialTheme.typography.bodyMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = removeDuplicates,
                onCheckedChange = onRemoveDuplicatesChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text("Удаление повторяющихся строк", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TranslationProgress(
    translationProgress: Float,
    translatedCells: Int,
    totalCells: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Ход выполнения перевода: ${(translationProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = translationProgress,
            onValueChange = {},
            modifier = Modifier.width(350.dp),
            enabled = false,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.tertiary,
                activeTrackColor = MaterialTheme.colorScheme.tertiary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Переведено $translatedCells of $totalCells cells",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun TranslationStatus(
    status: String,
    outFile: File?,
    onOpenFile: (File) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = if (status.startsWith("Error")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        outFile?.let { file ->
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onOpenFile(file) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Открыть переведенный файл")
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FileDropZone(
    onFileDropped: (File) -> Unit,
    onClickClear: () -> Unit,
    selectedFile: File?,
    onTranslateClick: () -> Unit,
    isTranslating: Boolean,
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
                (event.awtTransferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<*>)
                    ?.filterIsInstance<File>()
                    ?.firstOrNull { it.extension.lowercase() in SUPPORTED_EXTENSIONS }
                    ?.let {
                        onFileDropped(it)
                        return true
                    }
                return false
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dropTarget
            ),
        border = BorderStroke(
            width = if (isHovering) 2.dp else 1.dp,
            color = if (isHovering) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovering) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selectedFile == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(Res.drawable.word_document_svgrepo_com),
                        contentDescription = "Upload icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Перетащите сюда файл",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                FileSelectedContent(
                    selectedFile = selectedFile,
                    onTranslateClick = onTranslateClick,
                    isTranslating = isTranslating,
                    onClickClear = onClickClear
                )
            }
        }
    }
}

@Composable
private fun FileSelectedContent(
    selectedFile: File,
    onTranslateClick: () -> Unit,
    isTranslating: Boolean,
    onClickClear: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = painterResource(getFileIcon(selectedFile.extension.lowercase())),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = selectedFile.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.width(300.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(
                onClick = onTranslateClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTranslating,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text(if (isTranslating) "Перевод..." else "Начать перевод")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onClickClear,
                enabled = !isTranslating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Очистить")
            }
        }
    }
}

private fun getFileIcon(extension: String): DrawableResource {
    return when (extension) {
        in EXCEL_EXTENSIONS -> Res.drawable.gsheet_document_svgrepo_com
        in WORD_EXTENSIONS -> Res.drawable.word_document_svgrepo_com
        in PDF_EXTENSIONS -> Res.drawable.pdf_document_svgrepo_com
        else -> Res.drawable.icons // Default icon
    }
}

@Composable
fun NotificationCard(title: String, message: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Image(
            painterResource(Res.drawable.icons),
            null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.padding(start = 16.dp))
        Column {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        }
    }
}
