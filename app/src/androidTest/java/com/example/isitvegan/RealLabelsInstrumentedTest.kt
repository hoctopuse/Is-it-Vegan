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
            assertEquals(note, baseline, result.copy(crossContactWarnings = emptyList()))
            assertEquals(1, result.crossContactWarnings.size)
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
    @Test
    fun smokedTofuProductPreservesSectionsAndNestedCompositions() {
        val label = """
        Coeur au tofu fumé 62,6%: Tofu fumé au bois de hêtre 53,6%
        (eau, graines de SOJA dépelliculées 22,5%, épaississant : Nigari),
        huile de tournesol,
        sauce SOJA 2,5% (eau, graines de SOJA 0,6%, BLE, sel),
        eau,
        farine de lin,
        fibre de chicorée,
        sel.
        Enrobage 37,4% :
        chapelure aux flocons de maïs
        [farine de BLE, flocons de maïs, levure, sel,
        épices (curcuma, paprika)],
        eau,
        farine de BLE,
        huile de tournesol,
        sel.
    """.trimIndent()

        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(label)
        val result = diagnostics.result

        assertEquals(AnalysisVerdict.VEGAN, result.verdict)

        assertTrue(
            "Ingrédients encore non reconnus : ${result.unknown}",
            result.unknown.isEmpty()
        )

        assertTrue(result.crossContactWarnings.isEmpty())

        assertTrue(result.matched.any { it.id == "tofu" })
        assertTrue(result.matched.any { it.id == "soy" })
        assertTrue(result.matched.any { it.id == "sunflower_oil" })
        assertTrue(result.matched.any { it.id == "wheat_flour" })
        assertTrue(result.matched.any { it.id == "corn" })

        // Les titres de sections et les mots de préparation
        // ne sont pas des ingrédients inconnus.
        val unknownText = result.unknown.joinToString(" ").lowercase()

        listOf(
            "coeur",
            "enrobage",
            "fumé",
            "bois",
            "hêtre",
            "dépelliculées",
            "sauce",
            "chapelure"
        ).forEach { unexpected ->
            assertFalse(
                "$unexpected ne devrait pas être un ingrédient inconnu",
                unknownText.contains(unexpected)
            )
        }

        // Les sous-compositions doivent conserver leur profondeur.
        assertTrue(diagnostics.tokens.any {
            it.text.contains("graines de SOJA", ignoreCase = true) &&
                    it.depth > 0
        })

        assertTrue(diagnostics.tokens.any {
            it.text.equals("curcuma", ignoreCase = true) &&
                    it.depth >= 2
        })

        assertTrue(diagnostics.tokens.any {
            it.text.equals("paprika", ignoreCase = true) &&
                    it.depth >= 2
        })
    }

    @Test
    fun realSoyProductUsesNestedRecipesAndPreparationModifiers() {
        val label = """
        protéines de SOJA réhydratées 37% (eau, protéines de SOJA concentrées 15%),
        eau,
        chapelure 14% (farine de BLÉ, eau, huile de colza, levure, sel, extrait de paprika),
        huiles végétales en proportion variable (colza, tournesol),
        farine de BLÉ,
        amidon de maïs,
        vinaigre d'alcool,
        stabilisants (méthylcellulose, gomme guar),
        fibres d'agrumes,
        arômes naturels,
        sel,
        oignon en poudre 0,3%,
        ail en poudre,
        correcteur d'acidité (hydroxyde de potassium).

        Peut contenir: SÉSAME, MOUTARDE, CELERI et OEUF.
    """.trimIndent()

        val result = VeganAnalyzer.analyze(label)

        // L'arôme naturel reste volontairement incertain.
        assertEquals(AnalysisVerdict.UNCERTAIN, result.verdict)
        assertTrue(result.matched.any { it.id == "natural_flavouring" })

        // Tout le reste de la composition doit être reconnu comme vegan.
        assertEquals(AnalysisVerdict.VEGAN, result.verdictWithoutUncertain)
        assertTrue(
            "Ingrédients encore non reconnus : ${result.unknown}",
            result.unknown.isEmpty()
        )

        // Les modificateurs de préparation ne doivent pas devenir des inconnus.
        assertFalse(result.unknown.any {
            it.contains("proteines", ignoreCase = true) ||
                    it.contains("rehydrate", ignoreCase = true) ||
                    it.contains("concentre", ignoreCase = true)
        })

        // Le groupe « huiles végétales » doit transmettre son contexte à ses enfants.
        assertTrue(result.matched.any { it.id == "rapeseed_oil" })
        assertTrue(result.matched.any { it.id == "sunflower_oil" })

        // L'œuf est uniquement présent dans l'avertissement de contamination croisée.
        assertFalse(result.matched.any { it.id == "egg" })
        assertEquals(
            listOf("Peut contenir: SÉSAME, MOUTARDE, CELERI et OEUF."),
            result.crossContactWarnings
        )
    }

    @Test
    fun breadcrumbVerdictComesFromItsDeclaredSubcomposition() {
        val describedBreadcrumb = VeganAnalyzer.analyze(
            "chapelure (farine de blé, eau, huile de colza, levure, sel)"
        )

        assertEquals(AnalysisVerdict.VEGAN, describedBreadcrumb.verdict)
        assertTrue(
            "Résidus inattendus : ${describedBreadcrumb.unknown}",
            describedBreadcrumb.unknown.isEmpty()
        )

        // Une chapelure sans composition détaillée ne doit pas être déclarée
        // automatiquement vegan.
        val unspecifiedBreadcrumb = VeganAnalyzer.analyze("chapelure")

        assertEquals(AnalysisVerdict.INCONCLUSIVE, unspecifiedBreadcrumb.verdict)
        assertEquals(listOf("chapelure"), unspecifiedBreadcrumb.unknown)
    }

    @Test
    fun vegetableOilChildrenInheritTheOilContextFromTheirParent() {
        val result = VeganAnalyzer.analyze(
            "huiles végétales en proportion variable (colza, tournesol)"
        )

        assertEquals(AnalysisVerdict.VEGAN, result.verdict)
        assertTrue(result.unknown.isEmpty())
        assertTrue(result.matched.any { it.id == "rapeseed_oil" })
        assertTrue(result.matched.any { it.id == "sunflower_oil" })
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
