@file:JvmName("markdownrenderer") // Must have no spaces!!!
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import theme.AppTheme
import java.awt.Dimension

fun main() {
    startKoin {
        modules(appModule)
    }

    application {
        val viewModel = getKoin().get<MainViewModel>()
        val uiState by viewModel.uiState.collectAsState()

    val windowState = rememberWindowState(
        size = DpSize(800.dp, 600.dp)
    )

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Markdownify - ${uiState.activeFile?.displayTitle ?: "No file"}"
        ) {
            window.minimumSize = Dimension(800, 600)

            AppTheme(darkTheme = uiState.darkMode) {
                App(viewModel = viewModel)
            }
        }
    }
}