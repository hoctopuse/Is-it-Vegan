package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Version065OcrPipelineTest {
    private val database = listOf(
        ingredient("sugar", "sucre", "sucre", "suiker", "sugar"),
        ingredient("water", "eau", "eau", "water", "wasser"),
        ingredient("wheat", "farine de blé", "farine de ble"),
        ingredient("rapeseed", "huile de colza"),
        ingredient("cereals", "céréales"),
        ingredient("sunflower", "huile de tournesol"),
        ingredient("whole-wheat", "blé entier"),
        ingredient("oats", "flocons d’avoine"),
        ingredient("corn", "farine de mais"),
        ingredient("rye", "flocons de seigle entier"),
        ingredient("barley-malt", "extrait de malte d’orge"),
        ingredient("berry", "mûre lyophilisée"),
        ingredient("pectin", "pectines"),
        ingredient("flavor", "arôme naturel de fraise"),
        ingredient("lecithin", "lécithines"),
        ingredient("emulsifier", "émulsifiant"),
        ingredient("soy", "soja"),
        ingredient("tocopherols", "extrait riche en tocophérols"),
        ingredient("glucose", "sirop de glucose"),
        ingredient("salt", "sel"),
        ingredient("pepper", "poivron"),
        ingredient("onion", "oignon"),
        ingredient("garlic", "ail"),
        ingredient("vinegar", "vinaigre"),
        ingredient("palm", "graisse de palme"),
        ingredient("coconut", "noix de coco"),
        ingredient("starch", "amidon de ble"),
        ingredient("ammonium", "carbonate d ammonium"),
        ingredient("sodium", "carbonate de sodium"),
        Ingredient("milk", "lait", listOf("lait", "melk", "milk", "milch"), null, VeganStatus.VEGETARIAN, "test"),
        Ingredient("egg", "oeuf", listOf("oeuf", "oeufs", "ei", "eie"), null, VeganStatus.VEGETARIAN, "test")
    )

    @Test fun distinctiveAndOcrHeadingsKeepTheirLanguage() {
        val cases = listOf(
            "Zutaten: Zucker, Wasser" to LabelLanguage.GERMAN,
            "Ingrediënten: suiker, water" to LabelLanguage.DUTCH,
            "Ingrediènten: suiker, water" to LabelLanguage.DUTCH,
            "Sngredients: sucre, eau" to LabelLanguage.FRENCH,
            "Ingrécients: sucre, eau" to LabelLanguage.FRENCH,
            "Ingredientes: agua, azúcar" to LabelLanguage.SPANISH
        )
        cases.forEach { (text, language) ->
            val block = LabelLanguageSegmenter.segment(text).blocks.single()
            assertEquals(text, language, block.language)
            assertEquals(text, language, block.headingLanguage)
        }
    }

    @Test fun completeBlockWinsAndItsIdentityFlowsToEditableTextAndAnalysis() {
        val text = """
            NL: Ingrediënten: 74 basilicum, zout.
            EN: Ingredients: water, sugar, rapeseed oil, garlic, onion, vinegar, spices, salt.
        """.trimIndent()
        val segmentation = LabelLanguageSegmenter.segment(text, UiLanguage.NL)
        val selection = OcrTextSelection.from(segmentation, text)
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL, preferredLanguage = UiLanguage.NL)

        assertEquals(LabelLanguage.ENGLISH, segmentation.selectedLanguage)
        assertTrue(selection.editableText.contains("rapeseed oil"))
        assertFalse(selection.editableText.contains("74 basilicum"))
        assertEquals(segmentation.selectedBlockId, selection.selectedBlockId)
        assertEquals(segmentation.selectedBlockId, diagnostics.labelSections.selectedBlockId)
        assertTrue(diagnostics.labelSections.ingredientsText.orEmpty().contains("rapeseed oil"))
    }

    @Test fun multilingualHeadingsSplitThePreviousList() {
        val text = "Zutaten: Zucker, Wasser. Ingrédients: sucre, eau. Ingrediënten: suiker, water. Ingredientes: agua, azúcar."
        val blocks = LabelLanguageSegmenter.segment(text).blocks

        assertEquals(listOf(LabelLanguage.GERMAN, LabelLanguage.FRENCH, LabelLanguage.DUTCH, LabelLanguage.SPANISH), blocks.map { it.language })
        assertFalse(blocks.first().rawText.contains("Ingrédients"))
        assertFalse(blocks[1].rawText.contains("Ingrediënten"))
    }

    @Test fun degradedTraceMarkersAndStorageNeverCreateAnimalBlockers() {
        val labels = listOf(
            "Ingrédients: sucre. Pet conterir oeufs, lait. A conserver au frais.",
            "Zutaten: Zucker. Kann Selenie Eie und Milch enthalten. Aufbewahrung: kühl.",
            "Ingrediënten: suiker. Kan sporen van melk en soja bevatten. Bewaring: koel."
        )
        labels.forEach { text ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL)
            assertEquals(text, VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
            assertTrue(text, diagnostics.crossContactWarnings.isNotEmpty())
            assertTrue(text, diagnostics.result.veganBlockers.isEmpty())
            assertFalse(text, diagnostics.crossContactWarnings.joinToString().contains("conserver", true))
            assertFalse(text, diagnostics.crossContactWarnings.joinToString().contains("aufbewahrung", true))
            assertFalse(text, diagnostics.crossContactWarnings.joinToString().contains("bewaring", true))
        }
    }

    @Test fun groupedENumbersBecomeSeparateAdditivesOnlyInsideASeries() {
        val tokens = IngredientTokenizer.flatten(IngredientTreeParser.parse("E202-EZ621-E270"))
        assertEquals(listOf("E202", "E621", "E270"), tokens.map { it.text })
        assertEquals("EZ621", IngredientTokenizer.flatten(IngredientTreeParser.parse("EZ621")).single().text)
    }

    @Test fun germanTermsAreBridgedForMatchingWithoutChangingUnknownText() {
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "Zutaten: Zucker, Weizenmehl, Palmfett, Rapsöl, Glukosesirup, Speisesalz, Knoblauch, Zwiebel, Essig, Ammoniumcarbonate, Natriumcarbonate.",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
        assertTrue(diagnostics.tokens.any { it.text == "Weizenmehl" && it.matcherText == "farine de ble" })
        assertFalse(diagnostics.result.unknown.any { it.contains("Weizenmehl") })
    }

    @Test fun explicitSunflowerOriginResolvesGermanLecithinAsVegan() {
        val rules = OriginQualifierRuleSet.load(
            File("src/main/assets/origin_qualifier_rules.json").readText()
        ).also { assertTrue(it.errors.joinToString(), it.isValid) }.rules
        val lecithin = Ingredient("e322", "Lécithines", listOf("lecithinen"), "E322", VeganStatus.UNCERTAIN, "test")
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            "Zutaten: Lecithinen (Sonnenblumen).",
            listOf(lecithin),
            InputMode.FULL_LABEL,
            rules
        )

        assertEquals(VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
        assertEquals("plant-sunflower", diagnostics.tokens.single().originOutcomeId)
    }

    @Test fun distortedFrenchTraceEndsAtAllergenSentenceAndNeverBlocks() {
        val text = "Ingrédients : céréales, sucre, huile de tournesol. Peut conteir des traces éventuelles de : lait, arachides et fruits à coque. A conserver dans un endroit frais."
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL)

        assertEquals(VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
        assertEquals("céréales, sucre, huile de tournesol.", diagnostics.labelSections.ingredientsText?.trim())
        assertEquals("Peut conteir des traces éventuelles de : lait, arachides et fruits à coque.", diagnostics.labelSections.tracesText)
        assertTrue(diagnostics.result.veganBlockers.isEmpty())
        assertFalse(diagnostics.crossContactWarnings.joinToString().contains("conserver", true))
        assertFalse(diagnostics.tokens.any { it.text.contains("lait", true) })
    }

    @Test fun cerealBarCorrectionsAreBoundedAndRawInputStaysUntouched() {
        val text = """
            Ingrédients : céréales 48,5% (blé entie, flocons davoine entier, farine de blé, farine de mais, flocons de seigle entie, extrait de malte d’orge), huile de toumesol, müre lyophilisée 2%, sucre, gelfiant : pectines, aröớme naturel de frase sel, emuisifiant; lecthines (soja), extait riche en tocophérols.
            Peut conteir des traces éventuelles de : lait, arachides et fruits à coque.
            Fabriqué en Espagne. A conserver dans un endroit frais.
        """.trimIndent()
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL)

        assertEquals(text, diagnostics.input)
        assertEquals(VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
        assertTrue(diagnostics.result.veganBlockers.isEmpty())
        assertEquals("Peut conteir des traces éventuelles de : lait, arachides et fruits à coque.", diagnostics.labelSections.tracesText)
        assertFalse(diagnostics.labelSections.tracesText.orEmpty().contains("Fabriqué", true))
        assertFalse(diagnostics.labelSections.tracesText.orEmpty().contains("conserver", true))
        listOf("blé entier", "flocons d’avoine", "flocons de seigle entier", "tournesol", "mûre", "gélifiant", "arôme naturel de fraise, sel", "émulsifiant", "lécithines", "extrait").forEach {
            assertTrue("Correction absente : $it", diagnostics.preprocessedInput.contains(it))
        }
        assertFalse(diagnostics.preprocessedInput.contains("Peut conteir"))
    }

    @Test fun eggAfterDistortedTraceCannotBecomeABlockerAndBlockIdIsReported() {
        val text = "Ingrédients : farine de blé, sucre, huile de colza. Peut conteir des traces éventuelles de : lait et œufs."
        val segmentation = LabelLanguageSegmenter.segment(text)
        val selection = OcrTextSelection.from(segmentation, text)
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database, InputMode.FULL_LABEL)
        val report = DiagnosticReport.build(diagnostics, "0.6.5")

        assertEquals(VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
        assertTrue(diagnostics.result.veganBlockers.isEmpty())
        assertEquals(segmentation.selectedBlockId, selection.selectedBlockId)
        assertEquals(segmentation.selectedBlockId, diagnostics.labelSections.selectedBlockId)
        assertTrue(report.contains("Identifiant du bloc sélectionné : ${segmentation.selectedBlockId}"))
        assertTrue(diagnostics.labelSections.tracesText.orEmpty().contains("œufs"))
    }

    private fun ingredient(id: String, name: String, vararg aliases: String) =
        Ingredient(id, name, aliases.toList() + name, null, VeganStatus.VEGAN, "test")
}
