package com.example.isitvegan

/** A source-preserving ingredient occurrence from one OCR language block. */
data class IngredientOccurrence(
    val id: String,
    val blockId: String,
    val language: LabelLanguage,
    val rawText: String,
    val order: Int,
    val parentheticalText: String? = null,
    val canonicalId: String? = null,
    val status: VeganStatus? = null,
    val depth: Int = 0,
    val parentOrder: Int? = null
)

/** Evidence must come from an explicit concept/translation mapping, never list position alone. */
enum class AlignmentBasis {
    CANONICAL_ID,
    EXPLICIT_TRANSLATION,
    UNIQUE_ANCHORED_SEQUENCE,
    POSITION_ONLY,
    APPROXIMATE,
    AMBIGUOUS
}

data class AlignmentLink(
    val firstOccurrenceId: String,
    val secondOccurrenceId: String,
    val basis: AlignmentBasis
)

enum class AlignmentKind {
    CORROBORATION,
    CORRECTION_PROPOSAL,
    CONFLICT,
    AMBIGUOUS,
    UNRESOLVED
}

/**
 * A review result, not a parsed ingredient, match, or verdict. The original occurrences remain
 * intact; in particular a proposed canonical id does not fill the source occurrence's id/status.
 */
data class IngredientAlignmentResult(
    val first: IngredientOccurrence,
    val second: IngredientOccurrence,
    val kind: AlignmentKind,
    val proposedCanonicalId: String? = null,
    val proposedConceptName: String? = null
) {
    val proposalKey: String get() = "${first.id}->${second.id}:${proposedCanonicalId.orEmpty()}"
}

data class MultilingualAlignmentEvaluation(
    /** Every source occurrence, including unlinked and unknown terms, in caller-provided order. */
    val occurrences: List<IngredientOccurrence>,
    val links: List<IngredientAlignmentResult>
)

/**
 * Pure prototype for evaluating caller-supplied cross-language links. This deliberately does not
 * infer links from position or spelling and is not called by the production analysis path.
 */
