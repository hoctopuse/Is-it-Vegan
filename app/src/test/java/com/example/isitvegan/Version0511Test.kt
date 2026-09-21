package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Version0511Test {
    private val rules = OriginQualifierRuleSet.load(
        File("src/main/assets/origin_qualifier_rules.json").readText()
    ).also { assertTrue(it.errors.joinToString(), it.isValid) }.rules

    private val database = listOf(
        ingredient("vegetable_oil", "Huile végétale", extraAliases = arrayOf("huiles végétales")),
        ingredient("e322", "Lécithines", VeganStatus.UNCERTAIN, "lécithine", "E322"),
        ingredient("e471", "Mono- et diglycérides d’acides gras", VeganStatus.UNCERTAIN, "E471"),
        ingredient("milk", "Lait", VeganStatus.VEGETARIAN),
        ingredient("egg", "Œuf", VeganStatus.VEGETARIAN, "oeuf"),
        ingredient("pork", "Porc", VeganStatus.NON_VEGAN, "viande de porc"),
        ingredient("gelatin", "Gélatine", VeganStatus.NON_VEGAN, "gelatine"),
        ingredient("cocoa", "Cacao"),
        ingredient("cocoa_butter", "Beurre de cacao"),
        ingredient("water", "Eau"),
        ingredient("salt", "Sel")
    )

    @Test fun lecithinParentheticalOriginIsAttachedAndResolvedWithoutMatchingTheQualifier() {
        val soy = VeganAnalyzer.analyzeWithDiagnostics(
            "émulsifiant : lécithines (soja)", database, rules = rules
        )
        val soyToken = soy.tokens.single()

        assertEquals(IngredientNodeKind.ADDITIVE, soy.ingredientTree.single().kind)
        assertEquals("lecithin-e322", soyToken.originRuleId)
        assertEquals("plant-soy", soyToken.originOutcomeId)
        assertEquals("soja", soyToken.originQualifierText)
        assertEquals(listOf("e322"), soyToken.matchedIngredientIds)
        assertEquals(listOf(VeganStatus.UNCERTAIN), soyToken.baseStatuses)
        assertEquals(listOf(VeganStatus.VEGAN), soyToken.effectiveStatuses)
        assertTrue(soy.result.unknown.isEmpty())
        assertEquals(AnalysisVerdict.VEGAN, soy.result.verdict)

        val egg = VeganAnalyzer.analyzeWithDiagnostics("lécithines (œuf)", database, rules = rules)
        val eggToken = egg.tokens.single()
        assertEquals("egg", eggToken.originOutcomeId)
        assertEquals(listOf("e322"), eggToken.matchedIngredientIds)
        assertFalse(egg.result.matched.any { it.id == "egg" })
        assertEquals(VeganStatus.VEGETARIAN, egg.result.matched.single().status)
        assertTrue(egg.result.unknown.isEmpty())
        assertEquals(VeganAssessment.NOT_VEGAN, egg.result.veganAssessment)
        assertEquals(AnalysisVerdict.VEGETARIAN, egg.result.verdict)
    }

    @Test fun e471RequiresADirectUnambiguousOriginQualifier() {
        val unspecified = VeganAnalyzer.analyzeWithDiagnostics("E471", database, rules = rules)
        assertEquals(null, unspecified.tokens.single().originRuleId)
        assertEquals(VeganStatus.UNCERTAIN, unspecified.result.matched.single().status)
        assertEquals(AnalysisVerdict.UNCERTAIN, unspecified.result.verdict)

        listOf("E471 (origine végétale)", "E471 d’origine végétale").forEach { text ->
            val vegetal = VeganAnalyzer.analyzeWithDiagnostics(text, database, rules = rules)
            assertEquals(text, "plant", vegetal.tokens.single().originOutcomeId)
            assertEquals(text, VeganStatus.VEGAN, vegetal.result.matched.single().status)
            assertTrue(text, vegetal.result.unknown.isEmpty())
            assertEquals(text, AnalysisVerdict.VEGAN, vegetal.result.verdict)
        }

        val vagueAnimal = VeganAnalyzer.analyzeWithDiagnostics(
            "E471 d’origine animale", database, rules = rules
        )
        assertEquals("animal-unspecified", vagueAnimal.tokens.single().originOutcomeId)
        assertEquals(VeganStatus.UNCERTAIN, vagueAnimal.result.matched.single().status)
        assertEquals(listOf("e471"), vagueAnimal.result.originNonVeganIngredientIds)
        assertEquals(VeganAssessment.NOT_VEGAN, vagueAnimal.result.veganAssessment)
        assertEquals(AnalysisVerdict.UNCERTAIN, vagueAnimal.result.verdict)
        assertFalse(vagueAnimal.result.verdict == AnalysisVerdict.NON_VEGETARIAN)
        assertTrue(
            DiagnosticReport.build(vagueAnimal, "0.5.11")
                .contains("Origines explicitement non vegan : e471")
        )

        val pork = VeganAnalyzer.analyzeWithDiagnostics("E471 (porc)", database, rules = rules)
        assertEquals("meat-or-fish", pork.tokens.single().originOutcomeId)
        assertEquals(VeganStatus.NON_VEGAN, pork.result.matched.single().status)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, pork.result.verdict)
    }

    @Test fun vegetableOilParenthesisIsProtectedMetadataRatherThanAComposition() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "huiles végétales (palme, tournesol)",
            database,
            rules = rules
        )
        val node = diagnostics.ingredientTree.single()
        val token = diagnostics.tokens.single()

        assertEquals(IngredientNodeKind.LEAF, node.kind)
        assertTrue(node.children.isEmpty())
        assertEquals("huiles végétales (palme, tournesol)", token.text)
        assertEquals("huile végétale", token.matcherText)
        assertEquals(listOf("vegetable_oil"), token.matchedIngredientIds)
        assertTrue(diagnostics.result.unknown.isEmpty())
        assertEquals(AnalysisVerdict.VEGAN, diagnostics.result.verdict)
    }

    @Test fun vegetableOilParenthesisNeverResolvesTheFollowingE471() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "huiles végétales (palme, tournesol), E471",
            database,
            rules = rules
        )

        assertEquals(setOf("vegetable_oil", "e471"), diagnostics.result.matched.map { it.id }.toSet())
        assertEquals(VeganStatus.UNCERTAIN, diagnostics.result.matched.single { it.id == "e471" }.status)
        assertEquals(null, diagnostics.tokens.single { it.text == "E471" }.originRuleId)
        assertTrue(diagnostics.result.unknown.isEmpty())
        assertEquals(AnalysisVerdict.UNCERTAIN, diagnostics.result.verdict)
    }

    @Test fun originQualifierNeverSpreadsAcrossFunctionalClassChildren() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "émulsifiants : E471, lécithines (soja)",
            database,
            rules = rules
        )

        assertEquals(2, diagnostics.tokens.count { it.nodeKind == IngredientNodeKind.ADDITIVE })
        assertEquals(
            VeganStatus.UNCERTAIN,
            diagnostics.result.matched.single { it.id == "e471" }.status
        )
        assertEquals(
            VeganStatus.VEGAN,
            diagnostics.result.matched.single { it.id == "e322" }.status
        )
        assertEquals(
            null,
            diagnostics.tokens.single { it.matchedIngredientIds == listOf("e471") }.originRuleId
        )
        assertEquals(
            "plant-soy",
            diagnostics.tokens.single { it.matchedIngredientIds == listOf("e322") }.originOutcomeId
        )
        assertEquals(AnalysisVerdict.UNCERTAIN, diagnostics.result.verdict)
    }

    @Test fun distantAmbiguousAndUnrelatedQualifiersDoNotResolveE471() {
        listOf(
            "origine végétale, E471",
            "E471, huiles végétales (palme, tournesol)",
            "E471 d’origine végétale et d’origine animale",
            "E471 (porc) d’origine végétale",
            "E471 non hydrogénée"
        ).forEach { text ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database, rules = rules)
            assertTrue(text, diagnostics.result.matched.any { it.id == "e471" })
            val e471 = diagnostics.result.matched.single { it.id == "e471" }
            assertEquals(text, VeganStatus.UNCERTAIN, e471.status)
            assertFalse(text, diagnostics.result.verdict == AnalysisVerdict.VEGAN)
        }
    }

    @Test fun establishedIngredientsNestedCompositesAndTracesRemainIndependent() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "sauce (eau, cacao, beurre de cacao, lait, œuf, porc, gélatine, sel). " +
                "Peut contenir du soja.",
            database,
            rules = rules
        )

        assertEquals(IngredientNodeKind.COMPOSITE, diagnostics.ingredientTree.single().kind)
        assertEquals(
            setOf("water", "cocoa", "cocoa_butter", "milk", "egg", "pork", "gelatin", "salt"),
            diagnostics.result.matched.map { it.id }.toSet()
        )
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, diagnostics.result.verdict)
        assertEquals(listOf("Peut contenir du soja."), diagnostics.crossContactWarnings)
        assertFalse(diagnostics.result.matched.any { it.id == "soy" })
    }

    @Test fun syntheticRuleChangesBehaviorThroughJsonAlone() {
        val syntheticDatabase = listOf(
            ingredient("synthetic_x999", "Additif synthétique", VeganStatus.UNCERTAIN, "X999")
        )
        val firstLoad = OriginQualifierRuleSet.load(syntheticJson("source lunaire"))
        assertTrue(firstLoad.errors.joinToString(), firstLoad.isValid)
        val resolved = VeganAnalyzer.analyzeWithDiagnostics(
            "X999 (source lunaire)", syntheticDatabase, rules = firstLoad.rules
        )
        assertEquals("synthetic-origin", resolved.tokens.single().originRuleId)
        assertEquals(VeganStatus.VEGAN, resolved.result.matched.single().status)
        assertEquals(AnalysisVerdict.VEGAN, resolved.result.verdict)

        val modifiedLoad = OriginQualifierRuleSet.load(syntheticJson("source solaire"))
        assertTrue(modifiedLoad.errors.joinToString(), modifiedLoad.isValid)
        val oldWording = VeganAnalyzer.analyzeWithDiagnostics(
            "X999 (source lunaire)", syntheticDatabase, rules = modifiedLoad.rules
        )
        assertEquals(VeganStatus.UNCERTAIN, oldWording.result.matched.single().status)
        assertFalse(oldWording.result.verdict == AnalysisVerdict.VEGAN)
        val newWording = VeganAnalyzer.analyzeWithDiagnostics(
            "X999 (source solaire)", syntheticDatabase, rules = modifiedLoad.rules
        )
        assertEquals(VeganStatus.VEGAN, newWording.result.matched.single().status)

        val removedLoad = OriginQualifierRuleSet.load(syntheticJson(null))
        assertTrue(removedLoad.errors.joinToString(), removedLoad.isValid)
        val removed = VeganAnalyzer.analyze(
            "X999 (source lunaire)", syntheticDatabase, rules = removedLoad.rules
        )
        assertEquals(VeganStatus.UNCERTAIN, removed.matched.single().status)
        assertFalse(removed.verdict == AnalysisVerdict.VEGAN)
    }

    @Test fun invalidUnknownAndContradictoryRulesFailClosed() {
        val syntheticDatabase = listOf(
            ingredient("synthetic_x999", "Additif synthétique", VeganStatus.UNCERTAIN, "X999")
        )
        val loads = listOf(
            OriginQualifierRuleSet.load("{"),
            OriginQualifierRuleSet.load(syntheticJson("source lunaire", activation = "UNKNOWN")),
            OriginQualifierRuleSet.load(syntheticJson("source lunaire", contradictory = true))
        )
        loads.forEach { load ->
            assertFalse(load.isValid)
            assertTrue(load.errors.isNotEmpty())
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
                "X999 (source lunaire)",
                syntheticDatabase,
                rules = load.rules,
                ruleErrors = load.errors
            )
            assertEquals(VeganStatus.UNCERTAIN, diagnostics.result.matched.single().status)
            assertFalse(diagnostics.result.verdict == AnalysisVerdict.VEGAN)
            assertTrue(diagnostics.originRuleErrors.isNotEmpty())
        }
    }

    @Test fun vegetableOilParenthesisPolicyIsControlledByLoadedData() {
        val configured = IngredientTreeParser.parse(
            "huiles végétales (palme, tournesol)", rules
        ).single()
        val withoutPolicy = IngredientTreeParser.parse(
            "huiles végétales (palme, tournesol)", OriginQualifierRuleSet.empty()
        ).single()
        assertEquals(IngredientNodeKind.LEAF, configured.kind)
        assertEquals(IngredientNodeKind.COMPOSITE, withoutPolicy.kind)
    }

    private fun syntheticJson(
        qualifier: String?,
        activation: String = "ACTIVE",
        contradictory: Boolean = false
    ): String {
        val outcomes = if (qualifier == null) "[]" else buildString {
            append(
                """[{"id":"vegan","qualifiers":["$qualifier"],"attachmentForms":["PARENTHETICAL"],"veganStatus":"VEGAN","vegetarianStatus":"VEGETARIAN_OR_VEGAN","reason":"qualification synthétique"}"""
            )
            if (contradictory) append(
                """,{"id":"animal","qualifiers":["$qualifier"],"attachmentForms":["PARENTHETICAL"],"veganStatus":"NOT_VEGAN","vegetarianStatus":"VEGETARIAN","reason":"contradiction synthétique"}"""
            )
            append(']')
        }
        val rulesJson = if (qualifier == null) "[]" else """
            [{
              "id":"synthetic-origin",
              "target":{"ingredientIds":["synthetic_x999"],"eNumbers":["X999"],"aliases":["additif synthétique"]},
              "originMode":"variable",
              "evidenceLevel":"synthetic-test",
              "sourceIds":[],
              "qualifierOutcomes":$outcomes,
              "unresolvedStatus":"UNCERTAIN",
              "activation":"$activation"
            }]
        """.trimIndent()
        return """{"schemaVersion":2,"rules":$rulesJson,"protectedExpressions":[]}"""
    }

    private fun ingredient(
        id: String,
        name: String,
        status: VeganStatus = VeganStatus.VEGAN,
        vararg aliases: String,
        extraAliases: Array<String> = emptyArray()
    ) = Ingredient(id, name, aliases.toList() + extraAliases, null, status, "Test")
}
