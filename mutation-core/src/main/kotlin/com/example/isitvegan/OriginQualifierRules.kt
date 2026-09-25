package com.example.isitvegan

enum class QualifierVeganStatus { VEGAN, NOT_VEGAN, UNCERTAIN }
enum class QualifierVegetarianStatus { VEGETARIAN_OR_VEGAN, VEGETARIAN, NON_VEGETARIAN, UNCERTAIN }
enum class QualifierAttachmentForm { PARENTHETICAL, DIRECT_SUFFIX }
enum class ParenthesisPolicy { IGNORE_CONTENT_FOR_VERDICT }

data class OriginQualification(
    val ruleId: String,
    val outcomeId: String,
    val targetIngredientIds: Set<String>,
    val requiredBaseStatus: VeganStatus,
    val detectedPhrase: String,
    val veganStatus: QualifierVeganStatus,
    val vegetarianStatus: QualifierVegetarianStatus,
    val reason: String
)

data class OriginQualifierExtraction(
    val text: String,
    val qualification: OriginQualification? = null
)

data class ProtectedExpressionMatch(
    val id: String,
    val ingredientId: String,
    val matcherAlias: String,
    val knownStatus: VeganStatus,
    val parenthesisPolicy: ParenthesisPolicy
)

data class OriginResolution(
    val baseStatus: VeganStatus,
    val effectiveStatus: VeganStatus,
    val explanation: String?,
    val explicitlyNotVegan: Boolean = false
)

data class OriginRulesLoadResult(
    val rules: OriginQualifierRuleSet,
    val errors: List<String>
) {
    val isValid: Boolean get() = errors.isEmpty()
}

/**
 * Applies origin knowledge without knowing any concrete ingredient, E-number or qualifier.
 * Concrete targets, wording, outcomes and parenthesis policies all come from JSON.
 */
