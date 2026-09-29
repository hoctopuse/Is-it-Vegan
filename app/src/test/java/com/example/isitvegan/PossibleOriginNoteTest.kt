package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PossibleOriginNoteTest {
    private val knowledge = IngredientKnowledge.fromJson(
        projectFile("app/src/main/assets/ingredients.json").readText(),
        projectFile("app/src/main/assets/ingredient_aliases_multilingual.json").readText(),
        projectFile("app/src/main/assets/origin_qualifier_rules.json").readText()
    )
    private val service = IngredientAnalysisService(knowledge)

    @Test fun genericE471IsUncertainAndCarriesItsStructuredPossibleOrigins() {
        val diagnostics = service.analyzeWithDiagnostics("E471")
        val reference = diagnostics.verdictExplanation.uncertainIngredients.single()

        assertEquals("e471", reference.ingredientId)
        assertEquals(VeganStatus.UNCERTAIN, reference.status)
        assertEquals(
            listOf(PossibleOrigin.PLANT, PossibleOrigin.ANIMAL),
            reference.possibleOriginNote?.origins
        )
        assertEquals(OriginVariability.RAW_MATERIAL_AND_PROCESS, reference.possibleOriginNote?.variability)
        assertTrue(reference.possibleOriginNote?.sourceIds?.contains("vegan-easy-food-additives") == true)
    }

    @Test fun uncertainIngredientWithoutNoteKeepsTheExistingExplanationShape() {
        val reference = service.analyzeWithDiagnostics("E967").verdictExplanation.uncertainIngredients.single()
        assertEquals("e967", reference.ingredientId)
        assertNull(reference.possibleOriginNote)
    }

    @Test fun establishedStatusesAndUnknownTextDoNotReceivePossibleOriginNotes() {
        listOf("E902", "E966").forEach { number ->
            val diagnostics = service.analyzeWithDiagnostics(number)
            assertFalse(number, diagnostics.result.matched.single().status == VeganStatus.UNCERTAIN)
            assertTrue(number, diagnostics.verdictExplanation.uncertainIngredients.isEmpty())
        }
        val nonVegan = service.analyzeWithDiagnostics("E901").verdictExplanation.knownBlockingIngredients.single()
        assertEquals(VeganStatus.NON_VEGAN, nonVegan.status)
        assertNull(nonVegan.possibleOriginNote)
        val unknown = service.analyzeWithDiagnostics("additif-471-inconnu")
        assertTrue(unknown.result.matched.isEmpty())
        assertTrue(unknown.verdictExplanation.uncertainIngredients.isEmpty())
    }

    @Test fun localSoyLecithinResolutionSuppressesTheGenericUncertainNote() {
        val diagnostics = service.analyzeWithDiagnostics("lécithines (soja)")
        assertEquals(VeganStatus.VEGAN, diagnostics.result.matched.single { it.id == "e322" }.status)
        assertFalse(diagnostics.verdictExplanation.uncertainIngredients.any { it.ingredientId == "e322" })
    }

    @Test fun suffixedAndCollidingConceptsRemainDistinctAndEachOccurrenceKeepsItsNote() {
        val fattySalts = service.analyzeWithDiagnostics("E470b, E572, E471, E471")
        val references = fattySalts.verdictExplanation.uncertainIngredients
        assertEquals(setOf("e470b", "e572", "e471"), references.map { it.ingredientId }.toSet())
        assertEquals(2, references.count { it.ingredientId == "e471" })
        assertEquals(2, references.filter { it.ingredientId == "e471" }.map { it.occurrenceId }.distinct().size)
        assertTrue(references.filter { it.ingredientId in setOf("e470b", "e572") }.all { it.possibleOriginNote != null })

        val steviol = service.analyzeWithDiagnostics("E960a, E960b")
        assertEquals(setOf("e960a", "e960b"), steviol.result.matched.map { it.id }.toSet())
    }

    @Test fun generatedAssetKeepsAllEditorialNotesAndOnlyUncertainConceptsHaveThem() {
        val noted = knowledge.ingredients.filter { it.possibleOriginNote != null }
        assertEquals(
            setOf(
                "e322", "e422", "e470a", "e470b", "e471", "e572", "e627", "e631", "e635", "e640",
                "processed_fruit_vegetable_product", "spreadable_fat"
            ),
            noted.map { it.id }.toSet()
        )
        assertTrue(noted.all { it.status == VeganStatus.UNCERTAIN })
        assertTrue(noted.all { it.possibleOriginNote!!.sourceIds.isNotEmpty() })
        assertNotNull(knowledge.ingredients.single { it.id == "e471" }.possibleOriginNote)
    }

    private fun projectFile(relativePath: String): File {
        val direct = File(relativePath)
        if (direct.isFile) return direct
        return File("..", relativePath).also { require(it.isFile) { "Missing project file: $relativePath" } }
    }
}
