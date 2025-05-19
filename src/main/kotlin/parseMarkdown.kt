fun parseMarkdown(input: String): List<MarkdownToken> {
    val lines = input.lines()
    val tokens = mutableListOf<MarkdownToken>()

    for (line in lines) {
        when {
            line.startsWith("### ") -> tokens.add(MarkdownToken.Heading(3, line.drop(4)))
            line.startsWith("## ") -> tokens.add(MarkdownToken.Heading(2, line.drop(3)))
            line.startsWith("# ") -> tokens.add(MarkdownToken.Heading(1, line.drop(2)))
            line.startsWith("- ") -> tokens.add(MarkdownToken.Bullet(parseInline(line.drop(2))))
            line.startsWith("```") -> {
                // multiline code block later
            }
            line.startsWith("...") -> tokens.add(MarkdownToken.CodeBlock(line.drop(3).trim()))
            else -> tokens.add(MarkdownToken.Paragraph(parseInline(line)))
        }
    }

    return tokens
}



fun parseInline(text: String): List<InlineToken> {
    val result = mutableListOf<InlineToken>()
    val regex = Regex("""(\*\*.*?\*\*|\*.*?\*|`.*?`)""")
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
        }

        currentIndex = match.range.last + 1
    }

    if (currentIndex < text.length) {
        result += InlineToken.Text(text.substring(currentIndex))
    }

    return result
}
