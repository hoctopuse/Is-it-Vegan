package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Version066KnowledgeEnrichmentTest {
    private val database = runtimeDatabase()
    private val lexicon = MultilingualIngredientLexicon.load(
        File("src/main/assets/ingredient_aliases_multilingual.json").readText()
    )
    private val rules = OriginQualifierRuleSet.load(
        File("src/main/assets/origin_qualifier_rules.json").readText()
    ).also { assertTrue(it.errors.joinToString(), it.isValid) }.rules

    @Test fun newCanonicalConceptsHaveReviewedStatuses() {
        val expected = mapOf(
            "wheat_syrup" to VeganStatus.VEGAN,
            "coconut" to VeganStatus.VEGAN,
            "coconut_fat" to VeganStatus.VEGAN,
            "black_olive" to VeganStatus.VEGAN,
            "e306" to VeganStatus.VEGAN,
            "peanut" to VeganStatus.VEGAN,
            "cashew" to VeganStatus.VEGAN,
            "raisin" to VeganStatus.VEGAN,
            "sultana" to VeganStatus.VEGAN,
            "cranberry" to VeganStatus.VEGAN,
            "sour_cherry" to VeganStatus.VEGAN,
            "date" to VeganStatus.VEGAN,
            "pineapple" to VeganStatus.VEGAN,
            "papaya" to VeganStatus.VEGAN,
            "pumpkin_seed" to VeganStatus.VEGAN,
            "pecan" to VeganStatus.VEGAN,
            "hazelnut" to VeganStatus.VEGAN,
            "macadamia" to VeganStatus.VEGAN,
            "brazil_nut" to VeganStatus.VEGAN,
            "cottonseed_oil" to VeganStatus.VEGAN,
            "e202" to VeganStatus.VEGAN,
            "e220" to VeganStatus.VEGAN,
            "e262" to VeganStatus.VEGAN,
            "e270" to VeganStatus.VEGAN
        )

        expected.forEach { (id, status) ->
            assertEquals(status, database.single { it.id == id }.status)
        }
        val e306 = database.single { it.id == "e306" }
        assertEquals("E306", e306.eNumber)
        assertTrue(e306.reason.contains("dénomination correspond clairement"))
    }

    @Test fun wheatSyrupAndCoconutAliasesResolveInTheirReviewedLanguages() {
        val cases = listOf(
            Triple("siop de blé", LabelLanguage.FRENCH, "wheat_syrup"),
            Triple("tarwesioop", LabelLanguage.DUTCH, "wheat_syrup"),
            Triple("Werzersiup", LabelLanguage.GERMAN, "wheat_syrup"),
            Triple("wheat synup", LabelLanguage.ENGLISH, "wheat_syrup"),
            Triple("scranDe di frumento", LabelLanguage.ITALIAN, "wheat_syrup"),
            Triple("Kokosnuss", LabelLanguage.GERMAN, "coconut"),
            Triple("Kokosfett", LabelLanguage.GERMAN, "coconut_fat")
        )

        cases.forEach { (raw, language, id) ->
            val resolution = lexicon.resolve(raw, language, database)
            assertEquals(raw, id, resolution.canonicalId)
            assertEquals(raw, true, resolution.canonicalAvailable)
        }
        assertEquals(
            "tarwesiroop",
            lexicon.resolve("tarwesioop", LabelLanguage.DUTCH, database).correctedText
        )
    }

    @Test fun e306AndItsOcrVariantsAreVeganWhenTheDesignationIsExplicit() {
        val raw = "Ingrédients : sirop de blé, extait riche en tocophérols. Peut contenir : lait."
        val diagnostics = analyze(raw)
        val tocopherols = diagnostics.tokens.single { it.canonicalConceptId == "e306" }

        assertEquals(raw, diagnostics.input)
        assertTrue(diagnostics.ocrCorrections.contains("extait → extrait"))
        assertEquals("e306", tocopherols.canonicalConceptId)
        assertEquals(true, tocopherols.canonicalConceptAvailable)
        assertEquals(listOf(VeganStatus.VEGAN), tocopherols.effectiveStatuses)
        assertEquals(VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
        assertTrue(diagnostics.result.veganBlockers.isEmpty())
        assertTrue(diagnostics.tokens.none { it.text.contains("lait", true) })

        val english = analyze("Ingredients: wheat syrup, tocopherol-rich extract.")
        assertEquals(VeganAssessment.VEGAN, english.result.veganAssessment)
        assertTrue(english.tokens.any { it.canonicalConceptId == "wheat_syrup" })
        assertEquals(
            listOf(VeganStatus.VEGAN),
            english.tokens.single { it.canonicalConceptId == "e306" }.effectiveStatuses
        )
    }

    @Test fun driedFruitNutSeedAndOilAliasesResolveWithoutCreatingDuplicateConcepts() {
        val cases = listOf(
            "cacahuètes" to "peanut", "noix de cajou grillées" to "cashew",
            "raisins secs" to "raisin", "raisins sultanines" to "sultana",
            "canneberges séchées et sucrées" to "cranberry", "griottes" to "sour_cherry",
            "morceaux de dattes secs" to "date", "ananas confits" to "pineapple",
            "papaye confite" to "papaya", "graines de courge" to "pumpkin_seed",
            "noix de pécan" to "pecan", "noisettes" to "hazelnut",
            "noix de macadamia" to "macadamia", "noix du Brésil" to "brazil_nut",
            "huile de graine de coton" to "cottonseed_oil"
        )

        cases.forEach { (alias, id) ->
            val resolution = lexicon.resolve(alias, LabelLanguage.FRENCH, database)
            assertEquals(alias, id, resolution.canonicalId)
            assertEquals(alias, true, resolution.canonicalAvailable)
            assertEquals(alias, VeganStatus.VEGAN, database.single { it.id == id }.status)
        }
        assertEquals("peanut", lexicon.resolve("pinda's", LabelLanguage.DUTCH, database).canonicalId)
        assertEquals("cranberry", lexicon.resolve("cranberry's", LabelLanguage.DUTCH, database).canonicalId)
    }

    @Test fun additiveAliasesAndE270OriginBoundaryAreExplicit() {
        listOf(
            "sorbate de potassium" to "e202",
            "anhydride sulfureux" to "e220",
            "acétates de sodium" to "e262",
            "acide lactique" to "e270"
        ).forEach { (alias, id) ->
            assertEquals(alias, id, lexicon.resolve(alias, LabelLanguage.FRENCH, database).canonicalId)
        }

        val plain = analyze("Ingrédients : E270.")
        assertEquals(VeganAssessment.VEGAN, plain.result.veganAssessment)
        assertTrue(plain.result.veganBlockers.isEmpty())

        val attached = analyze("Ingrédients : E270 (lait).")
        assertEquals(VeganAssessment.NOT_VEGAN, attached.result.veganAssessment)
        assertTrue(attached.result.originNonVeganIngredientIds.contains("e270"))

        val separate = analyze("Ingrédients : E270, lait.")
        assertEquals(VeganAssessment.NOT_VEGAN, separate.result.veganAssessment)
        assertTrue(separate.result.veganBlockers.any { it.id == "milk" })

        val trace = analyze("Ingrédients : E270. Peut contenir du lait.")
        assertEquals(VeganAssessment.VEGAN, trace.result.veganAssessment)
        assertTrue(trace.result.veganBlockers.isEmpty())
        assertTrue(trace.crossContactWarnings.single().contains("lait", true))
    }

    @Test fun realNlAndGermanLabelsResolveWithoutTurningTracesIntoIngredients() {
        val dutch = analyze(
            "Ingrediënten: tarwesiroop, gerstermoutextract, gevriesdroogde bramen. " +
                "Kan sporen bevatten van melk en eieren."
        )
        assertEquals(VeganAssessment.VEGAN, dutch.result.veganAssessment)
        assertTrue(dutch.result.veganBlockers.isEmpty())
        assertTrue(dutch.tokens.none { it.text.contains("melk", true) || it.text.contains("eieren", true) })
        assertTrue(dutch.tokens.any { it.canonicalConceptId == "wheat_syrup" })
        assertTrue(dutch.tokens.any { it.canonicalConceptId == "malt" })
        assertTrue(dutch.tokens.any { it.canonicalConceptId == "blackberry" })

        val german = analyze("Zutaten: Palmfett, Kokosfett, Glukosesirup, Ammoniumcarbonate.")
        assertEquals(VeganAssessment.VEGAN, german.result.veganAssessment)
        assertTrue(german.tokens.mapNotNull { it.canonicalConceptId }.containsAll(
            listOf("palm_oil", "coconut_fat", "glucose_syrup", "e503")
        ))
    }

    @Test fun olivesAndSweetPeppersUseDistinctReviewedAliasesWithoutCapturingPaprika() {
        val diagnostics = analyze("Ingrédients : olives noires, piment doux, poivron jaune, paprika.")

        assertEquals(VeganAssessment.VEGAN, diagnostics.result.veganAssessment)
        assertTrue(diagnostics.tokens.any { it.canonicalConceptId == "black_olive" })
        assertEquals(2, diagnostics.tokens.count { it.canonicalConceptId == "bell_pepper" })
        assertEquals(
            "bell_pepper",
            lexicon.resolve("gebe Papria", LabelLanguage.GERMAN, database).canonicalId
        )
        assertTrue(diagnostics.tokens.any { "paprika" in it.matchedIngredientIds })
        assertFalse(diagnostics.tokens.any { it.text == "paprika" && it.canonicalConceptId == "bell_pepper" })
    }

    private fun analyze(text: String) = VeganAnalyzer.analyzeWithDiagnostics(
        text = text,
        database = database,
        inputMode = InputMode.FULL_LABEL,
        rules = rules,
        multilingualLexicon = lexicon
    )

    private fun runtimeDatabase(): List<Ingredient> = (MiniJson.parse(
        File("src/main/assets/ingredients.json").readText()
    ) as List<*>).map { value ->
        val item = value as Map<*, *>
        Ingredient(
            id = item["id"] as String,
            name = item["name"] as String,
            aliases = (item["aliases"] as List<*>).filterIsInstance<String>(),
            eNumber = item["eNumber"] as? String,
            status = VeganStatus.valueOf(item["status"] as String),
            reason = item["reason"] as String,
            source = item["source"] as? String
        )
    }
}
