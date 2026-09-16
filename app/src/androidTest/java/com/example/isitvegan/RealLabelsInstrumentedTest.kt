package com.example.isitvegan

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Real labels supplied by the user; use the actual bundled, offline database. */
@RunWith(AndroidJUnit4::class)
class RealLabelsInstrumentedTest {
    @Before fun loadBundledDatabase() {
        VeganAnalyzer.loadDatabase(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    @Test fun crossContactDoesNotChangeAnOtherwiseFullyKnownRecipe() {
        val recipe = "eau, sucre"
        val baseline = VeganAnalyzer.analyze(recipe)
        assertEquals(AnalysisVerdict.VEGAN, baseline.verdict)
        assertTrue(baseline.unknown.isEmpty())

        listOf(
            ". Peut contenir du lait et de la gélatine",
            "\n**Traces :** lait, gélatine",
            ". Fabriqué dans un atelier utilisant du lait et de la gélatine"
        ).forEach { note ->
            val result = VeganAnalyzer.analyze(recipe + note)
            assertEquals(note, baseline, result)
        }
    }

    @Test fun actualAnimalIngredientIsNotHiddenByATrailingTraceNote() {
        val result = VeganAnalyzer.analyze(
            "eau, sucre, gélatine. Peut contenir du lait"
        )
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.verdict)
        assertTrue(result.matched.any { it.status == VeganStatus.NON_VEGAN })
        assertFalse(result.matched.any { it.status == VeganStatus.VEGETARIAN })
    }

    @Test fun addingAnUnknownIngredientPreventsAVeganVerdict() {
        val result = VeganAnalyzer.analyze("eau, sucre, ingrédient mystère")
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdict)
        assertEquals(listOf("ingrédient mystère"), result.unknown)
        assertTrue(result.matched.isNotEmpty())
    }

    @Test fun plantBasedPreparedDishIsNotDeclaredVeganWithoutFullCoverage() {
        val label = """morceaux végétaliens (36%) [eau, protéine de SOJA, amidon de BLÉ, gluten de BLÉ, vinaigre]; huile de colza; pois chiche; eau; oignon; herbes préparé (1,5%) (épices (contient: MOUTARDE)); sirop de glucose; extrait de levure; arôme naturel; poudre de tomate; amidon; fibre végétale; protéine de pomme de terre; sel; plantes aromatiques (contient: MOUTARDE); vinaigre; MOUTARDE; sucre; jus de citron; mélasse; tamarin; gingembre; extrait d’ail; extrait de paprika; amidon modifié; acidifiants (acide acétique, acide lactique, acide citrique); conservateur (E202); stabilisants (gomme guar, gomme xanthane)"""
        val result = VeganAnalyzer.analyze(label)
        assertEquals(AnalysisVerdict.UNCERTAIN, result.verdict)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdictWithoutUncertain)
        assertTrue(result.matched.any { it.id == "natural_flavouring" })
        assertTrue(result.matched.any { it.id == "soy" })
        assertTrue(result.unknown.isNotEmpty())
    }

    @Test fun porkDishIsNotVegetarianEvenWithUnknownIngredients() {
        val label = """viande de porc (64%) (origine: Belgique); huile de colza; eau; jaune d’OEUF; vinaigre; MOUTARDE; épices (contient: MOUTARDE); sirop de glucose; extrait de levure; arôme; tomate; sucre; sel; extrait d’épice; jus de betterave rouge; mélasse; oignon; tamarinde; acidifiants (acide lactique, acide acétique, acide ascorbique); stabilisants (E412, E415); conservateur (E202)."""
        val result = VeganAnalyzer.analyze(label)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.verdict)
        assertTrue(result.matched.any { it.id == "meat" })
        assertFalse(result.matched.any { it.id == "egg" })
        assertTrue(result.stoppedAtNonVegetarian)
    }

    @Test fun tofuLabelDoesNotTreatTracesAsRecipeIngredients() {
        val label = """Eau, graines de soja*, présure (nigari), chlorure de calcium. *Agriculture biologique.
            **Allergènes :** soja
            **Traces :** céleri, gluten, lupin, moutarde, fruits à coque, graines de sésame""".trimIndent()
        val result = VeganAnalyzer.analyze(label)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdict)
        assertTrue(result.matched.any { it.id == "soy" })
        assertTrue(result.unknown.any { it.contains("nigari") })
        assertFalse(result.unknown.any { it.contains("lupin") })
    }

    @Test fun stuffedPastaWithNestedPercentagesDetectsAnimalRennet() {
        val label = """Farce (63%): ricotta de lait de buflonne 23,8% (lactoserum, sel), ricotta 23,5% (lactoserum, sel), sautéed épinards 22,5% (épinards 70%, beurre (lait), eau, formage Grana Padano AOP (lait, oeufs), sel, amidon de mais, ail), beurre (lait), chapelure (farine de blé dur, sel, levure), lactose, Grana Padano AOP (lait, oeufs), sel. Pate (37%): farine de blé, oeufs 28,5%, semoule de blé dur."""
        val result = VeganAnalyzer.analyze(label)
        assertEquals(AnalysisVerdict.NON_VEGETARIAN, result.verdict)
        assertTrue(result.matched.any { it.id == "grana_padano" })
        assertTrue(result.stoppedAtNonVegetarian)
        assertEquals(emptyList<String>(), result.unknown)
    }

    @Test fun cashewDrinkRemovesQuantitiesAndCrossContactNotes() {
        val label = """Eau, sucre, 2,9% CAJOU partiellement dégraissé, 1,7% poudre de cacao¹, 0,5% protéine de pois, fructose, huile de coco, sel, arôme, carbonate de calcium, correcteur d'acidité (phosphates de potassium, citrates de sodium), stabilisant (cellulose, gomme cellulosique, gomme gellane). Peut contenir du soja, des amandes, des noisettes et des noix.¹Rainforest Alliance Certified. Find out more at ra.org"""
        val result = VeganAnalyzer.analyze(label)
        assertEquals(AnalysisVerdict.INCONCLUSIVE, result.verdict)
        assertTrue(result.matched.any { it.id == "cocoa" })
        assertTrue(result.matched.any { it.id == "peas" })
        assertTrue(result.unknown.any { it.contains("CAJOU", ignoreCase = true) })
        assertFalse(result.unknown.any { it.contains("protéine", ignoreCase = true) })
        assertFalse(result.unknown.any { it.contains(Regex("\\d")) })
        assertFalse(result.unknown.any { it.contains("amandes", ignoreCase = true) })
        assertFalse(result.unknown.any { it.contains("Rainforest", ignoreCase = true) })
    }
}
