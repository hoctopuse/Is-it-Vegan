package com.example.isitvegan

import java.io.File
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Version0591Test {
    private val rules = OriginQualifierRuleSet.load(
        File("src/main/assets/origin_qualifier_rules.json").readText()
    ).also { assertTrue(it.errors.joinToString(), it.isValid) }.rules

    private val database = listOf(
        ingredient("water", "eau"), ingredient("sugar", "sucre"),
        ingredient("salt", "sel"), ingredient("milk", "lait", VeganStatus.VEGETARIAN),
        ingredient("meat", "viande de porc", VeganStatus.NON_VEGAN),
        ingredient("cheese", "fromage", VeganStatus.VEGETARIAN),
        ingredient("margarine", "margarine", VeganStatus.UNCERTAIN),
        ingredient("silicon_dioxide", "dioxyde de silicium"),
        ingredient("sunflower_oil", "huile de tournesol"),
        ingredient("rapeseed_oil", "huile de colza"),
        Ingredient(
            "vegetable_oil",
            "huile végétale",
            listOf("huile végétale", "huiles végétales"),
            null,
            VeganStatus.VEGAN,
            "Test"
        ),
        ingredient("spices", "épices"),
        ingredient("e471", "E471", VeganStatus.UNCERTAIN)
    )

    @Test fun inputModesControlFullTextFallback() {
        val manual = VeganAnalyzer.analyzeWithDiagnostics(
            "eau, sucre, sel",
            database,
            InputMode.MANUAL_INGREDIENT_LIST
        )
        assertEquals(AnalysisAvailability.INGREDIENT_LIST_ANALYZED, manual.result.availability)
        assertTrue(manual.usedManualFallback)
        assertEquals(listOf("water", "sugar", "salt"), manual.result.matched.map { it.id })

        val fullWithoutHeading = VeganAnalyzer.analyzeWithDiagnostics(
            "eau, sucre, sel",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, fullWithoutHeading.result.availability)
        assertTrue(fullWithoutHeading.ingredientTree.isEmpty())
        assertTrue(fullWithoutHeading.tokens.isEmpty())

        val full = VeganAnalyzer.analyzeWithDiagnostics(
            "Ingrédients : eau, sucre, sel",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(AnalysisAvailability.INGREDIENT_LIST_ANALYZED, full.result.availability)
        assertFalse(full.usedManualFallback)
        assertEquals(3, full.ingredientTree.size)

        listOf("Pommes", "Fromage").forEach { label ->
            val result = VeganAnalyzer.analyzeWithDiagnostics(label, database, InputMode.FULL_LABEL)
            assertEquals(label, AnalysisAvailability.NO_INGREDIENT_LIST, result.result.availability)
            assertTrue(label, result.ingredientTree.isEmpty())
            assertTrue(label, result.result.matched.isEmpty())
        }
    }

    @Test fun declaredPresenceAndCrossContactRemainDifferentWithoutAList() {
        val contains = VeganAnalyzer.analyzeWithDiagnostics(
            "Contient : LAIT",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, contains.result.availability)
        assertTrue(contains.ingredientTree.isEmpty())
        assertEquals(listOf("milk"), contains.result.declaredPresenceIngredientIds)
        assertEquals(VeganAssessment.NOT_VEGAN, contains.result.veganAssessment)

        val trace = VeganAnalyzer.analyzeWithDiagnostics(
            "Peut contenir : LAIT",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, trace.result.availability)
        assertTrue(trace.result.matched.isEmpty())
        assertTrue(trace.result.declaredPresenceIngredientIds.isEmpty())
        assertEquals(listOf("Peut contenir : LAIT"), trace.crossContactWarnings)
    }

    @Test fun nanoIsIngredientMetadataInsteadOfAChild() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "dioxyde de silicium ( NaNo ), sel",
            database
        )
        assertEquals(2, diagnostics.ingredientTree.size)
        val silicon = diagnostics.ingredientTree.first()
        assertEquals(IngredientNodeKind.LEAF, silicon.kind)
        assertEquals("dioxyde de silicium", silicon.rawText)
        assertTrue(silicon.isNano)
        assertTrue(silicon.children.isEmpty())
        assertFalse(diagnostics.result.unknown.any { it.contains("nano", true) })
    }

    @Test fun variableProportionsAndAlternativesRemainStructural() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "huiles végétales (tournesol et/ou colza) en proportions variables, sel",
            database,
            rules = rules
        )
        val oils = diagnostics.ingredientTree.first()
        assertEquals(IngredientNodeKind.LEAF, oils.kind)
        assertEquals("huiles végétales (tournesol et/ou colza)", oils.rawText)
        assertTrue(oils.children.isEmpty())
        assertTrue(oils.variableProportions)
        assertTrue(oils.hasAlternatives)
        assertEquals(setOf("vegetable_oil", "salt"), diagnostics.result.matched.map { it.id }.toSet())
        assertFalse(diagnostics.result.unknown.any {
            it.contains("et/ou", true) || it.contains("proportion", true)
        })

        val spices = IngredientTreeParser.parse("épices en proportion variable").single()
        assertEquals("épices", spices.rawText)
        assertTrue(spices.variableProportions)

        val alternatives = IngredientTreeParser.parse(
            "contient de l’huile de tournesol et/ou de l’huile de colza"
        )
        assertEquals(2, alternatives.size)
        assertFalse(alternatives.any { it.rawText.contains("et/ou") })
    }

    @Test fun e471OriginClaimsUseOnlyTheExplicitResolutionRegistry() {
        val unspecified = VeganAnalyzer.analyzeWithDiagnostics(
            "émulsifiant : E471", database, rules = rules
        )
        assertEquals(VeganStatus.UNCERTAIN, unspecified.result.matched.single().status)

        val vegetal = VeganAnalyzer.analyzeWithDiagnostics(
            "émulsifiant : E471 d’origine végétale",
            database,
            rules = rules
        )
        assertEquals("plant", vegetal.tokens.single().originOutcomeId)
        assertEquals(VeganStatus.VEGAN, vegetal.result.matched.single().status)
        assertEquals(VeganAssessment.VEGAN, vegetal.result.veganAssessment)
        assertEquals(listOf(VeganStatus.UNCERTAIN), vegetal.tokens.single().baseStatuses)

        val animal = VeganAnalyzer.analyzeWithDiagnostics(
            "émulsifiant : E471 d’origine animale",
            database,
            rules = rules
        )
        assertEquals("animal-unspecified", animal.tokens.single().originOutcomeId)
        assertEquals(VeganStatus.UNCERTAIN, animal.result.matched.single().status)
        assertEquals(listOf("e471"), animal.result.originNonVeganIngredientIds)
        assertEquals(VeganAssessment.NOT_VEGAN, animal.result.veganAssessment)
        assertEquals(AnalysisVerdict.UNCERTAIN, animal.result.verdict)

        val arbitrary = VeganAnalyzer.analyzeWithDiagnostics("E471 végétal", database, rules = rules)
        assertEquals(null, arbitrary.tokens.single().originRuleId)
        assertEquals(VeganStatus.UNCERTAIN, arbitrary.result.matched.single().status)
    }

    @Test fun jsonRulesCoverSupportedLabelLanguages() {
        mapOf(
            "E471 d’origine végétale" to "plant",
            "E471 d'origine animale" to "animal-unspecified",
            "E471 van plantaardige oorsprong" to "plant",
            "E471 van dierlijke oorsprong" to "animal-unspecified",
            "E471 plant-based" to "plant",
            "E471 of vegetable origin" to "plant",
            "E471 animal origin" to "animal-unspecified",
            "E471 pflanzlichen Ursprungs" to "plant",
            "E471 tierischen Ursprungs" to "animal-unspecified",
            "E471 de origen vegetal" to "plant",
            "E471 de origen animal" to "animal-unspecified"
        ).forEach { (text, expected) ->
            val extraction = rules.extractAttached(text)
            assertEquals(text, expected, extraction.qualification?.outcomeId)
            assertEquals(text, "E471", extraction.text)
        }
    }

    @Test fun subTwoPercentIngredientWithoutCompositionStaysALeaf() {
        val roots = IngredientTreeParser.parse("chocolat 1,5 %, sucre")
        assertEquals(IngredientNodeKind.LEAF, roots.first().kind)
        assertEquals("chocolat", roots.first().rawText)
        assertEquals(BigDecimal("1.5"), roots.first().quantityPercent)

        val composite = IngredientTreeParser.parse("sauce 1,5 % [eau, sel]").single()
        assertEquals(IngredientNodeKind.COMPOSITE, composite.kind)
        assertEquals(BigDecimal("1.5"), composite.quantityPercent)
        assertEquals(2, composite.children.size)
    }

    @Test fun genericAnnexCategoriesRemainOrdinaryLeavesWithoutInventedKnowledge() {
        listOf(
            "huiles végétales", "graisses végétales", "huiles animales",
            "graisses animales", "poisson", "fromage", "protéines de lait",
            "amidon", "fécule", "sucre", "épices", "plantes aromatiques", "herbes aromatiques"
        ).forEach { text ->
            val node = IngredientTreeParser.parse(text).single()
            assertEquals(text, IngredientNodeKind.LEAF, node.kind)
            assertEquals(text, text, node.rawText)
        }
    }

    @Test fun nonAllergenAnimalIngredientAndTraceRulesStayIndependent() {
        val pork = VeganAnalyzer.analyze(
            "Ingrédients : viande de porc, sel",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(VeganAssessment.NOT_VEGAN, pork.veganAssessment)
        assertTrue(pork.matched.any { it.id == "meat" })

        val trace = VeganAnalyzer.analyze(
            "Peut contenir : lait",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(AnalysisAvailability.NO_INGREDIENT_LIST, trace.availability)
        assertTrue(trace.matched.isEmpty())
    }

    @Test fun porkDishAndVegetarianVariantKeepTheirDistinctEvidence() {
        val pork = VeganAnalyzer.analyze(
            "Ingrédients : viande de porc, lait, fromage, margarine, sel",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, pork.verdict)
        assertEquals(VeganAssessment.NOT_VEGAN, pork.veganAssessment)
        assertTrue(pork.matched.any { it.id == "meat" })

        val vegetarian = VeganAnalyzer.analyze(
            "Ingrédients : lait, fromage, margarine, sel",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(AnalysisVerdict.UNCERTAIN, vegetarian.verdict)
        assertEquals(VeganAssessment.NOT_VEGAN, vegetarian.veganAssessment)
        assertFalse(vegetarian.matched.any { it.id == "meat" })
        assertEquals(setOf("milk", "cheese"), vegetarian.veganBlockers.map { it.id }.toSet())
    }

    @Test fun diagnosticsExposeModeAvailabilityMetadataAndScope() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "Ingrédients : dioxyde de silicium (nano), E471 d’origine végétale",
            database,
            InputMode.OCR_LABEL,
            rules
        )
        val report = DiagnosticReport.build(diagnostics, "0.5.9.1")
        assertTrue(report.contains("Mode d’entrée : étiquette OCR"))
        assertTrue(report.contains("Disponibilité : liste d’ingrédients analysée"))
        assertTrue(report.contains("compatibilité vegan selon les ingrédients déclarés"))
        assertTrue(report.contains("nano=oui"))
        assertTrue(report.contains("règle d’origine=mono-diglycerides-e471"))
        assertTrue(report.contains("classification de base=UNCERTAIN"))
        assertTrue(report.contains("résolution par origine=origine végétale directement rattachée"))
        assertNull(diagnostics.availabilityReason)
    }

    private fun ingredient(
        id: String,
        alias: String,
        status: VeganStatus = VeganStatus.VEGAN
    ) = Ingredient(id, alias, listOf(alias), null, status, "Test")
}