class OriginQualifierRuleSet private constructor(
    private val rules: List<Rule>,
    private val protectedExpressions: List<ProtectedExpression>
) {
    private data class Outcome(
        val id: String,
        val qualifiers: List<String>,
        val attachmentForms: Set<QualifierAttachmentForm>,
        val veganStatus: QualifierVeganStatus,
        val vegetarianStatus: QualifierVegetarianStatus,
        val reason: String
    )

    private data class Rule(
        val id: String,
        val ingredientIds: Set<String>,
        val targets: List<String>,
        val unresolvedStatus: VeganStatus,
        val outcomes: List<Outcome>
    )

    private data class ProtectedExpression(
        val id: String,
        val ingredientId: String,
        val aliases: List<String>,
        val matcherAlias: String,
        val knownStatus: VeganStatus,
        val parenthesisPolicy: ParenthesisPolicy
    )

    fun extractAttached(value: String): OriginQualifierExtraction {
        val trimmed = value.trim()
        val candidates = mutableListOf<OriginQualifierExtraction>()
        val opening = trailingParenthesisOpening(trimmed)
        if (opening >= 0) {
            val targetText = trimmed.substring(0, opening).trim()
            val qualifierText = trimmed.substring(opening + 1, trimmed.lastIndex).trim()
            collectMatches(targetText, qualifierText, QualifierAttachmentForm.PARENTHETICAL) {
                OriginQualifierExtraction(targetText, it)
            }.let(candidates::addAll)
        }
        rules.forEach { rule ->
            rule.targets.forEach { target ->
                rule.outcomes.forEach { outcome ->
                    if (QualifierAttachmentForm.DIRECT_SUFFIX !in outcome.attachmentForms) return@forEach
                    outcome.qualifiers.forEach qualifierLoop@{ qualifier ->
                        if (TextNormalizer.normalize(trimmed) !=
                            "${TextNormalizer.normalize(target)} ${TextNormalizer.normalize(qualifier)}"
                        ) return@qualifierLoop
                        candidates += OriginQualifierExtraction(
                            target,
                            qualification(rule, outcome, qualifier)
                        )
                    }
                }
            }
        }
        val distinct = candidates.distinctBy {
            Triple(
                TextNormalizer.normalize(it.text),
                it.qualification?.ruleId,
                it.qualification?.outcomeId
            )
        }
        return distinct.singleOrNull() ?: OriginQualifierExtraction(value)
    }

    private fun collectMatches(
        targetText: String,
        qualifierText: String,
        form: QualifierAttachmentForm,
        extraction: (OriginQualification) -> OriginQualifierExtraction
    ): List<OriginQualifierExtraction> = buildList {
        rules.forEach { rule ->
            if (rule.targets.none { sameText(it, targetText) }) return@forEach
            rule.outcomes.forEach { outcome ->
                if (form !in outcome.attachmentForms) return@forEach
                outcome.qualifiers.firstOrNull { sameText(it, qualifierText) }?.let { qualifier ->
                    add(extraction(qualification(rule, outcome, qualifier)))
                }
            }
        }
    }

    private fun qualification(rule: Rule, outcome: Outcome, phrase: String) = OriginQualification(
        ruleId = rule.id,
        outcomeId = outcome.id,
        targetIngredientIds = rule.ingredientIds,
        requiredBaseStatus = rule.unresolvedStatus,
        detectedPhrase = phrase,
        veganStatus = outcome.veganStatus,
        vegetarianStatus = outcome.vegetarianStatus,
        reason = outcome.reason
    )

    fun protectsParenthesis(parentLabel: String): Boolean = protectedExpressions.any { expression ->
        expression.parenthesisPolicy == ParenthesisPolicy.IGNORE_CONTENT_FOR_VERDICT &&
            expression.aliases.any { sameText(it, parentLabel) }
    }

    fun protectedExpression(value: String): ProtectedExpressionMatch? {
        val trimmed = value.trim()
        val opening = trailingParenthesisOpening(trimmed)
        if (opening < 0) return null
        val targetText = trimmed.substring(0, opening).trim()
        val expression = protectedExpressions.singleOrNull { candidate ->
            candidate.aliases.any { sameText(it, targetText) }
        } ?: return null
        return ProtectedExpressionMatch(
            expression.id,
            expression.ingredientId,
            expression.matcherAlias,
            expression.knownStatus,
            expression.parenthesisPolicy
        )
    }

    fun resolve(ingredient: Ingredient, qualification: OriginQualification?): OriginResolution {
        if (qualification == null ||
            ingredient.id !in qualification.targetIngredientIds ||
            ingredient.status != qualification.requiredBaseStatus
        ) {
            return OriginResolution(ingredient.status, ingredient.status, null)
        }
        val effective = when (qualification.veganStatus) {
            QualifierVeganStatus.VEGAN -> VeganStatus.VEGAN
            QualifierVeganStatus.UNCERTAIN -> VeganStatus.UNCERTAIN
            QualifierVeganStatus.NOT_VEGAN -> when (qualification.vegetarianStatus) {
                QualifierVegetarianStatus.VEGETARIAN -> VeganStatus.VEGETARIAN
                QualifierVegetarianStatus.NON_VEGETARIAN -> VeganStatus.NON_VEGAN
                QualifierVegetarianStatus.UNCERTAIN -> ingredient.status
                QualifierVegetarianStatus.VEGETARIAN_OR_VEGAN -> VeganStatus.UNCERTAIN
            }
        }
        return OriginResolution(
            baseStatus = ingredient.status,
            effectiveStatus = effective,
            explanation = qualification.reason,
            explicitlyNotVegan = qualification.veganStatus == QualifierVeganStatus.NOT_VEGAN
        )
    }

    private fun sameText(left: String, right: String): Boolean =
        TextNormalizer.normalize(left) == TextNormalizer.normalize(right)

    private fun trailingParenthesisOpening(value: String): Int {
        if (!value.endsWith(')')) return -1
        var depth = 0
        for (index in value.lastIndex downTo 0) {
            when (value[index]) {
                ')' -> depth++
                '(' -> {
                    depth--
                    if (depth == 0) return index
                }
            }
        }
        return -1
    }

    companion object {
        fun empty() = OriginQualifierRuleSet(emptyList(), emptyList())

        fun load(json: String): OriginRulesLoadResult {
            val errors = mutableListOf<String>()
            val root = try {
                MiniJson.parse(json) as? Map<*, *>
            } catch (error: IllegalArgumentException) {
                return OriginRulesLoadResult(empty(), listOf("JSON invalide : ${error.message}"))
            } ?: return OriginRulesLoadResult(empty(), listOf("La racine JSON doit être un objet."))

            if (root.number("schemaVersion")?.toInt() != 2) {
                errors += "schemaVersion doit valoir 2."
            }
            val activeRules = mutableListOf<Rule>()
            root.objects("rules", errors).forEachIndexed { index, value ->
                val path = "rules[$index]"
                val activation = value.string("activation")
                if (activation !in setOf("ACTIVE", "REVIEW_BEFORE_ENGINE_USE")) {
                    errors += "$path.activation inconnu."
                    return@forEachIndexed
                }
                if (activation != "ACTIVE") return@forEachIndexed
                val id = value.requiredString("id", path, errors)
                val target = value.obj("target")
                if (target == null) errors += "$path.target est requis."
                val ingredientIds = target?.strings("ingredientIds", errors, "$path.target")?.toSet().orEmpty()
                val aliases = target?.strings("aliases", errors, "$path.target").orEmpty()
                val eNumbers = target?.strings("eNumbers", errors, "$path.target").orEmpty()
                if (ingredientIds.isEmpty()) errors += "$path.target.ingredientIds doit être renseigné."
                if (aliases.isEmpty() && eNumbers.isEmpty()) errors += "$path doit avoir une cible textuelle."
                val unresolved = enumValue<VeganStatus>(
                    value.string("unresolvedStatus"), "$path.unresolvedStatus", errors
                )
                val outcomes = mutableListOf<Outcome>()
                value.objects("qualifierOutcomes", errors, path).forEachIndexed outcomeLoop@{ outcomeIndex, outcome ->
                    val outcomePath = "$path.qualifierOutcomes[$outcomeIndex]"
                    val outcomeId = outcome.requiredString("id", outcomePath, errors)
                    val qualifiers = outcome.strings("qualifiers", errors, outcomePath)
                    val forms = outcome.strings("attachmentForms", errors, outcomePath).mapNotNull {
                        enumValue<QualifierAttachmentForm>(it, "$outcomePath.attachmentForms", errors)
                    }.toSet()
                    val vegan = enumValue<QualifierVeganStatus>(
                        outcome.string("veganStatus"), "$outcomePath.veganStatus", errors
                    )
                    val vegetarian = enumValue<QualifierVegetarianStatus>(
                        outcome.string("vegetarianStatus"), "$outcomePath.vegetarianStatus", errors
                    )
                    val reason = outcome.requiredString("reason", outcomePath, errors)
                    if (qualifiers.isEmpty()) errors += "$outcomePath.qualifiers ne peut pas être vide."
                    if (forms.isEmpty()) errors += "$outcomePath.attachmentForms ne peut pas être vide."
                    if (vegan != null && vegetarian != null &&
                        vegan == QualifierVeganStatus.VEGAN &&
                        vegetarian != QualifierVegetarianStatus.VEGETARIAN_OR_VEGAN
                    ) errors += "$outcomePath contient des statuts contradictoires."
                    if (vegan != null && vegetarian != null) {
                        outcomes += Outcome(outcomeId, qualifiers, forms, vegan, vegetarian, reason)
                    }
                }
                if (outcomes.isEmpty()) errors += "$path.qualifierOutcomes ne contient aucune règle active."
                if (id.isNotBlank() && unresolved != null) {
                    activeRules += Rule(id, ingredientIds, (eNumbers + aliases).distinct(), unresolved, outcomes)
                }
            }

            val protected = mutableListOf<ProtectedExpression>()
            root.objects("protectedExpressions", errors).forEachIndexed { index, value ->
                val path = "protectedExpressions[$index]"
                val activation = value.string("activation")
                if (activation !in setOf("ACTIVE", "REVIEW_BEFORE_ENGINE_USE")) {
                    errors += "$path.activation inconnu."
                    return@forEachIndexed
                }
                if (activation != "ACTIVE") return@forEachIndexed
                val id = value.requiredString("id", path, errors)
                val ingredientId = value.requiredString("ingredientId", path, errors)
                val aliases = value.strings("aliases", errors, path)
                val matcherAlias = value.requiredString("matcherAlias", path, errors)
                val status = enumValue<VeganStatus>(value.string("knownStatus"), "$path.knownStatus", errors)
                val policy = enumValue<ParenthesisPolicy>(
                    value.string("parenthesisPolicy"), "$path.parenthesisPolicy", errors
                )
                if (aliases.isEmpty()) errors += "$path.aliases ne peut pas être vide."
                if (id.isNotBlank() && ingredientId.isNotBlank() && matcherAlias.isNotBlank() &&
                    status != null && policy != null
                ) protected += ProtectedExpression(id, ingredientId, aliases, matcherAlias, status, policy)
            }

            val duplicateRuleIds = activeRules.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys
            duplicateRuleIds.forEach { errors += "Identifiant de règle dupliqué : $it." }
            val signatures = mutableMapOf<String, Pair<QualifierVeganStatus, QualifierVegetarianStatus>>()
            activeRules.forEach { rule ->
                rule.targets.forEach { target -> rule.outcomes.forEach { outcome ->
                    outcome.qualifiers.forEach { qualifier -> outcome.attachmentForms.forEach { form ->
                        val signature = listOf(
                            TextNormalizer.normalize(target), TextNormalizer.normalize(qualifier), form.name
                        ).joinToString("|")
                        val result = outcome.veganStatus to outcome.vegetarianStatus
                        val previous = signatures.putIfAbsent(signature, result)
                        if (previous != null && previous != result) {
                            errors += "Qualifications contradictoires pour $target / $qualifier / $form."
                        }
                    } }
                } }
            }
            return if (errors.isEmpty()) {
                OriginRulesLoadResult(OriginQualifierRuleSet(activeRules, protected), emptyList())
            } else {
                OriginRulesLoadResult(empty(), errors.distinct())
            }
        }

        private inline fun <reified T : Enum<T>> enumValue(
            value: String?, path: String, errors: MutableList<String>
        ): T? = try {
            value?.let { enumValueOf<T>(it) } ?: run {
                errors += "$path est requis."
                null
            }
        } catch (_: IllegalArgumentException) {
            errors += "$path contient une valeur inconnue : $value."
            null
        }
    }
}

