package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Runtime evidence for the importer guard; production resolution is not changed. */
class FoodHygieneLexiconCompatibilityTest {
    private val root = KnowledgeValidationTestSupport.root()
    private val original = root.resolve("knowledge/ingredient_aliases_multilingual.json").readText()
    private val knowledge = IngredientKnowledge.fromJson(
        root.resolve("app/src/main/assets/ingredients.json").readText(), original,
        root.resolve("app/src/main/assets/origin_qualifier_rules.json").readText())
    private val forms = listOf("viandes séparées mécaniquement" to LabelLanguage.FRENCH,
        "separatorvlees" to LabelLanguage.DUTCH, "mechanically separated meat" to LabelLanguage.ENGLISH,
        "Separatorenfleisch" to LabelLanguage.GERMAN)

    @Suppress("UNCHECKED_CAST")
    private fun lexicon() = MiniJson.parse(original) as MutableMap<String, Any?>
    @Suppress("UNCHECKED_CAST")
    private fun corrections(lex: Map<String, Any?>) = lex["ocrCorrections"] as MutableList<MutableMap<String, Any?>>
    private fun addCorrection(lex: Map<String, Any?>, language: String, from: String, to: String) {
        corrections(lex).add(mutableMapOf("language" to language, "from" to from, "to" to to))
    }
    private fun assertBaseForms(lexicon: MultilingualIngredientLexicon) {
        forms.forEach { (surface, language) ->
            assertEquals(surface, "mechanically_separated_meat", lexicon.resolve(surface, language, knowledge.ingredients).canonicalId)
        }
    }

