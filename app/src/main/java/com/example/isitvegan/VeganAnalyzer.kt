package com.example.isitvegan

import android.content.Context
import org.json.JSONArray
import java.text.Normalizer

data class AnalysisResult(
    val matched: List<Ingredient>,
    val unknown: List<String>
) {
    val verdict: AnalysisVerdict
        get() = when {
            matched.any { it.status == VeganStatus.NON_VEGAN } -> AnalysisVerdict.NON_VEGETARIAN
            matched.any { it.status == VeganStatus.UNCERTAIN } -> AnalysisVerdict.UNCERTAIN
            unknown.isNotEmpty() || matched.isEmpty() -> AnalysisVerdict.INCONCLUSIVE
            matched.any { it.status == VeganStatus.VEGETARIAN } -> AnalysisVerdict.VEGETARIAN
            else -> AnalysisVerdict.VEGAN
        }
}

enum class AnalysisVerdict { VEGAN, VEGETARIAN, NON_VEGETARIAN, UNCERTAIN, INCONCLUSIVE }

object VeganAnalyzer {
    private var ingredients: List<Ingredient> = emptyList()

    fun loadDatabase(context: Context) {
        val json = context.assets.open("ingredients.json")
            .bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)
        ingredients = buildList {
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val aliasesArray = item.getJSONArray("aliases")
                val aliases = buildList {
                    for (j in 0 until aliasesArray.length()) add(aliasesArray.getString(j))
                }
                add(Ingredient(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    eNumber = item.optString("eNumber").takeIf { it.isNotBlank() },
                    aliases = aliases,
                    status = VeganStatus.valueOf(item.getString("status")),
                    reason = item.getString("reason"),
                    source = item.optString("source").takeIf { it.isNotBlank() }
                ))
            }
        }
    }

    fun analyze(text: String): AnalysisResult = analyze(text, ingredients)

    // Exposed for JVM tests: analysis never needs an Android context or a network connection.
    internal fun analyze(text: String, database: List<Ingredient>): AnalysisResult {
        val chunks = text.replace(Regex("(?i)^\\s*ingr[ée]dients?\\s*:\\s*"), "")
            .split(Regex("[,;()\\[\\]\\n]+"))
            .map { it.trim()
                .replace(Regex("^\\d+(?:[.,]\\d+)?\\s*%\\s*"), "")
                .replace(Regex("\\s+\\d+(?:[.,]\\d+)?\\s*%$"), "") }
            .filter { it.isNotBlank() }
        val found = linkedMapOf<String, Ingredient>()
        val unknown = mutableListOf<String>()
        for (chunk in chunks) {
            val normalized = normalize(chunk)
            val matches = database.filter { ingredient ->
                ingredient.aliases.any { normalize(it) == normalized } ||
                    ingredient.eNumber?.let { normalize(it) == normalized } == true
            }
            if (matches.isEmpty()) unknown.add(chunk)
            else matches.forEach { found[it.id] = it }
        }
        return AnalysisResult(found.values.toList(), unknown)
    }

    private fun normalize(value: String): String {
        val decomposed = Normalizer.normalize(value.lowercase().replace("œ", "oe"), Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{M}+"), "")
            .replace("œ", "oe").replace("æ", "ae")
            .replace(Regex("[^a-z0-9]+"), " ").trim()
            .replace(Regex("^e\\s+(?=\\d)"), "e")
    }
}
