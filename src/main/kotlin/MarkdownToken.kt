sealed class MarkdownToken {
    data class Heading(val level: Int, val text: String) : MarkdownToken()
    data class Paragraph(val inlines: List<InlineToken>) : MarkdownToken()
    data class Bullet(val level: Int, val inlines: List<InlineToken>) : MarkdownToken()
    data class CodeBlock(val language: String, val text: String) : MarkdownToken()
    data class Image(val altText: String, val url: String) : MarkdownToken()
    data class Banner(val type: BannerType, val inlines: List<InlineToken>) : MarkdownToken()
    data class Table(val headers: List<List<InlineToken>>, val rows: List<List<List<InlineToken>>>) : MarkdownToken()
    data class ClickableImage(val altText: String, val imageUrl: String, val linkUrl: String) : MarkdownToken()
}

enum class BannerType {
    INFO, WARNING, ERROR, SUCCESS
}

sealed class InlineToken {
    data class Text(val text: String) : InlineToken()
    data class Bold(val text: String, val italic: Boolean = false) : InlineToken()
    data class Italic(val text: String, val bold: Boolean = false) : InlineToken()
    data class Code(val text: String) : InlineToken()
    data class ClickableImage(val altText: String, val imageUrl: String, val linkUrl: String) : InlineToken()
    data class Link(
        val text: String,
        val url: String,
        val bold: Boolean = false,
        val italic: Boolean = false
    ) : InlineToken()
}
