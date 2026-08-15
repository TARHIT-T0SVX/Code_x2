package com.example.model

import java.util.UUID

enum class CodeLanguage(val extension: String, val displayName: String) {
    HTML("html", "HTML"),
    CSS("css", "CSS"),
    JAVASCRIPT("js", "JavaScript"),
    TYPESCRIPT("ts", "TypeScript"),
    JSON("json", "JSON"),
    KOTLIN("kt", "Kotlin"),
    PYTHON("py", "Python"),
    MARKDOWN("md", "Markdown"),
    UNKNOWN("txt", "Plain Text");

    companion object {
        fun fromExtension(ext: String): CodeLanguage {
            return entries.find { it.extension.equals(ext, ignoreCase = true) } ?: UNKNOWN
        }

        fun fromFileName(fileName: String): CodeLanguage {
            val ext = fileName.substringAfterLast('.', "")
            return fromExtension(ext)
        }
    }
}

data class CodeFile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val path: String, // e.g. "/index.html" or "/css/style.css"
    val content: String = "",
    val isDirectory: Boolean = false,
    val children: List<CodeFile> = emptyList(),
    val isModified: Boolean = false,
    val isExpanded: Boolean = true
) {
    val extension: String
        get() = name.substringAfterLast('.', "")

    val language: CodeLanguage
        get() = CodeLanguage.fromFileName(name)
}

data class CodeProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "My Web Project",
    val description: String = "Responsive Web Application",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val files: List<CodeFile> = emptyList()
)
