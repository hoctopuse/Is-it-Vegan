package com.example.isitvegan

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class FruitJuiceDirectiveImportTest {
    private val knowledge = IngredientKnowledge.fromJson(
        file("app/src/main/assets/ingredients.json").readText(), file("app/src/main/assets/ingredient_aliases_multilingual.json").readText(), file("app/src/main/assets/origin_qualifier_rules.json").readText())
    @Test fun directiveConceptsRemainDistinctAndMapped() {
        val service = IngredientAnalysisService(knowledge)
        assertEquals(VeganStatus.VEGAN, knowledge.ingredients.single { it.id == "fruit_juice" }.status)
        assertEquals(VeganStatus.VEGAN, knowledge.ingredients.single { it.id == "fruit_puree" }.status)
        assertEquals(VeganStatus.UNCERTAIN, knowledge.ingredients.single { it.id == "fruit_nectar" }.status)
        listOf("jus de fruits","vruchtensap","fruit juice","Fruchtsaft").forEach { assertTrue(service.analyze(it,InputMode.MANUAL_INGREDIENT_LIST).matched.any { x->x.id=="fruit_juice" }) }
        assertFalse(service.analyze("arôme de pomme",InputMode.MANUAL_INGREDIENT_LIST).matched.any { it.id=="apple" })
        assertNotEquals("fruit_juice","apple"); assertNotEquals("fruit_puree","fruit_juice"); assertNotEquals("fruit_nectar","fruit_juice")
    }
    private fun file(p:String)=File(p).takeIf { it.isFile } ?: File("..",p)
}
