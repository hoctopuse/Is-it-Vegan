package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Version066MultilingualLexiconTest {
    private val lexicon = MultilingualIngredientLexicon.load(
        File("src/main/assets/ingredient_aliases_multilingual.json").readText()
    )
    private val database = listOf(
        ingredient("wheat_flour", "farine de blé"), ingredient("oats", "avoine"),
        ingredient("sunflower_oil", "huile de tournesol"), ingredient("e322", "lécithines")
    )

    @Test fun aliasesResolveOnlyInTheirReviewedLanguage() {
        assertEquals("wheat_flour", lexicon.resolve("tarwebloem", LabelLanguage.DUTCH, database).canonicalId)
        assertEquals("oats", lexicon.resolve("Haferflocken", LabelLanguage.GERMAN, database).canonicalId)
        assertEquals("sunflower_oil", lexicon.resolve("aceite de girasol", LabelLanguage.SPANISH, database).canonicalId)
        assertNull(lexicon.resolve("tarwebloem", LabelLanguage.GERMAN, database).canonicalId)
    }

    @Test fun absentCanonicalConceptIsNeverPromotedToAVeganMatch() {
        val resolution = lexicon.resolve("gevriesdroogde bramen", LabelLanguage.DUTCH, database)

        assertEquals("blackberry", resolution.canonicalId)
        assertEquals("gevriesdroogde bramen", resolution.alias)
        assertFalse(resolution.canonicalAvailable!!)
    }

    @Test fun assetSchemaIsValidAndUnavailableConceptsAreReported() {
        val canonicalDatabase = (MiniJson.parse(
            File("src/main/assets/ingredients.json").readText()
        ) as List<*>).map { value ->
            val item = value as Map<*, *>
            ingredient(item["id"] as String, item["name"] as String)
        }

        val validation = lexicon.validateAgainst(canonicalDatabase)

        assertTrue(validation.errors.joinToString(), validation.isValid)
        assertEquals(listOf("cereals"), validation.unavailableCanonicalIds)
    }

    @Test fun reviewedCanonicalConceptsAndTheirMultilingualAliasesAreAvailable() {
        val canonicalDatabase = runtimeDatabase()
        val expected = mapOf(
            "barley" to VeganStatus.VEGAN, "rye" to VeganStatus.VEGAN,
            "malt" to VeganStatus.VEGAN, "blackberry" to VeganStatus.VEGAN,
            "blueberry" to VeganStatus.VEGAN, "palm_oil" to VeganStatus.VEGAN,
            "palm_kernel" to VeganStatus.VEGAN, "almond" to VeganStatus.VEGAN,
            "inulin" to VeganStatus.VEGAN, "e170" to VeganStatus.VEGAN,
            "e418" to VeganStatus.VEGAN, "e332" to VeganStatus.VEGAN,
            "e503" to VeganStatus.VEGAN
        )

        expected.forEach { (id, status) ->
            assertEquals(status, canonicalDatabase.single { it.id == id }.status)
        }
        assertEquals("barley", lexicon.resolve("orzo", LabelLanguage.ITALIAN, canonicalDatabase).canonicalId)
        assertEquals("blueberry", lexicon.resolve("Heidelbeeren", LabelLanguage.GERMAN, canonicalDatabase).canonicalId)
        assertEquals("e503", lexicon.resolve("carbonatos de amonio", LabelLanguage.SPANISH, canonicalDatabase).canonicalId)
        val generic = lexicon.resolve("granen", LabelLanguage.DUTCH, canonicalDatabase)
        assertEquals("cereals", generic.canonicalId)
        assertFalse(generic.canonicalAvailable!!)
    }

    @Test fun invalidOrDuplicatedEntriesAreRejectedAtLoadTime() {
        val invalid = """
            {
              "schemaVersion": 1,
              "aliases": [
                {"canonicalId":"oats","language":"XX","aliases":["haver","haver"],"ocrVariants":[]}
              ],
              "ocrCorrections": []
            }
        """.trimIndent()

        val error = runCatching { MultilingualIngredientLexicon.load(invalid) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    private fun ingredient(id: String, name: String) =
        Ingredient(id, name, listOf(name), null, VeganStatus.VEGAN, "test")

    private fun runtimeDatabase(): List<Ingredient> = (MiniJson.parse(
        File("src/main/assets/ingredients.json").readText()
    ) as List<*>).map { value ->
        val item = value as Map<*, *>
        Ingredient(
            id = item["id"] as String,
            name = item["name"] as String,
            aliases = (item["aliases"] as List<*>).filterIsInstance<String>(),
            eNumber = item["eNumber"] as? String,
            status = VeganStatus.valueOf(item["status"] as String),
            reason = item["reason"] as String,
            source = item["source"] as? String
        )
    }
}
