package com.example.isitvegan

import android.content.Context
import org.json.JSONArray
import java.text.Normalizer

data class AnalysisResult(
    val matched: List<Ingredient>,
    val unknown: List<String>,
    val stoppedAtNonVegetarian: Boolean = false
) {
    val uncertainIngredients: List<Ingredient>
        get() = matched.filter { it.status == VeganStatus.UNCERTAIN }

    val vegetarianIngredients: List<Ingredient>
        get() = matched.filter { it.status == VeganStatus.VEGETARIAN }

    /** Verdict for the known composition after setting uncertain ingredients aside. */
    val verdictWithoutUncertain: AnalysisVerdict
        get() {
            val certainMatches = matched.filter { it.status != VeganStatus.UNCERTAIN }
            return when {
                certainMatches.any { it.status == VeganStatus.NON_VEGAN } ->
                    AnalysisVerdict.NON_VEGETARIAN
                unknown.isNotEmpty() || certainMatches.isEmpty() -> AnalysisVerdict.INCONCLUSIVE
                certainMatches.any { it.status == VeganStatus.VEGETARIAN } ->
                    AnalysisVerdict.VEGETARIAN
                else -> AnalysisVerdict.VEGAN
            }
        }

    val verdict: AnalysisVerdict
        get() = when {
            matched.any { it.status == VeganStatus.NON_VEGAN } -> AnalysisVerdict.NON_VEGETARIAN
            uncertainIngredients.isNotEmpty() -> AnalysisVerdict.UNCERTAIN
            else -> verdictWithoutUncertain
        }
}

enum class AnalysisVerdict { VEGAN, VEGETARIAN, NON_VEGETARIAN, UNCERTAIN, INCONCLUSIVE }

object VeganAnalyzer {
    private var ingredients: List<Ingredient> = emptyList()

    private val crossContactMarker = Regex(
        "(?i)\\b(?:peut\\s+contenir|traces?\\s*(?:éventuelles?\\s*)?(?:de|d['’]|:)|" +
            "fabriqu[ée]\\s+dans\\s+un\\s+atelier)"
    )

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
        // Allergy and cross-contact notes are not ingredients of the recipe.
        val ingredientText = text.lines()
            .mapNotNull { line ->
                val heading = normalize(line.trim().trim('*').substringBefore(':'))
                if (heading == "traces" || heading == "allergenes") null
                else line.substringBeforeCrossContactNote()
            }
            .joinToString("\n")
            .replace(Regex("(?i)\\s*\\*\\s*Agriculture biologique\\.?\\s*$"), "")
        val chunks = ingredientText.replace(Regex("(?i)^\\s*ingr[ée]dients?\\s*:\\s*"), "")
            .split(Regex("[,;()\\[\\]\\n]+"))
            .map { it.trim()
                .replace(Regex("^\\d+(?:[.,]\\d+)?\\s*%\\s*"), "")
                .replace(Regex("\\s+\\d+(?:[.,]\\d+)?\\s*%$"), "")
                .trimEnd('*', ' ') }
            .filter { it.isNotBlank() }
        val found = linkedMapOf<String, Ingredient>()
        val unknown = mutableListOf<String>()
        var stoppedAtNonVegetarian = false
        for ((index, chunk) in chunks.withIndex()) {
            val normalized = normalize(chunk)
            val matches = database.filter { ingredient ->
                ingredient.aliases.any { normalize(it) == normalized } ||
                    ingredient.eNumber?.let { normalize(it) == normalized } == true
            }
            if (matches.isEmpty()) unknown.add(chunk)
            else matches.forEach { found[it.id] = it }

            // A single non-vegetarian ingredient is enough to settle the verdict.
            // Uncertain and vegetarian ingredients must not stop the remaining analysis.
            if (matches.any { it.status == VeganStatus.NON_VEGAN }) {
                stoppedAtNonVegetarian = index < chunks.lastIndex
                break
            }
        }
        return AnalysisResult(found.values.toList(), unknown, stoppedAtNonVegetarian)
    }

    private fun normalize(value: String): String {
        val decomposed = Normalizer.normalize(value.lowercase().replace("œ", "oe"), Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{M}+"), "")
            .replace("œ", "oe").replace("æ", "ae")
            .replace(Regex("[^a-z0-9]+"), " ").trim()
            .replace(Regex("^e\\s+(?=\\d)"), "e")
    }

    private fun String.substringBeforeCrossContactNote(): String {
        val marker = crossContactMarker.find(this) ?: return this
        return substring(0, marker.range.first)
    }
}
