package com.example.isitvegan

import java.text.Normalizer

object TextNormalizer {
    fun normalize(value: String): String {
        val decomposed = Normalizer.normalize(
            value.lowercase().replace("œ", "oe").replace("æ", "ae"),
            Normalizer.Form.NFD
        )
        return decomposed.replace(Regex("\\p{M}+"), "")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
            .replace(Regex("^e\\s+(?=\\d)"), "e")
    }
}
