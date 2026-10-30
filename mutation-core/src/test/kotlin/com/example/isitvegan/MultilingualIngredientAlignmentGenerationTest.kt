package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MultilingualIngredientAlignmentGenerationTest {
    @Test fun parsedSectionsProduceExactCorroborationAndAUniqueAnchoredCorrectionProposal() {
        val result = generate(
            block("en", LabelLanguage.ENGLISH, "Ingredients: sugar, WHEAT lour, citric acid"),
            block("nl", LabelLanguage.DUTCH, "IngrediÃ«nten: suiker, tarwebloem, citroenzuur")
        )

        assertTrue(result.links.any { it.kind == AlignmentKind.CORROBORATION && it.first.canonicalId == "sugar" })
        val proposal = result.links.single { it.kind == AlignmentKind.AMBIGUOUS }
        assertEquals("WHEAT lour", proposal.first.rawText)
        assertEquals(null, proposal.proposedCanonicalId)
        assertEquals("tarwebloem", proposal.second.rawText)
        assertEquals("block-en", proposal.first.blockId)
        assertEquals(LabelLanguage.ENGLISH, proposal.first.language)
        assertEquals(1, proposal.first.order)
    }

    @Test fun multipleItemsBetweenTheSameAnchorsRemainAmbiguous() {
        val result = generate(
            block("en", LabelLanguage.ENGLISH, "Ingredients: sugar, unknown one, unknown two, citric acid"),
            block("nl", LabelLanguage.DUTCH, "IngrediÃ«nten: suiker, tarwebloem, tarwemeel, citroenzuur")
        )

        assertFalse(result.links.any { it.kind == AlignmentKind.AMBIGUOUS })
        assertTrue(result.occurrences.any { it.rawText == "unknown one" && it.canonicalId == null })
        assertFalse(result.links.any { it.kind == AlignmentKind.CORRECTION_PROPOSAL })
    }

    @Test fun aPartialTranslationDoesNotTurnAnUnpairedUnknownIntoAProposal() {
        val result = generate(
            block("en", LabelLanguage.ENGLISH, "Ingredients: sugar, unknown additive, citric acid"),
            block("nl", LabelLanguage.DUTCH, "IngrediÃ«nten: suiker, citroenzuur")
        )

        assertFalse(result.links.any { it.kind == AlignmentKind.CORRECTION_PROPOSAL })
        assertTrue(result.occurrences.any { it.rawText == "unknown additive" && it.canonicalId == null })
    }

    @Test fun exactConceptsCorroborateWhenTheirListOrderDiffers() {
        val result = generate(
            block("en", LabelLanguage.ENGLISH, "Ingredients: sugar, wheat flour, citric acid"),
            block("nl", LabelLanguage.DUTCH, "IngrediÃ«nten: citroenzuur, tarwebloem, suiker")
        )

        assertEquals(3, result.links.count { it.kind == AlignmentKind.CORROBORATION })
        assertFalse(result.links.any { it.kind == AlignmentKind.CORRECTION_PROPOSAL })
    }

    @Test fun proposalRetainsParentheticalTextAndSourceOccurrence() {
        val result = generate(
            block("en", LabelLanguage.ENGLISH, "Ingredients: sugar, WHEAT lour (whole), citric acid"),
            block("nl", LabelLanguage.DUTCH, "IngrediÃ«nten: suiker, tarwebloem, citroenzuur")
        )

        val proposal = result.links.single { it.kind == AlignmentKind.AMBIGUOUS }
        assertEquals("WHEAT lour (whole)", proposal.first.rawText)
        assertEquals("(whole)", proposal.first.parentheticalText)
        assertEquals("WHEAT lour (whole)", result.occurrences.single { it.id == proposal.first.id }.rawText)
    }

    @Test fun sameAnchorsDoNotProveGelatinIsWheatFlour() {
        val result = generate(
            block("en", LabelLanguage.ENGLISH, "Ingredients: sugar, gelat1n, citric acid"),
            block("nl", LabelLanguage.DUTCH, "Ingrediënten: suiker, tarwebloem, citroenzuur")
        )
        assertFalse(result.links.any { it.kind == AlignmentKind.CORRECTION_PROPOSAL })
        assertTrue(result.links.any { it.kind == AlignmentKind.AMBIGUOUS && it.first.rawText == "gelat1n" })
        assertTrue(result.occurrences.any { it.rawText == "gelat1n" && it.canonicalId == null })
    }

    @Test fun repeatsAndNestedOrDifferentParenthesesDoNotProduceCorrections() {
        val result = generate(
            block("en", LabelLanguage.ENGLISH, "Ingredients: sugar, mystery (animal (pork)), mystery, citric acid"),
            block("nl", LabelLanguage.DUTCH, "Ingrediënten: suiker, tarwebloem (plant), citroenzuur")
        )
        assertFalse(result.links.any { it.kind == AlignmentKind.CORRECTION_PROPOSAL })
        assertTrue(result.occurrences.any { it.rawText.contains("(animal (pork))") })
    }

    @Test fun unknownsStayInTheSourceListAndAnimalAndUncertainStatusesArePreserved() {
        val result = generate(
            block("en", LabelLanguage.ENGLISH, "Ingredients: pork, vegetable oil (Palm), mystery extract"),
            block("nl", LabelLanguage.DUTCH, "IngrediÃ«nten: varkensvlees, plantaardige olie (Palm), mysterie-extract")
        )

        assertTrue(result.occurrences.any { it.rawText == "mystery extract" && it.canonicalId == null })
        assertTrue(result.links.any {
            it.kind == AlignmentKind.CORROBORATION && it.first.canonicalId == "pork_meat" &&
                it.first.status == VeganStatus.NON_VEGAN && it.second.status == VeganStatus.NON_VEGAN
        })
        assertTrue(result.links.any {
            it.kind == AlignmentKind.CORROBORATION && it.first.canonicalId == "vegetable_oil" &&
                it.first.status == VeganStatus.UNCERTAIN && it.second.status == VeganStatus.UNCERTAIN
        })
    }

    @Test fun swedishAndNorwegianBlocksAreRetainedButDoNotReceiveLexicalProposals() {
        val result = MultilingualIngredientAlignment.generate(
            listOf(
                block("en", LabelLanguage.ENGLISH, "Ingredients: sugar, unknown term, citric acid"),
                block("sv", LabelLanguage.SWEDISH, "Ingredienser: Socker, Vetemjol, Citron"),
                block("no", LabelLanguage.NORWEGIAN, "Ingredienser: Sukker, Hvetemel, Sitron")
            ),
            database(),
            lexicon(),
            OriginQualifierRuleSet.empty()
        )

        assertTrue(result.occurrences.any { it.blockId == "block-sv" })
        assertTrue(result.occurrences.any { it.blockId == "block-no" })
        assertFalse(result.links.any {
            it.kind == AlignmentKind.CORRECTION_PROPOSAL &&
                (it.first.language in setOf(LabelLanguage.SWEDISH, LabelLanguage.NORWEGIAN) ||
                    it.second.language in setOf(LabelLanguage.SWEDISH, LabelLanguage.NORWEGIAN))
        })
    }

    private fun generate(first: LanguageBlock, second: LanguageBlock) =
        MultilingualIngredientAlignment.generate(
            listOf(first, second), database(), lexicon(), OriginQualifierRuleSet.empty()
        )

    private fun block(id: String, language: LabelLanguage, text: String) = LanguageBlock(
        language = language,
        marketTags = emptySet(),
        rawText = text,
        startIndex = 0,
        endIndex = text.length,
        detectedMarker = language.displayName,
        hasIngredientHeading = true,
        id = "block-$id"
    )

    private fun database() = listOf(
        Ingredient("sugar", "Sugar", aliases = listOf("sugar", "sucre", "suiker"), status = VeganStatus.VEGAN, reason = "test"),
        Ingredient("wheat_flour", "Wheat flour", aliases = listOf("wheat flour", "tarwebloem", "tarwemeel"), status = VeganStatus.VEGAN, reason = "test"),
        Ingredient("e330", "Citric acid", aliases = listOf("citric acid", "citroenzuur"), status = VeganStatus.VEGAN, reason = "test"),
        Ingredient("pork_meat", "Pork", aliases = listOf("pork", "varkensvlees"), status = VeganStatus.NON_VEGAN, reason = "test"),
        Ingredient("vegetable_oil", "Vegetable oil", aliases = listOf("vegetable oil", "plantaardige olie"), status = VeganStatus.UNCERTAIN, reason = "test")
    )

    private fun lexicon() = MultilingualIngredientLexicon.load(
        """{"schemaVersion":1,"aliases":[
            {"canonicalId":"sugar","language":"EN","aliases":["sugar"],"ocrVariants":[]},
            {"canonicalId":"sugar","language":"NL","aliases":["suiker"],"ocrVariants":[]},
            {"canonicalId":"wheat_flour","language":"NL","aliases":["tarwebloem","tarwemeel"],"ocrVariants":[]},
            {"canonicalId":"e330","language":"EN","aliases":["citric acid"],"ocrVariants":[]},
            {"canonicalId":"e330","language":"NL","aliases":["citroenzuur"],"ocrVariants":[]},
            {"canonicalId":"pork_meat","language":"EN","aliases":["pork"],"ocrVariants":[]},
            {"canonicalId":"pork_meat","language":"NL","aliases":["varkensvlees"],"ocrVariants":[]},
            {"canonicalId":"vegetable_oil","language":"EN","aliases":["vegetable oil"],"ocrVariants":[]},
            {"canonicalId":"vegetable_oil","language":"NL","aliases":["plantaardige olie"],"ocrVariants":[]}
        ],"ocrCorrections":[],"mappings":[]}"""
    )
}
