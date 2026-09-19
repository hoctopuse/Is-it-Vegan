package com.example.isitvegan

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Version0591Test {
    private val database = listOf(
        ingredient("water", "eau"), ingredient("sugar", "sucre"),
        ingredient("salt", "sel"), ingredient("milk", "lait", VeganStatus.VEGETARIAN),
        ingredient("meat", "viande de porc", VeganStatus.NON_VEGAN),
        ingredient("cheese", "fromage", VeganStatus.VEGETARIAN),
        ingredient("margarine", "margarine", VeganStatus.UNCERTAIN),
        ingredient("silicon_dioxide", "dioxyde de silicium"),
        ingredient("sunflower_oil", "huile de tournesol"),
        ingredient("rapeseed_oil", "huile de colza"),
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
            database
        )
        val oils = diagnostics.ingredientTree.first()
        assertEquals(IngredientNodeKind.COMPOSITE, oils.kind)
        assertEquals(listOf("tournesol", "colza"), oils.children.map { it.rawText })
        assertTrue(oils.variableProportions)
        assertTrue(oils.hasAlternatives)
        assertTrue(diagnostics.result.matched.any { it.id == "sunflower_oil" })
        assertTrue(diagnostics.result.matched.any { it.id == "rapeseed_oil" })
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
        val unspecified = VeganAnalyzer.analyzeWithDiagnostics("émulsifiant : E471", database)
        assertEquals(VeganStatus.UNCERTAIN, unspecified.result.matched.single().status)

        val vegetal = VeganAnalyzer.analyzeWithDiagnostics(
            "émulsifiant : E471 d’origine végétale",
            database
        )
        assertEquals(SourceClaim.VEGETAL, vegetal.tokens.single().sourceClaim)
        assertEquals(VeganStatus.VEGAN, vegetal.result.matched.single().status)
        assertEquals(VeganAssessment.VEGAN, vegetal.result.veganAssessment)
        assertEquals(listOf(VeganStatus.UNCERTAIN), vegetal.tokens.single().baseStatuses)

        val animal = VeganAnalyzer.analyzeWithDiagnostics(
            "émulsifiant : E471 d’origine animale",
            database
        )
        assertEquals(SourceClaim.ANIMAL, animal.tokens.single().sourceClaim)
        assertEquals(VeganStatus.NON_VEGAN, animal.result.matched.single().status)
        assertEquals(VeganAssessment.NOT_VEGAN, animal.result.veganAssessment)

        val arbitrary = VeganAnalyzer.analyzeWithDiagnostics("E471 végétal", database)
        assertEquals(SourceClaim.UNSPECIFIED, arbitrary.tokens.single().sourceClaim)
        assertEquals(VeganStatus.UNCERTAIN, arbitrary.result.matched.single().status)
    }

    @Test fun sourceClaimLexiconCoversSupportedLabelLanguages() {
        mapOf(
            "E471 d’origine végétale" to SourceClaim.VEGETAL,
            "E471 d'origine animale" to SourceClaim.ANIMAL,
            "E471 origine microbienne" to SourceClaim.MICROBIAL,
            "E471 van plantaardige oorsprong" to SourceClaim.VEGETAL,
            "E471 van dierlijke oorsprong" to SourceClaim.ANIMAL,
            "E471 plant-based" to SourceClaim.VEGETAL,
            "E471 of vegetable origin" to SourceClaim.VEGETAL,
            "E471 animal origin" to SourceClaim.ANIMAL,
            "E471 microbial origin" to SourceClaim.MICROBIAL,
            "E471 pflanzlichen Ursprungs" to SourceClaim.VEGETAL,
            "E471 tierischen Ursprungs" to SourceClaim.ANIMAL,
            "E471 de origen vegetal" to SourceClaim.VEGETAL,
            "E471 de origen animal" to SourceClaim.ANIMAL
        ).forEach { (text, expected) ->
            val extraction = SourceClaimLexicon.extract(text)
            assertEquals(text, expected, extraction.claim)
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
            InputMode.OCR_LABEL
        )
        val report = DiagnosticReport.build(diagnostics, "0.5.9.1")
        assertTrue(report.contains("Mode d’entrée : étiquette OCR"))
        assertTrue(report.contains("Disponibilité : liste d’ingrédients analysée"))
        assertTrue(report.contains("compatibilité vegan selon les ingrédients déclarés"))
        assertTrue(report.contains("nano=oui"))
        assertTrue(report.contains("origine déclarée=VEGETAL"))
        assertTrue(report.contains("classification de base=UNCERTAIN"))
        assertTrue(report.contains("résolution par origine=origine végétale déclarée"))
        assertNull(diagnostics.availabilityReason)
    }

    private fun ingredient(
        id: String,
        alias: String,
        status: VeganStatus = VeganStatus.VEGAN
    ) = Ingredient(id, alias, listOf(alias), null, status, "Test")
}
