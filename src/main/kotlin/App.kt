import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import theme.AppTheme


@Composable
@Preview
fun App(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AppTheme(darkTheme = uiState.darkMode) {
        MarkdownEditor(viewModel)
    }
}

