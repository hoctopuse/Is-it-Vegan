package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JamsDirectiveImportTest {
    private val root = projectRoot()
    private val knowledgeAsset = root.resolve("app/src/main/assets/ingredients.json")
    private val aliasesAsset = root.resolve("app/src/main/assets/ingredient_aliases_multilingual.json")
    private val originRulesAsset = root.resolve("app/src/main/assets/origin_qualifier_rules.json")
    private val knowledge = IngredientKnowledge.fromJson(
        knowledgeAsset.readText(), aliasesAsset.readText(), originRulesAsset.readText()
    )
    private val service = IngredientAnalysisService(knowledge)

    @Test fun annexIConceptsAreDistinctVeganAndResolveInAllFourLanguages() {
        val statuses = knowledge.ingredients
            .filter { it.id in directiveConcepts }
            .associate { it.id to it.status }
        assertEquals(
            directiveConcepts.associateWith { VeganStatus.VEGAN },
            statuses
        )

        cases.forEach { (concept, terms) ->
            terms.forEach { term ->
                assertTrue("$term should resolve to $concept", matches(term, concept))
            }
        }
        assertEquals(4, directiveConcepts.size)
        assertTrue(directiveConcepts.none { it in setOf("fruit_juice", "fruit_puree", "fruit_nectar") })
    }

    @Test fun regulatedFamiliesDoNotCollapseIntoOtherFruitConceptsOrFlavours() {
        assertFalse(matches("confiture", "fruit_jelly"))
        assertFalse(matches("gelée", "fruit_jam"))
        assertFalse(matches("marmelade d’agrumes", "fruit_jam"))
        assertFalse(matches("crème de marrons", "sweetened_chestnut_puree") && matches("crème de marrons", "fruit_jam"))
        assertFalse(service.analyze("arôme de fraise", InputMode.MANUAL_INGREDIENT_LIST).matched.any { it.id in directiveConcepts })
        assertEquals(VeganStatus.VEGAN, knowledge.ingredients.single { it.id == "fruit_juice" }.status)
        assertEquals(VeganStatus.VEGAN, knowledge.ingredients.single { it.id == "fruit_puree" }.status)
        assertEquals(VeganStatus.UNCERTAIN, knowledge.ingredients.single { it.id == "fruit_nectar" }.status)
    }

    @Test fun editorialAliasesMappingsAndGeneratedAssetsStayInParity() {
        val sourceAliases = root.resolve("knowledge/ingredient_aliases_multilingual.json")
        assertTrue("missing editorial aliases", sourceAliases.isFile)
        assertEquals(sourceAliases.readBytes().toList(), aliasesAsset.readBytes().toList())
        val sourceIngredients = root.resolve("knowledge/ingredients.json")
        assertTrue("missing editorial ingredients", sourceIngredients.isFile)
        directiveConcepts.forEach { concept ->
            assertTrue(sourceIngredients.readText().contains("\"id\": \"$concept\""))
        }
        val source = MiniJson.parse(sourceAliases.readText()) as Map<*, *>
        val mappings = source["mappings"] as List<*>
        directiveConcepts.forEach { concept ->
            val records = mappings.filter { (it as Map<*, *>)["conceptId"] == concept }.map { it as Map<*, *> }
            assertEquals(setOf("FR", "NL", "EN", "DE"), records.map { it["language"] }.toSet())
            assertTrue(records.all { it["mappingGroup"] == "fruit-jams-directive" && it["relation"] == "REGULATORY_ALIAS" })
            assertTrue(records.all { (it["surfaceForm"] as String).isNotBlank() && !(it["surfaceForm"] as String).contains('?') })
        }
    }

    @Test fun importerIsIdempotentAfterTheEditorialWrite() {
        val process = ProcessBuilder("python", "tools/import_eu_jams_directive.py", "--check")
            .directory(root)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        assertEquals(output, 0, process.waitFor())
        assertTrue(output, output.contains("changes=0"))
    }

    @Test fun priorKnowledgeAndMappingsRemainAvailable() {
        val previous = setOf("fruit_juice", "fruit_puree", "fruit_nectar", "honey", "milk", "cream", "whey", "casein", "lactose")
        assertTrue(knowledge.ingredients.map { it.id }.containsAll(previous))
        assertTrue("knowledge must remain additive", knowledge.ingredients.size >= 459)
        val source = MiniJson.parse(root.resolve("knowledge/ingredient_aliases_multilingual.json").readText()) as Map<*, *>
        val mappings = source["mappings"] as List<*>
        assertTrue("historical mappings must remain available", mappings.size >= 1834)
        setOf("fruit_juice", "fruit_puree", "fruit_nectar", "honey", "milk", "cream", "whey").forEach { concept ->
            assertTrue("missing mapping for $concept", mappings.any { (it as Map<*, *>)["conceptId"] == concept })
        }
    }

    private fun matches(term: String, concept: String): Boolean =
        service.analyze(term, InputMode.MANUAL_INGREDIENT_LIST).matched.any { it.id == concept }

    private fun projectRoot(): File {
        var current = File(System.getProperty("user.dir") ?: error("user.dir is unavailable")).absoluteFile
        while (!current.resolve("settings.gradle.kts").isFile) {
            val parent = current.parentFile ?: error("Cannot locate project root from ${System.getProperty("user.dir")}")
            current = parent
        }
        return current
    }

    private companion object {
        val directiveConcepts = setOf("fruit_jam", "fruit_jelly", "citrus_marmalade", "sweetened_chestnut_puree")
        val cases = mapOf(
            "fruit_jam" to listOf("confiture", "confituur", "jam", "Konfitüre"),
            "fruit_jelly" to listOf("gelée", "gelei", "jelly", "Gelee"),
            "citrus_marmalade" to listOf("marmelade d’agrumes", "citrusmarmelade", "citrus marmalade", "Zitrusmarmelade"),
            "sweetened_chestnut_puree" to listOf("crème de marrons", "kastanjepasta", "sweetened chestnut purée", "Maronenkrem")
        )
    }
}
