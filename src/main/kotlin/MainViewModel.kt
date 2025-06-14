import androidx.annotation.Keep
import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import markdownify.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

@Keep
class MainViewModel(
    private val database: Database,
) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val githubRepoUrl = "https://api.github.com/repos/zahid4kh/markdownify/tags"

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

    fun checkForUpdates() {
        _uiState.value = _uiState.value.copy(
            isCheckingUpdates = true,
            updateMessage = null,
            isUpdateAvailable = false
        )

        scope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(githubRepoUrl)
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        scope.launch(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                isCheckingUpdates = false,
                                updateMessage = "Error fetching updates: HTTP ${response.code}",
                                isUpdateAvailable = false
                            )
                        }
                        return@use
                    }

                    val responseBody = response.body?.string() ?: return@use
                    val json = Json.parseToJsonElement(responseBody).jsonArray

                    if (json.isNotEmpty()) {
                        val latestTag = json[0].jsonObject["name"]?.jsonPrimitive?.content ?: return@use
                        val current = Semver(BuildConfig.VERSION_NAME) // Current version
                        val latest = Semver(latestTag.replace("^v".toRegex(), ""))
                        scope.launch(Dispatchers.Main) {
                            if (latest.isGreaterThan(current)) {
                                _uiState.value = _uiState.value.copy(
                                    isCheckingUpdates = false,
                                    updateMessage = "New version available: $latestTag. Download the latest version from our website.",
                                    isUpdateAvailable = true
                                )
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isCheckingUpdates = false,
                                    updateMessage = "You are using the latest version: ${BuildConfig.VERSION_NAME}",
                                    isUpdateAvailable = false
                                )
                            }
                        }
                    } else {
                        scope.launch(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                isCheckingUpdates = false,
                                updateMessage = "No version tags found in the repository.",
                                isUpdateAvailable = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                scope.launch(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdates = false,
                        updateMessage = "Error checking for updates: ${e.message}",
                        isUpdateAvailable = false
                    )
                }
            }
        }
    }

    @Keep
    data class UiState(
        val darkMode: Boolean = false,
        val openFiles: List<OpenFile> = emptyList(),
        val activeFileIndex: Int = 0,
        val showFileChooser: Boolean = false,
        val showFileSaver: Boolean = false,
        val showInfo: Boolean = false,
        val isCheckingUpdates: Boolean = false,
        val updateMessage: String? = null,
        val isUpdateAvailable: Boolean = false
    ) {
        val activeFile: OpenFile?
            get() = openFiles.getOrNull(activeFileIndex)
    }
}