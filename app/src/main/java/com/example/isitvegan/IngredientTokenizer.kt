package com.example.isitvegan

internal data class IngredientToken(
    val text: String,
    val depth: Int,
    val order: Int
)

/** Splits an ingredient label while retaining the nesting depth of sub-compositions. */
internal object IngredientTokenizer {
    fun tokenize(text: String): List<IngredientToken> {
        val tokens = mutableListOf<IngredientToken>()
        val buffer = StringBuilder()
        var depth = 0

        fun flush() {
            val value = buffer.toString().trim().trim('*', ' ')
            buffer.clear()
            if (TextNormalizer.normalize(value).isNotBlank()) {
                tokens += IngredientToken(value, depth, tokens.size)
            }
        }

        text.forEach { character ->
            when (character) {
                ',', ';', '\n' -> flush()
                '(', '[' -> {
                    flush()
                    depth += 1
                }
                ')', ']' -> {
                    flush()
                    depth = (depth - 1).coerceAtLeast(0)
                }
                else -> buffer.append(character)
            }
        }
        flush()
        return tokens
    }
}
