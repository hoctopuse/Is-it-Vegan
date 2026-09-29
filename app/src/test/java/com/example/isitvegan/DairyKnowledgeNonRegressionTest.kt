package com.example.isitvegan

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class DairyKnowledgeNonRegressionTest {
 @Test fun dairyAndAnimalKnowledgeRemainsReachable() {
  val k=IngredientKnowledge.fromJson(projectFile("app/src/main/assets/ingredients.json").readText(),projectFile("app/src/main/assets/ingredient_aliases_multilingual.json").readText(),projectFile("app/src/main/assets/origin_qualifier_rules.json").readText())
  val ids=k.ingredients.map { it.id }.toSet()
  assertTrue(setOf("whey","casein","lactose","milk","cream","butter","cheese","egg","honey","gelatin").all(ids::contains))
  val m=IngredientMatcher(k.ingredients)
  listOf("lactosérum","petit lait","whey","caséine","caséinates","lait concentré","crème de lait","beurre","fromage","œuf","albumine d’œuf","miel","gélatine").forEach { assertTrue(it,m.match(IngredientToken(it,0,0)).ingredients.isNotEmpty()) }
  assertEquals(VeganStatus.VEGETARIAN,k.ingredients.single { it.id=="whey" }.status)
  assertEquals(VeganStatus.NON_VEGAN,k.ingredients.single { it.id=="gelatin" }.status)
 }
 private fun projectFile(path:String):File {
  val start=System.getProperty("user.dir") ?: error("System property user.dir is unavailable")
  val root=generateSequence(File(start).canonicalFile){it.parentFile}
   .firstOrNull { File(it,"settings.gradle.kts").isFile }
   ?: error("Project root containing settings.gradle.kts not found from ${System.getProperty("user.dir")}")
  return File(root,path).also { require(it.isFile){"Missing project file: ${it.absolutePath}"} }
 }
}
