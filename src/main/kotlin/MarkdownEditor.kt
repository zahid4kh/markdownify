import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import deskit.dialogs.InfoDialog
import deskit.dialogs.file.FileChooserDialog
import deskit.dialogs.file.FileSaverDialog

@Preview
@Composable
fun MarkdownEditor(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeFile = uiState.activeFile

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    val tokens = parseMarkdown(textFieldValue.text) // not using remember to avoid caching the links
    val scrollState = rememberScrollState()

    LaunchedEffect(activeFile) {
        if (activeFile != null) {
            textFieldValue = TextFieldValue(activeFile.content)
        }
    }


    Column{
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MenuBar(
                onNewFile = { viewModel.createNewFile() },
                onOpenFile = { viewModel.showFileChooser() },
                onSaveFile = {
                    if (activeFile?.file != null) {
                        viewModel.saveFile()
                    } else {
                        viewModel.showFileSaver()
                    }
                },
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onShowInfo = { viewModel.showInfo() }
            )

            Spacer(Modifier.width(16.dp))

            TabDropdown(
                openFiles = uiState.openFiles,
                activeFileIndex = uiState.activeFileIndex,
                onTabSelected = { viewModel.switchToFile(it) },
                onTabClosed = { viewModel.closeFile(it) }
            )
        }

        Row(modifier = Modifier.fillMaxSize()) {
            TextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    viewModel.updateFileContent(newValue.text)

                    if (newValue.text.length > textFieldValue.text.length) {
                        val addedChar = newValue.text.getOrNull(newValue.selection.start - 1)

                        if (addedChar == '`') {
                            val previousTwoChars = newValue.text.substring(
                                maxOf(0, newValue.selection.start - 3),
                                newValue.selection.start
                            )

                            if (previousTwoChars.endsWith("```")) {
                                // inserting closing backticks and language placeholder
                                val beforeCursor = newValue.text.substring(0, newValue.selection.start)
                                val afterCursor = newValue.text.substring(newValue.selection.start)
                                val newText = "$beforeCursor\n`$afterCursor"
                                val newPosition = beforeCursor.length // cursor stays in current line

                                textFieldValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(newPosition)
                                )
                            } else {
                                // inserting closing backtick
                                val beforeCursor = newValue.text.substring(0, newValue.selection.start)
                                val afterCursor = newValue.text.substring(newValue.selection.start)
                                val newText = "$beforeCursor`$afterCursor"

                                textFieldValue = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(newValue.selection.start)
                                )
                            }
                        } else {
                            textFieldValue = newValue
                        }
                    } else {
                        textFieldValue = newValue
                    }
                },
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.Enter -> {
                                    val text = textFieldValue.text
                                    val cursorPos = textFieldValue.selection.start

                                    val lineStart = text.lastIndexOf('\n', cursorPos - 1) + 1
                                    val currentLine = text.substring(lineStart, cursorPos)

                                    val trimmedLine = currentLine.trimStart()
                                    val leadingSpaces = currentLine.length - trimmedLine.length

                                    if (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")) {
                                        val bulletChar = if (trimmedLine.startsWith("- ")) "-" else "*"
                                        val beforeCursor = text.substring(0, cursorPos)
                                        val afterCursor = text.substring(cursorPos)

                                        val indentation = " ".repeat(leadingSpaces)
                                        val newText = "$beforeCursor\n$indentation$bulletChar $afterCursor"
                                        val newPosition = cursorPos + 1 + leadingSpaces + 2

                                        textFieldValue = TextFieldValue(
                                            text = newText,
                                            selection = TextRange(newPosition)
                                        )
                                        viewModel.updateFileContent(newText)
                                        true
                                    } else {
                                        false
                                    }
                                }
                                Key.Tab -> {
                                    val beforeCursor = textFieldValue.text.substring(0, textFieldValue.selection.start)
                                    val afterCursor = textFieldValue.text.substring(textFieldValue.selection.end)
                                    val newText = "$beforeCursor  $afterCursor"
                                    val newPosition = textFieldValue.selection.start + 2

                                    textFieldValue = TextFieldValue(
                                        text = newText,
                                        selection = TextRange(newPosition)
                                    )
                                    viewModel.updateFileContent(newText)
                                    true
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    },
                textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    unfocusedContainerColor = Color.LightGray,
                )
            )

//            Box(
//                modifier = Modifier
//                    .width(1.dp)
//                    .fillMaxHeight()
//                    .background(Color.LightGray)
//            )

            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                //.padding(start = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // making sure rendering gets recomposed to refresh the text
                    key(textFieldValue.text) {
                        RenderMarkdown(tokens)
                    }
                }
            }
        }

    }

    if (uiState.showFileChooser) {
        FileChooserDialog(
            title = "Open Markdown File",
            allowedExtensions = listOf("md", "markdown", "txt"),
            onFileSelected = { file ->
                viewModel.openFile(file)
                viewModel.hideFileChooser()
            },
            onCancel = { viewModel.hideFileChooser() }
        )
    }

    if (uiState.showFileSaver) {
        FileSaverDialog(
            title = "Save Markdown File",
            suggestedFileName = activeFile?.title ?: "untitled",
            extension = ".md",
            onSave = { file ->
                viewModel.saveFile(file)
                viewModel.hideFileSaver()
            },
            onCancel = { viewModel.hideFileSaver() }
        )
    }

    if (uiState.showInfo) {
        InfoDialog(
            title = "Markdown Renderer",
            content = {
                Column {
                    Text("Markdown Renderer v1.0.0")
                    Spacer(Modifier.height(8.dp))
                    Text("A powerful markdown editor built with Compose for Desktop")
                    Spacer(Modifier.height(8.dp))
                    Text("Features:")
                    Text("• Live markdown preview")
                    Text("• Multiple file tabs")
                    Text("• Dark/Light theme")
                    Text("• File operations")
                }
            },
            onClose = { viewModel.hideInfo() }
        )
    }
}