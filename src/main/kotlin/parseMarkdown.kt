fun parseMarkdown(input: String): List<MarkdownToken> {
    val lines = input.lines()
    val tokens = mutableListOf<MarkdownToken>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        when {
            line.startsWith("#### ") -> {
                tokens.add(MarkdownToken.Heading(4, line.drop(5)))
                i++
            }
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
            isBulletLine(line) -> {
                val (level, content) = parseBulletLine(line)
                tokens.add(MarkdownToken.Bullet(level, parseInline(content)))
                i++
            }
            line.startsWith("```") -> {
                val hasClosingBackticks = lines.subList(i + 1, lines.size).any { it.trim() == "```" }
                if (hasClosingBackticks) {
                    i = parseCodeBlock(lines, i, tokens)
                } else {
                    tokens.add(MarkdownToken.Paragraph(parseInline(line)))
                    i++
                }
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
            line.startsWith("[![") -> {
                val imgCloseBracket = line.indexOf("](", 3)
                if (imgCloseBracket != -1) {
                    val imgCloseParen = line.indexOf(")", imgCloseBracket + 2)
                    if (imgCloseParen != -1 && line.startsWith("](", imgCloseParen + 1)) {
                        val linkCloseParen = line.indexOf(")", imgCloseParen + 3)
                        if (linkCloseParen != -1) {
                            val altText = line.substring(3, imgCloseBracket)
                            val imageUrl = line.substring(imgCloseBracket + 2, imgCloseParen)
                            val linkUrl = line.substring(imgCloseParen + 3, linkCloseParen)
                            tokens.add(MarkdownToken.ClickableImage(altText, imageUrl, linkUrl))
                            i++
                            continue
                        }
                    }
                }
                tokens.add(MarkdownToken.Paragraph(parseInline(line)))
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

fun isBulletLine(line: String): Boolean {
    var i = 0
    while (i < line.length && (line[i] == ' ' || line[i] == '\t')) {
        i++
    }
    return i < line.length && (line.substring(i).startsWith("- ") || line.substring(i).startsWith("* "))
}

fun parseBulletLine(line: String): Pair<Int, String> {
    var i = 0
    var spaceCount = 0
    while (i < line.length && line[i] == ' ') {
        spaceCount++
        i++
    }
    val level = spaceCount / 2
    val content = line.substring(i + 2)
    return Pair(level, content)
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
    val headerLine = lines[i].trim('|').split('|').map { it.trim() }
    val headers = headerLine.map { parseInline(it) }
    i++
    if (i < lines.size && lines[i].matches(Regex("\\|[-:\\s|]*\\|"))) {
        i++
    }
    val rows = mutableListOf<List<List<InlineToken>>>()
    while (i < lines.size && lines[i].startsWith("|") && lines[i].endsWith("|")) {
        val rowLine = lines[i].trim('|').split('|').map { it.trim() }
        val row = rowLine.map { parseInline(it) }
        rows.add(row)
        i++
    }
    tokens.add(MarkdownToken.Table(headers, rows))
    return i
}

fun parseInline(text: String): List<InlineToken> {
    val result = mutableListOf<InlineToken>()

    parseInlineRecursive(text = text, result = result, insideBold = false, insideItalic = false, insideCode = false)

    return result
}

private fun parseInlineRecursive(
    text: String,
    result: MutableList<InlineToken>,
    insideBold: Boolean = false,
    insideItalic: Boolean = false,
    insideCode: Boolean = false
) {
    var i = 0

    while (i < text.length) {
        if (text.startsWith("[", i) && !insideCode) {
            val closeBracketIndex = text.indexOf(']', i + 1)

            if (closeBracketIndex != -1 && text.startsWith("(", closeBracketIndex + 1)) {
                val closeParenIndex = text.indexOf(')', closeBracketIndex + 1)

                if (closeParenIndex != -1) {
                    val linkText = text.substring(i + 1, closeBracketIndex)
                    val linkUrl = text.substring(closeBracketIndex + 2, closeParenIndex)

                    if (insideBold) {
                        result.add(InlineToken.Link(
                            text = linkText,
                            url = linkUrl,
                            bold = true,
                            italic = insideItalic
                        ))
                    } else if (insideItalic) {
                        result.add(InlineToken.Link(
                            text = linkText,
                            url = linkUrl,
                            bold = false,
                            italic = true
                        ))
                    } else {
                        result.add(InlineToken.Link(
                            text = linkText,
                            url = linkUrl,
                            bold = false,
                            italic = false
                        ))
                    }

                    i = closeParenIndex + 1
                    continue
                }
            }
        }

        if (text.startsWith("[![", i) && !insideCode) {
            val imgCloseBracket = text.indexOf("](", i + 3)
            if (imgCloseBracket != -1) {
                val imgCloseParen = text.indexOf(")", imgCloseBracket + 2)
                if (imgCloseParen != -1 && text.startsWith("](", imgCloseParen + 1)) {
                    val linkCloseParen = text.indexOf(")", imgCloseParen + 3)
                    if (linkCloseParen != -1) {
                        val altText = text.substring(i + 3, imgCloseBracket)
                        val imageUrl = text.substring(imgCloseBracket + 2, imgCloseParen)
                        val linkUrl = text.substring(imgCloseParen + 3, linkCloseParen)

                        result.add(InlineToken.ClickableImage(altText, imageUrl, linkUrl))
                        i = linkCloseParen + 1
                        continue
                    }
                }
            }
        }

        if (text.startsWith("**", i) && !insideCode && !insideBold) {
            val closeBoldIndex = text.indexOf("**", i + 2)

            if (closeBoldIndex != -1) {
                val boldContent = text.substring(i + 2, closeBoldIndex)

                val nestedResult = mutableListOf<InlineToken>()
                parseInlineRecursive(boldContent, nestedResult, true, insideItalic, false)

                if (nestedResult.isEmpty()) {
                    result.add(InlineToken.Bold(boldContent))
                } else {
                    nestedResult.forEach { token ->
                        when (token) {
                            is InlineToken.Text -> result.add(InlineToken.Bold(token.text))
                            is InlineToken.Link -> result.add(InlineToken.Link(token.text, token.url, true, token.italic))
                            is InlineToken.Italic -> result.add(InlineToken.Italic(token.text, true))
                            is InlineToken.Code -> result.add(token)
                            is InlineToken.Bold -> result.add(token)
                            is InlineToken.ClickableImage -> {}
                        }
                    }
                }

                i = closeBoldIndex + 2
                continue
            }
        }

        if (text.startsWith("*", i) && !text.startsWith("**", i) && !insideCode && !insideItalic) {
            val closeItalicIndex = text.indexOf("*", i + 1)

            if (closeItalicIndex != -1) {
                val italicContent = text.substring(i + 1, closeItalicIndex)

                val nestedResult = mutableListOf<InlineToken>()
                parseInlineRecursive(italicContent, nestedResult, insideBold, true, false)

                if (nestedResult.isEmpty()) {
                    result.add(InlineToken.Italic(italicContent))
                } else {
                    nestedResult.forEach { token ->
                        when (token) {
                            is InlineToken.Text -> result.add(InlineToken.Italic(token.text))
                            is InlineToken.Link -> result.add(InlineToken.Link(token.text, token.url, token.bold, true))
                            is InlineToken.Bold -> result.add(InlineToken.Bold(token.text, true))
                            is InlineToken.Code -> result.add(token)
                            is InlineToken.Italic -> result.add(token)
                            is InlineToken.ClickableImage -> {}
                        }
                    }
                }

                i = closeItalicIndex + 1
                continue
            }
        }

        if (text.startsWith("`", i) && !insideCode) {
            val closeCodeIndex = text.indexOf("`", i + 1)

            if (closeCodeIndex != -1) {
                val codeContent = text.substring(i + 1, closeCodeIndex)
                result.add(InlineToken.Code(codeContent))
                i = closeCodeIndex + 1
                continue
            }
        }

        var nextSpecialChar = text.length
        for (marker in listOf("**", "*", "`", "[")) {
            val index = text.indexOf(marker, i)
            if (index != -1 && index < nextSpecialChar) {
                nextSpecialChar = index
            }
        }

        if (nextSpecialChar > i) {
            val plainText = text.substring(i, nextSpecialChar)
            result.add(InlineToken.Text(plainText))
            i = nextSpecialChar
        } else {
            result.add(InlineToken.Text(text.substring(i)))
            break
        }
    }
}