package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Version066OcrNormalizationTest {
    private val lexicon = MultilingualIngredientLexicon.load(
        File("src/main/assets/ingredient_aliases_multilingual.json").readText()
    )
    private val database = listOf(
        ingredient("oats", "avoine"), ingredient("sunflower_oil", "huile de tournesol"),
        ingredient("wheat_flour", "farine de blé"), ingredient("cocoa", "cacao")
    )

    @Test fun correctionsAreBoundedToTheSelectedLanguageAndRemainExplainable() {
        val sunflower = lexicon.resolve("zonnebloemole", LabelLanguage.DUTCH, database)
        val oats = lexicon.resolve("havervokken", LabelLanguage.DUTCH, database)
        val noisyOats = lexicon.resolve("haerioken", LabelLanguage.DUTCH, database)
        val cocoa = lexicon.resolve("Kakaopulvert", LabelLanguage.GERMAN, database)
        val palm = lexicon.resolve("Olpalme", LabelLanguage.GERMAN, database)
        assertEquals("zonnebloemolie", sunflower.correctedText)
        assertEquals("sunflower_oil", sunflower.canonicalId)
        assertEquals("havervlokken", oats.correctedText)
        assertEquals("oats", oats.canonicalId)
        assertEquals("havervlokken", noisyOats.correctedText)
        assertEquals("oats", noisyOats.canonicalId)
        assertEquals("Kakaopulver", cocoa.correctedText)
        assertEquals("cocoa", cocoa.canonicalId)
        assertEquals("Ölpalme", palm.correctedText)
        assertEquals("palm_oil", palm.canonicalId)
        assertEquals(false, palm.canonicalAvailable)
        assertNull(lexicon.resolve("havervokken", LabelLanguage.FRENCH, database).canonicalId)
        assertEquals("Weizenmehl", lexicon.resolve("Weizenmell", LabelLanguage.GERMAN, database).correctedText)
        assertEquals("qlucose", lexicon.resolve("qlucose", LabelLanguage.FRENCH, database).correctedText)
        assertEquals("lécithines", lexicon.resolve("écithines", LabelLanguage.FRENCH, database).correctedText)
    }

    private fun ingredient(id: String, name: String) =
        Ingredient(id, name, listOf(name), null, VeganStatus.VEGAN, "test")
}