object MultilingualIngredientAlignment {
    fun generate(
        blocks: List<LanguageBlock>,
        database: List<Ingredient>,
        lexicon: MultilingualIngredientLexicon,
        originRules: OriginQualifierRuleSet
    ): MultilingualAlignmentEvaluation {
        val eligibleBlocks = blocks.filter { it.hasIngredientHeading }
        if (eligibleBlocks.map { it.language }.distinct().size < 2) {
            return MultilingualAlignmentEvaluation(emptyList(), emptyList())
        }
        val matcher = IngredientMatcher(database)
        val occurrencesByBlock = eligibleBlocks.map { block ->
            block to parseOccurrences(block, database, lexicon, originRules, matcher)
        }
        val occurrences = occurrencesByBlock.flatMap { it.second }
        val links = mutableListOf<AlignmentLink>()

        for (firstIndex in occurrencesByBlock.indices) {
            for (secondIndex in firstIndex + 1 until occurrencesByBlock.size) {
                val (firstBlock, firstItems) = occurrencesByBlock[firstIndex]
                val (secondBlock, secondItems) = occurrencesByBlock[secondIndex]
                if (firstBlock.language == secondBlock.language) continue
                val firstSequence = firstItems.filter { it.canonicalId != null || it.depth == 0 }
                val secondSequence = secondItems.filter { it.canonicalId != null || it.depth == 0 }

                val firstByConcept = firstItems.filter { it.canonicalId != null }.groupBy { it.canonicalId }
                val secondByConcept = secondItems.filter { it.canonicalId != null }.groupBy { it.canonicalId }
                val uniqueShared = firstByConcept.keys.intersect(secondByConcept.keys).mapNotNull { concept ->
                    val left = firstByConcept[concept].orEmpty().singleOrNull()
                    val right = secondByConcept[concept].orEmpty().singleOrNull()
                    if (left == null || right == null) null else concept to (left to right)
                }
                uniqueShared.forEach { (_, pair) ->
                    links += AlignmentLink(pair.first.id, pair.second.id, AlignmentBasis.CANONICAL_ID)
                }

                // Shared anchors provide context only. Even one item per interval is not proof
                // of equivalence: translations may omit or substitute ingredients.
                val orderedAnchors = uniqueShared.sortedBy { it.second.first.order }
                val rightPositions = orderedAnchors.map { it.second.second.order }
                if (rightPositions.zipWithNext().any { (left, right) -> left >= right }) continue
                orderedAnchors.zipWithNext().forEach { (leftAnchor, rightAnchor) ->
                    val firstWindow = firstSequence.filter { it.order in (leftAnchor.second.first.order + 1) until rightAnchor.second.first.order }
                    val secondWindow = secondSequence.filter { it.order in (leftAnchor.second.second.order + 1) until rightAnchor.second.second.order }
                    if (firstWindow.size == 1 && secondWindow.size == 1) {
                        val left = firstWindow.single()
                        val right = secondWindow.single()
                        when {
                            left.canonicalId == null && right.canonicalId != null && supportsLexicalProposal(left.language) ->
                                links += AlignmentLink(left.id, right.id, AlignmentBasis.AMBIGUOUS)
                            right.canonicalId == null && left.canonicalId != null && supportsLexicalProposal(right.language) ->
                                links += AlignmentLink(right.id, left.id, AlignmentBasis.AMBIGUOUS)
                            left.canonicalId != null && right.canonicalId != null && left.canonicalId != right.canonicalId ->
                                links += AlignmentLink(left.id, right.id, AlignmentBasis.AMBIGUOUS)
                        }
                    } else if (firstWindow.isNotEmpty() && secondWindow.isNotEmpty()) {
                        firstWindow.filter { it.canonicalId == null }.forEach { unknown ->
                            secondWindow.filter { it.canonicalId != null }.forEach { candidate ->
                                links += AlignmentLink(unknown.id, candidate.id, AlignmentBasis.AMBIGUOUS)
                            }
                        }
                        secondWindow.filter { it.canonicalId == null }.forEach { unknown ->
                            firstWindow.filter { it.canonicalId != null }.forEach { candidate ->
                                links += AlignmentLink(unknown.id, candidate.id, AlignmentBasis.AMBIGUOUS)
                            }
                        }
                    }
                }
            }
        }
        val evaluation = evaluate(occurrences, links.distinct())
        return evaluation.copy(
            links = evaluation.links.map { link ->
                val name = link.proposedCanonicalId?.let { id -> database.firstOrNull { it.id == id }?.name }
                link.copy(proposedConceptName = name)
            }
        )
    }

    fun evaluate(
        occurrences: List<IngredientOccurrence>,
        links: List<AlignmentLink>
    ): MultilingualAlignmentEvaluation {
        val byId = occurrences.associateBy { it.id }
        require(byId.size == occurrences.size) { "Ingredient occurrence ids must be unique." }
        val results = links.map { link ->
            val first = requireNotNull(byId[link.firstOccurrenceId]) {
                "Unknown ingredient occurrence: ${link.firstOccurrenceId}"
            }
            val second = requireNotNull(byId[link.secondOccurrenceId]) {
                "Unknown ingredient occurrence: ${link.secondOccurrenceId}"
            }
            require(first.id != second.id) { "An occurrence cannot be aligned with itself." }
            require(first.language != second.language) { "Alignment requires distinct language blocks." }
            evaluateLink(first, second, link.basis)
        }
        return MultilingualAlignmentEvaluation(occurrences.toList(), results)
    }

