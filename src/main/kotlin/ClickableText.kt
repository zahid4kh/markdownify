import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.TooltipBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.window.PopupPositionProvider
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
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    layoutResult.value?.let { textLayoutResult ->
                        val position = textLayoutResult.getOffsetForPosition(offset)
                        text.getStringAnnotations("URL", position, position).firstOrNull()?.let { annotation ->
                            try {
                                val url = if (!annotation.item.startsWith("http://") &&
                                    !annotation.item.startsWith("https://")) {
                                    "https://${annotation.item}"
                                } else {
                                    annotation.item
                                }
                                Desktop.getDesktop().browse(URI(url))
                            } catch (e: Exception) {
                                println("Error opening URL: ${e.message}")
                            }
                        }
                    }
                }
            }
            .pointerHoverIcon(icon = PointerIcon.Hand),
        style = style,
        onTextLayout = { layoutResult.value = it }
    )
}