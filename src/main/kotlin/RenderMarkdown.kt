import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.net.URI

@OptIn(ExperimentalResourceApi::class)
@Composable
fun RenderMarkdown(tokens: List<MarkdownToken>) {
    Column(modifier = Modifier.padding(16.dp)) {
        for (token in tokens) {
            when (token) {
                is MarkdownToken.Heading -> Text(
                    token.text,
                    fontSize = when (token.level) {
                        1 -> 26.sp
                        2 -> 22.sp
                        3 -> 19.sp
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
                        modifier = Modifier.padding(
                            vertical = 2.dp,
                            horizontal = (8 + token.level * 16).dp
                        )
                    )
                }

                is MarkdownToken.CodeBlock -> {
                    val bgColor = Color(0xFFF5F5F5)
                    val borderColor = Color(0xFFDDDDDD)
                    val textColor = if (token.language.isNotEmpty()) Color(0xFF0000FF) else Color.Black

                    Column(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
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
                                    .padding(4.dp)
                            )
                        }

                        Text(
                            token.text,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                is MarkdownToken.Image -> {
                    val isWebUrl = token.url.startsWith("http://") || token.url.startsWith("https://")

                    if (isWebUrl) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            var isLoading by remember { mutableStateOf(true) }
                            var hasError by remember { mutableStateOf(false) }

                            val imageUrl = convertGitHubUrlToRaw(token.url)

                            AsyncImage(
                                model = imageUrl,
                                contentDescription = token.altText,
                                modifier = Modifier.padding(4.dp),
                                onState = { state ->
                                    when (state) {
                                        is AsyncImagePainter.State.Loading -> {
                                            isLoading = true
                                            hasError = false
                                        }
                                        is AsyncImagePainter.State.Error -> {
                                            isLoading = false
                                            hasError = true
                                            println("Error loading image from URL: $imageUrl")
                                            println("Original URL: ${token.url}")
                                            println("Error: ${state.result.throwable.message}")
                                        }
                                        is AsyncImagePainter.State.Success -> {
                                            isLoading = false
                                            hasError = false
                                        }
                                        else -> {}
                                    }
                                },
                                contentScale = ContentScale.Fit
                            )

                            when {
                                isLoading -> Text(
                                    "Loading image...",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(4.dp)
                                )
                                hasError -> Text(
                                    "Failed to load image: ${token.url}",
                                    fontSize = 12.sp,
                                    color = Color.Red,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }

                            if (token.altText.isNotEmpty()) {
                                Text(
                                    token.altText,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else {
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
                            Text(
                                "Image not found: ${token.url}",
                                color = Color.Red,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
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

                is MarkdownToken.ClickableImage -> {
                    val imageUrl = convertGitHubUrlToRaw(token.imageUrl)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        var isLoading by remember { mutableStateOf(true) }
                        var hasError by remember { mutableStateOf(false) }

                        AsyncImage(
                            model = imageUrl,
                            contentDescription = token.altText,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    try {
                                        Desktop.getDesktop().browse(URI(token.linkUrl))
                                    } catch (e: Exception) {
                                        println("Error opening URL: ${e.message}")
                                    }
                                },
                            onState = { state ->
                                when (state) {
                                    is AsyncImagePainter.State.Loading -> {
                                        isLoading = true
                                        hasError = false
                                    }
                                    is AsyncImagePainter.State.Error -> {
                                        isLoading = false
                                        hasError = true
                                        println("Error loading badge: ${state.result.throwable.message}")
                                    }
                                    is AsyncImagePainter.State.Success -> {
                                        isLoading = false
                                        hasError = false
                                    }
                                    else -> {}
                                }
                            },
                            contentScale = ContentScale.Fit
                        )

                        when {
                            isLoading -> Text(
                                "Loading badge...",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            hasError -> Text(
                                "[${token.altText}]",
                                fontSize = 12.sp,
                                color = Color.Blue,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable {
                                    try {
                                        Desktop.getDesktop().browse(URI(token.linkUrl))
                                    } catch (e: Exception) {
                                        println("Error opening URL: ${e.message}")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
private fun convertGitHubUrlToRaw(url: String): String {
    return if (url.contains("github.com") && url.contains("/blob/")) {
        url.replace("github.com", "raw.githubusercontent.com")
            .replace("/blob/", "/")
    } else {
        url
    }
}
fun AnnotatedString.Builder.appendInlineTokens(tokens: List<InlineToken>) {
    for (token in tokens) {
        when (token) {
            is InlineToken.Text -> append(token.text)

            is InlineToken.Bold -> {
                val style = if (token.italic) {
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    SpanStyle(fontWeight = FontWeight.Bold)
                }

                withStyle(style) {
                    append(token.text)
                }
            }

            is InlineToken.Italic -> {
                val style = if (token.bold) {
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic
                    )
                } else {
                    SpanStyle(fontStyle = FontStyle.Italic)
                }

                withStyle(style) {
                    append(token.text)
                }
            }

            is InlineToken.Code -> {
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

                val spanStyle = SpanStyle(
                    color = Color.Blue,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = if (token.bold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (token.italic) FontStyle.Italic else FontStyle.Normal
                )

                withStyle(spanStyle) {
                    append(token.text)
                }
            }

            is InlineToken.ClickableImage -> {
                addStringAnnotation(
                    tag = "URL",
                    annotation = token.linkUrl,
                    start = length,
                    end = length + token.altText.length
                )

                val spanStyle = SpanStyle(
                    color = Color.Blue,
                    background = Color(0xFFE8F4FD),
                    fontSize = 10.sp
                )

//                withStyle(spanStyle) {
//                    append("[${token.altText}]")
//                }

                withStyle(spanStyle) {
                    append("🏷️")
                }
            }
        }
    }
}