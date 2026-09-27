package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EuAdditivesReference06910Test {
    private val database = runtimeDatabase()
    private val imported = database.filter { it.source == "eu-additives" && it.id !in historicalEuIds }

    @Test fun importedReferenceIsStructurallySafeAndAllNewEntriesRemainUncertain() {
        assertEquals(database.size, database.map { it.id }.distinct().size)
        val numbered = database.filter { !it.eNumber.isNullOrBlank() }
        assertEquals(numbered.size, numbered.map { it.eNumber!!.uppercase() }.distinct().size)
        assertEquals(60, imported.size)
        assertTrue(imported.all { it.status == VeganStatus.UNCERTAIN })
        assertTrue(imported.all { it.eNumber!!.matches(Regex("E\\d{3}[A-Za-z]?")) })
        assertTrue(imported.all { it.aliases.size >= 4 && it.reason.isNotBlank() })
    }

    @Test fun historicClassificationsArePreserved() {
        val expected = mapOf("e120" to VeganStatus.NON_VEGAN, "e330" to VeganStatus.VEGAN, "e471" to VeganStatus.UNCERTAIN, "e904" to VeganStatus.NON_VEGAN)
        expected.forEach { (id, status) -> assertEquals(status, database.single { it.id == id }.status) }
    }

    @Test fun representativeFamiliesAndFourLanguagesResolveToUncertainAdditives() {
        val cases = listOf("curcumine" to "e100", "sorbic acid" to "e200", "Butylhydroxytoluol" to "e321", "natriumalginaat" to "e401", "Polysorbate 80" to "e433", "cire de carnauba" to "e903", "stikstof" to "e941", "L cysteine" to "e920")
        cases.forEach { (text, id) ->
            val result = VeganAnalyzer.analyze(text, database)
            assertEquals(text, AnalysisVerdict.UNCERTAIN, result.verdict)
            assertEquals(text, listOf(id), result.matched.map { it.id })
        }
    }

    @Test fun sameAdditiveNameResolvesInFrenchDutchEnglishAndGerman() {
        listOf("acide sorbique", "sorbinezuur", "sorbic acid", "Sorbinsäure").forEach { text ->
            val result = VeganAnalyzer.analyze(text, database)
            assertEquals(text, AnalysisVerdict.UNCERTAIN, result.verdict)
            assertEquals(text, listOf("e200"), result.matched.map { it.id })
        }
    }

    @Test fun eNumberFormsAndCategoryPrefixResolveWithoutRecognizingBareNumbers() {
        listOf("E433", "E 433", "e433", "e 433").forEach { form ->
            val result = VeganAnalyzer.analyze(form, database)
            assertEquals(form, AnalysisVerdict.UNCERTAIN, result.verdict)
            assertEquals(form, listOf("e433"), result.matched.map { it.id })
        }
        val category = VeganAnalyzer.analyze("émulsifiant : E433", database)
        assertEquals(AnalysisVerdict.UNCERTAIN, category.verdict)
        assertEquals(listOf("e433"), category.matched.map { it.id })
        assertTrue(IngredientMatcher(database).match(IngredientToken("330", 0, 0)).ingredients.isEmpty())
    }

    @Test fun traceWithENumberRemainsOutsideTheVerdict() {
        val result = VeganAnalyzer.analyze("sucre. Peut contenir : E433.", database)
        assertEquals(AnalysisVerdict.VEGAN, result.verdict)
        assertFalse(result.matched.any { it.id == "e433" })
        assertEquals(listOf("Peut contenir : E433."), result.crossContactWarnings)
    }

    private fun runtimeDatabase(): List<Ingredient> = (MiniJson.parse(File("src/main/assets/ingredients.json").readText()) as List<*>).map { value ->
        val item = value as Map<*, *>
        Ingredient(item["id"] as String, item["name"] as String, (item["aliases"] as List<*>).filterIsInstance<String>(), item["eNumber"] as? String, VeganStatus.valueOf(item["status"] as String), item["reason"] as String, item["source"] as? String)
    }

    private companion object {
        val historicalEuIds = setOf("e120", "e170", "e202", "e220", "e262", "e270", "e300", "e306", "e322", "e330", "e332", "e340", "e406", "e410", "e412", "e415", "e418", "e422", "e440", "e471", "e500", "e503", "e524", "e551", "e570", "e621", "e627", "e631", "e635", "e904", "e950", "e960")
    }
}
