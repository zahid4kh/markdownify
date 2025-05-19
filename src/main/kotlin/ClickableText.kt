import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import java.awt.Desktop
import java.net.URI

@Composable
fun ClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    BasicText(
        text = text,
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                layoutResult.value?.let { textLayoutResult ->
                    val position = textLayoutResult.getOffsetForPosition(offset)
                    text.getStringAnnotations("URL", position, position).firstOrNull()?.let { annotation ->
                        try {
                            Desktop.getDesktop().browse(URI(annotation.item))
                        } catch (e: Exception) {
                            println("Error opening URL: ${e.message}")
                        }
                    }
                }
            }
        },
        style = style,
        onTextLayout = { layoutResult.value = it }
    )
}