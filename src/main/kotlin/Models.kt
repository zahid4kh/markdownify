import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class AppSettings(
    val darkMode: Boolean = false,
    val windowState: WindowState = WindowState()
)

@Serializable
data class WindowState(
    val width: Int = 800,
    val height: Int = 600,
)

data class OpenFile(
    val file: File?,
    val content: String,
    val isModified: Boolean = false,
    val title: String = "Untitled"
) {
    val displayTitle: String
        get() = if (isModified) "$title*" else title
}