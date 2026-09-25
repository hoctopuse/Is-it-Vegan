package com.example.isitvegan

import android.content.Context

/** Android boundary: reads production assets and hands immutable text data to the JVM core. */
internal object AndroidIngredientKnowledgeLoader {
    fun load(context: Context): IngredientKnowledge = IngredientKnowledge.fromJson(
        ingredientsJson = context.assets.open("ingredients.json")
            .bufferedReader().use { it.readText() },
        multilingualAliasesJson = context.assets.open("ingredient_aliases_multilingual.json")
            .bufferedReader().use { it.readText() },
        originRulesJson = context.assets.open("origin_qualifier_rules.json")
            .bufferedReader().use { it.readText() }
    )
}