    private fun evaluateLink(
        first: IngredientOccurrence,
        second: IngredientOccurrence,
        basis: AlignmentBasis
    ): IngredientAlignmentResult {
        val trusted = basis == AlignmentBasis.CANONICAL_ID ||
            basis == AlignmentBasis.EXPLICIT_TRANSLATION
        if (!trusted) return IngredientAlignmentResult(first, second, AlignmentKind.AMBIGUOUS)

        val firstId = first.canonicalId
        val secondId = second.canonicalId
        val kind = when {
            firstId != null && firstId == secondId && first.status != null && second.status != null && first.status != second.status ->
                AlignmentKind.CONFLICT
            firstId != null && firstId == secondId -> AlignmentKind.CORROBORATION
            firstId != null && secondId != null -> AlignmentKind.CONFLICT
            basis == AlignmentBasis.EXPLICIT_TRANSLATION && firstId == null && secondId != null ->
                AlignmentKind.CORRECTION_PROPOSAL
            else -> AlignmentKind.UNRESOLVED
        }
        val proposal = if (kind == AlignmentKind.CORRECTION_PROPOSAL) firstId ?: secondId else null
        return IngredientAlignmentResult(first, second, kind, proposal)
    }

    private fun parseOccurrences(
        block: LanguageBlock,
        database: List<Ingredient>,
        lexicon: MultilingualIngredientLexicon,
        originRules: OriginQualifierRuleSet,
        matcher: IngredientMatcher
    ): List<IngredientOccurrence> {
        val ingredientText = LabelSectionExtractor.extract(block).ingredientsText.orEmpty()
        if (ingredientText.isBlank()) return emptyList()
        val tree = IngredientTreeParser.parse(ingredientText, originRules)
        var sourceSearchFrom = 0
        return IngredientTokenizer.flatten(tree)
            .filter { it.kind != NodeKind.SECTION_HEADING }
            .map { token ->
                val sourceItem = sourceItemText(ingredientText, token.text, sourceSearchFrom)
                sourceSearchFrom = sourceItem.searchFrom
                val lexical = lexicon.resolve(token.text, block.language, database)
                var canonicalId = lexical.canonicalId?.takeIf { lexical.canonicalAvailable == true }
                var matched = canonicalId?.let { id -> database.filter { it.id == id } }.orEmpty()
                if (canonicalId == null && supportsLexicalProposal(block.language)) {
                    val match = matcher.match(token)
                    if (match.resolution == MatchResolution.EXACT && match.ingredients.size == 1) {
                        matched = match.ingredients
                        canonicalId = matched.single().id
                    }
                }
                val qualification = originRules.extractAttached(token.text).qualification
                    ?: token.originQualification
                val statuses = matched.map { originRules.resolve(it, qualification).effectiveStatus }.distinct()
                val parenthetical = sourceItem.text.indexOf('(').takeIf { it >= 0 }
                    ?.let(sourceItem.text::substring)
                IngredientOccurrence(
                    id = "${block.id}:${token.order}",
                    blockId = block.id,
                    language = block.language,
                    rawText = sourceItem.text,
                    order = token.order,
                    parentheticalText = parenthetical,
                    canonicalId = canonicalId,
                    status = statuses.singleOrNull(),
                    depth = token.depth,
                    parentOrder = token.parentOrder
                )
            }
    }

    private data class SourceItemText(val text: String, val searchFrom: Int)

    private fun sourceItemText(source: String, parsedText: String, searchFrom: Int): SourceItemText {
        val start = source.indexOf(parsedText, searchFrom, ignoreCase = true)
        if (start < 0) return SourceItemText(parsedText, searchFrom)
        var contentEnd = start + parsedText.length
        var cursor = contentEnd
        while (cursor < source.length && source[cursor].isWhitespace()) cursor++
        if (cursor < source.length && source[cursor] == '(') {
            var depth = 0
            var closing = -1
            for (index in cursor until source.length) {
                when (source[index]) {
                    '(' -> depth++
                    ')' -> {
                        depth--
                        if (depth == 0) { closing = index; break }
                    }
                }
            }
            if (closing >= 0) contentEnd = closing + 1
        }
        return SourceItemText(source.substring(start, contentEnd), start + parsedText.length)
    }

    private fun supportsLexicalProposal(language: LabelLanguage): Boolean = language !in setOf(
        LabelLanguage.SWEDISH,
        LabelLanguage.NORWEGIAN
    )
}
