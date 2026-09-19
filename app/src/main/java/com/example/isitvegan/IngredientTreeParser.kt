package com.example.isitvegan

import java.math.BigDecimal

enum class IngredientNodeKind { LEAF, COMPOSITE, ADDITIVE }

data class IngredientNode(
    val rawText: String,
    val normalizedText: String,
    val quantityPercent: BigDecimal? = null,
    val functionalClass: String? = null,
    val kind: IngredientNodeKind,
    val children: List<IngredientNode> = emptyList(),
    val isSectionHeading: Boolean = false
)

/** Builds an ingredient tree without consulting the ingredient knowledge base. */
internal object IngredientTreeParser {
    private data class Group(
        val start: Int,
        val endInclusive: Int,
        val opener: Char,
        val content: String
    )

    private val quantityPattern = Regex(
        "(?<![\\p{L}\\d])([0-9]+(?:[.,][0-9]+)?)\\s*%",
        RegexOption.IGNORE_CASE
    )
    private val quantityOnlyPattern = Regex("^\\s*[0-9]+(?:[.,][0-9]+)?\\s*%\\s*$")
    private val containsPrefix = Regex(
        "(?i)^\\s*(?:contient|bevat|contains|enthält)\\s*:\\s*"
    )
    private val qualificationPrefix = Regex(
        "(?i)^\\s*(?:non\\s+hydrog[ée]n[ée]e?|origine|origin|provenance)\\b"
    )
    private val protectedDesignation = Regex("(?i)\\b(?:AOP|IGP|DOP|PDO)\\b")
    private val eNumberWithSpace = Regex("(?i)\\bE\\s+(?=\\d)")
    private val insNumberWithSpace = Regex("(?i)\\bINS\\s+(?=\\d)")
    private val vitaminWithSpace = Regex("(?i)\\b[BDK]\\s+(?=\\d)")

    fun parse(text: String): List<IngredientNode> = parseList(text)

    private fun parseList(text: String): List<IngredientNode> {
        val segments = splitAtCurrentDepth(text)
        val result = mutableListOf<IngredientNode>()
        var index = 0
        while (index < segments.size) {
            val section = sectionParts(segments[index])
            if (section == null) {
                result += parseSegment(segments[index])
                index++
                continue
            }

            val content = mutableListOf<String>()
            section.second.takeIf(String::isNotBlank)?.let(content::add)
            var next = index + 1
            while (next < segments.size && sectionParts(segments[next]) == null) {
                content += segments[next]
                next++
            }
            val children = parseList(content.joinToString(", "))
            val (name, quantity) = extractNameAndQuantity(section.first)
            if (name.isNotBlank() && children.isNotEmpty()) {
                result += node(name, quantity, IngredientNodeKind.COMPOSITE, children, true)
            }
            index = next
        }
        return result
    }

    private fun parseSegment(rawSegment: String): List<IngredientNode> {
        val segment = rawSegment.trim().trim('*', ' ').trimEnd('.').trim()
        if (TextNormalizer.normalize(segment).isBlank()) return emptyList()

        val withoutContains = segment.replace(containsPrefix, "").trim()
        if (withoutContains != segment) return parseList(withoutContains)

        parseFunctionalClass(segment)?.let { return it }

        val colon = topLevelColon(segment)
        if (colon >= 0) {
            val heading = segment.substring(0, colon).trim()
            val content = segment.substring(colon + 1).trim()
            if (heading.isNotBlank() && content.isNotBlank()) {
                val (name, quantity) = extractNameAndQuantity(heading)
                val children = parseList(content)
                if (name.isNotBlank() && children.isNotEmpty()) {
                    return listOf(node(name, quantity, IngredientNodeKind.COMPOSITE, children, true))
                }
            }
        }

        val groups = topLevelGroups(segment)
        if (groups.isEmpty()) {
            val (name, quantity) = extractNameAndQuantity(segment)
            return name.takeIf(String::isNotBlank)
                ?.let { listOf(node(it, quantity, IngredientNodeKind.LEAF)) }
                ?: emptyList()
        }

        val labelBeforeFirstGroup = segment.substring(0, groups.first().start).trim()
        val compositionGroups = groups.filter { group ->
            !quantityOnlyPattern.matches(group.content) &&
                (group.opener == '[' || isCompositionParenthesis(labelBeforeFirstGroup, group.content))
        }
        if (compositionGroups.isEmpty()) {
            val (name, quantity) = extractNameAndQuantity(segment)
            return name.takeIf(String::isNotBlank)
                ?.let { listOf(node(it, quantity, IngredientNodeKind.LEAF)) }
                ?: emptyList()
        }

        val compositionRanges = compositionGroups.map { it.start..it.endInclusive }
        val label = buildString {
            segment.forEachIndexed { index, character ->
                if (compositionRanges.none { index in it }) append(character)
            }
        }
        val (name, quantity) = extractNameAndQuantity(label)
        val children = compositionGroups.flatMap { parseList(it.content) }
        if (name.isBlank()) return children
        if (children.isEmpty()) return listOf(node(name, quantity, IngredientNodeKind.LEAF))
        return listOf(node(name, quantity, IngredientNodeKind.COMPOSITE, children))
    }

    private fun node(
        text: String,
        quantity: BigDecimal?,
        kind: IngredientNodeKind,
        children: List<IngredientNode> = emptyList(),
        isSectionHeading: Boolean = false,
        functionalClass: String? = null
    ): IngredientNode {
        val cleaned = normalizeIdentifiers(text.trim().replace(Regex("\\s+"), " "))
        return IngredientNode(
            rawText = cleaned,
            normalizedText = TextNormalizer.normalize(cleaned),
            quantityPercent = quantity,
            functionalClass = functionalClass,
            kind = kind,
            children = children,
            isSectionHeading = isSectionHeading
        )
    }

