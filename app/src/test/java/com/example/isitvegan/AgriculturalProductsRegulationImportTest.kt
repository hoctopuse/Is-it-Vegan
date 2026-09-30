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
        assertEquals(1996, mappings.size)
        assertEquals(mappings.size, mappings.map { listOf(it["conceptId"],it["language"],it["normalizedForm"]) }.distinct().size)
        setOf("edible_offal","animal_fat","poultry_meat_preparation","processed_fruit_vegetable_product","spreadable_fat").forEach { id ->
            assertEquals(setOf("FR","NL","EN","DE"), mappings.filter { it["conceptId"] == id }.map { it["language"] }.toSet())
        }
        val process = ProcessBuilder("python","tools/import_eu_agricultural_products_regulation.py","--check").directory(root).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText(); assertEquals(output,0,process.waitFor()); assertTrue(output,output.contains("changes=0"))
    }

    private fun Ingredient.sources() = source.orEmpty()
    private fun projectRoot(): File = generateSequence(File(requireNotNull(System.getProperty("user.dir"))).canonicalFile) { it.parentFile }.first { File(it,"settings.gradle.kts").isFile }
    private companion object { const val sourceId = "eu-agricultural-products-regulation-1308-2013-20260818" }
}
