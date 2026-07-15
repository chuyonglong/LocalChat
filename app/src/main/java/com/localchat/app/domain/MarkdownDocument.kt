package com.localchat.app.domain

import org.commonmark.Extension
import org.commonmark.ext.autolink.AutolinkExtension
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.ext.task.list.items.TaskListItemMarker
import org.commonmark.ext.task.list.items.TaskListItemsExtension
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.Heading
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser

data class MarkdownDocument(val blocks: List<MarkdownBlock>) {
    companion object {
        private val extensions: List<Extension> = listOf(
            AutolinkExtension.create(),
            StrikethroughExtension.create(),
            TablesExtension.create(),
            TaskListItemsExtension.create(),
        )
        private val parser = Parser.builder().extensions(extensions).build()

        fun parse(source: String): MarkdownDocument = MarkdownDocument(blocksOf(parser.parse(source)))

        private fun blocksOf(parent: Node): List<MarkdownBlock> = parent.children().flatMap { node ->
            when (node) {
                is Heading -> listOf(MarkdownBlock.Heading(node.level, inlineText(node)))
                is Paragraph -> listOf(MarkdownBlock.Paragraph(inlineText(node)))
                is FencedCodeBlock -> listOf(MarkdownBlock.Code(node.literal, CodeLanguage.fromFenceInfo(node.info)))
                is IndentedCodeBlock -> listOf(MarkdownBlock.Code(node.literal, CodeLanguage.PLAIN))
                is BulletList -> node.children().mapNotNull { item ->
                    if (item !is ListItem) return@mapNotNull null
                    val marker = item.children().filterIsInstance<TaskListItemMarker>().firstOrNull()
                    if (marker != null) MarkdownBlock.TaskItem(marker.isChecked, inlineText(item))
                    else MarkdownBlock.ListItem(inlineText(item))
                }
                is BlockQuote -> listOf(MarkdownBlock.Quote(blocksOf(node)))
                is TableBlock -> listOf(tableOf(node))
                is ThematicBreak -> listOf(MarkdownBlock.Rule)
                else -> emptyList()
            }
        }

        private fun tableOf(table: TableBlock): MarkdownBlock.Table {
            val rows = table.children().filterIsInstance<TableHead>().flatMap { it.children() } +
                table.children().filterIsInstance<TableRow>()
            return MarkdownBlock.Table(rows.map { row ->
                row.children().filterIsInstance<TableCell>().map(::inlineText)
            })
        }

        private fun inlineText(node: Node): String = buildString {
            node.walk { current ->
                when (current) {
                    is Text -> append(current.literal)
                    is Code -> append(current.literal)
                    is SoftLineBreak -> append('\n')
                    is Link -> append(current.destination)
                    is Strikethrough -> Unit
                }
            }
        }
    }
}

sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class Code(val content: String, val language: CodeLanguage) : MarkdownBlock
    data class TaskItem(val checked: Boolean, val text: String) : MarkdownBlock
    data class ListItem(val text: String) : MarkdownBlock
    data class Quote(val blocks: List<MarkdownBlock>) : MarkdownBlock
    data class Table(val rows: List<List<String>>) : MarkdownBlock
    data object Rule : MarkdownBlock
}

private fun Node.children(): List<Node> = generateSequence(firstChild) { it.next }.toList()

private fun Node.walk(action: (Node) -> Unit) {
    action(this)
    children().forEach { it.walk(action) }
}
