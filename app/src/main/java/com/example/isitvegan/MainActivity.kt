package com.example.isitvegan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.isitvegan.ui.theme.IsItVeganTheme

private const val WAITING_RESULT = "⚪ En attente d'analyse"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        VeganAnalyzer.loadDatabase(this)
        setContent {
            IsItVeganTheme {
                IsItVeganScreen()
            }
        }
    }
}

@Composable
fun IsItVeganScreen() {

    var ingredients by remember {
        mutableStateOf("")
    }

    var result by remember { mutableStateOf(WAITING_RESULT) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                text = "🌱 Is It Vegan?",
                fontSize = 30.sp
            )

            Text(
                text = "Entre la liste des ingrédients du produit :"
            )

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                label = {
                    Text("Ingrédients")
                },
                placeholder = {
                    Text("Sucre, farine de blé, huile de colza, E471...")
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()

                        val analysis = VeganAnalyzer.analyze(ingredients)
                        val matches = analysis.matched
                        val unknownMessage = if (analysis.unknown.isEmpty()) "" else
                            "\n\nNon reconnus : " + analysis.unknown.joinToString(", ")
                        result = when {
                            ingredients.isBlank() -> "ℹ️ Entre d'abord une liste d'ingrédients."
                            analysis.verdict == AnalysisVerdict.NON_VEGETARIAN -> {
                                val found = matches.filter { it.status == VeganStatus.NON_VEGAN }
                                "❌ NON VÉGÉTARIEN\n\nIngrédient détecté :\n" + found.joinToString("\n\n") {
                                    "${it.eNumber ?: it.name} — ${it.name}\n${it.reason}"
                                } + if (analysis.stoppedAtNonVegetarian) {
                                    "\n\nAnalyse arrêtée : cet ingrédient suffit pour conclure."
                                } else ""
                            }
                            analysis.verdict == AnalysisVerdict.UNCERTAIN -> {
                                val uncertain = analysis.uncertainIngredients
                                val uncertainMessage = "⚠️ INCERTAIN\n\nÀ vérifier :\n" +
                                    uncertain.joinToString("\n\n") {
                                        "${it.eNumber ?: it.name} — ${it.name}\n${it.reason}"
                                    }
                                val remainderMessage = when (analysis.verdictWithoutUncertain) {
                                    AnalysisVerdict.VEGAN ->
                                        "\n\nEn excluant ${if (uncertain.size == 1) "cet ingrédient" else "ces ingrédients"} :\n🌱 VEGAN"
                                    AnalysisVerdict.VEGETARIAN -> {
                                        val vegetarian = analysis.vegetarianIngredients.joinToString("\n") {
                                            "• ${it.eNumber ?: it.name} — ${it.name}"
                                        }
                                        "\n\nEn excluant ${if (uncertain.size == 1) "cet ingrédient" else "ces ingrédients"} :" +
                                            "\n🥕 VÉGÉTARIEN\n\nIngrédient${if (analysis.vegetarianIngredients.size > 1) "s" else ""} " +
                                            "végétarien${if (analysis.vegetarianIngredients.size > 1) "s" else ""} détecté${if (analysis.vegetarianIngredients.size > 1) "s" else ""} :\n" +
                                            vegetarian
                                    }
                                    else ->
                                        "\n\nEn excluant ${if (uncertain.size == 1) "cet ingrédient" else "ces ingrédients"} :" +
                                            "\n⚠️ INCONCLUS — certains ingrédients ne sont pas reconnus."
                                }
                                uncertainMessage + remainderMessage + unknownMessage
                            }
                            analysis.verdict == AnalysisVerdict.INCONCLUSIVE -> {
                                "⚠️ INCONCLUS\n\nLa base ne reconnaît pas toute la liste." + unknownMessage
                            }
                            analysis.verdict == AnalysisVerdict.VEGETARIAN -> {
                                val found = matches.filter { it.status == VeganStatus.VEGETARIAN }
                                "🥕 VÉGÉTARIEN\n\nIngrédient détecté :\n" + found.joinToString("\n\n") {
                                    "${it.eNumber ?: it.name} — ${it.name}\n${it.reason}"
                                } + "\n\nVerdict basé sur la liste d'ingrédients, pas une certification du produit."
                            }
                            else -> "✅ VEGAN\n\nTous les ingrédients de la liste ont été reconnus " +
                                "comme végétaux ou minéraux dans la base hors ligne."
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ANALYSER")
                }

                OutlinedButton(
                    onClick = {
                        ingredients = ""
                        result = WAITING_RESULT
                        focusManager.clearFocus()
                    },
                    enabled = ingredients.isNotEmpty() || result != WAITING_RESULT,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("EFFACER")
                }
            }

            HorizontalDivider()

            Text(
                text = "Résultat",
                fontSize = 20.sp
            )

            Text(
                text = result
            )
        }
    }
}
