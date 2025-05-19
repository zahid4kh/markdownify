@file:JvmName("markdownrenderer") // Must have no spaces!!!
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import theme.AppTheme
import java.awt.Dimension
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

fun main() = application {
    startKoin {
        modules(appModule)
    }

    val viewModel = getKoin().get<MainViewModel>()

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(800.dp, 600.dp)),
        title = "markdown renderer - Made with Compose for Desktop"
    ) {

        MenuBar {
            Menu("File") {
                Item("Exit", onClick = ::exitApplication)
                Item("Exit", onClick = ::exitApplication)
                Item("Exit", onClick = ::exitApplication)
                Item("Exit", onClick = ::exitApplication)
            }

            Menu("File") {
                Item("Exit", onClick = ::exitApplication)
                Item("Exit", onClick = ::exitApplication)
                Item("Exit", onClick = ::exitApplication)
                Item("Exit", onClick = ::exitApplication)
            }

            Menu("File") {
                Item("Exit", onClick = ::exitApplication)
                Item("Exit", onClick = ::exitApplication)
                Item("Exit", onClick = ::exitApplication)
                Item("Exit", onClick = ::exitApplication)
            }
        }
        window.minimumSize = Dimension(800, 600)

        AppTheme {
            App(
                viewModel = viewModel
            )
        }
    }
}