import androidx.annotation.Keep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

@Keep
class Database {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val appDir: File
    private val settingsFile: File

    init {
        val userHome = System.getProperty("user.home")
        appDir = File(userHome, ".markdownrenderer").apply {
            if (!exists()) mkdirs()
        }

        settingsFile = File(appDir, "settings.json")

        if (!settingsFile.exists()) {
            settingsFile.writeText(json.encodeToString(AppSettings.serializer(), AppSettings()))
        }
    }

    suspend fun getSettings(): AppSettings = withContext(Dispatchers.IO) {
        return@withContext try {
            json.decodeFromString(AppSettings.serializer(), settingsFile.readText())
        } catch (e: Exception) {
            AppSettings()
        }
    }

    suspend fun saveSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        settingsFile.writeText(json.encodeToString(AppSettings.serializer(), settings))
    }
}