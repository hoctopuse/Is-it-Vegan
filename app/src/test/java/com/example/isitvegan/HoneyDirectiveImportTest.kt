package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HoneyDirectiveImportTest {
    private val knowledge = IngredientKnowledge.fromJson(
        file("app/src/main/assets/ingredients.json").readText(),
        file("app/src/main/assets/ingredient_aliases_multilingual.json").readText(),
        file("app/src/main/assets/origin_qualifier_rules.json").readText()
    )
    private val service = IngredientAnalysisService(knowledge)

    @Test fun honeyDirectiveAliasesResolveInAllFourLanguagesAsVegetarian() {
        mapOf(
            "miel" to "FR", "honing" to "NL", "honey" to "EN", "Honig" to "DE",
            "miel en rayons" to "FR", "slingerhoning" to "NL", "extracted honey" to "EN", "Backhonig" to "DE"
        ).forEach { (text, language) ->
            val result = service.analyze(text, InputMode.MANUAL_INGREDIENT_LIST)
            assertEquals("$language / $text", VeganStatus.VEGETARIAN, result.matched.single { it.id == "honey" }.status)
            assertTrue("$language / $text", result.verdict != AnalysisVerdict.VEGAN)
        }
    }

    @Test fun honeyBlocksVeganVerdictButDoesNotMergeWithBeeswaxOrFlavourWording() {
        val honey = service.analyze("miel", InputMode.MANUAL_INGREDIENT_LIST)
        assertEquals(AnalysisVerdict.VEGETARIAN, honey.verdict)
        assertFalse(honey.matched.any { it.status == VeganStatus.VEGAN })

        val wax = service.analyze("cire d’abeille", InputMode.MANUAL_INGREDIENT_LIST)
        assertTrue(wax.matched.any { it.id == "beeswax" })
        assertFalse(wax.matched.any { it.id == "honey" })

        val flavour = service.analyze("arôme de miel", InputMode.MANUAL_INGREDIENT_LIST)
        assertFalse(flavour.matched.any { it.id == "honey" })
    }

    @Test fun editorialAndGeneratedHoneyDataStayInParityAndImportIsIdempotent() {
        val source = MiniJson.parse(file("knowledge/ingredients.json").readText()) as List<*>
        val honey = (source.map { it as Map<*, *> }.single { it["id"] == "honey" })
        assertEquals("VEGETARIAN", honey["status"])
        assertTrue((honey["sources"] as List<*>).contains("eu-honey-directive-2001-110-20260614"))
        assertEquals(1, source.map { (it as Map<*, *>)["id"] }.count { it == "honey" })
        assertEquals(1, knowledge.ingredients.count { it.id == "honey" })
        val process = ProcessBuilder("python", "tools/import_eu_honey_directive.py", "--dry-run")
            .directory(projectRoot())
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(output, 0, process.waitFor())
        assertTrue(output, output.contains("changes=0"))
    }

    @Test fun honeyTracesRemainOutsideTheVerdict() {
        val trace = service.analyzeWithDiagnostics("sucre. Peut contenir : miel")
        assertTrue(trace.result.matched.none { it.id == "honey" })
        assertTrue(trace.result.crossContactWarnings.any { it.contains("miel") })
        assertNull(trace.result.originNonVeganIngredientIds.singleOrNull())
    }

    private fun file(path: String): File {
        val direct = File(path)
        if (direct.isFile) return direct
        return File("..", path).also { require(it.isFile) }
    }

    private fun projectRoot(): File =
        File(".").takeIf { File(it, "tools/import_eu_honey_directive.py").isFile }
            ?: File("..").also { require(File(it, "tools/import_eu_honey_directive.py").isFile) }
}
