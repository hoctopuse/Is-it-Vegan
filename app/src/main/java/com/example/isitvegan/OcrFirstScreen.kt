package com.example.isitvegan

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OcrFirstScreen() {
    var ingredients by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("⚪ En attente d'analyse") }
    var diagnostics by remember { mutableStateOf<AnalysisDiagnostics?>(null) }
    var inputMode by remember { mutableStateOf(InputMode.FULL_LABEL) }
    var session by remember { mutableStateOf(OcrSession()) }
    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var preview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var ocrMessage by remember { mutableStateOf("Aucune photo sélectionnée.") }
    var extracting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val processor = remember(context) { OcrProcessor(context) }
    DisposableEffect(processor) { onDispose { processor.close() } }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) { ocrMessage = "Sélection annulée."; return@rememberLauncherForActivityResult }
        sourceUri = uri; inputMode = InputMode.OCR_LABEL; session = OcrSession(); diagnostics = null; result = "⚪ En attente d'analyse"; preview = null
        ocrMessage = "Photo sélectionnée. Appuyez sur EXTRAIRE LE TEXTE."
    }

    LaunchedEffect(sourceUri) {
        val uri = sourceUri ?: return@LaunchedEffect
        preview = withContext(Dispatchers.IO) {
            try { context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } } catch (_: Exception) { null }
        }
        if (preview == null) ocrMessage = "Aperçu indisponible. Vous pouvez saisir le texte manuellement."
    }
    LaunchedEffect(diagnostics, session.analyses.size) { if (diagnostics != null) { delay(150.milliseconds); scrollState.animateScrollTo(scrollState.maxValue) } }

    fun analyse(text: String) {
        focusManager.clearFocus()
        val current = VeganAnalyzer.analyzeWithDiagnostics(text, inputMode)
        val display = OcrFirstScreenResult.render(text, current)
        diagnostics = current; result = display
        session = session.addAnalysis(AnalysisSnapshot(text, DiagnosticReport.build(current, appVersionName(context)), display, System.currentTimeMillis()))
    }

    Scaffold(Modifier.fillMaxSize()) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState).imePadding().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("🌱 Is It Vegan?", style = MaterialTheme.typography.headlineLarge)
            Text("Colle une étiquette ou une liste d’ingrédients pour vérifier sa composition hors ligne.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Mode d’entrée", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(InputMode.FULL_LABEL to "Étiquette", InputMode.MANUAL_INGREDIENT_LIST to "Liste seule", InputMode.OCR_LABEL to "OCR").forEach { (mode, label) ->
                    FilterChip(selected = inputMode == mode, onClick = { inputMode = mode; diagnostics = null; result = "⚪ En attente d'analyse" }, label = { Text(label) })
                }
            }
            if (inputMode == InputMode.OCR_LABEL) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({ picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, Modifier.weight(1f)) { Text("CHOISIR UNE PHOTO") }
                    OutlinedButton({ sourceUri = null; preview = null; ocrMessage = "Saisie manuelle activée." }, Modifier.weight(1f)) { Text("SAISIE MANUELLE") }
                }
                preview?.let { Image(it.asImageBitmap(), "Aperçu de la photo sélectionnée", Modifier.fillMaxWidth().heightIn(max = 260.dp), contentScale = ContentScale.Fit) }
                Text(ocrMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button({
                    val uri = sourceUri ?: return@Button
                    extracting = true; ocrMessage = "Extraction du texte en cours…"
                    processor.process(uri, { raw -> extracting = false; session = session.withOcrText(raw); ocrMessage = if (raw.isBlank()) "Aucun texte détecté. Saisissez-le manuellement ci-dessous." else "Texte extrait. Vérifiez-le avant l’analyse." }, { message -> extracting = false; ocrMessage = message })
                }, enabled = sourceUri != null && !extracting, modifier = Modifier.fillMaxWidth()) { Text(if (extracting) "EXTRACTION…" else "EXTRAIRE LE TEXTE") }
                if (session.rawOcrText != null) {
                    Text("Texte brut OCR (lecture seule)", style = MaterialTheme.typography.titleMedium)
                    SelectionContainer { Text(session.rawOcrText!!.ifBlank { "(aucun texte détecté)" }) }
                    Text("Le texte brut est conservé tel que fourni par ML Kit.", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(session.editableText, { session = session.withEditableText(it); diagnostics = null }, Modifier.fillMaxWidth().heightIn(min = 150.dp), label = { Text("Texte éditable à analyser") }, placeholder = { Text("Corrigez ou saisissez le texte de l’étiquette") }, supportingText = { Text("L’OCR peut contenir des erreurs : vérifiez le texte avant l’analyse.") })
            } else {
                OutlinedTextField(ingredients, { ingredients = it; diagnostics = null }, Modifier.fillMaxWidth().heightIn(min = 150.dp), label = { Text(if (inputMode == InputMode.FULL_LABEL) "Texte de l’étiquette" else "Liste d’ingrédients") }, placeholder = { Text(if (inputMode == InputMode.FULL_LABEL) "Collez le texte complet de l’étiquette" else "Collez uniquement la liste des ingrédients") }, supportingText = { Text("La liste reste sur cet appareil.") })
            }
            Button({ analyse(if (inputMode == InputMode.OCR_LABEL) session.editableText else ingredients) }, Modifier.fillMaxWidth()) { Text("ANALYSER") }
            if (inputMode == InputMode.OCR_LABEL && session.analyses.any { it.submittedText != session.editableText }) Text("Le résultat affiché correspond à une version précédente du texte éditable.", color = MaterialTheme.colorScheme.tertiary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton({ ingredients = ""; session = OcrSession(); sourceUri = null; preview = null; result = "⚪ En attente d'analyse"; diagnostics = null }, Modifier.weight(1f)) { Text("EFFACER") }
                OutlinedButton({ shareOcrExport(context, OcrExportReport.build(session, appVersionName(context)), appVersionName(context)) }, enabled = session.analyses.isNotEmpty() || session.rawOcrText != null, modifier = Modifier.weight(1f)) { Text("EXPORTER") }
            }
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Résultat", style = MaterialTheme.typography.titleLarge); HorizontalDivider(); SelectionContainer { Text(result, style = MaterialTheme.typography.bodyLarge) } } }
            Text("Version ${appVersionName(context)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))
        }
    }
}

