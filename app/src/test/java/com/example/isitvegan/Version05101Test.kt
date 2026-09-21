package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Version05101Test {
    private val database = listOf(
        ingredient("sugar", "Sucre"),
        ingredient("wheat", "Blé", aliases = arrayOf("farine de blé", "amidon de blé")),
        ingredient("cocoa", "Cacao", extraAliases = arrayOf("pâte de cacao", "cocoa")),
        ingredient("sunflower", "Tournesol", aliases = arrayOf("huile de tournesol")),
        ingredient("glucose_syrup", "Sirop de glucose"),
        ingredient("milk", "Lait", VeganStatus.VEGETARIAN,
            "lait en poudre entier", "lait en poudre écrémé", "milk"),
        ingredient("butter", "Beurre", VeganStatus.VEGETARIAN, "butter"),
        ingredient("egg", "Œuf", VeganStatus.VEGETARIAN, "oeuf", "œufs", "oeufs"),
        ingredient("soy", "Soja"),
        ingredient("e322", "Lécithines", VeganStatus.UNCERTAIN, "lécithine", "lecithine", "lecithin", "lecithinen"),
        ingredient("e500", "Carbonates de sodium", eNumber = "E500"),
        ingredient("natural_flavouring", "Arôme naturel", VeganStatus.UNCERTAIN),
        ingredient("coconut", "Coco", extraAliases = arrayOf("coconut")),
        ingredient("peanut", "Cacahuète", extraAliases = arrayOf("peanut")),
        ingredient("cream", "Crème", VeganStatus.VEGETARIAN, "cream")
    )

    @Test fun referencedFootnotesAreExcludedAsTwoLogicalNotes() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            """Ingrédients : pâte de cacao*, œuf**.

*Certifié Rainforest Alliance. Pour en savoir plus, rendez-vous sur ra.org.
**Issu de poules élevées au sol.""",
            database,
            InputMode.FULL_LABEL
        )

        assertEquals(listOf("pâte de cacao", "œuf"), diagnostics.tokens.map { it.text })
        assertEquals(2, diagnostics.excludedNotes.size)
        assertTrue(diagnostics.excludedNotes[0].contains("rendez-vous sur ra.org"))
        assertEquals("Issu de poules élevées au sol.", diagnostics.excludedNotes[1])
        assertTrue(diagnostics.result.unknown.isEmpty())
        assertTrue(diagnostics.result.matched.any { it.id == "egg" })
    }

    @Test fun superscriptReferenceIsRemovedOnlyWhenItsNoteExists() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            """Ingrédients : pâte de cacao¹, sucre.
¹Rainforest Alliance Certified. Find out more at ra.org.""",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(listOf("pâte de cacao", "sucre"), diagnostics.tokens.map { it.text })
        assertEquals(1, diagnostics.excludedNotes.size)
        assertTrue(diagnostics.result.unknown.isEmpty())
    }

    @Test fun referencedNoteMayContinueOnSeveralLines() {
        val result = LabelPreprocessor.preprocess(
            """pâte de cacao*
*Certifié Rainforest Alliance.
Pour en savoir plus, rendez-vous sur ra.org."""
        )
        assertEquals(listOf("Certifié Rainforest Alliance. Pour en savoir plus, rendez-vous sur ra.org."),
            result.excludedNotes)
        assertEquals("pâte de cacao\n\n", result.compositionText)
    }

    @Test fun storageBoundariesDoNotExtendTraceOrComposition() {
        val text = """Ingrédients : sucre.
Peut contenir des traces de lait et de fruits à coque.

Non ouvert, à consommer de préférence avant le : voir sur le côté.
À conserver à l’abri de la chaleur.
Après ouverture, à consommer sous 7 jours."""
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL)

        assertEquals("Peut contenir des traces de lait et de fruits à coque.",
            diagnostics.crossContactWarnings.single())
        assertEquals(listOf("sucre"), diagnostics.tokens.map { it.text })
        assertTrue(diagnostics.labelSections.ignoredSections.first().startsWith("Non ouvert"))
        assertTrue(diagnostics.labelSections.ignoredSections.any { it.startsWith("À conserver") })
        assertTrue(diagnostics.labelSections.ignoredSections.any { it.startsWith("Après ouverture") })
        assertFalse(diagnostics.preprocessedInput.contains("conserver", ignoreCase = true))
    }

    @Test fun multilingualStorageBoundariesDoNotRequireAColon() {
        listOf(
            LabelLanguage.DUTCH to "Na opening koel bewaren",
            LabelLanguage.ENGLISH to "After opening keep refrigerated",
            LabelLanguage.GERMAN to "Nach dem Öffnen kühl lagern",
            LabelLanguage.SPANISH to "Una vez abierto conservar refrigerado"
        ).forEach { (language, boundary) ->
            val sections = LabelSectionExtractor.extract(
                language,
                "Ingrédients : sucre. Peut contenir du lait.\n$boundary"
            )
            assertEquals(boundary, "Peut contenir du lait.", sections.tracesText)
            assertTrue(boundary, sections.ignoredSections.single().startsWith(boundary))
        }
    }

    @Test fun functionalClassScopeContinuesAcrossCommasAndStopsAtSemicolon() {
        val nodes = IngredientTreeParser.parse(
            "poudres à lever : diphosphates, carbonates de sodium ; sel"
        )
        val group = nodes.first()
        assertEquals(IngredientNodeKind.COMPOSITE, group.kind)
        assertEquals(listOf("diphosphates", "carbonates de sodium"), group.children.map { it.rawText })
        assertTrue(group.children.all {
            it.kind == IngredientNodeKind.ADDITIVE &&
                it.functionalClassCanonical == FunctionalClass.RAISING_AGENT
        })
        assertEquals("sel", nodes.last().rawText)
        assertEquals(IngredientNodeKind.LEAF, nodes.last().kind)
    }

    @Test fun multilingualFunctionalClassListsKeepTheirScope() {
        listOf(
            "édulcorants : E950, E955 ;" to FunctionalClass.SWEETENER,
            "stabilisants : gomme guar, gomme xanthane ;" to FunctionalClass.STABILISER,
            "raising agents: diphosphates, sodium carbonates;" to FunctionalClass.RAISING_AGENT,
            "emulgatoren: lecithinen, E471;" to FunctionalClass.EMULSIFIER
        ).forEach { (text, expectedClass) ->
            val group = IngredientTreeParser.parse(text).single()
            assertEquals(text, 2, group.children.size)
            assertTrue(text, group.children.all { it.functionalClassCanonical == expectedClass })
        }
    }

    @Test fun matcherUsesCanonicalNameAndResolvesLecithinsWithSoy() {
        val canonicalOnly = Ingredient("canonical", "Surface canonique", emptyList(), null,
            VeganStatus.VEGAN, "Test")
        val canonicalMatch = IngredientMatcher(listOf(canonicalOnly))
            .match(IngredientToken("Surface canonique", 0, 0))
        assertEquals(listOf("canonical"), canonicalMatch.ingredients.map { it.id })
        assertEquals(MatchResolution.EXACT, canonicalMatch.resolution)

        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "émulsifiant : lécithines (soja)", database
        )
        val additive = diagnostics.tokens.single()
        assertEquals(FunctionalClass.EMULSIFIER, additive.functionalClassCanonical)
        assertEquals(setOf("e322", "soy"), additive.matchedIngredientIds.toSet())
        assertEquals(listOf(VeganStatus.UNCERTAIN, VeganStatus.VEGAN), additive.baseStatuses)
        assertTrue(diagnostics.result.unknown.isEmpty())
    }

    @Test fun protectedVeganSourceCompoundsAreCoveredButUnsafePhrasesAreNot() {
        val matcher = IngredientMatcher(database)
        listOf("beurre de cacao", "beurre de cacahuète", "lait de coco", "crème de coco",
            "cocoa butter", "peanut butter", "coconut milk", "coconut cream")
            .forEach { text ->
                val match = matcher.match(IngredientToken(text, 0, 0))
                assertEquals(text, MatchResolution.COVERED, match.resolution)
                assertTrue(text, match.ingredients.all { it.status == VeganStatus.VEGAN })
                assertTrue(text, match.blockedIngredientIds.isNotEmpty())
                assertEquals(text, null, UnknownCollector.collect(match))
            }

        listOf("beurre au lait", "crème avec lait", "beurre de provenance inconnue")
            .forEach { text ->
                val match = matcher.match(IngredientToken(text, 0, 0))
                assertFalse(text, match.resolution == MatchResolution.COVERED &&
                    match.ingredients.all { it.status == VeganStatus.VEGAN })
            }
    }

    @Test fun completeRealBiscuitLabelKeepsOnlyRegulatoryComposition() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(REAL_LABEL, database, InputMode.FULL_LABEL)

        assertEquals(AnalysisAvailability.INGREDIENT_LIST_ANALYZED, diagnostics.result.availability)
        assertEquals(LabelLanguage.FRENCH, diagnostics.labelSections.language)
        assertEquals(VeganAssessment.NOT_VEGAN, diagnostics.result.veganAssessment)
        assertEquals(setOf("milk", "egg"), diagnostics.result.veganBlockers.map { it.id }.toSet())
        assertEquals(AnalysisVerdict.UNCERTAIN, diagnostics.result.verdict)
        assertTrue(diagnostics.result.matched.any { it.id == "e322" && it.status == VeganStatus.UNCERTAIN })
        assertTrue(diagnostics.result.matched.any { it.id == "soy" })
        assertTrue(diagnostics.result.matched.any { it.id == "e500" })
        assertEquals(2, diagnostics.excludedNotes.size)
        assertEquals(1, diagnostics.crossContactWarnings.size)
        assertTrue(diagnostics.crossContactWarnings.single().startsWith("Peut contenir"))
        listOf("Rainforest", "ra.org", "poules élevées", "Non ouvert", "À conserver", "Après ouverture")
            .forEach { forbidden ->
                assertFalse(forbidden, diagnostics.tokens.any { it.text.contains(forbidden, true) })
                assertFalse(forbidden, diagnostics.result.unknown.any { it.contains(forbidden, true) })
            }
        listOf("beurre de cacao", "lécithines (soja)").forEach { artificial ->
            assertFalse(artificial, diagnostics.result.unknown.any { it.equals(artificial, true) })
        }
    }

    private fun ingredient(
        id: String,
        name: String,
        status: VeganStatus = VeganStatus.VEGAN,
        vararg aliases: String,
        eNumber: String? = null,
        extraAliases: Array<String> = emptyArray()
    ) = Ingredient(id, name, aliases.toList() + extraAliases, eNumber, status, "Test")

    private companion object {
        val REAL_LABEL = """BE LU Biscuits avec 29 % de pépites de chocolat noir et 11 % de pépites de chocolat au lait.

Ingrédients : sucre, farine de blé, pâte de cacao*, huiles végétales (palme, tournesol), sirop de glucose-fructose, lait en poudre entier, beurre de cacao*, beurre (lait), sirop de glucose, lait en poudre écrémé, œuf**, amidon de blé, poudres à lever : diphosphates, carbonates de sodium ; émulsifiant : lécithines (soja) ; colorant : caramel ordinaire ; arôme naturel.

*Certifié Rainforest Alliance. Pour en savoir plus, rendez-vous sur ra.org.
**Issu de poules élevées au sol.

Peut contenir des traces d’autres céréales contenant du gluten, d’amandes, de noisettes, de noix de pécan et de noix de Macadamia.

Non ouvert, à consommer de préférence avant le : voir sur le côté. À conserver à l’abri de la chaleur et de l’humidité. Après ouverture, à consommer sous 7 jours."""
    }
}
