fun parseMarkdown(input: String): List<MarkdownToken> {
    val lines = input.lines()
    val tokens = mutableListOf<MarkdownToken>()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]
        val trimmedLine = line.trim()

        when {
            trimmedLine.startsWith("#### ") -> {
                tokens.add(MarkdownToken.Heading(4, trimmedLine.drop(5)))
                i++
            }
            trimmedLine.startsWith("### ") -> {
                tokens.add(MarkdownToken.Heading(3, trimmedLine.drop(4)))
                i++
            }
            trimmedLine.startsWith("## ") -> {
                tokens.add(MarkdownToken.Heading(2, trimmedLine.drop(3)))
                i++
            }
            trimmedLine.startsWith("# ") -> {
                tokens.add(MarkdownToken.Heading(1, trimmedLine.drop(2)))
                i++
            }
            isBulletLine(line) -> {
                val (level, content) = parseBulletLine(line)
                tokens.add(MarkdownToken.Bullet(level, parseInline(content)))
                i++
            }
            trimmedLine.startsWith("```") -> {
                val hasClosingBackticks = lines.subList(i + 1, lines.size).any { it.trim().startsWith("```") }
                if (hasClosingBackticks) {
                    i = parseCodeBlock(lines, i, tokens)
                } else {
                    tokens.add(MarkdownToken.Paragraph(parseInline(line)))
                    i++
                }
            }
            trimmedLine.startsWith("!!! ") -> {
                tokens.add(MarkdownToken.Banner(BannerType.INFO, parseInline(trimmedLine.drop(4))))
                i++
            }
            trimmedLine.startsWith("!!! warning ") -> {
                tokens.add(MarkdownToken.Banner(BannerType.WARNING, parseInline(trimmedLine.drop(12))))
                i++
            }
            trimmedLine.startsWith("!!! error ") -> {
                tokens.add(MarkdownToken.Banner(BannerType.ERROR, parseInline(trimmedLine.drop(10))))
                i++
            }
            trimmedLine.startsWith("!!! success ") -> {
                tokens.add(MarkdownToken.Banner(BannerType.SUCCESS, parseInline(trimmedLine.drop(12))))
                i++
            }
            trimmedLine.startsWith("> Note:") -> {
                tokens.add(MarkdownToken.Banner(BannerType.NOTE, parseInline(trimmedLine.drop(2))))
                i++
            }
            trimmedLine.startsWith("!!! note ") -> {
                tokens.add(MarkdownToken.Banner(BannerType.NOTE, parseInline(trimmedLine.drop(9))))
                i++
            }
            trimmedLine.startsWith("[![") -> {
                val imgCloseBracket = trimmedLine.indexOf("](", 3)
                if (imgCloseBracket != -1) {
                    val imgCloseParen = trimmedLine.indexOf(")", imgCloseBracket + 2)
                    if (imgCloseParen != -1 && trimmedLine.startsWith("](", imgCloseParen + 1)) {
                        val linkCloseParen = trimmedLine.indexOf(")", imgCloseParen + 3)
                        if (linkCloseParen != -1) {
                            val altText = trimmedLine.substring(3, imgCloseBracket)
                            val imageUrl = trimmedLine.substring(imgCloseBracket + 2, imgCloseParen)
                            val linkUrl = trimmedLine.substring(imgCloseParen + 3, linkCloseParen)
                            tokens.add(MarkdownToken.ClickableImage(altText, imageUrl, linkUrl))
                            i++
                            continue
                        }
                    }
                }
                tokens.add(MarkdownToken.Paragraph(parseInline(line)))
                i++
            }
            trimmedLine.startsWith("![") -> {
                val closeBracket = trimmedLine.indexOf(']', 2)
                if (closeBracket != -1 && trimmedLine.getOrNull(closeBracket + 1) == '(' && trimmedLine.indexOf(')', closeBracket) != -1) {
                    val altText = trimmedLine.substring(2, closeBracket)
                    val urlStart = closeBracket + 2
                    val urlEnd = trimmedLine.indexOf(')', urlStart)
                    val url = trimmedLine.substring(urlStart, urlEnd)
                    tokens.add(MarkdownToken.Image(altText, url))
                } else {
                    tokens.add(MarkdownToken.Paragraph(parseInline(line)))
                }
                i++
            }
            line.startsWith("|") && line.endsWith("|") -> {
                i = parseTable(lines, i, tokens)
            }
            trimmedLine == "---" || trimmedLine == "***" || trimmedLine == "___" -> {
                tokens.add(MarkdownToken.HorizontalRule)
                i++
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
    val trimmedFirstLine = firstLine.trim()
    val language = trimmedFirstLine.drop(3).trim()

    val indentationLength = firstLine.length - firstLine.trimStart().length

    val codeLines = mutableListOf<String>()
    var i = startIndex + 1

    while (i < lines.size && !lines[i].trim().startsWith("```")) {
        val currentLine = lines[i]
        val processedLine = if (currentLine.length > indentationLength &&
            currentLine.substring(0, indentationLength).all { it == ' ' || it == '\t' }) {
            currentLine.substring(indentationLength)
        } else {
            currentLine
        }
        codeLines.add(processedLine)
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