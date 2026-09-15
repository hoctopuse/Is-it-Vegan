package com.example.isitvegan

import android.content.Context
import org.json.JSONArray

object VeganAnalyzer {

    private var ingredients: List<Ingredient> = emptyList()

    fun loadDatabase(context: Context) {

        val json = context.assets
            .open("ingredients.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonArray = JSONArray(json)

        ingredients = buildList {

            for (i in 0 until jsonArray.length()) {

                val item = jsonArray.getJSONObject(i)

                val aliasesArray = item.getJSONArray("aliases")

                val aliases = buildList {
                    for (j in 0 until aliasesArray.length()) {
                        add(aliasesArray.getString(j))
                    }
                }

                add(
                    Ingredient(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        eNumber = item.optString("eNumber")
                            .takeIf { it.isNotBlank() },
                        aliases = aliases,
                        status = VeganStatus.valueOf(
                            item.getString("status")
                        ),
                        reason = item.getString("reason"),
                        source = item.optString("source")
                            .takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }

    fun analyze(text: String): List<Ingredient> {

        val normalizedText = text
            .lowercase()
            .replace(Regex("[^a-z0-9à-ÿ]+"), " ")
            .trim()

        return ingredients.filter { ingredient ->

            val eNumberFound = ingredient.eNumber?.let { eNumber ->

                val number = eNumber
                    .lowercase()
                    .removePrefix("e")
                    .trim()

                val eNumberRegex =
                    Regex("""\be[\s-]*$number\b""")

                eNumberRegex.containsMatchIn(text.lowercase())

            } ?: false

            val aliasFound = ingredient.aliases.any { alias ->

                val normalizedAlias = alias
                    .lowercase()
                    .replace(Regex("[^a-z0-9à-ÿ]+"), " ")
                    .trim()

                Regex("""\b${Regex.escape(normalizedAlias)}\b""")
                    .containsMatchIn(normalizedText)
            }

            eNumberFound || aliasFound
        }
    }
}