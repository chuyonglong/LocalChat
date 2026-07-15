package com.localchat.app.domain

object MessageFormatting {
    fun normalizeForSend(text: String): String = text.trimEnd()
}

enum class CodeLanguage {
    JSON,
    KOTLIN,
    JAVA,
    XML,
    YAML,
    BASH,
    PYTHON,
    JAVASCRIPT,
    PLAIN;

    companion object {
        fun fromFenceInfo(info: String): CodeLanguage = when (info.trim().substringBefore(' ').lowercase()) {
            "json" -> JSON
            "kotlin", "kt" -> KOTLIN
            "java" -> JAVA
            "xml", "html" -> XML
            "yaml", "yml" -> YAML
            "bash", "sh", "shell" -> BASH
            "python", "py" -> PYTHON
            "javascript", "js", "typescript", "ts" -> JAVASCRIPT
            else -> PLAIN
        }
    }
}
