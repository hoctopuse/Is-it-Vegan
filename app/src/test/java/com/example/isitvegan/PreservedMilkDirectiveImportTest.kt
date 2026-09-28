package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreservedMilkDirectiveImportTest {
    private val knowledge = IngredientKnowledge.fromJson(
        file("app/src/main/assets/ingredients.json").readText(),
        file("app/src/main/assets/ingredient_aliases_multilingual.json").readText(),
        file("app/src/main/assets/origin_qualifier_rules.json").readText()
    )
    private val service = IngredientAnalysisService(knowledge)

    @Test fun preservedMilkAliasesResolveInFourLanguagesAsVegetarian() {
        mapOf(
            "lait" to "FR", "melk" to "NL", "milk" to "EN", "Milch" to "DE",
            "lait en poudre entier" to "FR", "volle melkpoeder" to "NL",
            "whole milk powder" to "EN", "Vollmilchpulver" to "DE",
            "lait concentré" to "FR", "geëvaporeerde volle melk" to "NL",
            "condensed milk" to "EN", "Kondensmilch" to "DE",
            "lait concentré sucré" to "FR", "gecondenseerde volle melk met suiker" to "NL",
            "sweetened condensed milk" to "EN", "Gezuckerte Kondensmilch" to "DE",
            "lait concentré écrémé" to "FR", "geëvaporeerde magere melk" to "NL",
            "skimmed-milk powder" to "EN", "Magermilchpulver" to "DE",
            "evaporated milk" to "EN"
        ).forEach { (text, language) ->
            val result = service.analyze(text, InputMode.MANUAL_INGREDIENT_LIST)
            val milk = result.matched.single { it.id == "milk" }
            assertEquals("$language / $text", VeganStatus.VEGETARIAN, milk.status)
            assertTrue("$language / $text", result.verdict != AnalysisVerdict.VEGAN)
        }
    }

    @Test fun milkBlocksVeganButDoesNotAbsorbPlantDrinksFlavourOrContainsWording() {
        val dairy = service.analyze("lait concentré", InputMode.MANUAL_INGREDIENT_LIST)
        assertEquals(AnalysisVerdict.VEGETARIAN, dairy.verdict)
        assertTrue(dairy.matched.any { it.id == "milk" && it.status == VeganStatus.VEGETARIAN })

        listOf("lait de soja", "lait végétal", "plant milk", "arôme de lait", "milk flavour")
            .forEach { text ->
                val result = service.analyze(text, InputMode.MANUAL_INGREDIENT_LIST)
                assertFalse(text, result.matched.any { it.id == "milk" })
            }
        // The full analyser preserves "contient" as a dedicated factual-presence
        // declaration.  It must not, however, turn that wording itself into an
        // ingredient alias in the matcher.
        assertTrue(
            IngredientMatcher(knowledge.ingredients)
                .match(IngredientToken("contient du lait", 0, 0))
                .ingredients
                .none { it.id == "milk" }
        )
    }

    @Test fun editorialDataAndGeneratedAssetStayInParityAndImportIsIdempotent() {
        val source = MiniJson.parse(file("knowledge/ingredients.json").readText()) as List<*>
        val milk = source.map { it as Map<*, *> }.single { it["id"] == "milk" }
        val cream = source.map { it as Map<*, *> }.single { it["id"] == "cream" }
        assertEquals("VEGETARIAN", milk["status"])
        assertEquals("VEGETARIAN", cream["status"])
        assertTrue((milk["sources"] as List<*>).contains("eu-preserved-milk-directive-2001-114-20260614"))
        assertTrue((cream["sources"] as List<*>).contains("eu-preserved-milk-directive-2001-114-20260614"))
        assertEquals(1, source.map { (it as Map<*, *>)["id"] }.count { it == "milk" })
        assertEquals(1, knowledge.ingredients.count { it.id == "milk" })
        assertTrue(knowledge.ingredients.single { it.id == "milk" }.aliases.contains("sweetened condensed milk"))
        val process = ProcessBuilder("python", "tools/import_eu_preserved_milk_directive.py", "--dry-run")
            .directory(projectRoot())
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(output, 0, process.waitFor())
        assertTrue(output, output.contains("changes=0"))
    }

    @Test fun milkTracesRemainOutsideTheVerdict() {
        val trace = service.analyzeWithDiagnostics("sucre. Peut contenir : lait en poudre entier")
        assertTrue(trace.result.matched.none { it.id == "milk" })
        assertTrue(trace.result.crossContactWarnings.any { it.contains("lait en poudre entier") })
        assertNull(trace.result.originNonVeganIngredientIds.singleOrNull())
    }

    private fun file(path: String): File {
        val direct = File(path)
        if (direct.isFile) return direct
        return File("..", path).also { require(it.isFile) }
    }

    private fun projectRoot(): File =
        File(".").takeIf { File(it, "tools/import_eu_preserved_milk_directive.py").isFile }
            ?: File("..").also { require(File(it, "tools/import_eu_preserved_milk_directive.py").isFile) }
}
