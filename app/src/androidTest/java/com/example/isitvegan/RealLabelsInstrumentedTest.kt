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
}