private fun Map<*, *>.string(key: String): String? = this[key] as? String
private fun Map<*, *>.number(key: String): Number? = this[key] as? Number
private fun Map<*, *>.obj(key: String): Map<*, *>? = this[key] as? Map<*, *>
private fun Map<*, *>.requiredString(key: String, path: String, errors: MutableList<String>): String =
    string(key)?.takeIf(String::isNotBlank) ?: run {
        errors += "$path.$key est requis."
        ""
    }

private fun Map<*, *>.strings(
    key: String,
    errors: MutableList<String>,
    path: String
): List<String> {
    val values = this[key] as? List<*> ?: run {
        errors += "$path.$key doit être un tableau."
        return emptyList()
    }
    if (values.any { it !is String || it.isBlank() }) {
        errors += "$path.$key ne doit contenir que des chaînes non vides."
        return emptyList()
    }
    return values.filterIsInstance<String>()
}

private fun Map<*, *>.objects(
    key: String,
    errors: MutableList<String>,
    path: String = "racine"
): List<Map<*, *>> {
    val values = this[key] as? List<*> ?: run {
        errors += "$path.$key doit être un tableau."
        return emptyList()
    }
    if (values.any { it !is Map<*, *> }) {
        errors += "$path.$key ne doit contenir que des objets."
        return emptyList()
    }
    return values.filterIsInstance<Map<*, *>>()
}

