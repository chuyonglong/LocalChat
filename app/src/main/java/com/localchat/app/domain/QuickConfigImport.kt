package com.localchat.app.domain

object QuickConfigImport {
    sealed interface Result {
        data class Success(val endpoint: String, val apiKey: String, val model: String) : Result
        data class Error(val message: String) : Result
    }

    fun parse(value: String, currentModel: String): Result {
        val lines = value.lineSequence().map(String::trim).filter(String::isNotEmpty).toList()
        if (lines.size !in 2..3) return Result.Error("请输入两行或三行配置")
        val endpoint = when (val validation = ApiConfiguration.validateEndpoint(lines[0])) {
            is ApiConfiguration.Validation.Valid -> validation.endpoint
            is ApiConfiguration.Validation.Invalid -> return Result.Error(validation.message)
        }
        return Result.Success(endpoint, lines[1], lines.getOrElse(2) { currentModel })
    }
}
