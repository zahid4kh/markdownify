import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import java.awt.Desktop
import java.net.URI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default
) {
    val textKey = text.toString()

    val layoutResult = remember(textKey) { mutableStateOf<TextLayoutResult?>(null) }
    var hoverPosition by remember(textKey) { mutableStateOf<Offset?>(null) }
    var hoverUrl by remember(textKey) { mutableStateOf<String?>(null) }
    val density = LocalDensity.current

    Box {
        // force update when text changes
        key(textKey) {
            BasicText(
                text = text,
                modifier = modifier
                    .pointerInput(textKey) {
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
                    .onPointerEvent(PointerEventType.Move) { event ->
                        val position = event.changes.first().position
                        hoverPosition = position
                        layoutResult.value?.let { textLayoutResult ->
                            val textPosition = textLayoutResult.getOffsetForPosition(position)
                            val urlAnnotation = text.getStringAnnotations("URL", textPosition, textPosition).firstOrNull()
                            hoverUrl = urlAnnotation?.item
                        }
                    }
                    .onPointerEvent(PointerEventType.Exit) {
                        hoverPosition = null
                        hoverUrl = null
                    }
                    .pointerHoverIcon(icon = if (hoverUrl != null) PointerIcon.Hand else PointerIcon.Default),
                style = style,
                onTextLayout = { layoutResult.value = it }
            )
        }

        hoverPosition?.let { position ->
            hoverUrl?.let { url ->
                Popup(
                    offset = with(density) {
                        IntOffset(
                            x = (position.x).toInt(),
                            y = (position.y - 40).toInt()
                        )
                    }
                ) {
                    Card(
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF303030)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = url,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}