    // The real dispatcher sees a substituted lexical JSON string, with actual
    // editorial ingredients/sources. Provenance already verified by the Python
    // acquisition tests is read from the unchanged original mappings here.
    private fun pythonCheck(raw: String, mode: String = "--check"): Pair<Int, String> {
        val code = """
            import importlib.util,json,sys
            from pathlib import Path
            from unittest.mock import patch
            s=importlib.util.spec_from_file_location('eu853','tools/import_eu_food_hygiene_regulation.py')
            m=importlib.util.module_from_spec(s);s.loader.exec_module(m)
            original=json.loads(m.L.read_text(encoding='utf8'))
            evidence={x['language']:x['sourceEvidence'] for x in original['mappings'] if x['conceptId']==m.CONCEPT}
            raw=sys.stdin.read();read=Path.read_text
            def replacement(p,*a,**kw):return raw if p==m.L else read(p,*a,**kw)
            mode=sys.argv[1];sys.argv=['importer',mode]
            with patch.object(Path,'read_text',replacement),patch.object(m,'source_evidence',return_value=evidence),patch.object(Path,'write_text',side_effect=AssertionError('forbidden write')),patch.object(Path,'write_bytes',side_effect=AssertionError('forbidden write')):
                m.main()
        """.trimIndent()
        val process = ProcessBuilder("python", "-B", "-X", "utf8", "-c", code, mode)
            .directory(root).redirectErrorStream(true).start()
        process.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(raw) }
        val output = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
        return process.waitFor() to output
    }

    @Test fun currentBatchIsAcceptedByImporterAndActualRuntimeInBothReadOnlyModes() {
        val actual = MultilingualIngredientLexicon.load(original)
        assertTrue(actual.validateAgainst(knowledge.ingredients).isValid)
        assertBaseForms(actual)
        listOf("--check", "--dry-run").forEach { mode ->
            val (code, output) = pythonCheck(original, mode)
            assertEquals(output, 0, code)
            assertTrue(output, output.contains("no modification necessary"))
        }
    }

    @Test fun unsupportedSchemaFailsBothImporterAndActualLoader() {
        val raw = KnowledgeValidationTestSupport.json(lexicon().apply { put("schemaVersion", 2) })
        assertThrows(IllegalArgumentException::class.java) { MultilingualIngredientLexicon.load(raw) }
        listOf("--check", "--dry-run").forEach { mode ->
            val (code, output) = pythonCheck(raw, mode)
            assertEquals(output, 1, code)
            assertTrue(output, output.contains("schemaVersion"))
        }
    }

    @Test fun redirectAcceptedByLoaderReallyChangesOwnerAndIsRefusedByImporter() {
        val lex = lexicon()
        addCorrection(lex, "DUTCH", "separatorvlees", "rundvlees")
        val raw = KnowledgeValidationTestSupport.json(lex)
        val actual = MultilingualIngredientLexicon.load(raw)
        assertEquals("meat", actual.resolve("separatorvlees", LabelLanguage.DUTCH, knowledge.ingredients).canonicalId)
        listOf("--check", "--dry-run").forEach { mode ->
            val (code, output) = pythonCheck(raw, mode)
            assertEquals(output, 1, code)
            assertTrue(output, output.contains("OCR correction redirects approved surface"))
        }
    }

    @Test fun harmlessCorrectionsRemainAcceptedWithRuntimeOwnerPreserved() {
        listOf("separatovlees" to "separatorvlees", "SEPARAT\u034fORVLEES" to "Separatorvlees").forEach { (from, to) ->
            val lex = lexicon()
            addCorrection(lex, "dutch", from, to)
            val raw = KnowledgeValidationTestSupport.json(lex)
            val actual = MultilingualIngredientLexicon.load(raw)
            assertBaseForms(actual)
            assertEquals("mechanically_separated_meat", actual.resolve(from, LabelLanguage.DUTCH, knowledge.ingredients).canonicalId)
            listOf("--check", "--dry-run").forEach { mode ->
                val (code, output) = pythonCheck(raw, mode)
                assertEquals(output, 0, code)
            }
        }
    }

    @Test fun invalidCorrectionLanguageBlankTargetAndDuplicateKeysFailActualLoader() {
        listOf("language", "blank", "duplicate").forEach { case ->
            val lex = lexicon()
            when (case) {
                "language" -> addCorrection(lex, "XX", "new typo", "separatorvlees")
                "blank" -> addCorrection(lex, "NL", "new typo", "")
                else -> corrections(lex).add(corrections(lex).first().toMutableMap())
            }
            val raw = KnowledgeValidationTestSupport.json(lex)
            assertThrows(case, IllegalArgumentException::class.java) { MultilingualIngredientLexicon.load(raw) }
            val (code, output) = pythonCheck(raw)
            assertEquals(output, 1, code)
        }
        val duplicateJson = original.trimEnd().dropLast(1) + ",\"schemaVersion\":1}"
        assertThrows(IllegalArgumentException::class.java) { MultilingualIngredientLexicon.load(duplicateJson) }
        assertEquals(1, pythonCheck(duplicateJson).first)
    }

    @Test fun pythonNormalizerAgreesWithRealKotlinForEveryCurrentLexicalSurface() {
        val lex = lexicon()
        @Suppress("UNCHECKED_CAST")
        val rows = lex["aliases"] as List<Map<String, Any?>>
        @Suppress("UNCHECKED_CAST")
        val mappings = lex["mappings"] as List<Map<String, Any?>>
        val surfaces = rows.flatMap { row ->
            @Suppress("UNCHECKED_CAST")
            ((row["aliases"] as List<String>) + (row["ocrVariants"] as List<String>))
        } + mappings.map { it["surfaceForm"] as String } + corrections(lex).flatMap { listOf(it["from"] as String, it["to"] as String) } +
            listOf("Œuf Æ", " É-cole ", "E  120", "x E 120", "SEPARAT\u034fORVLEES")
        val code = """
            import importlib.util,json,sys
            s=importlib.util.spec_from_file_location('eu853','tools/import_eu_food_hygiene_regulation.py')
            m=importlib.util.module_from_spec(s);s.loader.exec_module(m)
            print(json.dumps([m.normalized(x) for x in json.load(sys.stdin)],ensure_ascii=False))
        """.trimIndent()
        val process = ProcessBuilder("python", "-B", "-X", "utf8", "-c", code).directory(root).redirectErrorStream(true).start()
        process.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(KnowledgeValidationTestSupport.json(surfaces)) }
        val output = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
        assertEquals(output, 0, process.waitFor())
        assertEquals(surfaces.map(TextNormalizer::normalize), MiniJson.parse(output))
    }
}
