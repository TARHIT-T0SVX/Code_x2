package com.example.engine

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.model.CodeLanguage
import com.example.ui.theme.*

object SyntaxHighlighter {

    private val jsKeywords = setOf(
        "const", "let", "var", "function", "return", "if", "else", "for", "while",
        "switch", "case", "break", "default", "import", "export", "from", "class",
        "new", "this", "async", "await", "try", "catch", "finally", "throw", "typeof",
        "instanceof", "in", "of", "null", "undefined", "true", "false"
    )

    private val htmlTags = setOf(
        "html", "head", "body", "div", "span", "p", "a", "h1", "h2", "h3", "h4", "h5", "h6",
        "button", "input", "form", "section", "article", "header", "footer", "nav", "main",
        "canvas", "script", "style", "link", "meta", "title", "ul", "ol", "li", "table", "tr", "td", "th",
        "img", "svg", "path", "circle", "rect", "video", "audio", "iframe", "b", "strong", "i", "em"
    )

    fun highlight(code: String, language: CodeLanguage): AnnotatedString {
        if (code.length > 50000) {
            // Fast-path fallback for huge files
            return AnnotatedString(code)
        }

        return when (language) {
            CodeLanguage.HTML -> highlightHtml(code)
            CodeLanguage.CSS -> highlightCss(code)
            CodeLanguage.JAVASCRIPT, CodeLanguage.TYPESCRIPT -> highlightJs(code)
            CodeLanguage.JSON -> highlightJson(code)
            else -> highlightGeneric(code)
        }
    }

    private fun highlightHtml(code: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Comments <!-- ... -->
            val commentRegex = "<!--[\\s\\S]*?-->".toRegex()
            commentRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = SyntaxComment), match.range.first, match.range.last + 1)
            }

            // HTML Tags <tag ... >
            val tagRegex = "</?([a-zA-Z0-9-]+)(\\s+[^>]*)?>".toRegex()
            tagRegex.findAll(code).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                addStyle(SpanStyle(color = SyntaxPunctuation), start, start + (if (code.startsWith("</", start)) 2 else 1))
                addStyle(SpanStyle(color = SyntaxPunctuation), end - 1, end)

                val tagGroup = match.groups[1]
                if (tagGroup != null) {
                    addStyle(
                        SpanStyle(color = SyntaxTag, fontWeight = FontWeight.SemiBold),
                        tagGroup.range.first,
                        tagGroup.range.last + 1
                    )
                }

                // Attributes within tag
                val attrsGroup = match.groups[2]
                if (attrsGroup != null) {
                    val attrRegex = "([a-zA-Z0-9-:]+)(?:=([\"'][^\"']*[\"']))?".toRegex()
                    attrRegex.findAll(attrsGroup.value).forEach { attrMatch ->
                        val attrName = attrMatch.groups[1]
                        if (attrName != null) {
                            val nameStart = attrsGroup.range.first + attrName.range.first
                            val nameEnd = attrsGroup.range.first + attrName.range.last + 1
                            addStyle(SpanStyle(color = SyntaxAttr), nameStart, nameEnd)
                        }
                        val attrVal = attrMatch.groups[2]
                        if (attrVal != null) {
                            val valStart = attrsGroup.range.first + attrVal.range.first
                            val valEnd = attrsGroup.range.first + attrVal.range.last + 1
                            addStyle(SpanStyle(color = SyntaxString), valStart, valEnd)
                        }
                    }
                }
            }
        }
    }

    private fun highlightCss(code: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Comments /* ... */
            val commentRegex = "/\\*[\\s\\S]*?\\*/".toRegex()
            commentRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = SyntaxComment), match.range.first, match.range.last + 1)
            }

            // CSS rules
            val ruleRegex = "([^{]+)\\{([^}]+)\\}".toRegex()
            ruleRegex.findAll(code).forEach { match ->
                val selectorGroup = match.groups[1]
                if (selectorGroup != null) {
                    addStyle(SpanStyle(color = SyntaxTag, fontWeight = FontWeight.SemiBold), selectorGroup.range.first, selectorGroup.range.last + 1)
                }

                val bodyGroup = match.groups[2]
                if (bodyGroup != null) {
                    val propRegex = "([a-zA-Z0-9-]+)\\s*:\\s*([^;]+);".toRegex()
                    propRegex.findAll(bodyGroup.value).forEach { propMatch ->
                        val propName = propMatch.groups[1]
                        val propVal = propMatch.groups[2]

                        if (propName != null) {
                            val pStart = bodyGroup.range.first + propName.range.first
                            val pEnd = bodyGroup.range.first + propName.range.last + 1
                            addStyle(SpanStyle(color = SyntaxProperty), pStart, pEnd)
                        }
                        if (propVal != null) {
                            val vStart = bodyGroup.range.first + propVal.range.first
                            val vEnd = bodyGroup.range.first + propVal.range.last + 1
                            addStyle(SpanStyle(color = SyntaxString), vStart, vEnd)
                        }
                    }
                }
            }
        }
    }

    private fun highlightJs(code: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Line & Block comments
            val commentRegex = "//.*|/\\*[\\s\\S]*?\\*/".toRegex()
            commentRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = SyntaxComment), match.range.first, match.range.last + 1)
            }

            // Strings
            val stringRegex = "([\"'`])(?:(?=(\\\\?))\\2.)*?\\1".toRegex()
            stringRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = SyntaxString), match.range.first, match.range.last + 1)
            }

            // Numbers
            val numRegex = "\\b\\d+(\\.\\d+)?(px|ms|s|%|deg)?\\b".toRegex()
            numRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = SyntaxNumber), match.range.first, match.range.last + 1)
            }

            // Word tokens (keywords & functions)
            val wordRegex = "\\b[a-zA-Z_$][a-zA-Z0-9_$]*\\b".toRegex()
            wordRegex.findAll(code).forEach { match ->
                val word = match.value
                if (word in jsKeywords) {
                    addStyle(SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.SemiBold), match.range.first, match.range.last + 1)
                } else if (match.range.last + 1 < code.length && code[match.range.last + 1] == '(') {
                    addStyle(SpanStyle(color = SyntaxFunction), match.range.first, match.range.last + 1)
                }
            }
        }
    }

    private fun highlightJson(code: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Keys
            val keyRegex = "\"([^\"\\\\]*)\"\\s*:".toRegex()
            keyRegex.findAll(code).forEach { match ->
                val keyGroup = match.groups[1]
                if (keyGroup != null) {
                    addStyle(SpanStyle(color = SyntaxProperty, fontWeight = FontWeight.SemiBold), keyGroup.range.first, keyGroup.range.last + 1)
                }
            }

            // Values in quotes
            val strValRegex = ":\\s*\"([^\"\\\\]*)\"".toRegex()
            strValRegex.findAll(code).forEach { match ->
                val valGroup = match.groups[1]
                if (valGroup != null) {
                    addStyle(SpanStyle(color = SyntaxString), valGroup.range.first, valGroup.range.last + 1)
                }
            }

            // Numbers & booleans
            val literalRegex = ":\\s*(\\d+|true|false|null)".toRegex()
            literalRegex.findAll(code).forEach { match ->
                val litGroup = match.groups[1]
                if (litGroup != null) {
                    addStyle(SpanStyle(color = SyntaxNumber), litGroup.range.first, litGroup.range.last + 1)
                }
            }
        }
    }

    private fun highlightGeneric(code: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            val stringRegex = "([\"'])(?:(?=(\\\\?))\\2.)*?\\1".toRegex()
            stringRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = SyntaxString), match.range.first, match.range.last + 1)
            }
        }
    }
}
