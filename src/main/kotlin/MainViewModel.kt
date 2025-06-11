import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainViewModel(
    private val database: Database,
) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        scope.launch {
            val settings = database.getSettings()
            _uiState.value = _uiState.value.copy(
                darkMode = settings.darkMode
            )
        }
        createNewFile()
    }

    fun createNewFile() {
        val currentFiles = _uiState.value.openFiles
        val newFile = OpenFile(
            file = null,
            content = "",
            title = "Untitled${if (currentFiles.isEmpty()) "" else " ${currentFiles.size + 1}"}" ,
            originalContent = ""
        )
        _uiState.value = _uiState.value.copy(
            openFiles = currentFiles + newFile,
            activeFileIndex = currentFiles.size
        )
    }

    fun openFile(file: File) {
        scope.launch {
            try {
                val content = withContext(Dispatchers.IO) { file.readText() }
                val currentFiles = _uiState.value.openFiles
                val existingIndex = currentFiles.indexOfFirst { it.file?.absolutePath == file.absolutePath }

                if (existingIndex != -1) {
                    _uiState.value = _uiState.value.copy(activeFileIndex = existingIndex)
                } else {
                    val newFile = OpenFile(
                        file = file,
                        content = content,
                        title = file.nameWithoutExtension,
                        originalContent = content
                    )
                    _uiState.value = _uiState.value.copy(
                        openFiles = currentFiles + newFile,
                        activeFileIndex = currentFiles.size
                    )
                }
            } catch (e: Exception) {
                println("Error opening file: ${e.message}")
            }
        }
    }

    fun saveFile(file: File? = null) {
        scope.launch {
            val currentState = _uiState.value
            val activeFile = currentState.openFiles.getOrNull(currentState.activeFileIndex) ?: return@launch

            val targetFile = file ?: activeFile.file
            if (targetFile != null) {
                try {
                    withContext(Dispatchers.IO) {
                        targetFile.writeText(activeFile.content)
                    }

                    val updatedFile = activeFile.copy(
                        file = targetFile,
                        isModified = false,
                        title = targetFile.nameWithoutExtension,
                        originalContent = activeFile.content
                    )
                    updateFile(currentState.activeFileIndex, updatedFile)
                } catch (e: Exception) {
                    println("Error saving file: ${e.message}")
                }
            }
        }
    }

    fun updateFileContent(content: String) {
        val currentState = _uiState.value
        val activeFile = currentState.openFiles.getOrNull(currentState.activeFileIndex) ?: return

        val updatedFile = activeFile.copy(
            content = content,
            isModified = content != activeFile.originalContent
        )
        updateFile(currentState.activeFileIndex, updatedFile)
    }

    private fun updateFile(index: Int, file: OpenFile) {
        val currentFiles = _uiState.value.openFiles.toMutableList()
        currentFiles[index] = file
        _uiState.value = _uiState.value.copy(openFiles = currentFiles)
    }

    fun switchToFile(index: Int) {
        if (index < _uiState.value.openFiles.size) {
            _uiState.value = _uiState.value.copy(activeFileIndex = index)
        }
    }

    fun closeFile(index: Int) {
        val currentFiles = _uiState.value.openFiles.toMutableList()
        if (currentFiles.size <= 1) {
            currentFiles[0] = OpenFile(file = null, content = "", title = "Untitled", originalContent = "")
            _uiState.value = _uiState.value.copy(openFiles = currentFiles, activeFileIndex = 0)
        } else {
            currentFiles.removeAt(index)
            val newActiveIndex = when {
                index < _uiState.value.activeFileIndex -> _uiState.value.activeFileIndex - 1
                index == _uiState.value.activeFileIndex && index >= currentFiles.size -> currentFiles.size - 1
                else -> _uiState.value.activeFileIndex
            }
            _uiState.value = _uiState.value.copy(
                openFiles = currentFiles,
                activeFileIndex = newActiveIndex
            )
        }
    }

    fun toggleDarkMode() {
        val newDarkMode = !_uiState.value.darkMode
        _uiState.value = _uiState.value.copy(darkMode = newDarkMode)

        scope.launch {
            val settings = database.getSettings()
            database.saveSettings(settings.copy(darkMode = newDarkMode))
        }
    }

    fun showFileChooser() {
        _uiState.value = _uiState.value.copy(showFileChooser = true)
    }

    fun hideFileChooser() {
        _uiState.value = _uiState.value.copy(showFileChooser = false)
    }

    fun showFileSaver() {
        _uiState.value = _uiState.value.copy(showFileSaver = true)
    }

    fun hideFileSaver() {
        _uiState.value = _uiState.value.copy(showFileSaver = false)
    }

    fun showInfo() {
        _uiState.value = _uiState.value.copy(showInfo = true)
    }

    fun hideInfo() {
        _uiState.value = _uiState.value.copy(showInfo = false)
    }

    data class UiState(
        val darkMode: Boolean = false,
        val openFiles: List<OpenFile> = emptyList(),
        val activeFileIndex: Int = 0,
        val showFileChooser: Boolean = false,
        val showFileSaver: Boolean = false,
        val showInfo: Boolean = false
    ) {
        val activeFile: OpenFile?
            get() = openFiles.getOrNull(activeFileIndex)
    }
}