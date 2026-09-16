package com.example.isitvegan

import android.content.Context
import org.json.JSONArray

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
        get() = VerdictEngine.evaluate(matched, unknown, excludeUncertain = true)

    val verdict: AnalysisVerdict
        get() = VerdictEngine.evaluate(matched, unknown)
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
        val tokens = IngredientTokenizer.tokenize(LabelPreprocessor.preprocess(text))
        val matcher = IngredientMatcher(database)
        val found = linkedMapOf<String, Ingredient>()
        val unknown = mutableListOf<String>()
        var stoppedAtNonVegetarian = false
        for ((index, token) in tokens.withIndex()) {
            val match = matcher.match(token)
            match.ingredients.forEach { found[it.id] = it }
            UnknownCollector.collect(match)?.let(unknown::add)

            // A single non-vegetarian ingredient is enough to settle the verdict.
            // Uncertain and vegetarian ingredients must not stop the remaining analysis.
            if (VerdictEngine.isDecisiveNonVegetarian(match.ingredients)) {
                stoppedAtNonVegetarian = index < tokens.lastIndex
                break
            }
        }
        return AnalysisResult(found.values.toList(), unknown, stoppedAtNonVegetarian)
    }
}
