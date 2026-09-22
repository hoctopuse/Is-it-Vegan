package com.example.isitvegan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.isitvegan.ui.theme.IsItVeganTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

private const val WAITING_RESULT = "⚪ En attente d'analyse"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var databaseReady by remember { mutableStateOf(VeganAnalyzer.isDatabaseLoaded()) }
            LaunchedEffect(Unit) {
                if (!databaseReady) {
                    databaseReady = withContext(Dispatchers.IO) {
                        runCatching { VeganAnalyzer.loadDatabase(applicationContext) }.isSuccess
                    }
                }
            }
            IsItVeganTheme {
                OcrFirstScreen(analysisEnabled = databaseReady)
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
    var diagnostics by remember { mutableStateOf<AnalysisDiagnostics?>(null) }
    var inputMode by remember { mutableStateOf(InputMode.FULL_LABEL) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(diagnostics) {
        if (diagnostics != null) {
            // Wait for the result card to be measured before scrolling to it.
            delay(150.milliseconds)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "🌱 Is It Vegan?",
                style = MaterialTheme.typography.headlineLarge
            )

            Text(
                text = "Colle une étiquette ou une liste d’ingrédients pour vérifier sa composition hors ligne.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Mode d’entrée", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    InputMode.FULL_LABEL to "Étiquette",
                    InputMode.MANUAL_INGREDIENT_LIST to "Liste seule",
                    InputMode.OCR_LABEL to "OCR"
                ).forEach { (mode, label) ->
                    FilterChip(
                        selected = inputMode == mode,
                        onClick = {
                            inputMode = mode
                            diagnostics = null
                            result = WAITING_RESULT
                        },
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = ingredients,
                onValueChange = {
                    ingredients = it
                    diagnostics = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                label = {
                    Text(
                        when (inputMode) {
                            InputMode.FULL_LABEL -> "Texte de l’étiquette"
                            InputMode.MANUAL_INGREDIENT_LIST -> "Liste d’ingrédients"
                            InputMode.OCR_LABEL -> "Texte OCR"
                        }
                    )
                },
                placeholder = {
                    Text(
                        when (inputMode) {
                            InputMode.FULL_LABEL -> "Collez le texte complet de l’étiquette"
                            InputMode.MANUAL_INGREDIENT_LIST -> "Collez uniquement la liste des ingrédients"
                            InputMode.OCR_LABEL -> "Collez le texte extrait de la photo"
                        }
                    )
                },
                supportingText = {
                    Text("La liste reste sur cet appareil.")
                }
            )

            Button(
                onClick = {
                    focusManager.clearFocus()

                    val currentDiagnostics = VeganAnalyzer.analyzeWithDiagnostics(
                        ingredients,
                        inputMode
                    )
                    diagnostics = currentDiagnostics
                    val analysis = currentDiagnostics.result
                    val matches = analysis.matched
                    val unknownMessage = if (analysis.unknown.isEmpty()) "" else
                        "\n\nNon reconnus : " + analysis.unknown.joinToString(", ")
                    val detailedResult = when {
                        ingredients.isBlank() -> "ℹ️ Entre d'abord une liste d'ingrédients."
                        analysis.availability == AnalysisAvailability.NO_INGREDIENT_LIST -> {
                            val presence = analysis.declaredPresenceIngredientIds
                            if (presence.isEmpty()) {
                                "ℹ️ AUCUNE LISTE D’INGRÉDIENTS DÉTECTÉE\n\n" +
                                    "Aucune conclusion vegan n’est produite."
                            } else {
                                val compatibility = when (analysis.veganAssessment) {
                                    VeganAssessment.VEGAN -> "VEGAN"
                                    VeganAssessment.NOT_VEGAN -> "NON VEGAN"
                                    VeganAssessment.UNCERTAIN -> "INCERTAINE"
                                }
                                "ℹ️ AUCUNE LISTE D’INGRÉDIENTS DÉTECTÉE\n\n" +
                                    "Compatibilité vegan selon les ingrédients déclarés : $compatibility\n" +
                                    "Présence réelle déclarée : ${presence.joinToString(", ")}"
                            }
                        }
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
                    result = if (analysis.availability == AnalysisAvailability.NO_INGREDIENT_LIST) {
                        detailedResult + CrossContactNotice.format(analysis.crossContactWarnings)
                    } else {
                        val veganCompatibility = when (analysis.veganAssessment) {
                            VeganAssessment.VEGAN -> "VEGAN"
                            VeganAssessment.NOT_VEGAN -> "NON VEGAN"
                            VeganAssessment.UNCERTAIN -> "INCERTAINE"
                        }
                        "Compatibilité vegan selon les ingrédients déclarés : $veganCompatibility" +
                            "\n\nClassification détaillée :\n" + detailedResult +
                            "\n\nAnalyse fondée sur les informations déclarées sur l’étiquette." +
                            CrossContactNotice.format(analysis.crossContactWarnings)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ANALYSER")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        ingredients = ""
                        result = WAITING_RESULT
                        diagnostics = null
                        focusManager.clearFocus()
                    },
                    enabled = ingredients.isNotEmpty() || result != WAITING_RESULT,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("EFFACER")
                }

                OutlinedButton(
                    onClick = {
                        diagnostics?.let {
                            val versionName = appVersionName(context)
                            shareDiagnosticReport(
                                context,
                                DiagnosticReport.build(it, versionName),
                                versionName
                            )
                        }
                    },
                    enabled = diagnostics != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("EXPORTER")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Résultat",
                        style = MaterialTheme.typography.titleLarge
                    )
                    HorizontalDivider()
                    SelectionContainer {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Text(
                text = "Version ${appVersionName(context)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

private fun appVersionName(context: Context): String =
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "inconnue"

private fun shareDiagnosticReport(context: Context, report: String, versionName: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Is It Vegan? — diagnostic v$versionName")
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(intent, "Exporter le rapport"))
}
