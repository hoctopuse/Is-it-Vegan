package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FlavouringsRegulationImportTest {
    private val root = projectRoot()
    private val ingredientsAsset = root.resolve("app/src/main/assets/ingredients.json")
    private val aliasesAsset = root.resolve("app/src/main/assets/ingredient_aliases_multilingual.json")
    private val knowledge = IngredientKnowledge.fromJson(
        ingredientsAsset.readText(), aliasesAsset.readText(),
        root.resolve("app/src/main/assets/origin_qualifier_rules.json").readText()
    )
    private val service = IngredientAnalysisService(knowledge)

    @Test fun regulatoryCategoriesAreUncertainAndCarryOriginNotes() {
        categoryIds.forEach { id ->
            val ingredient = knowledge.ingredients.single { it.id == id }
            assertEquals(id, VeganStatus.UNCERTAIN, ingredient.status)
            assertEquals(id, listOf(PossibleOrigin.PLANT, PossibleOrigin.ANIMAL, PossibleOrigin.MICROBIAL), ingredient.possibleOriginNote?.origins)
        }
    }

    @Test fun articleThreeAliasesResolveInEachReviewedLanguage() {
        mapOf(
            "flavouring" to listOf("arôme", "aroma", "flavouring", "Aroma"),
            "flavouring_substance" to listOf("substance aromatisante", "aromastof", "flavouring substance", "Aromastoff"),
            "natural_flavouring" to listOf("substance aromatisante naturelle", "natuurlijke aromastof", "natural flavouring substance", "natürlicher Aromastoff"),
            "flavouring_preparation" to listOf("préparation aromatisante", "aromatiserend preparaat", "flavouring preparation", "Aromaextrakt"),
            "thermal_process_flavouring" to listOf("arôme obtenu par traitement thermique", "via een thermisch procedé verkregen aroma", "thermal process flavouring", "thermisch gewonnenes Reaktionsaroma"),
            "smoke_flavouring" to listOf("arôme de fumée", "rookaroma", "smoke flavouring", "Raucharoma"),
            "flavour_precursor" to listOf("précurseur d'arôme", "aromaprecursor", "flavour precursor", "Aromavorstufe"),
            "other_flavouring" to listOf("autre arôme", "overig aroma", "other flavouring", "sonstiges Aroma"),
            "food_ingredient_with_flavouring_properties" to listOf("ingrédient alimentaire possédant des propriétés aromatisantes", "voedselingrediënt met aromatiserende eigenschappen", "food ingredient with flavouring properties", "Lebensmittelzutat mit Aromaeigenschaften")
        ).forEach { (id, aliases) -> aliases.forEach { assertMatches(it, id) } }
    }

    @Test fun flavourAndExtractContextsDoNotResolveTheNamedRealIngredient() {
        listOf("arôme fraise", "arôme de fraise").forEach { text ->
            val matched = service.analyze(text, InputMode.MANUAL_INGREDIENT_LIST).matched
            assertTrue(text, matched.none { it.id == "strawberry" })
        }
        val coffeeExtract = service.analyze("extrait de café", InputMode.MANUAL_INGREDIENT_LIST).matched
        assertTrue(coffeeExtract.none { it.id == "coffee" })
        // CELEX identifies flavouring categories, not raw vanilla; no vanilla
        // concept is manufactured from its vanillate entries by this importer.
        assertTrue(service.analyze("arôme vanille", InputMode.MANUAL_INGREDIENT_LIST).matched.none { it.id == "vanilla" })
        assertMatches("fraise", "strawberry")
        assertMatches("café", "coffee")
    }

    @Test fun flavourTracesAndChocolateProtectionRemainOutsideIngredientMatching() {
        val diagnostics = service.analyzeWithDiagnostics("sucre. Peut contenir : arôme chocolat")
        assertTrue(diagnostics.result.matched.none { it.id in chocolateConceptIds || it.id == "flavouring" })
        assertTrue(diagnostics.result.crossContactWarnings.any { it.contains("arôme chocolat", ignoreCase = true) })
        listOf("arôme chocolat", "arôme de chocolat", "chocolate flavour", "chocoladearoma", "Schokoladenaroma").forEach { text ->
            assertTrue(text, service.analyze(text, InputMode.MANUAL_INGREDIENT_LIST).matched.none { it.id in chocolateConceptIds })
        }
        assertMatches("chocolat", "chocolate")
    }

    @Test fun mappingsAssetsAndImporterAreIdempotent() {
        val source = root.resolve("knowledge/ingredient_aliases_multilingual.json")
        assertTrue(source.readBytes().contentEquals(aliasesAsset.readBytes()))
        val rootJson = MiniJson.parse(source.readText()) as Map<*, *>
        val mappings = (rootJson["mappings"] as List<*>).map { it as Map<*, *> }
        assertEquals(mappings.size, mappings.map { listOf(it["conceptId"], it["language"], it["normalizedForm"]) }.distinct().size)
        categoryIds.forEach { id ->
            assertEquals(id, setOf("FR", "NL", "EN", "DE"), mappings.filter { it["conceptId"] == id }.map { it["language"] }.toSet())
        }
        val process = ProcessBuilder("python", "tools/import_eu_flavourings_regulation.py", "--check")
            .directory(root).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(output, 0, process.waitFor())
        assertTrue(output, output.contains("changes=0"))
    }

    private fun assertMatches(term: String, id: String) = assertTrue("$term -> $id", service.analyze(term, InputMode.MANUAL_INGREDIENT_LIST).matched.any { it.id.orEmpty() == id })
    private fun projectRoot(): File = generateSequence(File(requireNotNull(System.getProperty("user.dir"))).canonicalFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").isFile }

    private companion object {
        val categoryIds = setOf(
            "flavouring", "flavouring_substance", "natural_flavouring", "flavouring_preparation",
            "thermal_process_flavouring", "smoke_flavouring", "flavour_precursor", "other_flavouring",
            "food_ingredient_with_flavouring_properties"
        )
        val chocolateConceptIds = setOf("chocolate", "milk_chocolate", "white_chocolate", "filled_chocolate", "chocolate_confection", "powdered_chocolate")
    }
}
