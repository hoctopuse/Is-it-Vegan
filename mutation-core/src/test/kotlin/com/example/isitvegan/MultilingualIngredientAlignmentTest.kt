package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class MultilingualIngredientAlignmentTest {
    @Test fun sameCanonicalConceptIsCorroboratedWithoutDroppingEitherOccurrence() {
        val french = occurrence("fr", LabelLanguage.FRENCH, "huile de palme", "palm_oil", VeganStatus.NON_VEGAN)
        val english = occurrence("en", LabelLanguage.ENGLISH, "palm oil", "palm_oil", VeganStatus.NON_VEGAN)

        val result = align(french, english, AlignmentBasis.CANONICAL_ID)

        assertEquals(AlignmentKind.CORROBORATION, result.kind)
        assertSame(french, result.first)
        assertSame(english, result.second)
        assertNull(result.proposedCanonicalId)
    }

    @Test fun unknownOcrFormGetsOnlyAProposalAndItsSourceAndUnknownStatusRemain() {
        val damaged = occurrence("ocr", LabelLanguage.ENGLISH, "WHEAT lour", null, null)
        val dutch = occurrence("nl", LabelLanguage.DUTCH, "tarwebloem", "wheat_flour", VeganStatus.VEGAN)

        val result = align(damaged, dutch, AlignmentBasis.EXPLICIT_TRANSLATION)

        assertEquals(AlignmentKind.CORRECTION_PROPOSAL, result.kind)
        assertEquals("wheat_flour", result.proposedCanonicalId)
        assertEquals("WHEAT lour", result.first.rawText)
        assertNull(result.first.canonicalId)
        assertNull(result.first.status)
    }

    @Test fun aKnownAnimalIngredientCannotBecomeTheSourceOfACorrectionForAnUnknown() {
        val animal = occurrence("meat", LabelLanguage.ENGLISH, "pork", "pork_meat", VeganStatus.NON_VEGAN)
        val unknown = occurrence("unknown", LabelLanguage.FRENCH, "inconnu", null, null)
        val result = align(animal, unknown, AlignmentBasis.EXPLICIT_TRANSLATION)
        assertEquals(AlignmentKind.UNRESOLVED, result.kind)
        assertNull(result.proposedCanonicalId)
        assertEquals(VeganStatus.NON_VEGAN, result.first.status)
    }

    @Test fun partialTranslationLeavesUnlinkedOccurrencesAlone() {
        val blocks = listOf(
            occurrence("en-0", LabelLanguage.ENGLISH, "sugar", "sugar", VeganStatus.VEGAN),
            occurrence("en-1", LabelLanguage.ENGLISH, "unknown additive", null, null),
            occurrence("fr-0", LabelLanguage.FRENCH, "sucre", "sugar", VeganStatus.VEGAN)
        )

        val evaluation = evaluate(blocks, listOf(link("en-0", "fr-0", AlignmentBasis.CANONICAL_ID)))

        assertEquals(1, evaluation.links.size)
        assertEquals(blocks, evaluation.occurrences)
        assertEquals("en-1", evaluation.occurrences[1].id)
        assertNull(evaluation.occurrences[1].canonicalId)
    }

    @Test fun explicitLinksDoNotDependOnTheOrderOfItemsInEachBlock() {
        val englishPalm = occurrence("en-palm", LabelLanguage.ENGLISH, "palm oil", "palm_oil", VeganStatus.NON_VEGAN, 0)
        val englishSugar = occurrence("en-sugar", LabelLanguage.ENGLISH, "sugar", "sugar", VeganStatus.VEGAN, 1)
        val frenchSugar = occurrence("fr-sugar", LabelLanguage.FRENCH, "sucre", "sugar", VeganStatus.VEGAN, 0)
        val frenchPalm = occurrence("fr-palm", LabelLanguage.FRENCH, "huile de palme", "palm_oil", VeganStatus.NON_VEGAN, 1)

        val results = evaluate(
            listOf(englishPalm, englishSugar, frenchSugar, frenchPalm),
            listOf(
                link("en-palm", "fr-palm", AlignmentBasis.CANONICAL_ID),
                link("en-sugar", "fr-sugar", AlignmentBasis.CANONICAL_ID)
            )
        )

        assertEquals(listOf(AlignmentKind.CORROBORATION, AlignmentKind.CORROBORATION), results.links.map { it.kind })
        assertEquals(0, results.links[0].first.order)
        assertEquals(1, results.links[0].second.order)
    }

    @Test fun parentheticalSourceTextIsPreservedVerbatim() {
        val first = occurrence("en", LabelLanguage.ENGLISH, "vegetable oil (Palm)", "vegetable_oil", VeganStatus.UNCERTAIN, parenthetical = "(Palm)")
        val second = occurrence("de", LabelLanguage.GERMAN, "pflanzliches Öl (Palm)", "vegetable_oil", VeganStatus.UNCERTAIN, parenthetical = "(Palm)")

        val result = align(first, second, AlignmentBasis.CANONICAL_ID)

        assertEquals(AlignmentKind.CORROBORATION, result.kind)
        assertEquals("vegetable oil (Palm)", result.first.rawText)
        assertEquals("(Palm)", result.first.parentheticalText)
        assertEquals("pflanzliches Öl (Palm)", result.second.rawText)
    }

    @Test fun conflictingCanonicalIdsAreNeverCollapsed() {
        val first = occurrence("a", LabelLanguage.ENGLISH, "animal fat", "animal_fat", VeganStatus.NON_VEGAN)
        val second = occurrence("b", LabelLanguage.FRENCH, "graisse végétale", "vegetable_fat", VeganStatus.VEGAN)

        val result = align(first, second, AlignmentBasis.EXPLICIT_TRANSLATION)

        assertEquals(AlignmentKind.CONFLICT, result.kind)
        assertNull(result.proposedCanonicalId)
        assertEquals("animal_fat", result.first.canonicalId)
        assertEquals("vegetable_fat", result.second.canonicalId)
    }

    @Test fun approximateOrPositionOnlyLinksAreAmbiguous() {
        val unknown = occurrence("a", LabelLanguage.ENGLISH, "mystery", null, null)
        val known = occurrence("b", LabelLanguage.GERMAN, "zucker", "sugar", VeganStatus.VEGAN)

        listOf(AlignmentBasis.POSITION_ONLY, AlignmentBasis.APPROXIMATE, AlignmentBasis.AMBIGUOUS)
            .forEach { basis ->
                val result = align(unknown, known, basis)
                assertEquals(basis.name, AlignmentKind.AMBIGUOUS, result.kind)
                assertNull(result.proposedCanonicalId)
                assertNull(result.first.canonicalId)
            }
    }

    @Test fun unknownTermsStayUnknownAndDoNotProduceAConsolidatedVerdict() {
        val first = occurrence("en", LabelLanguage.ENGLISH, "mystery extract", null, null)
        val second = occurrence("nl", LabelLanguage.DUTCH, "onbekend extract", null, null)

        val result = align(first, second, AlignmentBasis.EXPLICIT_TRANSLATION)

        assertEquals(AlignmentKind.UNRESOLVED, result.kind)
        assertNull(result.proposedCanonicalId)
        assertNull(result.first.status)
        assertNull(result.second.status)
    }

    @Test fun animalAndUncertainStatusesArePreservedAndNeverImprovedByAProposal() {
        val animal = occurrence("meat", LabelLanguage.GERMAN, "Schweinefleisch", "pork_meat", VeganStatus.NON_VEGAN)
        val uncertain = occurrence("uncertain", LabelLanguage.GERMAN, "pflanzliches Öl (Palm)", "vegetable_oil", VeganStatus.UNCERTAIN, parenthetical = "(Palm)")
        val damaged = occurrence("ocr", LabelLanguage.ENGLISH, "palm o1l", null, null)

        val corroboration = align(animal, occurrence("meat-fr", LabelLanguage.FRENCH, "viande de porc", "pork_meat", VeganStatus.NON_VEGAN), AlignmentBasis.CANONICAL_ID)
        val proposal = align(damaged, uncertain, AlignmentBasis.EXPLICIT_TRANSLATION)

        assertEquals(VeganStatus.NON_VEGAN, corroboration.first.status)
        assertEquals(VeganStatus.NON_VEGAN, corroboration.second.status)
        assertEquals(AlignmentKind.CORRECTION_PROPOSAL, proposal.kind)
        assertEquals("vegetable_oil", proposal.proposedCanonicalId)
        assertNull(proposal.first.status)
        assertEquals(VeganStatus.UNCERTAIN, proposal.second.status)
    }

    @Test fun sameCanonicalIdWithContradictoryStatusesIsAConflict() {
        val first = occurrence("a", LabelLanguage.ENGLISH, "pork", "pork_meat", VeganStatus.NON_VEGAN)
        val second = occurrence("b", LabelLanguage.FRENCH, "porc", "pork_meat", VeganStatus.VEGAN)

        val result = align(first, second, AlignmentBasis.CANONICAL_ID)

        assertEquals(AlignmentKind.CONFLICT, result.kind)
        assertEquals(VeganStatus.NON_VEGAN, result.first.status)
        assertEquals(VeganStatus.VEGAN, result.second.status)
    }

    private fun align(first: IngredientOccurrence, second: IngredientOccurrence, basis: AlignmentBasis) =
        align(listOf(first, second), listOf(link(first.id, second.id, basis))).single()

    private fun align(occurrences: List<IngredientOccurrence>, links: List<AlignmentLink>) =
        evaluate(occurrences, links).links

    private fun evaluate(occurrences: List<IngredientOccurrence>, links: List<AlignmentLink>) =
        MultilingualIngredientAlignment.evaluate(occurrences, links)

    private fun occurrence(
        id: String,
        language: LabelLanguage,
        text: String,
        canonicalId: String?,
        status: VeganStatus?,
        order: Int = 0,
        parenthetical: String? = null
    ) = IngredientOccurrence(id, "block-$id", language, text, order, parenthetical, canonicalId, status)

    private fun link(first: String, second: String, basis: AlignmentBasis) = AlignmentLink(first, second, basis)
}
