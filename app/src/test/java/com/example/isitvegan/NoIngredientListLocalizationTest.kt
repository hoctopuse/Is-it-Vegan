package com.example.isitvegan

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NoIngredientListLocalizationTest {
    @Test fun noIngredientListMessagesExistInEverySupportedLanguage() {
        val resourceFiles = listOf(
            File("src/main/res/values/strings.xml"),
            File("src/main/res/values-nl/strings.xml"),
            File("src/main/res/values-en/strings.xml"),
            File("src/main/res/values-de/strings.xml")
        )
        val requiredKeys = listOf(
            "no_ingredient_list",
            "no_vegan_conclusion",
            "analysis_not_evaluated",
            "traces_excluded_notice"
        )

        resourceFiles.forEach { file ->
            val content = file.readText()
            requiredKeys.forEach { key ->
                assertTrue("$key absent de ${file.path}", content.contains("name=\"$key\""))
            }
        }
    }
}
