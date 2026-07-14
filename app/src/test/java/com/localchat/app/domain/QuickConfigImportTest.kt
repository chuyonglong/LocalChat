package com.localchat.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class QuickConfigImportTest {
    @Test
    fun `two valid lines import endpoint and key while retaining current model`() {
        assertEquals(
            QuickConfigImport.Result.Success("https://api.example.com/v1", "sk-test", "gpt-4o"),
            QuickConfigImport.parse(" https://api.example.com/v1 \n sk-test ", "gpt-4o"),
        )
    }

    @Test
    fun `three valid lines import endpoint key and model while ignoring blank lines`() {
        assertEquals(
            QuickConfigImport.Result.Success("https://api.example.com/v1", "sk-test", "model-x"),
            QuickConfigImport.parse("\nhttps://api.example.com/v1\n\nsk-test\nmodel-x\n", "old"),
        )
    }

    @Test
    fun `invalid line count and address return errors`() {
        assertEquals(QuickConfigImport.Result.Error("请输入两行或三行配置"), QuickConfigImport.parse("one", "model"))
        assertEquals(QuickConfigImport.Result.Error("服务地址必须以 http:// 或 https:// 开头"), QuickConfigImport.parse("api.example.com\nsk-test", "model"))
    }
}