private object OcrFirstScreenResult {
    fun render(text: String, diagnostics: AnalysisDiagnostics): String {
        val a = diagnostics.result
        val unknown = if (a.unknown.isEmpty()) "" else "\n\nNon reconnus : " + a.unknown.joinToString(", ")
        val detail = when {
            text.isBlank() -> "ℹ️ Entre d'abord une liste d'ingrédients."
            a.availability == AnalysisAvailability.NO_INGREDIENT_LIST -> "ℹ️ AUCUNE LISTE D’INGRÉDIENTS DÉTECTÉE\n\nAucune conclusion vegan n’est produite."
            a.verdict == AnalysisVerdict.NON_VEGETARIAN -> "❌ NON VÉGÉTARIEN\n\nIngrédient détecté :\n" + a.matched.filter { it.status == VeganStatus.NON_VEGAN }.joinToString("\n\n") { "${it.eNumber ?: it.name} — ${it.name}\n${it.reason}" }
            a.verdict == AnalysisVerdict.UNCERTAIN -> "⚠️ INCERTAIN\n\nÀ vérifier :\n" + a.uncertainIngredients.joinToString("\n\n") { "${it.eNumber ?: it.name} — ${it.name}\n${it.reason}" } + unknown
            a.verdict == AnalysisVerdict.INCONCLUSIVE -> "⚠️ INCONCLUS\n\nLa base ne reconnaît pas toute la liste." + unknown
            a.verdict == AnalysisVerdict.VEGETARIAN -> "🥕 VÉGÉTARIEN\n\nIngrédient détecté :\n" + a.vegetarianIngredients.joinToString("\n\n") { "${it.eNumber ?: it.name} — ${it.name}\n${it.reason}" }
            else -> "✅ VEGAN\n\nTous les ingrédients de la liste ont été reconnus comme végétaux ou minéraux dans la base hors ligne."
        }
        return detail + CrossContactNotice.format(a.crossContactWarnings)
    }
}

private fun appVersionName(context: Context): String = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "inconnue"
private fun shareOcrExport(context: Context, report: String, versionName: String) {
    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, "Is It Vegan? — export v$versionName"); putExtra(Intent.EXTRA_TEXT, report) }
    context.startActivity(Intent.createChooser(intent, "Exporter les textes et résultats"))
}
