import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownEditor() {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(""))
    }
    val tokens = remember(textFieldValue.text) { parseMarkdown(textFieldValue.text) }
    val scrollState = rememberScrollState()

    Row(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = textFieldValue,
            onValueChange = { newValue ->
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