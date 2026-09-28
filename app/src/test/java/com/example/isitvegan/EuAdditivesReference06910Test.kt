package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EuAdditivesReference06910Test {
    private val database = runtimeDatabase()
    private val reference = referenceRows()
    private val referenceImportableNumbers = reference
        .filter { it.verificationStatus != "AMBIGUOUS_GROUP_OR_RANGE" }
        .map { it.eNumber }
        .toSet()
    private val byNumber = database.filter { !it.eNumber.isNullOrBlank() }.associateBy { it.eNumber!! }

    @Test fun officialReferenceCoverageIsCompleteForEveryImportableRow() {
        assertEquals(340, reference.size)
        assertEquals(340, reference.map { it.eNumber }.distinct().size)
        assertEquals(337, reference.count { it.verificationStatus == "COMPLETE_4_LANGUAGES" })
        assertEquals(1, reference.count { it.verificationStatus == "PARTIAL_OFFICIAL" })
        assertEquals(2, reference.count { it.verificationStatus == "AMBIGUOUS_GROUP_OR_RANGE" })
        assertEquals(338, referenceImportableNumbers.size)
        assertEquals(referenceImportableNumbers, referenceImportableNumbers.mapNotNull { byNumber[it]?.eNumber }.toSet())
        assertEquals(452, database.size)
        assertEquals(342, byNumber.size)

    }

    @Test fun editorialSourceGeneratesTheProductionAssetForAllImportableRows() {
        val editorial = editorialDatabase()
        val editorialByNumber = editorial.filter { !it.eNumber.isNullOrBlank() }.associateBy { it.eNumber!! }
        assertEquals(452, editorial.size)
        assertEquals(referenceImportableNumbers, referenceImportableNumbers.mapNotNull { editorialByNumber[it]?.eNumber }.toSet())
        assertNull(editorialByNumber["E345"])
        assertNull(editorialByNumber["E345(i)"])

        database.forEach { runtime ->
            val source = editorial.single { it.id == runtime.id }
            assertEquals(runtime.status, source.status)
            assertEquals(runtime.reason, source.reason)
            assertEquals(runtime.source, source.sources.joinToString(", "))
        }
        referenceImportableNumbers.forEach { number ->
            assertEquals(number, byNumber.getValue(number).eNumber)
            assertEquals(number, editorialByNumber.getValue(number).eNumber)
        }
    }

    @Test fun officialNamesAreInTheProductionMultilingualLexiconByLanguage() {
        val aliases = multilingualAliases()
        reference.filter { it.eNumber in referenceImportableNumbers }.forEach { row ->
            val id = row.eNumber.lowercase()
            listOf("FR", "NL", "EN", "DE").zip(row.officialNames).forEach { (language, name) ->
                val terms = aliases.filter { it.canonicalId == id && it.language == language }
                    .flatMap { it.aliases + it.ocrVariants }
                if (name.isBlank()) assertTrue("$id/$language", terms.isEmpty())
                else assertTrue("$id/$language: $name", name in terms)
            }
        }
        assertEquals(setOf("FR", "NL", "EN"), aliases.filter { it.canonicalId == "e322a" }.map { it.language }.toSet())
        assertTrue(aliases.none { it.canonicalId == "e345" || it.canonicalId == "e345(i)" })
    }

    @Test fun everyNewlyImportedAdditiveIsUncertain() {
        val imported = database.filter { it.eNumber in referenceImportableNumbers && it.eNumber !in preImportStatuses }
        assertEquals(250, imported.size)
        assertTrue(imported.all { it.status == VeganStatus.UNCERTAIN })
        assertTrue(imported.all { it.source == "eu-additives" })
        assertFalse(imported.any { it.status.name == "INCONCLUSIVE" })
    }

    @Test fun allPreImportBusinessClassificationsRemainUnchanged() {
        assertEquals(92, preImportStatuses.size)
        preImportStatuses.forEach { (eNumber, expectedStatus) ->
            assertEquals(eNumber, expectedStatus, byNumber.getValue(eNumber).status)
        }
    }

    @Test fun partialAndAmbiguousRowsRemainExplicitAndDistinct() {
        val e322aReference = reference.single { it.eNumber == "E322a" }
        assertEquals("PARTIAL_OFFICIAL", e322aReference.verificationStatus)
        assertEquals(listOf("Lécithine d’avoine", "Haverlecithine", "Oat lecithin", ""), e322aReference.officialNames)
        val e322a = byNumber.getValue("E322a")
        assertEquals(VeganStatus.UNCERTAIN, e322a.status)
        assertTrue(e322aReference.officialNames.take(3).all { it in e322a.aliases })
        assertEquals(3, e322a.aliases.size)

        val ambiguous = reference.filter { it.verificationStatus == "AMBIGUOUS_GROUP_OR_RANGE" }
        assertEquals(listOf("E345", "E345(i)"), ambiguous.map { it.eNumber })
        assertNotEquals(ambiguous[0].eNumber, ambiguous[1].eNumber)
        assertNull(byNumber["E345"])
        assertNull(byNumber["E345(i)"])
        assertTrue(match("E345").isEmpty())
        assertTrue(match("E345(i)").isEmpty())
    }

    @Test fun suffixedRegulatoryNumbersArePresentAndResolveExactly() {
        val expected = mapOf(
            "E160b(i)" to "e160b(i)",
            "E322a" to "e322a",
            "E960a" to "e960a",
            "E960b" to "e960b"
        )
        assertEquals(expected.keys, expected.keys.map { byNumber.getValue(it).eNumber }.toSet())
        assertEquals(expected.values.toSet().size, expected.size)
        expected.forEach { (form, id) ->
            assertEquals(form, listOf(id), match(form))
            assertEquals("spaced $form", listOf(id), match(form.replaceFirst("E", "E ")))
        }
        assertEquals(listOf("e160b"), match("E160b"))
        assertEquals(listOf("e960"), match("E960"))
    }

    @Test fun e330SupportsSafeFormsAndRejectsTheBareNumber() {
        assertEquals(listOf("e330"), match("E330"))
        assertEquals(listOf("e330"), match("E 330"))
        assertTrue(match("330").isEmpty())
    }

    @Test fun importedFrenchDutchEnglishAndGermanAliasesResolve() {
        val cases = mapOf(
            "E102" to listOf("Tartrazine", "Tartrazine", "Tartrazine", "Tartrazin"),
            "E160b(i)" to listOf("Bixine de rocou", "Annatto bixine", "Annatto bixin", "Annatto Bixin"),
            "E960b" to listOf(
                "Glycosides de stéviol produits par fermentation",
                "Steviolglycosiden uit fermentatie",
                "Steviol glycosides from fermentation",
                "Steviolglycoside aus Fermentation"
            )
        )
        cases.forEach { (eNumber, aliases) ->
            aliases.forEach { alias -> assertEquals("$eNumber / $alias", listOf(eNumber.lowercase()), match(alias)) }
        }
        listOf("Lécithine d’avoine", "Haverlecithine", "Oat lecithin").forEach { alias ->
            assertEquals(alias, listOf("e322a"), match(alias))
        }
    }

    @Test fun historicalE572AndOfficialE470bStayDistinctDespiteNameCollision() {
        assertEquals(VeganStatus.UNCERTAIN, byNumber.getValue("E572").status)
        assertEquals(VeganStatus.UNCERTAIN, byNumber.getValue("E470b").status)
        assertEquals(listOf("e572"), match("E572"))
        assertEquals(listOf("e470b"), match("E470b"))
        val officialNames = reference.single { it.eNumber == "E470b" }.officialNames
        assertTrue(officialNames.all { it in byNumber.getValue("E470b").aliases })
        assertTrue(officialNames.all { name ->
            byNumber.getValue("E572").aliases.any { TextNormalizer.normalize(it) == TextNormalizer.normalize(name) }
        })

        officialNames.forEach { name ->
            val labelOnly = VeganAnalyzer.analyze(name, database)
            assertEquals("label only: $name", AnalysisVerdict.UNCERTAIN, labelOnly.verdict)
            assertEquals("label only: $name", listOf("e572"), labelOnly.matched.map { it.id })

            val withE470b = VeganAnalyzer.analyze("$name E470b", database)
            assertEquals("E470b: $name", AnalysisVerdict.UNCERTAIN, withE470b.verdict)
            assertEquals("E470b: $name", setOf("e572", "e470b"), withE470b.matched.map { it.id }.toSet())

            val withE572 = VeganAnalyzer.analyze("$name E572", database)
            assertEquals("E572: $name", AnalysisVerdict.UNCERTAIN, withE572.verdict)
            assertEquals("E572: $name", listOf("e572"), withE572.matched.map { it.id })
            assertNotEquals("numbered forms remain distinct: $name", withE470b.matched.map { it.id }.toSet(), withE572.matched.map { it.id }.toSet())
        }
    }

    private fun match(text: String): List<String> =
        IngredientMatcher(database).match(IngredientToken(text, 0, 0)).ingredients.map { it.id }

    private fun runtimeDatabase(): List<Ingredient> =
        (MiniJson.parse(projectFile("app/src/main/assets/ingredients.json").readText()) as List<*>).map { value ->
            val item = value as Map<*, *>
            Ingredient(
                id = item["id"] as String,
                name = item["name"] as String,
                aliases = (item["aliases"] as List<*>).filterIsInstance<String>(),
                eNumber = (item["eNumber"] as? String)?.takeIf(String::isNotBlank),
                status = VeganStatus.valueOf(item["status"] as String),
                reason = item["reason"] as String,
                source = item["source"] as? String
            )
        }

    private fun editorialDatabase(): List<EditorialIngredient> =
        (MiniJson.parse(projectFile("knowledge/ingredients.json").readText()) as List<*>).map { value ->
            val item = value as Map<*, *>
            EditorialIngredient(
                id = item["id"] as String,
                eNumber = (item["eNumber"] as? String)?.takeIf(String::isNotBlank),
                status = VeganStatus.valueOf(item["status"] as String),
                reason = item["reason"] as String,
                sources = (item["sources"] as List<*>).filterIsInstance<String>()
            )
        }

    private fun multilingualAliases(): List<MultilingualAlias> {
        val root = MiniJson.parse(projectFile("app/src/main/assets/ingredient_aliases_multilingual.json").readText()) as Map<*, *>
        return (root["aliases"] as List<*>).map { value ->
            val item = value as Map<*, *>
            MultilingualAlias(
                canonicalId = item["canonicalId"] as String,
                language = item["language"] as String,
                aliases = (item["aliases"] as List<*>).filterIsInstance<String>(),
                ocrVariants = (item["ocrVariants"] as List<*>).filterIsInstance<String>()
            )
        }
    }

    private fun referenceRows(): List<ReferenceRow> {
        val lines = projectFile("reference-input/eu-food-labelling/03-food-additives/EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv").readLines()
        val header = parseCsvLine(lines.first()).withIndex().associate { it.value to it.index }
        return lines.drop(1).filter(String::isNotBlank).map { line ->
            val fields = parseCsvLine(line)
            fun field(name: String) = fields[header.getValue(name)]
            ReferenceRow(
                eNumber = field("e_number"),
                officialNames = listOf(
                    field("official_name_fr"), field("official_name_nl"),
                    field("official_name_en"), field("official_name_de")
                ),
                verificationStatus = field("verification_status")
            )
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var quoted = false
        var index = 0
        while (index < line.length) {
            when {
                line[index] == '"' && quoted && index + 1 < line.length && line[index + 1] == '"' -> {
                    current.append('"')
                    index++
                }
                line[index] == '"' -> quoted = !quoted
                line[index] == ',' && !quoted -> {
                    fields += current.toString()
                    current.clear()
                }
                else -> current.append(line[index])
            }
            index++
        }
        fields += current.toString()
        return fields
    }

    private fun projectFile(relativePath: String): File {
        val direct = File(relativePath)
        if (direct.isFile) return direct
        return File("..", relativePath).also { require(it.isFile) { "Missing project file: $relativePath" } }
    }

    private data class ReferenceRow(
        val eNumber: String,
        val officialNames: List<String>,
        val verificationStatus: String
    )

    private data class EditorialIngredient(
        val id: String,
        val eNumber: String?,
        val status: VeganStatus,
        val reason: String,
        val sources: List<String>
    )

    private data class MultilingualAlias(
        val canonicalId: String,
        val language: String,
        val aliases: List<String>,
        val ocrVariants: List<String>
    )

    private companion object {
        val preImportStatuses = buildMap {
            listOf("E120", "E904").forEach { put(it, VeganStatus.NON_VEGAN) }
            listOf(
                "E471", "E322", "E422", "E570", "E627", "E631", "E635", "E100", "E101", "E140",
                "E150a", "E153", "E160a", "E160b", "E162", "E200", "E203", "E210", "E211", "E223",
                "E224", "E234", "E235", "E249", "E250", "E251", "E252", "E260", "E280", "E281",
                "E282", "E296", "E304", "E307", "E310", "E320", "E321", "E331", "E333", "E400",
                "E401", "E405", "E407", "E407a", "E414", "E416", "E420", "E432", "E433", "E450",
                "E452", "E460", "E466", "E476", "E481", "E501", "E504", "E572", "E575", "E620",
                "E622", "E901", "E903", "E920", "E938", "E941", "E948"
            ).forEach { put(it, VeganStatus.UNCERTAIN) }
            listOf(
                "E300", "E330", "E170", "E406", "E418", "E410", "E412", "E415", "E440", "E500",
                "E503", "E524", "E551", "E621", "E332", "E950", "E960", "E306", "E340", "E202",
                "E220", "E262", "E270"
            ).forEach { put(it, VeganStatus.VEGAN) }
        }
    }
}
