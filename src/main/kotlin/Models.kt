import androidx.annotation.Keep
import kotlinx.serialization.Serializable
import java.io.File

@Keep
@Serializable
data class AppSettings(
    val darkMode: Boolean = false,
    val windowState: WindowState = WindowState()
)

@Keep
@Serializable
data class WindowState(
    val width: Int = 800,
    val height: Int = 600,
)

@Keep
data class OpenFile(
    val file: File?,
    val content: String,
    val isModified: Boolean = false,
    val title: String = "Untitled",
    val originalContent: String = ""
) {
    val displayTitle: String
        get() = if (isModified) "$title*" else title
}