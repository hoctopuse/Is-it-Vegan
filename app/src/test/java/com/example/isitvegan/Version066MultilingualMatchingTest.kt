package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Version066MultilingualMatchingTest {
    private val lexicon = MultilingualIngredientLexicon.load(
        File("src/main/assets/ingredient_aliases_multilingual.json").readText()
    )
    private val rules = OriginQualifierRuleSet.load(
        File("src/main/assets/origin_qualifier_rules.json").readText()
    ).also { assertTrue(it.errors.joinToString(), it.isValid) }.rules
    private val database = listOf(
        vegan("wheat", "blé"), vegan("wheat_flour", "farine de blé"),
        vegan("oats", "avoine"), vegan("rye", "seigle"), vegan("barley", "orge"),
        vegan("malt", "malt"), vegan("starch", "amidon"), vegan("rice", "riz"), vegan("corn", "maïs"),
        vegan("sunflower_oil", "huile de tournesol"), vegan("rapeseed_oil", "huile de colza"),
        vegan("olive_oil", "huile d'olive"), vegan("sugar", "sucre"), vegan("salt", "sel"),
        vegan("vinegar", "vinaigre"), vegan("glucose_syrup", "sirop de glucose"),
        vegan("cocoa", "cacao"), vegan("blackberry", "mûre"), vegan("blueberry", "myrtille"),
        vegan("natural_flavouring", "arôme naturel"), vegan("e440", "pectines", "E440"),
        Ingredient(
            "e322", "lécithines",
            listOf("lécithines", "E322", "lecitinen", "lecithinen", "Lecithine", "lecitine", "lecitinas"),
            "E322", VeganStatus.UNCERTAIN, "origine variable"
        )
    )

    @Test fun dutchCerealLabelMapsToCanonicalIngredientsAndKeepsTracesOutOfVerdict() {
        val diagnostics = analyze("""
            Ingrediënten: Granen 48,5% (volkoren tarwe, volkoren havervlokken,
            volkoren roggevlokken, gerstermoutextract), tarwesiroop,
            zonnebloemolie, gevriesdroogde bramen, gevriesdroogde bosbessen,
            suiker, pectine, natuurlijk aardbeienaroma, zout,
            lecithinen (zonnebloem).
            Kan sporen bevatten van: melk, pinda's en noten.
        """.trimIndent())

        assertEquals(LabelLanguage.DUTCH, diagnostics.labelSections.language)
        assertEquals(VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
        assertTrue(diagnostics.result.veganBlockers.isEmpty())
        assertTrue(diagnostics.crossContactWarnings.single().contains("melk"))
        assertTrue(diagnostics.tokens.any { it.canonicalConceptId == "sunflower_oil" })
        assertTrue(diagnostics.tokens.any { it.canonicalConceptId == "malt" })
        assertTrue(diagnostics.tokens.none { it.text.contains("melk", true) })
    }

    @Test fun germanItalianAndSpanishAliasesPreserveTheCanonicalStatus() {
        val labels = listOf(
            "Zutaten: Zucker, Weizenmehl, Rapsöl, Weizenstärke, Glukosesirup, Salz, Lecithine (Sonnenblumen). Kann Spuren von Milch und Eiern enthalten.",
            "Ingredienti: zucchero, farina di frumento, olio di girasole, lecitine (girasole). Può contenere tracce di latte e uova.",
            "Ingredientes: azúcar, harina de trigo, aceite de girasol, lecitinas (girasol). Puede contener trazas de leche y huevos."
        )
        labels.forEach { label ->
            val diagnostics = analyze(label)
            assertEquals(label, VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
            assertTrue(label, diagnostics.result.veganBlockers.isEmpty())
            assertTrue(label, diagnostics.crossContactWarnings.isNotEmpty())
            assertEquals(label, VeganStatus.VEGAN, diagnostics.tokens.first { it.canonicalConceptId == "e322" }.effectiveStatuses.single())
        }
    }

    @Test fun nlOcrVariantsAreLanguageBoundAndReportedWithoutChangingRawInput() {
        val raw = "Ingrediënten: suiker, zonnebloemole, havervokken, lecithinen."
        val diagnostics = analyze(raw)

        assertEquals(raw, diagnostics.input)
        assertEquals(VeganAssessment.UNCERTAIN, diagnostics.result.veganAssessment)
        assertTrue(diagnostics.tokens.any { it.text == "zonnebloemole" && it.correctedText == "zonnebloemolie" })
        assertTrue(diagnostics.tokens.any { it.text == "havervokken" && it.correctedText == "havervlokken" })
        assertTrue(diagnostics.tokens.any { it.canonicalConceptId == "sunflower_oil" })
        assertFalse(diagnostics.tokens.any { it.text == "zonnebloemole" && it.unknown != null })
    }

    @Test fun recognizedAliasWithMissingCanonicalConceptStaysUnclassifiedAndExplained() {
        val reducedDatabase = database.filterNot { it.id == "malt" }
        val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
            text = "Ingrediënten: gerstermoutextract.",
            database = reducedDatabase,
            inputMode = InputMode.FULL_LABEL,
            rules = rules,
            multilingualLexicon = lexicon
        )
        val token = diagnostics.tokens.single { it.text == "gerstermoutextract" }

        assertEquals("gerstermoutextract", token.multilingualAlias)
        assertEquals("malt", token.canonicalConceptId)
        assertFalse(token.canonicalConceptAvailable!!)
        assertTrue(token.matchedIngredientIds.isEmpty())
        assertTrue(token.effectiveStatuses.isEmpty())
        assertEquals(VeganAssessment.UNCERTAIN, diagnostics.result.veganAssessment)
        val report = DiagnosticReport.build(diagnostics, "0.6.6")
        assertTrue(report.contains("disponibilité du concept=absente de ingredients.json"))
        assertTrue(report.contains("classification=non classifiable"))
    }

    @Test fun multilingualSunflowerAndSoyOriginsRemainVegan() {
        listOf(
            "Ingredienti: lecitinen (girasol).",
            "Ingrediënten: lecithinen (zonnebloem).",
            "Zutaten: Lecithine (Sonnenblumen).",
            "Ingredienti: lecitine (soia).",
            "Ingredientes: lecitinas (soja).",
            "Ingrédients: lécithines (soja)."
        ).forEach { label ->
            val diagnostics = analyze(label)
            val lecithin = diagnostics.tokens.firstOrNull { "e322" in it.matchedIngredientIds }
            assertTrue(
                "$label | ${diagnostics.tokens.joinToString { "${it.text}:${it.matchedIngredientIds}" }}",
                lecithin != null
            )
            assertEquals(label, VeganStatus.VEGAN, lecithin!!.effectiveStatuses.single())
            assertTrue(label, lecithin.originResolution.orEmpty().isNotBlank())
        }
    }

    private fun analyze(text: String) = VeganAnalyzer.analyzeWithDiagnostics(
        text = text,
        database = database,
        inputMode = InputMode.FULL_LABEL,
        rules = rules,
        multilingualLexicon = lexicon
    )

    private fun vegan(id: String, name: String, eNumber: String? = null) =
        Ingredient(id, name, listOf(name), eNumber, VeganStatus.VEGAN, "test")
}
