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
        assertEquals(1996, mappings.size)
        assertTrue(mappings.size >= 1864)
        mappings.map { it as Map<*, *> }.forEach { mapping ->
            assertTrue(mapping["conceptId"] in known)
            assertTrue(mapping["language"] in setOf("FR", "NL", "EN", "DE", "IT", "ES", "PL"))
            assertTrue((mapping["surfaceForm"] as String).isNotBlank())
            assertTrue((mapping["normalizedForm"] as String).isNotBlank())
            assertTrue((mapping["source"] as String).isNotBlank())
        }
        val mappingRows = mappings.map { it as Map<*, *> }
        assertEquals(mappingRows.size, mappingRows.map { listOf(it["conceptId"], it["language"], it["normalizedForm"], it["relation"]) }.distinct().size)
        setOf("fruit_juice", "fruit_puree", "fruit_nectar").forEach { id ->
            assertEquals(setOf("FR", "NL", "EN", "DE"), mappingRows.filter { it["conceptId"] == id }.map { it["language"] }.toSet())
        }
        setOf("cocoa", "cocoa_butter", "powdered_chocolate", "chocolate", "milk_chocolate", "white_chocolate", "filled_chocolate", "chocolate_confection").forEach { id ->
            assertEquals(setOf("FR", "NL", "EN", "DE"), mappingRows.filter { it["conceptId"] == id }.map { it["language"] }.toSet())
        }
        setOf("fruit_jam", "fruit_jelly", "citrus_marmalade", "sweetened_chestnut_puree").forEach { id ->
            assertEquals(setOf("FR", "NL", "EN", "DE"), mappingRows.filter { it["conceptId"] == id }.map { it["language"] }.toSet())
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
        val process = ProcessBuilder("python", "tools/build_multilingual_ingredient_mapping.py", "--check")
            .directory(projectRoot()).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(output, 0, process.waitFor())
    }

    private fun projectRoot(): File = generateSequence(File(System.getProperty("user.dir") ?: error("System property user.dir is unavailable")).canonicalFile) { it.parentFile }
        .firstOrNull { File(it, "settings.gradle.kts").isFile }
        ?: error("Project root containing settings.gradle.kts not found")
    private fun file(path: String): File = File(projectRoot(), path).also { require(it.isFile) { "Missing project file: ${it.absolutePath}" } }
}
