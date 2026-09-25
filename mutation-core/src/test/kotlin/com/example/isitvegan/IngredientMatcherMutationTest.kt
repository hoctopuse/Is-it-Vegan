package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IngredientMatcherMutationTest {
    @Test fun unknownExpressionRemainsUnmatchedWithOrWithoutUnrelatedDatabaseEntries() {
        listOf(
            emptyList(),
            listOf(ingredient("salt", "sel"))
        ).forEach { database ->
            val match = IngredientMatcher(database).match(token("ingrédient mystère"))

            assertEquals(MatchResolution.NONE, match.resolution)
            assertTrue(match.ingredients.isEmpty())
            assertTrue(match.blockedIngredientIds.isEmpty())
            assertEquals("ingredient mystere", match.residualNormalized)
            assertEquals("ingrédient mystère", UnknownCollector.collect(match))
        }
    }

    @Test fun derivedIngredientAcceptsOnlyReviewedSuffixes() {
        val matcher = IngredientMatcher(listOf(ingredient("apricot", "abricot")))

        listOf("purée d'abricot", "purée d'abricot moulue").forEach { text ->
            val match = matcher.match(token(text))
            assertEquals(text, MatchResolution.COVERED, match.resolution)
            assertEquals(text, listOf("apricot"), match.ingredients.map { it.id })
            assertNull(text, UnknownCollector.collect(match))
        }

        val sweetened = matcher.match(token("purée d'abricot sucrée"))
        assertEquals(MatchResolution.PARTIAL_CONTEXTUAL, sweetened.resolution)
        assertEquals(listOf("apricot"), sweetened.ingredients.map { it.id })
        assertEquals("purée d'abricot sucrée", UnknownCollector.collect(sweetened))
    }

    @Test fun glueWordsDoNotHideARealUnknownRemainder() {
        val matcher = IngredientMatcher(listOf(ingredient("salt", "sel")))

        val glueOnly = matcher.match(token("sel et"))
        assertEquals(MatchResolution.COVERED, glueOnly.resolution)
        assertNull(UnknownCollector.collect(glueOnly))

        val semanticRemainder = matcher.match(token("sel et mystère"))
        assertEquals(MatchResolution.PARTIAL_CONTEXTUAL, semanticRemainder.resolution)
        assertEquals("sel et mystère", UnknownCollector.collect(semanticRemainder))
    }

    @Test fun englishCocoaButterProtectsButterOnlyWhenThePhraseIsContiguous() {
        val matcher = IngredientMatcher(
            listOf(
                ingredient("butter", "butter", VeganStatus.VEGETARIAN),
                ingredient("cocoa", "cocoa")
            )
        )

        val cocoaButter = matcher.match(token("cocoa butter"))
        assertEquals(MatchResolution.COVERED, cocoaButter.resolution)
        assertEquals(listOf("cocoa"), cocoaButter.ingredients.map { it.id })
        assertEquals(listOf("butter"), cocoaButter.blockedIngredientIds)
        assertNull(UnknownCollector.collect(cocoaButter))

        val interrupted = matcher.match(token("cocoa sweet butter"))
        assertEquals(MatchResolution.PARTIAL_CONTEXTUAL, interrupted.resolution)
        assertEquals(setOf("cocoa", "butter"), interrupted.ingredients.map { it.id }.toSet())
        assertTrue(interrupted.blockedIngredientIds.isEmpty())
        assertEquals("cocoa sweet butter", UnknownCollector.collect(interrupted))
    }

    @Test fun protectedPhraseAllowsPunctuationButRejectsSemanticRemainders() {
        val matcher = IngredientMatcher(
            listOf(
                ingredient("butter", "beurre", VeganStatus.VEGETARIAN),
                ingredient("cocoa", "cacao"),
                ingredient("salt", "sel")
            )
        )

        listOf("(beurre de cacao)", "[beurre de cacao]", "beurre de cacao.").forEach { text ->
            val match = matcher.match(token(text))
            assertEquals(text, MatchResolution.COVERED, match.resolution)
            assertEquals(text, listOf("cocoa"), match.ingredients.map { it.id })
            assertEquals(text, listOf("butter"), match.blockedIngredientIds)
            assertNull(text, UnknownCollector.collect(match))
        }

        val extra = matcher.match(token("beurre de cacao extra"))
        assertEquals(MatchResolution.BLOCKED_CONFLICT, extra.resolution)
        assertEquals(listOf("cocoa"), extra.ingredients.map { it.id })
        assertEquals(listOf("butter"), extra.blockedIngredientIds)
        assertEquals("beurre de cacao extra", UnknownCollector.collect(extra))

        val independentIngredient = matcher.match(token("beurre de cacao sel"))
        assertEquals(setOf("cocoa", "salt"), independentIngredient.ingredients.map { it.id }.toSet())
        assertFalse(independentIngredient.ingredients.any { it.id == "butter" })
        assertEquals(listOf("butter"), independentIngredient.blockedIngredientIds)
    }

    private fun token(text: String) = IngredientToken(text, 0, 0)

    private fun ingredient(
        id: String,
        alias: String,
        status: VeganStatus = VeganStatus.VEGAN
    ) = Ingredient(id, alias, listOf(alias), null, status, "Test")
}