    private fun extractNameAndQuantity(text: String): Pair<String, BigDecimal?> {
        val quantity = quantityPattern.find(text)?.groupValues?.get(1)
            ?.replace(',', '.')?.toBigDecimalOrNull()
        val name = text.replace(quantityPattern, " ")
            .replace(Regex("\\(\\s*\\)"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return name to quantity
    }

    private fun isCompositionParenthesis(parentLabel: String, content: String): Boolean {
        if (protectedDesignation.containsMatchIn(parentLabel)) return false
        if (containsPrefix.containsMatchIn(content)) return true
        if (splitAtCurrentDepth(content).size > 1) return true
        if (topLevelGroups(content).any {
                it.opener == '[' || isCompositionParenthesis(content.substring(0, it.start), it.content)
            }
        ) return true
        if (qualificationPrefix.containsMatchIn(content)) return false
        val letters = content.filter(Char::isLetter)
        if (letters.length > 1 && letters.all(Char::isUpperCase)) return true
        val contentWords = TextNormalizer.normalize(content).split(Regex("\\s+")).filter(String::isNotBlank)
        return contentWords.size == 1
    }

    private fun splitAtCurrentDepth(text: String): List<String> {
        val parts = mutableListOf<String>()
        var start = 0
        var depth = 0
        text.forEachIndexed { index, character ->
            when (character) {
                '(', '[' -> depth++
                ')', ']' -> depth = (depth - 1).coerceAtLeast(0)
                ',', ';', '\n' -> if (depth == 0 &&
                    !(character == ',' && index > 0 && index < text.lastIndex &&
                        text[index - 1].isDigit() && text[index + 1].isDigit()) &&
                    !(character == '\n' && nextNonWhitespace(text, index + 1) in listOf('(', '['))
                ) {
                    parts += text.substring(start, index)
                    start = index + 1
                }
                '.' -> if (depth == 0 && isSentenceBoundary(text, index)) {
                    parts += text.substring(start, index)
                    start = index + 1
                }
            }
        }
        parts += text.substring(start)
        return parts.map(String::trim).filter(String::isNotBlank)
    }

    private fun isSentenceBoundary(text: String, index: Int): Boolean {
        val decimal = index > 0 && index < text.lastIndex &&
            text[index - 1].isDigit() && text[index + 1].isDigit()
        return !decimal && (index == text.lastIndex || text.getOrNull(index + 1)?.isWhitespace() == true)
    }

    private fun topLevelGroups(text: String): List<Group> {
        val groups = mutableListOf<Group>()
        val stack = mutableListOf<Pair<Char, Int>>()
        text.forEachIndexed { index, character ->
            when (character) {
                '(', '[' -> stack += character to index
                ')', ']' -> if (stack.isNotEmpty()) {
                    val (opener, start) = stack.removeAt(stack.lastIndex)
                    if (stack.isEmpty()) {
                        groups += Group(start, index, opener, text.substring(start + 1, index))
                    }
                }
            }
        }
        return groups
    }

    private fun topLevelColon(text: String): Int {
        var depth = 0
        text.forEachIndexed { index, character ->
            when (character) {
                '(', '[' -> depth++
                ')', ']' -> depth = (depth - 1).coerceAtLeast(0)
                ':' -> if (depth == 0) return index
            }
        }
        return -1
    }

    private fun sectionParts(text: String): Pair<String, String>? {
        if (parseFunctionalClass(text) != null || containsPrefix.containsMatchIn(text)) return null
        val colon = topLevelColon(text)
        if (colon < 0) return null
        val heading = text.substring(0, colon).trim()
        val content = text.substring(colon + 1).trim()
        val isSection = content.isBlank() || quantityPattern.containsMatchIn(heading)
        return if (heading.isNotBlank() && isSection) heading to content else null
    }

    private fun nextNonWhitespace(text: String, start: Int): Char? {
        for (index in start until text.length) {
            if (!text[index].isWhitespace()) return text[index]
        }
        return null
    }

    private fun parseFunctionalClass(segment: String): List<IngredientNode>? {
        val colon = topLevelColon(segment)
        if (colon >= 0) {
            val className = segment.substring(0, colon).trim()
            val designation = segment.substring(colon + 1).trim()
            if (FunctionalClassLexicon.contains(className) && designation.isNotBlank()) {
                return additiveNodes(className, designation)
            }
        }

        val groups = topLevelGroups(segment)
        val group = groups.singleOrNull()?.takeIf { it.endInclusive == segment.lastIndex }
            ?: return null
        val className = segment.substring(0, group.start).trim()
        if (!FunctionalClassLexicon.contains(className) || group.content.isBlank()) return null
        return additiveNodes(className, group.content)
    }

    private fun additiveNodes(className: String, designationText: String): List<IngredientNode> {
        val designations = splitAtCurrentDepth(designationText)
        val children = designations.map { designation ->
            val cleaned = normalizeIdentifiers(designation.trim())
            IngredientNode(
                rawText = cleaned,
                normalizedText = TextNormalizer.normalize(cleaned),
                functionalClass = className,
                kind = IngredientNodeKind.ADDITIVE
            )
        }
        if (children.size == 1) return children
        return listOf(
            node(
                text = className,
                quantity = null,
                kind = IngredientNodeKind.COMPOSITE,
                children = children,
                functionalClass = className
            )
        )
    }

    private fun normalizeIdentifiers(text: String): String = text
        .replace(eNumberWithSpace) { it.value.replace(Regex("\\s+"), "") }
        .replace(insNumberWithSpace) { it.value.replace(Regex("\\s+"), "") }
        .replace(vitaminWithSpace) { it.value.replace(Regex("\\s+"), "") }
}
