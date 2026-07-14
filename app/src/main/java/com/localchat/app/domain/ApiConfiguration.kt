package com.localchat.app.domain

object ApiConfiguration {
    sealed interface Validation {
        data class Valid(val endpoint: String) : Validation
        data class Invalid(val message: String) : Validation
    }

    fun validateEndpoint(value: String): Validation {
        val normalized = value.trim().trimEnd('/')
        return if (normalized.startsWith("https://") || normalized.startsWith("http://")) {
            Validation.Valid(normalized)
        } else {
            Validation.Invalid("服务地址必须以 http:// 或 https:// 开头")
        }
    }
}
