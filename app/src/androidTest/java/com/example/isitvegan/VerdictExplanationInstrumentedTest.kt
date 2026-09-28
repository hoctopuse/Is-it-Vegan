package com.example.isitvegan

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerdictExplanationInstrumentedTest {
    private val service = IngredientAnalysisService(
        IngredientKnowledge(
            listOf(
                Ingredient("water", "eau", listOf("eau", "water"), null, VeganStatus.VEGAN, "plant"),
                Ingredient("milk", "lait", listOf("lait", "milk"), null, VeganStatus.VEGETARIAN, "dairy"),
                Ingredient("gelatin", "gélatine", listOf("gélatine", "gelatin"), null, VeganStatus.NON_VEGAN, "animal"),
                Ingredient("flavour", "arôme", listOf("arôme", "flavour"), null, VeganStatus.UNCERTAIN, "variable")
            )
        )
    )
    private val diagnostics = service.analyzeWithDiagnostics("préparation végétale [eau, arôme]")

    @Test fun conditionalVerdictTerminologyIsLocalizedInEverySupportedInterfaceLanguage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expected = mapOf(
            UiLanguage.FR to listOf("INCERTAIN", "Selon les ingrédients au statut établi : VEGAN"),
            UiLanguage.EN to listOf("UNCERTAIN", "According to ingredients with an established status: VEGAN"),
            UiLanguage.NL to listOf("ONZEKER", "Volgens ingrediënten met een vastgestelde status: VEGAN"),
            UiLanguage.DE to listOf("UNSICHER", "Nach Zutaten mit festgestelltem Status: VEGAN")
        )

        expected.forEach { (language, terms) ->
            val resources = localizedContext(context, language).resources
            val rendered = VerdictExplanationFormatter.render(resources, diagnostics.input, diagnostics)
            terms.forEach { term -> assertTrue("$language: $term", rendered.contains(term)) }
            assertTrue(rendered.contains("préparation végétale → arôme"))
        }
    }

    @Test fun vegetarianConditionalTerminologyIsLocalizedInEverySupportedLanguage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val diagnostics = service.analyzeWithDiagnostics("eau, lait, arôme")
        val expected = mapOf(
            UiLanguage.FR to "Selon les ingrédients au statut établi : VÉGÉTARIEN",
            UiLanguage.EN to "According to ingredients with an established status: VEGETARIAN",
            UiLanguage.NL to "Volgens ingrediënten met een vastgestelde status: VEGETARISCH",
            UiLanguage.DE to "Nach Zutaten mit festgestelltem Status: VEGETARISCH"
        )

        expected.forEach { (language, conditional) ->
            val resources = localizedContext(context, language).resources
            val rendered = VerdictExplanationFormatter.render(resources, diagnostics.input, diagnostics)
            assertTrue("$language: $conditional", rendered.contains(conditional))
        }
    }

    @Test fun nonVeganVerdictShowsKnownCauseAndUncertainIngredientWithoutConditionalResult() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val diagnostics = service.analyzeWithDiagnostics("gélatine, arôme")
        val expected = mapOf(
            UiLanguage.FR to listOf("NON VEGAN", "Causes connues", "Ingrédients incertains"),
            UiLanguage.EN to listOf("NON VEGAN", "Known causes", "Uncertain ingredients"),
            UiLanguage.NL to listOf("NIET VEGAN", "Bekende oorzaken", "Onzekere ingrediënten"),
            UiLanguage.DE to listOf("NICHT VEGAN", "Bekannte Gründe", "Unsichere Zutaten")
        )

        assertTrue(diagnostics.verdictExplanation.conditionalVerdict == null)
        expected.forEach { (language, terms) ->
            val resources = localizedContext(context, language).resources
            val rendered = VerdictExplanationFormatter.render(resources, diagnostics.input, diagnostics)
            terms.forEach { term -> assertTrue("$language: $term", rendered.contains(term)) }
            assertTrue(rendered.contains("gélatine"))
            assertTrue(rendered.contains("arôme"))
            assertFalse(rendered.contains(resources.getString(R.string.conditional_vegan)))
        }
    }

    @Test fun traceFramingAndItsExclusionNoticeAreLocalizedInEverySupportedLanguage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val diagnostics = service.analyzeWithDiagnostics("eau. Peut contenir : lait")
        val expected = mapOf(
            UiLanguage.FR to listOf("TRACES SIGNALÉES", "n’ont influencé ni le verdict"),
            UiLanguage.EN to listOf("REPORTED TRACES", "influenced neither the verdict"),
            UiLanguage.NL to listOf("VERMELDE SPOREN", "geen invloed op het oordeel"),
            UiLanguage.DE to listOf("ANGEGEBENE SPUREN", "weder das Urteil")
        )

        expected.forEach { (language, terms) ->
            val resources = localizedContext(context, language).resources
            val rendered = VerdictExplanationFormatter.render(resources, diagnostics.input, diagnostics)
            terms.forEach { term -> assertTrue("$language: $term", rendered.contains(term)) }
            assertTrue(rendered.contains("Peut contenir"))
        }
    }

    @Test fun unknownIngredientsUseBulletsAndKeepNestedOccurrenceContext() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val diagnostics = service.analyzeWithDiagnostics(
            "cumin, piment rouge, sirop de grenade 4,4 % (concentré de grenade 50 %), cumin"
        )

        val parent = diagnostics.tokens.single { it.text.startsWith("sirop de grenade") }
        val child = diagnostics.tokens.single { it.text.startsWith("concentré de grenade") }
        assertEquals(parent.order, child.parentOrder)
        assertTrue(diagnostics.visibleUnknownTokens.any { it.order == child.order })
        assertFalse(diagnostics.visibleUnknownIngredients.any { it.startsWith("sirop de grenade") })
        assertTrue(diagnostics.ingredientGroups.unknownIngredients.any { it.startsWith("concentré de grenade") })
        assertEquals(diagnostics.result.verdict, diagnostics.verdictExplanation.mainVerdict)

        UiLanguage.entries.forEach { language ->
            val resources = localizedContext(context, language).resources
            val rendered = VerdictExplanationFormatter.render(resources, diagnostics.input, diagnostics)
            assertTrue(rendered.contains(resources.getString(R.string.unidentified_ingredients_title)))
            assertTrue(rendered.contains("• cumin"))
            assertEquals(2, rendered.split("• cumin").size - 1)
            assertTrue(rendered.contains("• sirop de grenade —"))
            assertTrue(rendered.contains("└─ concentré de grenade —"))
            assertFalse(rendered.contains("|"))
            assertEquals(1, rendered.split(resources.getString(R.string.unconfirmed_vegan_notice)).size - 1)
        }
    }

    @Test fun uncertainVerdictShowsUnknownIngredientsAndOneSharedBlockingNotice() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val diagnostics = service.analyzeWithDiagnostics("eau, arôme, mystère")
        val resources = localizedContext(context, UiLanguage.FR).resources
        val rendered = VerdictExplanationFormatter.render(resources, diagnostics.input, diagnostics)

        assertEquals(AnalysisVerdict.UNCERTAIN, diagnostics.result.verdict)
        assertTrue(rendered.contains(resources.getString(R.string.uncertain_ingredients_title)))
        assertTrue(rendered.contains(resources.getString(R.string.unidentified_ingredients_title)))
        assertTrue(rendered.contains("• mystère"))
        assertFalse(rendered.contains(resources.getString(R.string.conditional_vegan)))
        assertFalse(rendered.contains(resources.getString(R.string.conditional_vegetarian)))
        assertTrue(rendered.contains(resources.getString(R.string.established_vegan)))
        assertEquals(1, rendered.split(resources.getString(R.string.established_ingredients_notice)).size - 1)
    }

    @Test fun possibleOriginNotesAreLocalizedAndOnlyShownForUncertainOccurrences() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val notedService = IngredientAnalysisService(
            IngredientKnowledge(
                listOf(
                    Ingredient(
                        "e471", "mono- et diglycérides", listOf("E471"), "E471", VeganStatus.UNCERTAIN,
                        "origine variable",
                        possibleOriginNote = PossibleOriginNote(
                            listOf(PossibleOrigin.PLANT, PossibleOrigin.ANIMAL),
                            OriginVariability.RAW_MATERIAL_AND_PROCESS,
                            listOf("vegan-easy-food-additives"),
                            "MODERATE"
                        )
                    ),
                    Ingredient("water", "eau", listOf("eau"), null, VeganStatus.VEGAN, "plant")
                )
            )
        )
        val uncertain = notedService.analyzeWithDiagnostics("E471")
        val vegan = notedService.analyzeWithDiagnostics("eau")
        val expected = mapOf(
            UiLanguage.FR to listOf("Origines possibles", "Selon la matière première", "Cet élément empêche"),
            UiLanguage.EN to listOf("Possible origins", "Depending on the raw material", "This item prevents"),
            UiLanguage.NL to listOf("Mogelijke oorsprongen", "Afhankelijk van de grondstof", "Dit element verhindert"),
            UiLanguage.DE to listOf("Mögliche Ursprünge", "Abhängig vom Rohstoff", "Dieses Element verhindert")
        )

        expected.forEach { (language, terms) ->
            val resources = localizedContext(context, language).resources
            val uncertainRendered = VerdictExplanationFormatter.render(resources, uncertain.input, uncertain)
            terms.forEach { term -> assertTrue("$language: $term", uncertainRendered.contains(term)) }
            val veganRendered = VerdictExplanationFormatter.render(resources, vegan.input, vegan)
            assertFalse("$language vegan", veganRendered.contains(resources.getString(R.string.possible_origin_note, "", "")))
        }
    }
}
