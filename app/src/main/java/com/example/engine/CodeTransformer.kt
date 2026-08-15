package com.example.engine

import com.example.model.CodeLanguage

object CodeTransformer {

    /**
     * Formats code with clean indentation, consistent spacing and syntax fixes.
     */
    fun formatCode(code: String, language: CodeLanguage): String {
        if (code.isBlank()) return code

        return when (language) {
            CodeLanguage.HTML -> formatHtml(code)
            CodeLanguage.CSS -> formatCss(code)
            CodeLanguage.JAVASCRIPT, CodeLanguage.TYPESCRIPT -> formatJs(code)
            CodeLanguage.JSON -> formatJson(code)
            else -> formatGeneric(code)
        }
    }

    private fun formatHtml(html: String): String {
        val clean = html.trim()
        val tokens = clean
            .replace("><", ">\n<")
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val sb = StringBuilder()
        var indent = 0
        val voidTags = setOf("area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr", "!doctype")

        for (line in tokens) {
            val isClosing = line.startsWith("</")
            val isSelfClosing = line.endsWith("/>") || voidTags.any { line.lowercase().startsWith("<$it") }
            val isOpening = line.startsWith("<") && !isClosing && !isSelfClosing && !line.startsWith("<!--")

            if (isClosing) {
                indent = (indent - 1).coerceAtLeast(0)
            }

            sb.append("  ".repeat(indent)).append(line).append("\n")

            if (isOpening) {
                // If it's single line open and close like <h1>Title</h1>, don't indent
                val tagName = line.substringAfter("<").substringBefore(" ").substringBefore(">")
                val hasClosingInSameLine = line.contains("</$tagName>")
                if (!hasClosingInSameLine) {
                    indent++
                }
            }
        }
        return sb.toString().trimEnd()
    }

    private fun formatCss(css: String): String {
        val clean = css
            .replace("\\s+".toRegex(), " ")
            .replace("\\{".toRegex(), " {\n")
            .replace(";".toRegex(), ";\n")
            .replace("\\}".toRegex(), "\n}\n")

        val lines = clean.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val sb = StringBuilder()
        var indent = 0

        for (line in lines) {
            if (line.startsWith("}")) {
                indent = (indent - 1).coerceAtLeast(0)
            }
            sb.append("  ".repeat(indent)).append(line).append("\n")
            if (line.endsWith("{")) {
                indent++
            }
        }
        return sb.toString().trimEnd()
    }

    private fun formatJs(js: String): String {
        val lines = js.split("\n")
        val sb = StringBuilder()
        var indent = 0

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) {
                sb.append("\n")
                continue
            }

            val closingCount = line.count { it == '}' || it == ']' || it == ')' }
            val openingCount = line.count { it == '{' || it == '[' || it == '(' }

            if (line.startsWith("}") || line.startsWith("]") || line.startsWith(")")) {
                indent = (indent - 1).coerceAtLeast(0)
            }

            sb.append("  ".repeat(indent)).append(line).append("\n")

