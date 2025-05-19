fun parseMarkdown(input: String): List<MarkdownToken> {
    val lines = input.lines()
    val tokens = mutableListOf<MarkdownToken>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        when {
            line.startsWith("### ") -> {
                tokens.add(MarkdownToken.Heading(3, line.drop(4)))
                i++
            }
            line.startsWith("## ") -> {
                tokens.add(MarkdownToken.Heading(2, line.drop(3)))
                i++
            }
            line.startsWith("# ") -> {
                tokens.add(MarkdownToken.Heading(1, line.drop(2)))
                i++
            }
            line.startsWith("- ") -> {
                tokens.add(MarkdownToken.Bullet(parseInline(line.drop(2))))
                i++
            }
            line.startsWith("```") -> {
                i = parseCodeBlock(lines, i, tokens)
            }
            line.startsWith("!!! ") -> {
                tokens.add(MarkdownToken.Banner(BannerType.INFO, parseInline(line.drop(4))))
                i++
            }
            line.startsWith("!!! warning ") -> {
                tokens.add(MarkdownToken.Banner(BannerType.WARNING, parseInline(line.drop(12))))
                i++
            }
            line.startsWith("!!! error ") -> {
                tokens.add(MarkdownToken.Banner(BannerType.ERROR, parseInline(line.drop(10))))
                i++
            }
            line.startsWith("!!! success ") -> {
                tokens.add(MarkdownToken.Banner(BannerType.SUCCESS, parseInline(line.drop(12))))
                i++
            }
            line.startsWith("![") -> {
                val closeBracket = line.indexOf(']', 2)
                if (closeBracket != -1 && line.getOrNull(closeBracket + 1) == '(' && line.indexOf(')', closeBracket) != -1) {
                    val altText = line.substring(2, closeBracket)
                    val urlStart = closeBracket + 2
                    val urlEnd = line.indexOf(')', urlStart)
                    val url = line.substring(urlStart, urlEnd)
                    tokens.add(MarkdownToken.Image(altText, url))
                } else {
                    tokens.add(MarkdownToken.Paragraph(parseInline(line)))
                }
                i++
            }
            line.startsWith("|") && line.endsWith("|") -> {
                i = parseTable(lines, i, tokens)
            }
            else -> {
                tokens.add(MarkdownToken.Paragraph(parseInline(line)))
                i++
            }
        }
    }
    return tokens
}

fun parseCodeBlock(lines: List<String>, startIndex: Int, tokens: MutableList<MarkdownToken>): Int {
    val firstLine = lines[startIndex]
    val language = firstLine.drop(3).trim()

    val codeLines = mutableListOf<String>()
    var i = startIndex + 1

    while (i < lines.size && !lines[i].startsWith("```")) {
        codeLines.add(lines[i])
        i++
    }

    if (i < lines.size) i++ // Skiping the closing ```

    tokens.add(MarkdownToken.CodeBlock(language, codeLines.joinToString("\n")))
    return i
}

fun parseTable(lines: List<String>, startIndex: Int, tokens: MutableList<MarkdownToken>): Int {
    var i = startIndex
    val headers = lines[i].trim('|').split('|').map { it.trim() }
    i++
    if (i < lines.size && lines[i].matches(Regex("\\|[-:\\s|]*\\|"))) {
        i++
    }
    val rows = mutableListOf<List<String>>()
    while (i < lines.size && lines[i].startsWith("|") && lines[i].endsWith("|")) {
        val row = lines[i].trim('|').split('|').map { it.trim() }
        rows.add(row)
        i++
    }
    tokens.add(MarkdownToken.Table(headers, rows))
    return i
}

fun parseInline(text: String): List<InlineToken> {
    val result = mutableListOf<InlineToken>()
    val regex = Regex("""(\*\*.*?\*\*|\*.*?\*|`.*?`|\[.*?\]\(.*?\))""")
    var currentIndex = 0

    for (match in regex.findAll(text)) {
        if (match.range.first > currentIndex) {
            result += InlineToken.Text(text.substring(currentIndex, match.range.first))
        }

        val matched = match.value
        when {
            matched.startsWith("**") && matched.endsWith("**") ->
                result += InlineToken.Bold(matched.drop(2).dropLast(2))
            matched.startsWith("*") && matched.endsWith("*") ->
                result += InlineToken.Italic(matched.drop(1).dropLast(1))
            matched.startsWith("`") && matched.endsWith("`") ->
                result += InlineToken.Code(matched.drop(1).dropLast(1))
            matched.startsWith("[") &&
                    matched.contains("](") &&
                    matched.endsWith(")") -> {
                val closeBracket = matched.indexOf(']')
                val text = matched.substring(1, closeBracket)
                val url = matched.substring(closeBracket + 2, matched.length - 1)
                result += InlineToken.Link(text, url)
            }
        }

        currentIndex = match.range.last + 1
    }

    if (currentIndex < text.length) {
        result += InlineToken.Text(text.substring(currentIndex))
    }

    return result
}
