package com.example.isitvegan

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
            UiLanguage.FR to listOf("INCERTAIN", "Vegan hors ingrédient incertain"),
            UiLanguage.EN to listOf("UNCERTAIN", "Vegan excluding uncertain ingredient"),
            UiLanguage.NL to listOf("ONZEKER", "Vegan zonder onzeker ingrediënt"),
            UiLanguage.DE to listOf("UNSICHER", "Vegan ohne unsichere Zutat")
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
            UiLanguage.FR to "Végétarien hors ingrédient incertain",
            UiLanguage.EN to "Vegetarian excluding uncertain ingredient",
            UiLanguage.NL to "Vegetarisch zonder onzeker ingrediënt",
            UiLanguage.DE to "Vegetarisch ohne unsichere Zutat"
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
}
