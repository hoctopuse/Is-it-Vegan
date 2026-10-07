package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgriculturalProductsRegulationImportTest {
    private val root = projectRoot()
    private val asset = root.resolve("app/src/main/assets/ingredients.json")
    private val aliasesAsset = root.resolve("app/src/main/assets/ingredient_aliases_multilingual.json")
    private val knowledge = IngredientKnowledge.fromJson(asset.readText(), aliasesAsset.readText(), root.resolve("app/src/main/assets/origin_qualifier_rules.json").readText())
    private val service = IngredientAnalysisService(knowledge)

    @Test fun importedConceptsHaveReviewedStatusesAndFourLanguages() {
        val expected = mapOf("edible_offal" to VeganStatus.NON_VEGAN, "animal_fat" to VeganStatus.NON_VEGAN,
            "poultry_meat_preparation" to VeganStatus.NON_VEGAN, "processed_fruit_vegetable_product" to VeganStatus.UNCERTAIN,
            "spreadable_fat" to VeganStatus.UNCERTAIN)
        expected.forEach { (id, status) -> assertEquals(id, status, knowledge.ingredients.single { it.id == id }.status) }
        val cases = mapOf(
            "edible_offal" to listOf("abats comestibles des animaux de l'espèce bovine", "eetbare slachtafvallen van runderen", "edible offal of bovine animals", "Genießbare Schlachtnebenerzeugnisse von Rindern"),
            "animal_fat" to listOf("graisses de porc (y compris le saindoux)", "varkensvet (reuzel daaronder begrepen)", "pig fat (including lard)", "Schweinefett (einschließlich Schweineschmalz)"),
            "poultry_meat_preparation" to listOf("préparation à base de viande de volaille", "bereiding op basis van pluimveevlees", "poultrymeat preparation", "Geflügelfleischzubereitungen"),
            "processed_fruit_vegetable_product" to listOf("produits transformés à base de fruits et légumes", "verwerkte groenten en fruit", "processed fruit and vegetable products", "Verarbeitungserzeugnisse aus Obst und Gemüse"),
            "spreadable_fat" to listOf("matières grasses tartinables", "smeerbare vetten", "spreadable fats", "Streichfette"))
        cases.forEach { (id, terms) -> terms.forEach { term -> assertTrue("$term -> $id", service.analyze(term, InputMode.MANUAL_INGREDIENT_LIST).matched.any { it.id == id }) } }
    }

    @Test fun enrichmentAndHistoricalKnowledgeArePreserved() {
        val ids = knowledge.ingredients.map { it.id }.toSet()
        assertTrue(ids.containsAll(setOf("meat","egg","olive_oil","milk","cream","butter","cheese","whey","casein","lactose","honey","gelatin","fruit_juice","fruit_puree","fruit_nectar","fruit_jam","fruit_jelly","citrus_marmalade")))
        assertEquals(VeganStatus.NON_VEGAN, knowledge.ingredients.single { it.id == "meat" }.status)
        assertEquals(VeganStatus.VEGETARIAN, knowledge.ingredients.single { it.id == "egg" }.status)
        assertEquals(VeganStatus.VEGAN, knowledge.ingredients.single { it.id == "olive_oil" }.status)
        assertTrue(knowledge.ingredients.filter { it.id in setOf("milk","cream","butter","whey","casein","lactose") }.all { it.status == VeganStatus.VEGETARIAN })
        assertFalse(knowledge.ingredients.any { it.sources().contains(sourceId) && it.id == "fish" })
    }

    @Test fun mappingsAssetsAndImporterAreConsistent() {
        val source = root.resolve("knowledge/ingredient_aliases_multilingual.json")
        assertTrue(source.readBytes().contentEquals(aliasesAsset.readBytes()))
        val parsed = MiniJson.parse(source.readText()) as Map<*, *>
        val mappings = (parsed["mappings"] as List<*>).map { it as Map<*, *> }
        assertEquals(2089, mappings.size)
        assertEquals(mappings.size, mappings.map { listOf(it["conceptId"],it["language"],it["normalizedForm"]) }.distinct().size)
        setOf("edible_offal","animal_fat","poultry_meat_preparation","processed_fruit_vegetable_product","spreadable_fat").forEach { id ->
            assertEquals(setOf("FR","NL","EN","DE"), mappings.filter { it["conceptId"] == id }.map { it["language"] }.toSet())
        }
        val process = ProcessBuilder("python","tools/import_eu_agricultural_products_regulation.py","--animal-enrichment","--check").directory(root).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText(); assertEquals(output,0,process.waitFor()); assertTrue(output,output.contains("changes=0"))
        val meatProcess = ProcessBuilder("python","tools/import_eu_agricultural_products_regulation.py","--meat-species","--check").directory(root).redirectErrorStream(true).start()
        val meatOutput = meatProcess.inputStream.bufferedReader().readText(); assertEquals(meatOutput,0,meatProcess.waitFor()); assertTrue(meatOutput,meatOutput.contains("changes=0"))
    }

    @Test fun reviewedMeatSpeciesAliasesProduceNonVegetarianVerdicts() {
        // Shared golden cases with the Python collision checker; exercise the
        // real Kotlin normalizer, including a mark with combining class zero.
        listOf("Œuf Æ" to "oeuf ae", " É-cole " to "e cole",
            "E  120" to "e120", "x E 120" to "x e 120",
            "GEITEN\u034fVLEES" to "geitenvlees").forEach { (raw, expected) ->
            assertEquals(raw, expected, TextNormalizer.normalize(raw))
        }
        val cases = listOf(
            Triple("bovine_meat", "Viandes des animaux de l'espèce bovine", LabelLanguage.FRENCH),
            Triple("bovine_meat", "Vlees van runderen", LabelLanguage.DUTCH),
            Triple("bovine_meat", "Meat of bovine animals", LabelLanguage.ENGLISH),
            Triple("bovine_meat", "Fleisch von Rindern", LabelLanguage.GERMAN),
            Triple("pork_meat", "Viandes des animaux de l'espèce porcine domestique", LabelLanguage.FRENCH),
            Triple("pork_meat", "Vlees van varkens", LabelLanguage.DUTCH),
            Triple("pork_meat", "Meat of domestic swine", LabelLanguage.ENGLISH),
            Triple("sheep_meat", "schapenvlees", LabelLanguage.DUTCH),
            Triple("sheep_meat", "Sheepmeat", LabelLanguage.ENGLISH),
            Triple("goat_meat", "geitenvlees", LabelLanguage.DUTCH),
            Triple("goat_meat", "goatmeat", LabelLanguage.ENGLISH),
            Triple("goat_meat", "Ziegenfleisch", LabelLanguage.GERMAN),
            Triple("horse_meat", "Viandes de cheval", LabelLanguage.FRENCH),
            Triple("horse_meat", "Vlees van paarden", LabelLanguage.DUTCH),
            Triple("horse_meat", "Meat of horses", LabelLanguage.ENGLISH),
            Triple("horse_meat", "Horsemeat", LabelLanguage.ENGLISH),
            Triple("horse_meat", "Fleisch von Pferden", LabelLanguage.GERMAN)
        )
        cases.forEach { (id, surface, language) ->
            assertEquals("$language/$surface", id,
                knowledge.multilingualLexicon.resolve(surface, language, knowledge.ingredients).canonicalId)
            val match = IngredientMatcher(knowledge.ingredients).match(IngredientToken(surface, 0, 0))
            assertEquals(surface, listOf(id), match.ingredients.map { it.id })
            assertEquals(surface, MatchResolution.EXACT, match.resolution)
            assertEquals(surface, "", match.residualNormalized)
            val diagnostics = service.analyzeWithDiagnostics(surface, InputMode.MANUAL_INGREDIENT_LIST)
            val result = diagnostics.result
            assertEquals(surface, listOf(id), result.matched.map { it.id })
            assertTrue(surface, result.unknown.isEmpty())
            assertEquals(surface, AnalysisVerdict.NON_VEGETARIAN, result.verdict)
            assertEquals(surface, listOf(id), diagnostics.decision.responsibleIngredientIds)
            assertEquals(surface, listOf(id), diagnostics.verdictExplanation.knownBlockingIngredients.map { it.ingredientId })
            assertEquals(surface, listOf(id), diagnostics.tokens.flatMap { it.matchedIngredientIds })
            assertTrue(surface, diagnostics.tokens.all { it.unknown == null })
        }
        assertEquals(5, knowledge.ingredients.count { it.id in setOf(
            "bovine_meat", "pork_meat", "sheep_meat", "goat_meat", "horse_meat"
        ) && it.status == VeganStatus.NON_VEGAN })
    }


    @Test fun authorizedAnimalAliasesRemainReachableWithoutChangingTheMatcher() {
        val cases = listOf(
            Triple("whey", "lactosérum", LabelLanguage.FRENCH),
            Triple("whey", "wei", LabelLanguage.DUTCH),
            Triple("whey", "whey", LabelLanguage.ENGLISH),
            Triple("whey", "Molke", LabelLanguage.GERMAN),
            Triple("buttermilk", "babeurre", LabelLanguage.FRENCH),
            Triple("buttermilk", "karnemelk", LabelLanguage.DUTCH),
            Triple("buttermilk", "botermelk", LabelLanguage.DUTCH),
            Triple("buttermilk", "buttermilk", LabelLanguage.ENGLISH),
            Triple("buttermilk", "Buttermilch", LabelLanguage.GERMAN),
            Triple("casein", "caséines", LabelLanguage.FRENCH),
            Triple("casein", "caseïne", LabelLanguage.DUTCH),
            Triple("casein", "caseins", LabelLanguage.ENGLISH),
            Triple("casein", "Kaseine", LabelLanguage.GERMAN),
            Triple("milk", "lait cru", LabelLanguage.FRENCH),
            Triple("milk", "rauwe melk", LabelLanguage.DUTCH),
            Triple("milk", "raw milk", LabelLanguage.ENGLISH),
            Triple("milk", "Rohmilch", LabelLanguage.GERMAN),
            Triple("milk", "lait entier", LabelLanguage.FRENCH),
            Triple("milk", "volle melk", LabelLanguage.DUTCH),
            Triple("milk", "whole milk", LabelLanguage.ENGLISH),
            Triple("milk", "Vollmilch", LabelLanguage.GERMAN),
            Triple("milk", "lait demi-écrémé", LabelLanguage.FRENCH),
            Triple("milk", "halfvolle melk", LabelLanguage.DUTCH),
            Triple("milk", "semi-skimmed milk", LabelLanguage.ENGLISH),
            Triple("milk", "teilentrahmte Milch", LabelLanguage.GERMAN),
            Triple("milk", "fettarme Milch", LabelLanguage.GERMAN),
            Triple("milk", "lait écrémé", LabelLanguage.FRENCH),
            Triple("milk", "magere melk", LabelLanguage.DUTCH),
            Triple("milk", "skimmed-milk", LabelLanguage.ENGLISH),
            Triple("milk", "skimmed milk", LabelLanguage.ENGLISH),
            Triple("milk", "entrahmte Milch", LabelLanguage.GERMAN),
            Triple("milk", "Magermilch", LabelLanguage.GERMAN),
            Triple("honey", "Miel naturel", LabelLanguage.FRENCH),
            Triple("honey", "Natuurhoning", LabelLanguage.DUTCH),
            Triple("honey", "Natural honey", LabelLanguage.ENGLISH),
            Triple("honey", "Natürlicher Honig", LabelLanguage.GERMAN),
            Triple("royal_jelly", "Gelée royale", LabelLanguage.FRENCH),
            Triple("royal_jelly", "koninginnengelei", LabelLanguage.DUTCH),
            Triple("royal_jelly", "Royal jelly", LabelLanguage.ENGLISH),
            Triple("royal_jelly", "Gelée Royale", LabelLanguage.GERMAN),
            Triple("propolis", "propolis", LabelLanguage.FRENCH),
            Triple("propolis", "propolis", LabelLanguage.DUTCH),
            Triple("propolis", "propolis", LabelLanguage.ENGLISH),
            Triple("propolis", "Kittharz", LabelLanguage.GERMAN),
            Triple("egg", "jaunes d'œufs", LabelLanguage.FRENCH),
            Triple("egg", "eigeel", LabelLanguage.DUTCH),
            Triple("egg", "egg yolks", LabelLanguage.ENGLISH),
            Triple("egg", "Eigelb", LabelLanguage.GERMAN),
            Triple("edible_offal", "Abats comestibles", LabelLanguage.FRENCH),
            Triple("edible_offal", "Eetbare slachtafvallen", LabelLanguage.DUTCH),
            Triple("edible_offal", "Edible offal", LabelLanguage.ENGLISH),
            Triple("edible_offal", "Genießbare Schlachtnebenerzeugnisse", LabelLanguage.GERMAN),
            Triple("edible_offal", "Foies de volailles", LabelLanguage.FRENCH),
            Triple("edible_offal", "Levers van pluimvee", LabelLanguage.DUTCH),
            Triple("edible_offal", "Poultry livers", LabelLanguage.ENGLISH),
            Triple("edible_offal", "Geflügelleber", LabelLanguage.GERMAN),
            Triple("edible_offal", "Geflügellebern", LabelLanguage.GERMAN),
            Triple("animal_fat", "graisse de porc", LabelLanguage.FRENCH),
            Triple("animal_fat", "Varkensvet", LabelLanguage.DUTCH),
            Triple("animal_fat", "Pig fat", LabelLanguage.ENGLISH),
            Triple("animal_fat", "Schweinefett", LabelLanguage.GERMAN),
            Triple("animal_fat", "saindoux", LabelLanguage.FRENCH),
            Triple("animal_fat", "reuzel", LabelLanguage.DUTCH),
            Triple("animal_fat", "lard", LabelLanguage.ENGLISH),
            Triple("animal_fat", "Schweineschmalz", LabelLanguage.GERMAN),
            Triple("animal_fat", "Graisses de volaille", LabelLanguage.FRENCH),
            Triple("animal_fat", "Vet van gevogelte", LabelLanguage.DUTCH),
            Triple("animal_fat", "Poultry fat", LabelLanguage.ENGLISH),
            Triple("animal_fat", "Geflügelfett", LabelLanguage.GERMAN),
            Triple("animal_fat", "Graisses des animaux de l'espèce bovine", LabelLanguage.FRENCH),
            Triple("animal_fat", "Rundervet", LabelLanguage.DUTCH),
            Triple("animal_fat", "Fats of bovine animals", LabelLanguage.ENGLISH),
            Triple("animal_fat", "Fett von Rindern", LabelLanguage.GERMAN),
            Triple("animal_fat", "Graisse des animaux des espèces ovine et caprine", LabelLanguage.FRENCH),
            Triple("animal_fat", "Schapen- of geitenvet", LabelLanguage.DUTCH),
            Triple("animal_fat", "Fats of sheep or goats", LabelLanguage.ENGLISH),
            Triple("animal_fat", "Fett von Schafen oder Ziegen", LabelLanguage.GERMAN)
        )
        cases.forEach { (id, surface, language) ->
            val resolution = knowledge.multilingualLexicon.resolve(surface, language, knowledge.ingredients)
            assertEquals("$language/$surface", id, resolution.canonicalId)
            assertTrue("$language/$surface", resolution.canonicalAvailable == true)
            assertTrue("$surface -> $id", service.analyze(surface, InputMode.MANUAL_INGREDIENT_LIST)
                .matched.any { it.id == id })
        }
    }

    @Test fun applicationConventionIsExplicitAndNeverAPositiveVeganVerdict() {
        listOf("buttermilk", "royal_jelly", "propolis").forEach { id ->
            val ingredient = knowledge.ingredients.single { it.id == id }
            assertEquals(id, VeganStatus.VEGETARIAN, ingredient.status)
            ingredient.aliases.forEach { surface ->
                val result = VeganAnalyzer.analyzeWithDiagnostics(
                    surface, knowledge.ingredients, InputMode.MANUAL_INGREDIENT_LIST
                ).result
                assertEquals(surface, VeganAssessment.NOT_VEGAN, result.veganAssessment)
            }
        }
        listOf("royal_jelly", "propolis").forEach { id ->
            val ingredient = knowledge.ingredients.single { it.id == id }
            assertTrue(ingredient.reason.contains("Convention de l’application"))
            assertTrue(ingredient.reason.contains("règle universelle de certification"))
            assertTrue(ingredient.sources().contains("isitvegan-animal-products-convention-v0-7"))
        }
        assertEquals(VeganStatus.UNCERTAIN, knowledge.ingredients.single { it.id == "beeswax" }.status)
        assertEquals(VeganStatus.NON_VEGAN, knowledge.ingredients.single { it.id == "e901" }.status)
        assertFalse(knowledge.ingredients.any { it.id == "pollen" })
    }

    @Test fun enrichmentKeepsTracesAndProtectedPlantContextsSeparate() {
        listOf(
            "Ingrédients : eau." to "Peut contenir : gelée royale.",
            "Ingrediënten: water." to "Kan sporen van propolis bevatten.",
            "Ingredients: water." to "May contain royal jelly.",
            "Zutaten: Wasser." to "Kann Spuren von Kittharz enthalten."
        ).forEach { (prefix, trace) ->
            val text = "$prefix $trace"
            val baseline = VeganAnalyzer.analyzeWithDiagnostics(
                prefix, knowledge.ingredients, InputMode.FULL_LABEL
            ).result
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
                text, knowledge.ingredients, InputMode.FULL_LABEL
            )
            assertEquals(text, baseline.veganAssessment, diagnostics.result.veganAssessment)
            assertEquals(text, baseline.veganBlockers, diagnostics.result.veganBlockers)
            assertEquals(text, baseline.unknown, diagnostics.result.unknown)
            assertTrue(text, diagnostics.crossContactWarnings.isNotEmpty())
        }
        listOf("lait végétal", "plant milk", "plantaardige melk", "pflanzliche Milch",
            "beurre de cacao", "crème de coco", "honey flavour", "arôme de miel").forEach { text ->
            assertFalse(text, service.analyze(text, InputMode.MANUAL_INGREDIENT_LIST)
                .matched.any { it.id in setOf("milk", "cream", "butter", "honey") })
        }
        val actual = VeganAnalyzer.analyzeWithDiagnostics(
            "Ingrédients : eau, babeurre.", knowledge.ingredients, InputMode.FULL_LABEL
        )
        assertEquals(VeganAssessment.NOT_VEGAN, actual.result.veganAssessment)
    }

    private fun Ingredient.sources() = source.orEmpty()
    private fun projectRoot(): File = generateSequence(File(requireNotNull(System.getProperty("user.dir"))).canonicalFile) { it.parentFile }.first { File(it,"settings.gradle.kts").isFile }
    private companion object { const val sourceId = "eu-agricultural-products-regulation-1308-2013-20260818" }
}
