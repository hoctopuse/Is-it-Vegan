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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.isitvegan.ui.theme.IsItVeganTheme

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

    var result by remember { mutableStateOf("⚪ En attente d'analyse") }

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

            Button(
                onClick = {

                    val analysis = VeganAnalyzer.analyze(ingredients)
                    val matches = analysis.matched
                    val unknownMessage = if (analysis.unknown.isEmpty()) "" else
                        "\n\nNon reconnus : " + analysis.unknown.joinToString(", ")
                    result = when {
                        ingredients.isBlank() -> "ℹ️ Entre d'abord une liste d'ingrédients."
                        matches.any { it.status == VeganStatus.NON_VEGAN } -> {
                            val found = matches.filter { it.status == VeganStatus.NON_VEGAN }
                            "❌ NON VEGAN\n\n" + found.joinToString("\n\n") {
                                "${it.eNumber ?: it.name} — ${it.name}\n${it.reason}"
                            } + unknownMessage
                        }
                        matches.any { it.status == VeganStatus.UNCERTAIN } -> {
                            val found = matches.filter { it.status == VeganStatus.UNCERTAIN }
                            "⚠️ INCERTAIN\n\n" + found.joinToString("\n\n") {
                                "${it.eNumber ?: it.name} — ${it.name}\n${it.reason}"
                            } + unknownMessage
                        }
                        analysis.unknown.isNotEmpty() -> {
                            "⚠️ INCONCLUS\n\nLa base ne reconnaît pas toute la liste." + unknownMessage
                        }
                        else -> "✅ VEGAN\n\nTous les ingrédients de la liste ont été reconnus " +
                            "comme végétaux ou minéraux dans la base hors ligne."
                    }

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ANALYSER")
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
