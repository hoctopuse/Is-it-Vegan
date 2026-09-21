package com.example.isitvegan

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Version0511InstrumentedTest {
    @Before fun loadBundledDatabase() {
        VeganAnalyzer.loadDatabase(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test fun committedFruitAndVegetableEntriesMatchFromTheBundledAsset() {
        val expected = mapOf(
            "aubergine" to "aubergine",
            "tomates" to "tomato",
            "purée de carotte" to "carrot",
            "ail" to "garlic",
            "courgette" to "zucchini",
            "poivron" to "bell_pepper",
            "poireau" to "leek",
            "champignon" to "mushroom",
            "courge" to "pumpkin",
            "abricot" to "apricot",
            "pomme" to "apple",
            "poire" to "pear",
            "banane" to "banana",
            "pêche" to "peach",
            "fraise" to "strawberry"
        )

        expected.forEach { (label, id) ->
            val result = VeganAnalyzer.analyze(label)
            assertEquals(label, listOf(id), result.matched.map { it.id })
            assertTrue(label, result.unknown.isEmpty())
            assertEquals(label, AnalysisVerdict.VEGAN, result.verdict)
        }
    }
}
