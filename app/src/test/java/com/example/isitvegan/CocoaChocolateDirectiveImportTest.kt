package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CocoaChocolateDirectiveImportTest {
    private val root = projectRoot()
    private val ingredientsAsset = root.resolve("app/src/main/assets/ingredients.json")
    private val aliasesAsset = root.resolve("app/src/main/assets/ingredient_aliases_multilingual.json")
    private val knowledge = IngredientKnowledge.fromJson(
        ingredientsAsset.readText(), aliasesAsset.readText(),
        root.resolve("app/src/main/assets/origin_qualifier_rules.json").readText()
    )
    private val service = IngredientAnalysisService(knowledge)

    @Test fun cocoaAndCocoaButterRemainSeparateVeganConcepts() {
        assertEquals(VeganStatus.VEGAN, status("cocoa"))
        assertEquals(VeganStatus.VEGAN, status("cocoa_butter"))
        assertEquals(VeganStatus.VEGAN, status("powdered_chocolate"))
        assertMatches("cacao", "cocoa")
        listOf("beurre de cacao", "cacaoboter", "cocoa butter", "Kakaobutter").forEach { assertMatches(it, "cocoa_butter") }
        assertFalse(knowledge.ingredients.single { it.id == "cocoa_butter" }.aliases.contains("cacao"))
    }

    @Test fun chocolateCategoriesUseDocumentedPrudentStatuses() {
        assertEquals(VeganStatus.UNCERTAIN, status("chocolate"))
        assertEquals(VeganStatus.VEGETARIAN, status("milk_chocolate"))
        assertEquals(VeganStatus.VEGETARIAN, status("white_chocolate"))
        assertEquals(VeganStatus.UNCERTAIN, status("filled_chocolate"))
        assertEquals(VeganStatus.UNCERTAIN, status("chocolate_confection"))
        listOf("chocolat au lait", "melkchocolade", "milk chocolate", "Milchschokolade").forEach { assertMatches(it, "milk_chocolate") }
        listOf("chocolat blanc", "witte chocolade", "white chocolate", "Weiße Schokolade").forEach { assertMatches(it, "white_chocolate") }
        listOf("chocolat fourré", "gevulde chocolade", "filled chocolate", "Gefüllte Schokolade").forEach { assertMatches(it, "filled_chocolate") }
    }

    @Test fun chocolateFlavourIsNotAnAliasForRealChocolate() {
        listOf(
            "arôme chocolat", "arôme de chocolat", "arôme naturel de chocolat",
            "chocolate flavour", "chocolate flavor", "chocolate aroma",
            "chocoladearoma", "Schokoladenaroma", "arôme goût chocolat", "goût chocolat"
        ).forEach { text ->
            val matched = service.analyze(text, InputMode.MANUAL_INGREDIENT_LIST).matched
            assertTrue(text, matched.none { it.id in chocolateConceptIds })
        }
    }

    @Test fun realChocolateNamesStillResolveToTheirPreciseConcept() {
        listOf(
            "chocolat", "chocolat noir", "chocolate", "dark chocolate",
            "chocolade", "pure chocolade", "Schokolade", "Bitterschokolade"
        ).forEach { assertMatches(it, "chocolate") }
        listOf("chocolat au lait", "milk chocolate", "melkchocolade", "Milchschokolade")
            .forEach { assertMatches(it, "milk_chocolate") }
        listOf("chocolat blanc", "white chocolate", "witte chocolade", "weiße Schokolade")
            .forEach { assertMatches(it, "white_chocolate") }
    }

    @Test fun chocolateTraceRemainsOutsideTheVerdict() {
        val diagnostics = service.analyzeWithDiagnostics("sucre. Peut contenir : chocolat")
        assertTrue(diagnostics.result.matched.none { it.id in chocolateConceptIds })
        assertTrue(diagnostics.result.crossContactWarnings.any { it.contains("chocolat", ignoreCase = true) })
    }

    @Test fun sourceAssetParityMappingsAndHistoricalConceptsArePreserved() {
        val source = root.resolve("knowledge/ingredient_aliases_multilingual.json")
        assertTrue(source.readBytes().contentEquals(aliasesAsset.readBytes()))
        val rootJson = MiniJson.parse(source.readText()) as Map<*, *>
        val mappings = (rootJson["mappings"] as List<*>).map { it as Map<*, *> }
        assertEquals(mappings.size, mappings.map { listOf(it["conceptId"], it["language"], it["normalizedForm"]) }.distinct().size)
        setOf("cocoa", "cocoa_butter", "powdered_chocolate", "chocolate", "milk_chocolate", "white_chocolate", "filled_chocolate", "chocolate_confection").forEach { id ->
            assertEquals(id, setOf("FR", "NL", "EN", "DE"), mappings.filter { it["conceptId"] == id }.map { it["language"] }.toSet())
        }
        assertTrue(knowledge.ingredients.map { it.id }.containsAll(setOf("milk", "butter", "cream", "sugar", "hazelnut", "almond", "fruit_juice", "fruit_jam", "edible_offal", "animal_fat")))
        val process = ProcessBuilder("python", "tools/import_eu_cocoa_chocolate_directive.py", "--check")
            .directory(root).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(output, 0, process.waitFor())
        assertTrue(output, output.contains("changes=0"))
    }

    private fun status(id: String) = knowledge.ingredients.single { it.id == id }.status
    private fun assertMatches(term: String, id: String) = assertTrue("$term -> $id", service.analyze(term, InputMode.MANUAL_INGREDIENT_LIST).matched.any { it.id.orEmpty() == id })
    private fun projectRoot(): File = generateSequence(File(requireNotNull(System.getProperty("user.dir"))).canonicalFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").isFile }

    private companion object {
        val chocolateConceptIds = setOf(
            "chocolate", "milk_chocolate", "white_chocolate", "filled_chocolate",
            "chocolate_confection", "powdered_chocolate"
        )
    }
}