/** Small strict JSON reader used on Android and in local JVM tests without another runtime dependency. */
class MiniJson(private val source: String) {
    private var index = 0

    private fun read(): Any? {
        whitespace()
        return when (source.getOrNull(index)) {
            '{' -> objectValue()
            '[' -> arrayValue()
            '"' -> stringValue()
            't' -> literal("true", true)
            'f' -> literal("false", false)
            'n' -> literal("null", null)
            '-', in '0'..'9' -> numberValue()
            else -> fail("valeur attendue")
        }
    }

    private fun objectValue(): Map<String, Any?> {
        expect('{')
        val result = linkedMapOf<String, Any?>()
        whitespace()
        if (take('}')) return result
        while (true) {
            whitespace()
            val key = stringValue()
            if (result.containsKey(key)) fail("clé dupliquée '$key'")
            whitespace(); expect(':')
            result[key] = read()
            whitespace()
            if (take('}')) return result
            expect(',')
        }
    }

    private fun arrayValue(): List<Any?> {
        expect('[')
        val result = mutableListOf<Any?>()
        whitespace()
        if (take(']')) return result
        while (true) {
            result += read()
            whitespace()
            if (take(']')) return result
            expect(',')
        }
    }

    private fun stringValue(): String {
        expect('"')
        val result = StringBuilder()
        while (index < source.length) {
            val character = source[index++]
            when (character) {
                '"' -> return result.toString()
                '\\' -> {
                    val escaped = source.getOrNull(index++) ?: fail("échappement incomplet")
                    result.append(when (escaped) {
                        '"', '\\', '/' -> escaped
                        'b' -> '\b'
                        'f' -> '\u000c'
                        'n' -> '\n'
                        'r' -> '\r'
                        't' -> '\t'
                        'u' -> {
                            val hex = source.substring(index, (index + 4).coerceAtMost(source.length))
                            if (hex.length != 4) fail("échappement Unicode incomplet")
                            index += 4
                            hex.toIntOrNull(16)?.toChar() ?: fail("échappement Unicode invalide")
                        }
                        else -> fail("échappement inconnu")
                    })
                }
                else -> {
                    if (character.code < 0x20) fail("caractère de contrôle dans une chaîne")
                    result.append(character)
                }
            }
        }
        fail("chaîne non terminée")
    }

