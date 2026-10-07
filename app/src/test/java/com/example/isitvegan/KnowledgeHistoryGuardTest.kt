package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class KnowledgeHistoryGuardTest {
    private val root = KnowledgeValidationTestSupport.root()
    @Suppress("UNCHECKED_CAST")
    private fun ingredients() = MiniJson.parse(root.resolve("knowledge/ingredients.json").readText()) as MutableList<MutableMap<String, Any?>>
    @Suppress("UNCHECKED_CAST")
    private fun lexicon() = MiniJson.parse(root.resolve("knowledge/ingredient_aliases_multilingual.json").readText()) as MutableMap<String, Any?>
    @Suppress("UNCHECKED_CAST")
    private fun rows(lex: Map<String, Any?>, field: String) = lex[field] as MutableList<MutableMap<String, Any?>>
    private fun check(db: Any? = ingredients(), lex: Any? = lexicon()) =
        KnowledgeValidationTestSupport.historicalCheck(root, KnowledgeValidationTestSupport.json(db), KnowledgeValidationTestSupport.json(lex))

    @Test fun currentCorpusIncludingTheFourNewMappingsPreservesHistory() = check()

    @Test fun coordinatedHistoricalEntryAndMappingDeletionFails() {
        val lex = lexicon()
        rows(lex, "aliases").removeAll { it["canonicalId"] == "e100" && it["language"] == "EN" }
        rows(lex, "mappings").removeAll { it["conceptId"] == "e100" && it["language"] == "EN" }
        assertThrows(IllegalArgumentException::class.java) { check(lex = lex) }
    }

    @Test fun changedOwnerMetadataOrAdditionalHistoricalMappingFieldFails() {
        listOf("conceptId", "surfaceForm", "language", "mappingGroup", "relation", "normalizedForm",
            "source", "confidence", "sourceEvidence", "unexpectedProperty").forEach { field ->
            val lex = lexicon()
            val mapping = rows(lex, "mappings").first { it["conceptId"] == "horse_meat" }
            mapping[field] = if (field == "conceptId") "meat" else "WRONG"
            assertThrows(field, IllegalArgumentException::class.java) { check(lex = lex) }
        }
    }

    @Test fun historicalConceptCannotBecomeUnavailableEvenAfterCoordinatedRemoval() {
        val db = ingredients().also { it.removeAll { row -> row["id"] == "e100" } }
        assertThrows(IllegalArgumentException::class.java) { check(db = db) }
        val lex = lexicon()
        rows(lex, "aliases").removeAll { it["canonicalId"] == "e100" }
        rows(lex, "mappings").removeAll { it["conceptId"] == "e100" }
        assertThrows(IllegalArgumentException::class.java) { check(db, lex) }
    }

    @Test fun legitimateNewConceptAliasAndMappingAreAllowed() {
        val db = ingredients()
        val concept = db.first().toMutableMap().apply {
            put("id", "future_reviewed_test_concept"); put("name", "future test surface"); put("aliases", listOf("future test surface"))
        }
        db.add(concept)
        val lex = lexicon()
        rows(lex, "aliases").add(mutableMapOf("canonicalId" to concept["id"], "language" to "FR",
            "aliases" to listOf("future test surface"), "ocrVariants" to emptyList<String>()))
        rows(lex, "mappings").add(mutableMapOf("conceptId" to concept["id"], "language" to "FR",
            "surfaceForm" to "future test surface", "normalizedForm" to "future test surface",
            "mappingGroup" to concept["id"], "relation" to "COMMON_LABEL_NAME", "source" to "vegan-society", "confidence" to "REVIEWED"))
        check(db, lex)
    }

    @Test fun onlyTheEstablishedCerealsNlGranenExceptionIsAccepted() {
        val lex = lexicon()
        val exception = rows(lex, "aliases").single { it["canonicalId"] == "cereals" }
        assertEquals("NL", exception["language"])
        assertEquals(listOf("granen"), exception["aliases"])
        assertEquals(emptyList<String>(), exception["ocrVariants"])
        check(lex = lex)
        exception["aliases"] = listOf("gran")
        assertThrows(IllegalArgumentException::class.java) { check(lex = lex) }
        val another = lexicon()
        rows(another, "aliases").add(mutableMapOf("canonicalId" to "unexpected_missing", "language" to "FR",
            "aliases" to listOf("unavailable test surface"), "ocrVariants" to emptyList<String>()))
        assertThrows(IllegalArgumentException::class.java) { check(lex = another) }
    }
}
