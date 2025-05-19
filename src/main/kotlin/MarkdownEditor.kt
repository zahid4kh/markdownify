import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownEditor() {
    var inputText by remember { mutableStateOf("") }
    val tokens = remember(inputText) { parseMarkdown(inputText) }
    val scrollState = rememberScrollState()

    Row(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight(),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace)
        )

        Box(
            modifier = Modifier
                .weight(0.5f)
                .fillMaxHeight()
                .padding(start = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                RenderMarkdown(tokens)
            }
        }
    }
}