package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MultilingualIngredientMappingTest {
    private val source = file("knowledge/ingredient_aliases_multilingual.json").readBytes()
    private val asset = file("app/src/main/assets/ingredient_aliases_multilingual.json").readBytes()
    private val knowledge = IngredientKnowledge.fromJson(
        file("app/src/main/assets/ingredients.json").readText(),
        asset.toString(Charsets.UTF_8),
        file("app/src/main/assets/origin_qualifier_rules.json").readText()
    )

    @Test fun sourceAssetAndRuntimeMappingStayConsistent() {
        assertTrue(source.contentEquals(asset))
        val root = MiniJson.parse(source.toString(Charsets.UTF_8)) as Map<*, *>
        val known = knowledge.ingredients.map { it.id }.toSet()
        val mappings = root["mappings"] as List<*>
        assertEquals(1808, mappings.size)
        mappings.map { it as Map<*, *> }.forEach { mapping ->
            assertTrue(mapping["conceptId"] in known)
            assertTrue(mapping["language"] in setOf("FR", "NL", "EN", "DE", "IT", "ES", "PL"))
            assertTrue((mapping["surfaceForm"] as String).isNotBlank())
            assertTrue((mapping["normalizedForm"] as String).isNotBlank())
            assertTrue((mapping["source"] as String).isNotBlank())
        }
        val owners = mutableMapOf<Pair<String, String>, String>()
        (root["aliases"] as List<*>).map { it as Map<*, *> }.forEach { entry ->
            val id = entry["canonicalId"] as String
            val language = entry["language"] as String
            assertTrue("$id is unavailable", id in known || id == "cereals")
            assertTrue("$id/$language", language in setOf("FR", "NL", "EN", "DE", "IT", "ES", "PL"))
            ((entry["aliases"] as List<*>) + (entry["ocrVariants"] as List<*>)).filterIsInstance<String>().forEach { alias ->
                assertFalse(alias.contains("?"))
                val key = language to TextNormalizer.normalize(alias)
                assertEquals("collision for $key", owners.putIfAbsent(key, id) ?: id, id)
            }
        }
        listOf("miel", "honing", "honey", "Honig", "lait concentré", "whole milk powder", "Vollmilchpulver", "Rahmpulver", "E470b", "E960a").forEach { text ->
            assertTrue(text, IngredientMatcher(knowledge.ingredients).match(IngredientToken(text, 0, 0)).ingredients.isNotEmpty())
        }
        assertTrue(knowledge.ingredients.filter { it.possibleOriginNote != null }.all { it.id in known })
    }

    private fun file(path: String): File = File(path).takeIf { it.isFile } ?: File("..", path)
}
