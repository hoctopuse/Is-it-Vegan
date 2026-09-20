package com.example.isitvegan

import java.math.BigDecimal

enum class IngredientNodeKind { LEAF, COMPOSITE, ADDITIVE }

data class IngredientNode(
    val rawText: String,
    val normalizedText: String,
    val quantityPercent: BigDecimal? = null,
    val functionalClass: String? = null,
    val functionalClassCanonical: FunctionalClass? = null,
    val isNano: Boolean = false,
    val nanoText: String? = null,
    val variableProportions: Boolean = false,
    val variableProportionsText: String? = null,
    val hasAlternatives: Boolean = false,
    val alternativesText: String? = null,
    val sourceClaim: SourceClaim = SourceClaim.UNSPECIFIED,
    val sourceClaimText: String? = null,
    val kind: IngredientNodeKind,
    val children: List<IngredientNode> = emptyList(),
    val isSectionHeading: Boolean = false
)

/** Builds an ingredient tree without consulting the ingredient knowledge base. */
internal object IngredientTreeParser {
    private data class StructuralMetadata(
        val text: String,
        val isNano: Boolean,
        val nanoText: String?,
        val variableProportions: Boolean,
        val variableProportionsText: String?,
        val hasAlternatives: Boolean,
        val alternativesText: String?,
        val sourceClaim: SourceClaim,
        val sourceClaimText: String?
    )
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
        "(?i)^\\s*(?:contient|bevat|contains|enthält|contiene)(?:\\s*:\\s*|\\s+" +
            "(?:(?:de\\s+l['’]|de|du|des)\\s+)?)"
    )
    private val qualificationPrefix = Regex(
        "(?i)^\\s*(?:non\\s+hydrog[ée]n[ée]e?|origine|origin|provenance)\\b"
    )
    private val protectedDesignation = Regex("(?i)\\b(?:AOP|IGP|DOP|PDO)\\b")
    private val eNumberWithSpace = Regex("(?i)\\bE\\s+(?=\\d)")
    private val insNumberWithSpace = Regex("(?i)\\bINS\\s+(?=\\d)")
    private val vitaminWithSpace = Regex("(?i)\\b[BDK]\\s+(?=\\d)")
    private val nanoQualifier = Regex("(?i)(?:\\[\\s*nano\\s*]|\\(\\s*nano\\s*\\))")
    private val variableProportionQualifier = Regex(
        "(?i)\\b(?:en\\s+proportions?\\s+variables?|in\\s+(?:varying|variable)\\s+proportions|" +
            "in\\s+wisselende\\s+verhoudingen|in\\s+veränderlichen\\s+Gewichtsanteilen|" +
            "en\\s+proporci(?:ón|ones)\\s+variables?)\\b"
    )
    private val alternativeSeparator = Regex("(?i)\\s+(?:et\\s*/\\s*ou|and\\s*/\\s*or|en\\s*/\\s*of|und\\s*/\\s*oder|y\\s*/\\s*o)\\s+")
    private val additiveContinuation = Regex("(?i)^\\s*(?:E|INS)\\s*\\d[0-9a-z().-]*\\s*$")

    fun parse(text: String): List<IngredientNode> = parseList(text)

    private fun parseList(text: String): List<IngredientNode> {
        val segments = splitAtCurrentDepth(text)
        val result = mutableListOf<IngredientNode>()
        var index = 0
        while (index < segments.size) {
            val functionalGroup = functionalClassWithContinuations(segments, index)
            if (functionalGroup != null) {
                result += functionalGroup.first
                index += functionalGroup.second
                continue
            }
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

    private fun functionalClassWithContinuations(
        segments: List<String>,
        index: Int
    ): Pair<List<IngredientNode>, Int>? {
        val metadata = extractStructuralMetadata(segments[index])
        val colon = topLevelColon(metadata.text)
        if (colon < 0) return null
        val functionalClass = FunctionalClassLexicon.match(metadata.text.substring(0, colon).trim())
            ?: return null
        val firstDesignation = metadata.text.substring(colon + 1).trim()
        if (firstDesignation.isBlank()) return null
        val designations = mutableListOf(firstDesignation)
        var next = index + 1
        while (next < segments.size && additiveContinuation.matches(segments[next])) {
            designations += segments[next]
            next++
        }
        if (designations.size == 1) return null
        return additiveNodes(functionalClass, designations.joinToString(", "), metadata) to (next - index)
    }

    private fun parseSegment(rawSegment: String): List<IngredientNode> {
        val raw = rawSegment.trim().trim('*', ' ').trimEnd('.').trim()
        val metadata = extractStructuralMetadata(raw)
        val segment = metadata.text
        if (TextNormalizer.normalize(segment).isBlank()) return emptyList()

        val withoutContains = segment.replace(containsPrefix, "").trim()
        if (withoutContains != segment) return parseList(withoutContains)

        parseFunctionalClass(segment, metadata)?.let { return it }

        val colon = topLevelColon(segment)
        if (colon >= 0) {
            val heading = segment.substring(0, colon).trim()
            val content = segment.substring(colon + 1).trim()
            if (heading.isNotBlank() && content.isNotBlank()) {
                val (name, quantity) = extractNameAndQuantity(heading)
                val children = parseList(content)
                if (name.isNotBlank() && children.isNotEmpty()) {
                    return listOf(node(name, quantity, IngredientNodeKind.COMPOSITE, children, true, metadata = metadata))
                }
            }
        }

        val groups = topLevelGroups(segment)
        if (groups.isEmpty()) {
            val (name, quantity) = extractNameAndQuantity(segment)
            return name.takeIf(String::isNotBlank)
                ?.let { listOf(node(it, quantity, IngredientNodeKind.LEAF, metadata = metadata)) }
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
                ?.let { listOf(node(it, quantity, IngredientNodeKind.LEAF, metadata = metadata)) }
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
        if (children.isEmpty()) return listOf(node(name, quantity, IngredientNodeKind.LEAF, metadata = metadata))
        return listOf(node(name, quantity, IngredientNodeKind.COMPOSITE, children, metadata = metadata))
    }

    private fun node(
        text: String,
        quantity: BigDecimal?,
        kind: IngredientNodeKind,
        children: List<IngredientNode> = emptyList(),
        isSectionHeading: Boolean = false,
        functionalClass: String? = null,
        functionalClassCanonical: FunctionalClass? = null,
        metadata: StructuralMetadata? = null
    ): IngredientNode {
        val cleaned = normalizeIdentifiers(text.trim().replace(Regex("\\s+"), " "))
        return IngredientNode(
            rawText = cleaned,
            normalizedText = TextNormalizer.normalize(cleaned),
            quantityPercent = quantity,
            functionalClass = functionalClass,
            functionalClassCanonical = functionalClassCanonical,
            isNano = metadata?.isNano == true,
            nanoText = metadata?.nanoText,
            variableProportions = metadata?.variableProportions == true,
            variableProportionsText = metadata?.variableProportionsText,
            hasAlternatives = metadata?.hasAlternatives == true,
            alternativesText = metadata?.alternativesText,
            sourceClaim = metadata?.sourceClaim ?: SourceClaim.UNSPECIFIED,
            sourceClaimText = metadata?.sourceClaimText,
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
        val source = replaceTopLevelAlternatives(text)
        val parts = mutableListOf<String>()
        var start = 0
        var depth = 0
        source.forEachIndexed { index, character ->
            when (character) {
                '(', '[' -> depth++
                ')', ']' -> depth = (depth - 1).coerceAtLeast(0)
                ',', ';', '\n' -> if (depth == 0 &&
                    !(character == ',' && index > 0 && index < source.lastIndex &&
                        source[index - 1].isDigit() && source[index + 1].isDigit()) &&
                    !(character == '\n' && nextNonWhitespace(source, index + 1) in listOf('(', '['))
                ) {
                    parts += source.substring(start, index)
                    start = index + 1
                }
                '.' -> if (depth == 0 && isSentenceBoundary(source, index)) {
                    parts += source.substring(start, index)
                    start = index + 1
                }
            }
        }
        parts += source.substring(start)
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
        val metadata = extractStructuralMetadata(text)
        if (parseFunctionalClass(metadata.text, metadata) != null || containsPrefix.containsMatchIn(text)) return null
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

    private fun parseFunctionalClass(
        segment: String,
        metadata: StructuralMetadata
    ): List<IngredientNode>? {
        val colon = topLevelColon(segment)
        if (colon >= 0) {
            val className = segment.substring(0, colon).trim()
            val designation = segment.substring(colon + 1).trim()
            val functionalClass = FunctionalClassLexicon.match(className)
            if (functionalClass != null && designation.isNotBlank()) {
                return additiveNodes(functionalClass, designation, metadata)
            }
        }

        val groups = topLevelGroups(segment)
        val group = groups.singleOrNull()?.takeIf { it.endInclusive == segment.lastIndex }
        if (group != null) {
            val className = segment.substring(0, group.start).trim()
            val functionalClass = FunctionalClassLexicon.match(className)
            if (functionalClass != null && group.content.isNotBlank()) {
                return additiveNodes(functionalClass, group.content, metadata)
            }
        }

        val prefix = FunctionalClassLexicon.matchPrefix(segment) ?: return null
        if (prefix.designation.isNotBlank()) return additiveNodes(prefix, prefix.designation, metadata)
        if (prefix.canonical == FunctionalClass.MODIFIED_STARCH) {
            val (name, quantity) = extractNameAndQuantity(segment)
            return listOf(
                node(
                    name, quantity, IngredientNodeKind.LEAF,
                    functionalClass = prefix.originalText,
                    functionalClassCanonical = prefix.canonical,
                    metadata = metadata
                )
            )
        }
        return emptyList()
    }

    private fun additiveNodes(
        functionalClass: FunctionalClassMatch,
        designationText: String,
        metadata: StructuralMetadata
    ): List<IngredientNode> {
        val designations = splitAtCurrentDepth(designationText)
        val children = designations.map { designation ->
            val cleaned = normalizeIdentifiers(designation.trim())
            IngredientNode(
                rawText = cleaned,
                normalizedText = TextNormalizer.normalize(cleaned),
                functionalClass = functionalClass.originalText,
                functionalClassCanonical = functionalClass.canonical,
                isNano = metadata.isNano,
                nanoText = metadata.nanoText,
                variableProportions = metadata.variableProportions,
                variableProportionsText = metadata.variableProportionsText,
                hasAlternatives = metadata.hasAlternatives,
                alternativesText = metadata.alternativesText,
                sourceClaim = metadata.sourceClaim,
                sourceClaimText = metadata.sourceClaimText,
                kind = IngredientNodeKind.ADDITIVE
            )
        }
        if (children.size == 1) return children
        return listOf(
            node(
                text = functionalClass.originalText,
                quantity = null,
                kind = IngredientNodeKind.COMPOSITE,
                children = children,
                functionalClass = functionalClass.originalText,
                functionalClassCanonical = functionalClass.canonical,
                metadata = metadata
            )
        )
    }

    private fun extractStructuralMetadata(value: String): StructuralMetadata {
        val origin = SourceClaimLexicon.extract(value)
        val nano = nanoQualifier.find(origin.text)
        val variable = variableProportionQualifier.find(origin.text)
        val alternatives = alternativeSeparator.find(origin.text)
        val cleaned = origin.text
            .replace(nanoQualifier, " ")
            .replace(variableProportionQualifier, " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return StructuralMetadata(
            text = cleaned,
            isNano = nano != null,
            nanoText = nano?.value,
            variableProportions = variable != null,
            variableProportionsText = variable?.value,
            hasAlternatives = alternatives != null,
            alternativesText = alternatives?.value?.trim(),
            sourceClaim = origin.claim,
            sourceClaimText = origin.detectedPhrase
        )
    }

    private fun replaceTopLevelAlternatives(value: String): String {
        val result = StringBuilder(value)
        var depth = 0
        var cursor = 0
        while (cursor < value.length) {
            when (value[cursor]) {
                '(', '[' -> depth++
                ')', ']' -> depth = (depth - 1).coerceAtLeast(0)
            }
            if (depth == 0) {
                val match = alternativeSeparator.find(value, cursor)
                    ?.takeIf { it.range.first == cursor }
                if (match != null) {
                    result.setCharAt(match.range.first, ',')
                    for (index in match.range.first + 1..match.range.last) result.setCharAt(index, ' ')
                    cursor = match.range.last + 1
                    continue
                }
            }
            cursor++
        }
        return result.toString()
    }

    private fun normalizeIdentifiers(text: String): String = text
        .replace(eNumberWithSpace) { it.value.replace(Regex("\\s+"), "") }
        .replace(insNumberWithSpace) { it.value.replace(Regex("\\s+"), "") }
        .replace(vitaminWithSpace) { it.value.replace(Regex("\\s+"), "") }
}
