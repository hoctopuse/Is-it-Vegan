package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Version059CompletionTest {
    private val database = listOf(
        ingredient("salt", "sel"), ingredient("sugar", "sucre"),
        ingredient("milk", "lait", VeganStatus.VEGETARIAN),
        ingredient("egg", "œuf", VeganStatus.VEGETARIAN, "œufs", "oeuf", "oeufs", "blanc d'œuf"),
        ingredient("butter", "beurre", VeganStatus.VEGETARIAN),
        ingredient("cream", "crème", VeganStatus.VEGETARIAN),
        ingredient("wheat", "blé"),
        ingredient("cocoa", "cacao"), ingredient("peanut", "cacahuète"),
        ingredient("coconut", "coco"), ingredient("apricot", "abricot"),
        ingredient("palm", "palme"), ingredient("celery", "céleri"),
        ingredient("natural_flavouring", "arôme naturel", VeganStatus.UNCERTAIN, "arômes naturels"),
        ingredient("glucose_syrup", "sirop de glucose", aliases = arrayOf("glucose syrup", "glucosestroop")),
        ingredient("e471", "E471", VeganStatus.UNCERTAIN)
    )

    @Test fun extendedIngredientHeadingsCreateBoundedLanguageBlocks() {
        val samples = listOf(
            "Ingrédients du bouillon déshydraté : sel" to LabelLanguage.FRENCH,
            "Ingrédients de la préparation : sel" to LabelLanguage.FRENCH,
            "Ingrédients de la sauce : sel" to LabelLanguage.FRENCH,
            "Ingrediënten van de gedehydrateerde bouillon: zout" to LabelLanguage.DUTCH,
            "Ingredients of the filling: sugar" to LabelLanguage.ENGLISH,
            "Zutaten der Füllung: Zucker" to LabelLanguage.GERMAN,
            "Ingredientes del caldo deshidratado: sal" to LabelLanguage.SPANISH
        )
        samples.forEach { (text, language) ->
            val segmentation = LabelLanguageSegmenter.segment(text)
            val sections = LabelSectionExtractor.extract(segmentation.blocks.single())
            assertEquals(text, language, segmentation.selectedLanguage)
            assertTrue(text, sections.hasIngredientHeading)
            assertEquals(text, text.substringAfter(':').trim(), sections.ingredientsText)
        }
    }

    @Test fun allEndSectionHeadingsStopIngredientsAtTopLevel() {
        listOf(
            "Préparation", "Conservation", "Quantité nette",
            "À consommer de préférence avant", "Lot", "Conseils de conservation",
            "Fabriqué en", "Bereiding", "Bewaring", "Preparación", "Conservación", "Storage"
        ).forEach { heading ->
            val sections = LabelSectionExtractor.extract(
                LabelLanguage.FRENCH,
                "Ingrédients : eau, sucre. $heading : texte hors composition"
            )
            assertTrue(heading, sections.ingredientsText!!.contains("eau, sucre"))
            assertFalse(heading, sections.ingredientsText.contains("texte hors composition"))
            assertTrue(heading, sections.ignoredSections.isNotEmpty())
        }
    }

    @Test fun brothDescriptionAndEndSectionsNeverReachTheTree() {
        val text = """Bouillon de légumes (5,8%), déshydraté et aromatisé. Ingrédients du bouillon déshydraté : sel, amidon de pomme de terre, sucre, graisse végétale de karité, extrait de levure, oignon 4,5%, arômes naturels, carotte 0,5%, curcuma, graines de céleri moulu, arôme naturel de céleri, noix de muscade, tomate 0,2%, persil 0,15%, poivre blanc, coriandre. Peut contenir des traces de céréales contenant du gluten, crustacés, lait, moutarde, oeufs, poissons, soja.

Préparation : Pour préparer 1 litre de bouillon, dissoudre 1 cube dans l’eau.
Conservation : À conserver dans un endroit frais et sec.

Gedroogde en gearomatiseerde groentebouillon. Ingrediënten van de gedehydrateerde bouillon: zout, aardappelzetmeel, suiker."""
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database)
        val treeText = diagnostics.tokens.joinToString(" ") { it.text }

        assertEquals(LabelLanguage.FRENCH, diagnostics.labelSections.language)
        assertFalse(diagnostics.languageSegmentation.usedFallback)
        assertTrue(diagnostics.preprocessedInput.trimStart().startsWith("sel"))
        listOf("Bouillon de légumes", "déshydraté et aromatisé", "Préparation", "Conservation", "zout")
            .forEach { assertFalse(it, treeText.contains(it, ignoreCase = true)) }
        assertTrue(diagnostics.crossContactWarnings.single().contains("céréales"))
    }

    @Test fun regulatoryFunctionalClassesProduceAdditivesAndGroups() {
        val singleCases = listOf(
            "gélifiant : pectine", "correcteur d’acidité : acide citrique",
            "correcteur d’acidité (acide citrique)", "émulsifiant : lécithines (soja)",
            "émulsifiants (mono- et diglycérides d’acides gras)",
            "poudres à lever : diphosphates et carbonates de sodium",
            "colorant : caramel ordinaire"
        )
        singleCases.forEach { text ->
            val node = IngredientTreeParser.parse(text).single()
            assertEquals(text, IngredientNodeKind.ADDITIVE, node.kind)
            assertNotNull(text, node.functionalClass)
        }
        val group = IngredientTreeParser.parse(
            "agents levants (carbonate d’ammonium, diphosphate disodique, carbonate acide de sodium)"
        ).single()
        assertEquals(IngredientNodeKind.COMPOSITE, group.kind)
        assertEquals(3, group.children.size)
        assertTrue(group.children.all {
            it.kind == IngredientNodeKind.ADDITIVE && it.functionalClass == "agents levants"
        })
    }

    @Test fun matcherDistinguishesCoveredPartialAndBlockedMatches() {
        val matcher = IngredientMatcher(database)
        mapOf(
            "LAIT écrémé en poudre" to "milk",
            "lait entier en poudre" to "milk",
            "blanc d’ŒUF en poudre" to "egg",
            "arôme naturel de vanille" to "natural_flavouring",
            "sirop de glucose-fructose" to "glucose_syrup",
            "sirop de glucose–fructose" to "glucose_syrup",
            "sirop de glucose fructose" to "glucose_syrup",
            "glucose-fructose syrup" to "glucose_syrup",
            "glucose-fructosestroop" to "glucose_syrup",
            "pâte de cacao" to "cocoa",
            "purée d’abricot" to "apricot",
            "graisse de palme" to "palm",
            "graines de céleri" to "celery"
        ).forEach { (text, id) ->
            val match = matcher.match(IngredientToken(text, 0, 0))
            assertEquals(text, MatchResolution.COVERED, match.resolution)
            assertEquals(text, listOf(id), match.ingredients.map { it.id })
            assertEquals(text, null, UnknownCollector.collect(match))
        }

        val partial = matcher.match(IngredientToken("semoule de BLÉ dur", 0, 0))
        assertEquals(MatchResolution.PARTIAL_CONTEXTUAL, partial.resolution)
        assertEquals("semoule de BLÉ dur", UnknownCollector.collect(partial))

        listOf("beurre de cacao", "beurre de cacahuète", "lait de coco", "crème de coco")
            .forEach { text ->
                val match = matcher.match(IngredientToken(text, 0, 0))
                assertFalse(text, match.ingredients.any { it.id in setOf("butter", "milk", "cream") })
            }
        val cocoaButter = matcher.match(IngredientToken("beurre de cacao", 0, 0))
        assertEquals(MatchResolution.BLOCKED_CONFLICT, cocoaButter.resolution)
        assertEquals(listOf("cocoa"), cocoaButter.ingredients.map { it.id })
        assertEquals("beurre de cacao", UnknownCollector.collect(cocoaButter))
        assertTrue(
            matcher.match(IngredientToken("beurre au cacao", 0, 0))
                .ingredients.any { it.id == "butter" }
        )
    }

    @Test fun veganCompatibilityDoesNotHideCertainBlockersBehindUncertainty() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics("œufs, lait, E471", database)
        val result = diagnostics.result
        assertEquals(VeganAssessment.NOT_VEGAN, result.veganAssessment)
        assertEquals(AnalysisVerdict.UNCERTAIN, result.verdict)
        assertEquals(AnalysisVerdict.VEGETARIAN, result.verdictWithoutUncertain)
        assertEquals(setOf("egg", "milk"), result.veganBlockers.map { it.id }.toSet())
        assertFalse(result.stoppedAtNonVegetarian)
        val report = DiagnosticReport.build(diagnostics, "0.5.9")
        assertTrue(report.contains("Compatibilité vegan : NON VEGAN"))
        assertTrue(report.contains("Bloqueurs détectés : egg, milk"))
        assertTrue(report.contains("Classification détaillée : UNCERTAIN"))
    }

    @Test fun repeatedUnknownsAreDeduplicatedByNormalizedForm() {
        val result = VeganAnalyzer.analyze(
            "sirop mystérieux, SIROP MYSTÉRIEUX, autre inconnu",
            emptyList()
        )
        assertEquals(listOf("sirop mystérieux", "autre inconnu"), result.unknown)
    }

    @Test fun apricotBriocheKeepsCoveredMilkEggAndFunctionalGroups() {
        val text = """INGRÉDIENTS : farine de BLÉ (28%), confiture d'abricot 22% (sucre, purée d'abricot 41%, sirop de glucose-fructose, gélifiant : pectine, correcteur d'acidité : acide citrique), sucre, ŒUFS (12,5%), huile de palme non hydrogénée, flocons moulus (2,5%) d'ORGE et d'AVOINE ; purée de carotte, sirop de glucose-fructose, arôme, agents levants (carbonate d'ammonium, diphosphate disodique, carbonate acide de sodium), LAIT écrémé en poudre, émulsifiants (mono- et diglycérides d'acides gras), amidon de BLÉ, blanc d'ŒUF en poudre, sel, correcteur d'acidité (acide citrique).

Peut contenir : SOJA, AMANDES, NOISETTES, MOUTARDE."""
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database)
        assertEquals(VeganAssessment.NOT_VEGAN, diagnostics.result.veganAssessment)
        assertFalse(diagnostics.result.unknown.any { it.contains("LAIT écrémé", true) })
        assertFalse(diagnostics.result.unknown.any { it.contains("blanc d'ŒUF", true) })
        assertTrue(diagnostics.tokens.any {
            it.nodeKind == IngredientNodeKind.ADDITIVE &&
                it.functionalClass?.contains("correcteur", true) == true
        })
        assertTrue(diagnostics.tokens.any {
            it.nodeKind == IngredientNodeKind.COMPOSITE &&
                it.functionalClass?.contains("agents levants", true) == true
        })
        assertFalse(diagnostics.result.matched.any { it.id == "milk" && it.name.contains("SOJA") })
    }

    @Test fun chocolateBiscuitsBlockCocoaButterConflictAndKeepRealButter() {
        val text = """Ingrédients : Pépites de chocolat 29,2% [sucre, pâte de cacao, sirop de glucose, beurre de cacao, émulsifiant : lécithines (soja)], farine de blé 27,5%, sucre, graisse de palme, morceaux de chocolat au lait 11,2% [sucre, lait entier en poudre, beurre de cacao, pâte de cacao, sirop de glucose, lait écrémé en poudre, émulsifiant : lécithines (soja), arôme naturel de vanille], sirop de glucose-fructose, beurre, huile de tournesol, oeufs, lait écrémé en poudre, poudres à lever : diphosphates et carbonates de sodium, amidon de blé, colorant : caramel ordinaire, arôme naturel.

Peut contenir des traces de fruits à coque."""
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database)
        val cocoaButter = diagnostics.tokens.filter { it.text.equals("beurre de cacao", true) }
        assertTrue(cocoaButter.isNotEmpty())
        assertTrue(cocoaButter.all { "butter" !in it.matchedIngredientIds })
        assertTrue(diagnostics.tokens.single { it.text.equals("beurre", true) }
            .matchedIngredientIds.contains("butter"))
        listOf("lait entier en poudre", "lait écrémé en poudre", "arôme naturel de vanille", "sirop de glucose-fructose")
            .forEach { covered ->
                assertEquals(covered, MatchResolution.COVERED,
                    diagnostics.tokens.first { it.text.equals(covered, true) }.matchKind)
            }
        assertEquals(VeganAssessment.NOT_VEGAN, diagnostics.result.veganAssessment)
        assertFalse(diagnostics.result.matched.any { it.id == "milk" && it.name.contains("trace", true) })
    }

    private fun ingredient(
        id: String,
        alias: String,
        status: VeganStatus = VeganStatus.VEGAN,
        vararg extraAliases: String,
        aliases: Array<String> = emptyArray()
    ) = Ingredient(id, alias, listOf(alias) + extraAliases + aliases, null, status, "Test")
}
