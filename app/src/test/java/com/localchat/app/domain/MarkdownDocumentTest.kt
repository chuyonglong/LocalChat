package com.localchat.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownDocumentTest {
    @Test
    fun `parse recognizes GFM blocks and fenced code language`() {
        val document = MarkdownDocument.parse(
            """
            # Title

            - [x] done

            | key | value |
            | --- | ----- |
            | a | b |

            ~~old~~ [site](https://example.com)

            ```json
            {"ok": true}
            ```
            """.trimIndent(),
        )

        assertTrue(document.blocks.any { it is MarkdownBlock.Heading })
        assertTrue(document.blocks.any { it is MarkdownBlock.TaskItem && it.checked })
        assertTrue(document.blocks.any { it is MarkdownBlock.Table })
        assertTrue(document.blocks.any { it is MarkdownBlock.Paragraph && it.text.contains("site") })
        assertEquals(CodeLanguage.JSON, (document.blocks.last() as MarkdownBlock.Code).language)
    }

    @Test
    fun `parse keeps unknown fenced language as plain code`() {
        val document = MarkdownDocument.parse("```rust\nfn main() {}\n```")

        assertEquals(CodeLanguage.PLAIN, (document.blocks.single() as MarkdownBlock.Code).language)
    }
}
