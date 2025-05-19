sealed class MarkdownToken {
    data class Heading(val level: Int, val text: String) : MarkdownToken()
    data class Paragraph(val inlines: List<InlineToken>) : MarkdownToken()
    data class Bullet(val inlines: List<InlineToken>) : MarkdownToken()
    data class CodeBlock(val language: String, val text: String) : MarkdownToken()
    data class Image(val altText: String, val url: String) : MarkdownToken()
    data class Banner(val type: BannerType, val inlines: List<InlineToken>) : MarkdownToken()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownToken()
}

enum class BannerType {
    INFO, WARNING, ERROR, SUCCESS
}

sealed class InlineToken {
    data class Text(val text: String) : InlineToken()
    data class Bold(val text: String) : InlineToken()
    data class Italic(val text: String) : InlineToken()
    data class Code(val text: String) : InlineToken()
}
