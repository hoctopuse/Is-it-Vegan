package com.example.isitvegan

import java.text.Normalizer

object TextNormalizer {
    private val combiningMarks = Regex("\\p{M}+")
    private val nonAlphanumeric = Regex("[^a-z0-9]+")
    private val spacedENumberPrefix = Regex("^e\\s+(?=\\d)")

    fun normalize(value: String): String {
        val decomposed = Normalizer.normalize(
            value.lowercase().replace("œ", "oe").replace("æ", "ae"),
            Normalizer.Form.NFD
        )
        return decomposed.replace(combiningMarks, "")
            .replace(nonAlphanumeric, " ")
            .trim()
            .replace(spacedENumberPrefix, "e")
    }
}