            if (openingCount > closingCount && !line.startsWith("}") && !line.startsWith("]") && !line.startsWith(")")) {
                indent += (openingCount - closingCount)
            } else if (closingCount > openingCount && (line.startsWith("}") || line.startsWith("]") || line.startsWith(")"))) {
                // already adjusted above
            }
        }
        return sb.toString().trimEnd()
    }

    private fun formatJson(json: String): String {
        val sb = StringBuilder()
        var indent = 0
        var inQuotes = false

        for (char in json) {
            when (char) {
                '"' -> {
                    inQuotes = !inQuotes
                    sb.append(char)
                }
                '{', '[' -> {
                    sb.append(char)
                    if (!inQuotes) {
                        indent++
                        sb.append("\n").append("  ".repeat(indent))
                    }
                }
                '}', ']' -> {
                    if (!inQuotes) {
                        indent = (indent - 1).coerceAtLeast(0)
                        sb.append("\n").append("  ".repeat(indent))
                    }
                    sb.append(char)
                }
                ',' -> {
                    sb.append(char)
                    if (!inQuotes) {
                        sb.append("\n").append("  ".repeat(indent))
                    }
                }
                ':' -> {
                    sb.append(char)
                    if (!inQuotes) sb.append(" ")
                }
                ' ', '\t', '\n', '\r' -> {
                    if (inQuotes) sb.append(char)
                }
                else -> sb.append(char)
            }
        }
        return sb.toString()
    }

    private fun formatGeneric(code: String): String {
        return code.lines().joinToString("\n") { it.trimEnd() }
    }

    /**
     * Embeds CSS and JavaScript into a single self-contained HTML file.
     */
    fun embedCode(html: String, css: String, js: String): String {
        var merged = html

        // Strip existing external link tags for style.css if present
        merged = merged.replace("<link[^>]*href=[\"'][^\"']*style\\.css[\"'][^>]*>".toRegex(RegexOption.IGNORE_CASE), "")
        // Strip existing external script tags for script.js if present
        merged = merged.replace("<script[^>]*src=[\"'][^\"']*script\\.js[\"'][^>]*>\\s*</script>".toRegex(RegexOption.IGNORE_CASE), "")

        val styleTag = "\n  <style>\n${css.prependIndent("    ")}\n  </style>"
        val scriptTag = "\n  <script>\n${js.prependIndent("    ")}\n  </script>"

        merged = if (merged.contains("</head>", ignoreCase = true)) {
            merged.replace("</head>", "$styleTag\n</head>", ignoreCase = true)
        } else {
            "$styleTag\n$merged"
        }

        merged = if (merged.contains("</body>", ignoreCase = true)) {
            merged.replace("</body>", "$scriptTag\n</body>", ignoreCase = true)
        } else {
            "$merged\n$scriptTag"
        }

        return formatHtml(merged)
    }

    /**
     * Splits an embedded HTML document into separate HTML, CSS and JS.
     */
    fun splitCode(embeddedHtml: String): Triple<String, String, String> {
        var cleanHtml = embeddedHtml
        val cssBuilder = StringBuilder()
        val jsBuilder = StringBuilder()

        // Extract style tags
        val styleRegex = "<style[^>]*>([\\s\\S]*?)</style>".toRegex(RegexOption.IGNORE_CASE)
        styleRegex.findAll(embeddedHtml).forEach { match ->
            cssBuilder.append(match.groupValues[1].trim()).append("\n\n")
        }
        cleanHtml = cleanHtml.replace(styleRegex, "")

        // Extract inline script tags (excluding external src scripts)
        val scriptRegex = "<script(?!.*src=)[^>]*>([\\s\\S]*?)</script>".toRegex(RegexOption.IGNORE_CASE)
        scriptRegex.findAll(embeddedHtml).forEach { match ->
            val scriptContent = match.groupValues[1].trim()
            if (scriptContent.isNotEmpty()) {
                jsBuilder.append(scriptContent).append("\n\n")
            }
        }
        cleanHtml = cleanHtml.replace(scriptRegex, "")

        // Insert link tag in head
        val linkTag = "  <link rel=\"stylesheet\" href=\"style.css\">"
        cleanHtml = if (cleanHtml.contains("</head>", ignoreCase = true)) {
            cleanHtml.replace("</head>", "$linkTag\n</head>", ignoreCase = true)
        } else {
            "$linkTag\n$cleanHtml"
        }

        // Insert script tag before body close
        val scriptTag = "  <script src=\"script.js\"></script>"
        cleanHtml = if (cleanHtml.contains("</body>", ignoreCase = true)) {
            cleanHtml.replace("</body>", "$scriptTag\n</body>", ignoreCase = true)
        } else {
            "$cleanHtml\n$scriptTag"
        }

        val formattedHtml = formatHtml(cleanHtml)
        val formattedCss = formatCss(cssBuilder.toString().ifBlank { "/* Extracted CSS */\nbody {\n  margin: 0;\n  font-family: sans-serif;\n}" })
        val formattedJs = formatJs(jsBuilder.toString().ifBlank { "// Extracted JavaScript\nconsole.log('App ready');" })

        return Triple(formattedHtml, formattedCss, formattedJs)
    }
}
