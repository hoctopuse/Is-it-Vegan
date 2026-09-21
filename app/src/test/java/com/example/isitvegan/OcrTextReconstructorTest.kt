package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrTextReconstructorTest {
    @Test fun severalLinesInOneBlockKeepUsefulLineBreaks() {
        val block = block(0, listOf(
            line("Ingrédients : eau,", 0, 0, 200, 20, 0, 0),
            line("sucre, sel", 0, 25, 200, 45, 0, 1)
        ))

        assertEquals("Ingrédients : eau,\nsucre, sel", reconstruct(block))
    }

    @Test fun verticallyDistantBlocksGetAnEmptyLine() {
        val first = block(0, listOf(line("Ingrédients : eau", 0, 0, 200, 20, 0, 0)))
        val second = block(1, listOf(line("Conserver au frais", 0, 100, 200, 120, 1, 0)))

        assertEquals("Ingrédients : eau\n\nConserver au frais", reconstruct(first, second))
    }

    @Test fun twoColumnsAreReadColumnByColumn() {
        val mixedByMlKit = block(0, listOf(
            line("FR Ingrédients", 0, 0, 120, 20, 0, 0),
            line("NL Ingrediënten", 300, 0, 440, 20, 0, 1),
            line("eau, sucre", 0, 30, 120, 50, 0, 2),
            line("water, suiker", 300, 30, 440, 50, 0, 3)
        ), OcrBounds(0, 0, 440, 50))

        assertEquals(
            "FR Ingrédients\neau, sucre\nNL Ingrediënten\nwater, suiker",
            reconstruct(mixedByMlKit)
        )
    }

    @Test fun missingCoordinatesFallBackToMlKitOrder() {
        val lines = listOf(
            OcrLine("deuxième", null, 0, 1, 1),
            OcrLine("première", null, 0, 0, 0)
        )
        val result = OcrTextReconstructor.reconstruct(OcrDocument("brut", null, listOf(OcrBlock(0, 0, null, lines))))

        assertEquals("première\ndeuxième", result.text)
        assertTrue(result.warnings.any { it.contains("ordre ML Kit") })
    }

    private fun reconstruct(vararg blocks: OcrBlock): String =
        OcrTextReconstructor.reconstruct(OcrDocument("sortie brute", 0, blocks.toList())).text

    private fun block(number: Int, lines: List<OcrLine>, bounds: OcrBounds? = union(lines)) =
        OcrBlock(number, number, bounds, lines)

    private fun line(text: String, left: Int, top: Int, right: Int, bottom: Int, block: Int, order: Int) =
        OcrLine(text, OcrBounds(left, top, right, bottom), block, order, order)

    private companion object {
        fun union(lines: List<OcrLine>): OcrBounds? {
            val bounds = lines.mapNotNull { it.bounds }
            return if (bounds.isEmpty()) null else OcrBounds(
                bounds.minOf { it.left }, bounds.minOf { it.top },
                bounds.maxOf { it.right }, bounds.maxOf { it.bottom }
            )
        }
    }
}
