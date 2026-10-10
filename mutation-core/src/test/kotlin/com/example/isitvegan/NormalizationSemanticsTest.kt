package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NormalizationSemanticsTest {
    @Test fun normalizationPreservesReviewedBoundaries() {
        val cases = mapOf(
            "" to "", " \t\n " to "", "---()!!!" to "",
            "ÉLÉMENTS, Crème!" to "elements creme",
            "œufs Æther" to "oeufs aether", "Cafe\u0301" to "cafe",
            "  A\tB\n C  " to "a b c", "E  471" to "e471",
            "E\t306" to "e306", "E 4 71" to "e4 71",
            "Acids (E 471)" to "acids e 471", "E foo" to "e foo",
            "E471 (D’ORIGINE VÉGÉTALE)" to "e471 d origine vegetale"
        )
        cases.forEach { (input, expected) ->
            assertEquals(input, expected, TextNormalizer.normalize(input))
        }
    }

    @Test fun originVariantsPreserveTheFullQualification() {
        val rules = OriginQualifierRuleSet.load(
            File("../app/src/main/assets/origin_qualifier_rules.json").readText()
        ).also { assertTrue(it.errors.joinToString(), it.isValid) }.rules
        listOf(
            "d’origine végétale", "d'origine animale", "plant-based",
            "van plantaardige oorsprong", "pflanzlichen Ursprungs"
        ).forEach { qualifier ->
            val expected = rules.extractAttached("E471 $qualifier")
            assertTrue(qualifier, expected.qualification != null)
            listOf(
                "  E 471 ${qualifier.uppercase()}  ",
                "E471 ( $qualifier )", "E471 ( ${qualifier.uppercase()} )"
            ).forEach { input ->
                assertEquals(input, expected, rules.extractAttached(input))
            }
        }
        assertEquals(rules.extractAttached("E471 plant-based"),
            rules.extractAttached("E471 ((plant-based))"))
        assertEquals(rules.extractAttached("E471 plant-based"),
            rules.extractAttached("E471 (plant-based"))
        listOf("", "  ", "E471", "E471 (unknown)",
            "E471 plant-based extra", "Vegetable oil (Palm)")
            .forEach { input ->
                assertEquals(input, OriginQualifierExtraction(input), rules.extractAttached(input))
            }
    }
}
