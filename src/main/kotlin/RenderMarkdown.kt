import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.awt.Desktop

@OptIn(ExperimentalResourceApi::class)
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
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                is MarkdownToken.Paragraph -> {
                    val annotatedString = buildAnnotatedString {
                        appendInlineTokens(token.inlines)
                    }
                    ClickableText(
                        text = annotatedString,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                is MarkdownToken.Bullet -> {
                    val annotatedString = buildAnnotatedString {
                        append("• ")
                        appendInlineTokens(token.inlines)
                    }
                    ClickableText(
                        text = annotatedString,
                        modifier = Modifier.padding(vertical = 2.dp, horizontal = 8.dp)
                    )
                }

                is MarkdownToken.CodeBlock -> {
                    val bgColor = Color(0xFFF0F0F0)
                    val textColor = if (token.language.isNotEmpty()) Color(0xFF0000FF) else Color.Black

                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(bgColor)
                            .fillMaxWidth()
                    ) {
                        if (token.language.isNotEmpty()) {
                            Text(
                                token.language,
                                color = textColor,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            token.text,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                is MarkdownToken.Image -> {
                    val imageFile = File(token.url)
                    if (imageFile.exists() && imageFile.isFile) {
                        val imageBitmap = remember { mutableStateOf<ImageBitmap?>(null) }

                        LaunchedEffect(token.url) {
                            withContext(Dispatchers.IO) {
                                try {
                                    FileInputStream(imageFile).use { stream ->
                                        imageBitmap.value = stream.readAllBytes().decodeToImageBitmap()
                                    }
                                } catch (e: Exception) {
                                    println("Error loading image: ${e.message}")
                                }
                            }
                        }

                        imageBitmap.value?.let { bitmap ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Image(
                                    painter = BitmapPainter(bitmap),
                                    contentDescription = token.altText,
                                    modifier = Modifier.padding(4.dp)
                                )
                                if (token.altText.isNotEmpty()) {
                                    Text(
                                        token.altText,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Image not found: ${token.url}")
                    }
                }

                is MarkdownToken.Banner -> {
                    val (bgColor, borderColor) = when (token.type) {
                        BannerType.INFO -> Color(0xFFE0F7FA) to Color(0xFF00BCD4)
                        BannerType.WARNING -> Color(0xFFFFF8E1) to Color(0xFFFFC107)
                        BannerType.ERROR -> Color(0xFFFFEBEE) to Color(0xFFF44336)
                        BannerType.SUCCESS -> Color(0xFFE8F5E9) to Color(0xFF4CAF50)
                    }

                    Box(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        val annotatedString = buildAnnotatedString {
                            appendInlineTokens(token.inlines)
                        }
                        ClickableText(text = annotatedString)
                    }
                }

                is MarkdownToken.Table -> {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth()
                            .border(1.dp, Color.LightGray)
                    ) {
                        // Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFEEEEEE))
                        ) {
                            token.headers.forEach { header ->
                                Text(
                                    header,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                        .border(
                                            width = 1.dp,
                                            color = Color.LightGray
                                        )
                                        .padding(4.dp)
                                )
                            }
                        }

                        // Data rows
                        token.rows.forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                row.forEach { cell ->
                                    Text(
                                        cell,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(8.dp)
                                            .border(
                                                width = 1.dp,
                                                color = Color.LightGray
                                            )
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
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

            is InlineToken.Code -> {
                append(" ")
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFFEFEFEF),
                        letterSpacing = 0.5.sp
                    )
                ) {
                    append(token.text)
                }
                append(" ")
            }

            is InlineToken.Link -> {
                addStringAnnotation(
                    tag = "URL",
                    annotation = token.url,
                    start = length,
                    end = length + token.text.length
                )
                withStyle(
                    SpanStyle(
                        color = Color.Blue,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(token.text)
                }
            }
        }
    }
}