    private fun numberValue(): Number {
        val start = index
        if (source.getOrNull(index) == '-') index++
        while (source.getOrNull(index)?.isDigit() == true) index++
        if (source.getOrNull(index) == '.') {
            index++
            while (source.getOrNull(index)?.isDigit() == true) index++
        }
        if (source.getOrNull(index) in listOf('e', 'E')) {
            index++
            if (source.getOrNull(index) in listOf('+', '-')) index++
            while (source.getOrNull(index)?.isDigit() == true) index++
        }
        return source.substring(start, index).toLongOrNull()
            ?: source.substring(start, index).toDoubleOrNull()
            ?: fail("nombre invalide")
    }

    private fun literal(text: String, value: Any?): Any? {
        if (!source.startsWith(text, index)) fail("littéral invalide")
        index += text.length
        return value
    }

    private fun expect(character: Char) {
        whitespace()
        if (!take(character)) fail("'$character' attendu")
    }

    private fun take(character: Char): Boolean {
        if (source.getOrNull(index) != character) return false
        index++
        return true
    }

    private fun whitespace() {
        while (source.getOrNull(index)?.isWhitespace() == true) index++
    }

    private fun fail(message: String): Nothing =
        throw IllegalArgumentException("$message à la position $index")

    companion object {
        fun parse(source: String): Any? {
            val parser = MiniJson(source)
            val value = parser.read()
            parser.whitespace()
            if (parser.index != source.length) parser.fail("contenu après la valeur JSON")
            return value
        }
    }
}
