import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RenderMarkdown(tokens: List<MarkdownToken>) {
    Column(modifier = Modifier.padding(16.dp)) {
        for (token in tokens) {
            when (token) {
                is MarkdownToken.Heading -> Text(
                    token.text,
                    fontSize = when (token.level) {
                        1 -> 24.sp
                        2 -> 20.sp
                        else -> 18.sp
                    },
                    fontWeight = FontWeight.Bold
                )
                is MarkdownToken.Paragraph -> Text(buildAnnotatedString {
                    appendInlineTokens(token.inlines)
                })

                is MarkdownToken.Bullet -> Text(buildAnnotatedString {
                    append("• ")
                    appendInlineTokens(token.inlines)
                })

                is MarkdownToken.CodeBlock -> Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFEFEFEF))
                        .padding(8.dp)
                ) {
                    Text(
                        token.text,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

fun AnnotatedString.Builder.appendInlineTokens(tokens: List<InlineToken>) {
    for (token in tokens) {
        when (token) {
            is InlineToken.Text -> append(token.text)
            is InlineToken.Bold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(token.text)
            }
            is InlineToken.Italic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                append(token.text)
            }
            is InlineToken.Code -> withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    background = Color(0xFFEFEFEF)
                )
            ) {
                append(token.text)
            }
        }
    }
}



@Composable
fun MarkdownEditor() {
    var inputText by remember { mutableStateOf("") }
    val tokens = remember(inputText) { parseMarkdown(inputText) }

    Row(modifier = Modifier.fillMaxSize()) {
        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier.weight(0.5f).fillMaxHeight()
        )
        Box(modifier = Modifier.weight(0.5f).fillMaxHeight().padding(16.dp)) {
            RenderMarkdown(tokens)
        }
    }
}
