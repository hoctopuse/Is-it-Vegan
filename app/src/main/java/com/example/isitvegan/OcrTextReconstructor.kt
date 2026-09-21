package com.example.isitvegan

import kotlin.math.abs

internal object OcrTextReconstructor {
    private data class OrderedBlock(val block: OcrBlock, val lines: List<OcrLine>)

    fun reconstruct(document: OcrDocument): OcrReconstruction {
        if (document.blocks.isEmpty()) return OcrReconstruction("", emptyList())
        val warnings = mutableListOf<String>()
        val orderedBlocks = orderBlocks(document.blocks, warnings).map { block ->
            OrderedBlock(block, orderLines(block.lines, warnings))
        }
        val averageHeight = orderedBlocks.flatMap { it.lines }.mapNotNull { it.bounds?.height }
            .filter { it > 0 }.averageOrNull()
        val text = buildString {
            orderedBlocks.forEachIndexed { index, ordered ->
                if (index > 0) {
                    val previous = orderedBlocks[index - 1]
                    append('\n')
                    val gap = verticalGap(previous.block.bounds, ordered.block.bounds)
                    if (averageHeight != null && gap != null && gap > averageHeight * 1.5) append('\n')
                }
                append(ordered.lines.joinToString("\n") { it.text })
            }
        }
        return OcrReconstruction(text, warnings.distinct())
    }

    private fun orderBlocks(blocks: List<OcrBlock>, warnings: MutableList<String>): List<OcrBlock> {
        if (blocks.any { it.bounds == null }) {
            warnings += "ordre ML Kit conservé pour des blocs sans coordonnées"
            return blocks.sortedBy { it.mlKitOrder }
        }
        val columns = splitColumns(blocks, { it.bounds!! })
        if (columns != null) {
            warnings += "colonnes visuelles conservées séparément"
            return columns.flatten()
        }
        return orderByRows(blocks, { it.bounds!! }, { it.mlKitOrder })
    }

    private fun orderLines(lines: List<OcrLine>, warnings: MutableList<String>): List<OcrLine> {
        if (lines.any { it.bounds == null }) {
            if (lines.size > 1) warnings += "ordre ML Kit conservé pour des lignes sans coordonnées"
            return lines.sortedBy { it.mlKitOrder }
        }
        val columns = splitColumns(lines, { it.bounds!! })
        if (columns != null) {
            warnings += "colonnes visuelles conservées séparément"
            return columns.flatten()
        }
        return orderByRows(lines, { it.bounds!! }, { it.mlKitOrder })
    }

    private fun <T> orderByRows(items: List<T>, bounds: (T) -> OcrBounds, order: (T) -> Int): List<T> {
        val remaining = items.sortedWith(compareBy<T> { bounds(it).top }.thenBy(order)).toMutableList()
        val rows = mutableListOf<MutableList<T>>()
        while (remaining.isNotEmpty()) {
            val seed = remaining.removeAt(0)
            val row = mutableListOf(seed)
            var changed: Boolean
            do {
                changed = false
                val iterator = remaining.iterator()
                while (iterator.hasNext()) {
                    val candidate = iterator.next()
                    if (row.any { bounds(it).verticallyOverlaps(bounds(candidate)) }) {
                        row += candidate
                        iterator.remove()
                        changed = true
                    }
                }
            } while (changed)
            rows += row
        }
        return rows.sortedBy { row -> row.minOf { bounds(it).top } }
            .flatMap { row -> row.sortedWith(compareBy<T> { bounds(it).left }.thenBy(order)) }
    }

    private fun <T> splitColumns(items: List<T>, bounds: (T) -> OcrBounds): List<List<T>>? {
        if (items.size < 3) return null
        val sorted = items.sortedBy { bounds(it).left }
        val averageHeight = sorted.map { bounds(it).height }.filter { it > 0 }.averageOrNull() ?: return null
        val candidates = (0 until sorted.lastIndex).map { index ->
            val leftRight = sorted.take(index + 1).maxOf { bounds(it).right }
            val rightLeft = sorted.drop(index + 1).minOf { bounds(it).left }
            Triple(index, rightLeft - leftRight, leftRight)
        }
        val split = candidates.maxByOrNull { it.second } ?: return null
        if (split.second <= averageHeight * 1.5) return null
        val left = sorted.take(split.first + 1)
        val right = sorted.drop(split.first + 1)
        if (left.isEmpty() || right.isEmpty()) return null
        if ((left.size < 2 || right.size < 2) && split.second <= averageHeight * 3.0) return null
        val verticalCoexistence = left.any { a -> right.any { b -> bounds(a).verticallyOverlaps(bounds(b)) } }
        if (!verticalCoexistence) return null
        return listOf(left, right).map { column ->
            column.sortedWith(compareBy<T> { bounds(it).top }.thenBy { bounds(it).left })
        }
    }

    private fun verticalGap(first: OcrBounds?, second: OcrBounds?): Int? {
        if (first == null || second == null) return null
        if (first.verticallyOverlaps(second)) return 0
        return minOf(abs(second.top - first.bottom), abs(first.top - second.bottom))
    }

    private fun List<Int>.averageOrNull(): Double? = if (isEmpty()) null else average()
}
