package com.example.isitvegan

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OcrFirstScreen(analysisEnabled: Boolean = true) {
    val context = LocalContext.current
    var uiLanguage by remember { mutableStateOf(UiLanguagePreferences.read(context)) }
    val localized = remember(uiLanguage) { localizedContext(context, uiLanguage) }
    fun ui(id: Int) = localized.resources.getString(id)
    var ingredients by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("⚪ En attente d'analyse") }
    var diagnostics by remember { mutableStateOf<AnalysisDiagnostics?>(null) }
    var inputMode by remember { mutableStateOf(InputMode.FULL_LABEL) }
    var session by remember { mutableStateOf(OcrSession()) }
    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var preview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var cropRect by remember { mutableStateOf<OcrCropRect?>(null) }
    var cropPreview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var cropDialogVisible by remember { mutableStateOf(false) }
    var ocrMessage by remember { mutableStateOf("Aucune photo sélectionnée.") }
    var extracting by remember { mutableStateOf(false) }
    var analyzing by remember { mutableStateOf(false) }
    var loadingPreview by remember { mutableStateOf(false) }
    var preparingCrop by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val processor = remember(context) { OcrProcessor(context) }
    val imageFactory = remember(context) { OcrImageInputFactory(context) }
    DisposableEffect(processor) { onDispose { processor.close() } }
    DisposableEffect(preview) {
        val ownedBitmap = preview
        onDispose { ownedBitmap?.takeUnless(Bitmap::isRecycled)?.recycle() }
    }
    DisposableEffect(cropPreview) {
        val ownedBitmap = cropPreview
        onDispose { ownedBitmap?.takeUnless(Bitmap::isRecycled)?.recycle() }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) { ocrMessage = "Sélection annulée."; return@rememberLauncherForActivityResult }
        sourceUri = uri; inputMode = InputMode.OCR_LABEL; session = OcrSession(); diagnostics = null; result = "⚪ En attente d'analyse"; preview = null; cropRect = null; cropPreview = null; loadingPreview = true
        ocrMessage = "Préparation de l’aperçu…"
    }

    LaunchedEffect(sourceUri) {
        val uri = sourceUri ?: return@LaunchedEffect
        preview = OcrThreading.io {
            try {
                imageFactory.decodeForPreview(uri)
            } catch (_: Exception) {
                null
            } catch (_: OutOfMemoryError) {
                null
            }
        }
        loadingPreview = false
        ocrMessage = if (preview == null) {
            "Aperçu indisponible. Vous pouvez saisir le texte manuellement."
        } else {
            "Photo sélectionnée. Appuyez sur EXTRAIRE LE TEXTE."
        }
    }
    LaunchedEffect(diagnostics, session.analyses.size) { if (diagnostics != null) { delay(150.milliseconds); scrollState.animateScrollTo(scrollState.maxValue) } }

    fun analyse(text: String) {
        if (!analysisEnabled || analyzing) return
        focusManager.clearFocus()
        val requestedMode = inputMode
        val requestedLanguage = uiLanguage
        val requestedBlockId = session.selectedOptionBlockId
            .takeIf { requestedMode == InputMode.OCR_LABEL && !session.fullTextSelected }
        val versionName = appVersionName(context)
        analyzing = true
        coroutineScope.launch {
            val completed = runCatching {
                OcrThreading.cpu {
                    val current = VeganAnalyzer.analyzeWithDiagnostics(
                        text, requestedMode, requestedLanguage, requestedBlockId
                    )
                    val display = OcrFirstScreenResult.render(text, current)
                    Triple(current, display, DiagnosticReport.build(current, versionName))
                }
            }
            analyzing = false
            completed.onSuccess { (current, display, report) ->
                val currentText = if (inputMode == InputMode.OCR_LABEL) session.editableText else ingredients
                if (inputMode != requestedMode || currentText != text) return@onSuccess
                diagnostics = current
                result = display
                session = session.addAnalysis(
                    AnalysisSnapshot(
                        text, report, display, System.currentTimeMillis(),
                        current.labelSections.selectedBlockId
                    )
                )
            }.onFailure {
                result = "Impossible d’analyser ce texte. Réessayez avec une zone plus courte."
            }
        }
    }

    Scaffold(Modifier.fillMaxSize()) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState).imePadding().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("🌱 Is It Vegan?", style = MaterialTheme.typography.headlineLarge)
            Text(ui(R.string.tagline), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(ui(R.string.input_mode), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(InputMode.FULL_LABEL to ui(R.string.mode_label), InputMode.MANUAL_INGREDIENT_LIST to ui(R.string.mode_list), InputMode.OCR_LABEL to ui(R.string.mode_ocr)).forEach { (mode, label) ->
                    FilterChip(selected = inputMode == mode, onClick = { inputMode = mode; diagnostics = null; result = "⚪ En attente d'analyse" }, label = { Text(label) })
                }
            }
            if (inputMode == InputMode.OCR_LABEL) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({ picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, Modifier.weight(1f), enabled = !extracting && !loadingPreview && !preparingCrop) { Text(ui(R.string.choose_photo)) }
                    OutlinedButton({ sourceUri = null; preview = null; cropRect = null; cropPreview = null; session = OcrSession(); diagnostics = null; ocrMessage = "Saisie manuelle activée." }, Modifier.weight(1f), enabled = !extracting && !loadingPreview && !preparingCrop) { Text(ui(R.string.manual_entry)) }
                }
                preview?.let { original ->
                    val displayed = cropPreview ?: original
                    Image(
                        displayed.asImageBitmap(),
                        if (cropRect == null) "Aperçu de la photo sélectionnée" else "Aperçu de la zone recadrée",
                        Modifier.fillMaxWidth().heightIn(max = 260.dp),
                        contentScale = ContentScale.Fit
                    )
                    Button({ cropDialogVisible = true }, Modifier.fillMaxWidth(), enabled = !extracting && !preparingCrop) { Text(ui(R.string.crop_for_ocr)) }
                    if (cropRect != null) {
                        Text(ui(R.string.ocr_cropped_image), color = MaterialTheme.colorScheme.primary)
                        OutlinedButton({ cropRect = null; cropPreview = null; session = OcrSession(); diagnostics = null; result = "⚪ En attente d'analyse"; ocrMessage = "Cadre réinitialisé. L’image entière sera utilisée." }, Modifier.fillMaxWidth(), enabled = !extracting && !preparingCrop) {
                            Text(ui(R.string.reset_crop))
                        }
                    } else {
                        Text(ui(R.string.ocr_full_image), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(ocrMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button({
                    val uri = sourceUri ?: return@Button
                    extracting = true; ocrMessage = "Extraction du texte en cours…"
                    val onSuccess: (OcrProcessingResult) -> Unit = { ocr -> extracting = false; session = session.withOcrResult(ocr); ocrMessage = when { ocr.rawText.isBlank() -> "Aucun texte détecté. Saisissez-le manuellement ci-dessous."; ocr.usedRawFallback -> "OCR réussi, mais le post-traitement a échoué. Le texte brut reste disponible et éditable."; else -> "Texte reconstruit. Vérifiez-le avant l’analyse." } }
                    val onFailure: (String) -> Unit = { message -> extracting = false; ocrMessage = message }
                    processor.process(uri, cropRect, onSuccess, onFailure, uiLanguage)
                }, enabled = sourceUri != null && !extracting && !loadingPreview && !preparingCrop, modifier = Modifier.fillMaxWidth()) { Text(if (extracting) "EXTRACTION…" else "EXTRAIRE LE TEXTE") }
                if (session.rawOcrText != null) {
                    Text(ui(R.string.raw_ocr), style = MaterialTheme.typography.titleMedium)
                    SelectionContainer { Text(session.rawOcrText!!.ifBlank { "(aucun texte détecté)" }) }
                    Text(ui(R.string.ocr_raw_kept), style = MaterialTheme.typography.bodySmall)
                }
                if (session.textOptions.size > 1) {
                    Text(ui(R.string.ocr_block_choice), style = MaterialTheme.typography.titleMedium)
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        session.textOptions.forEach { option ->
                            FilterChip(
                                selected = !session.fullTextSelected && session.selectedOptionLanguage == option.language,
                                onClick = {
                                    session = session.selectLanguage(option.language)
                                    diagnostics = null
                                    result = ui(R.string.waiting)
                                },
                                label = { Text(option.language.displayName) }
                            )
                        }
                        FilterChip(
                            selected = session.fullTextSelected,
                            onClick = {
                                session = session.selectFullText()
                                diagnostics = null
                                result = ui(R.string.waiting)
                            },
                            label = { Text(ui(R.string.full_ocr_text)) }
                        )
                    }
                }
                OutlinedTextField(session.editableText, { session = session.withEditableText(it); diagnostics = null }, Modifier.fillMaxWidth().heightIn(min = 150.dp), label = { Text(ui(R.string.editable_ocr)) }, placeholder = { Text(ui(R.string.paste_ocr)) }, supportingText = { Text(ui(R.string.check_text)) })
            } else {
                OutlinedTextField(ingredients, { ingredients = it; diagnostics = null }, Modifier.fillMaxWidth().heightIn(min = 150.dp), label = { Text(if (inputMode == InputMode.FULL_LABEL) ui(R.string.full_label_text) else ui(R.string.ingredient_list)) }, placeholder = { Text(if (inputMode == InputMode.FULL_LABEL) ui(R.string.paste_full_label) else ui(R.string.paste_ingredients)) }, supportingText = { Text(ui(R.string.local_only)) })
            }
            Button(
                { analyse(if (inputMode == InputMode.OCR_LABEL) session.editableText else ingredients) },
                Modifier.fillMaxWidth(),
                enabled = analysisEnabled && !analyzing
            ) { Text(if (analyzing) "ANALYSE…" else ui(R.string.analyze)) }
            if (inputMode == InputMode.OCR_LABEL && session.analyses.any { it.submittedText != session.editableText }) Text("Le résultat affiché correspond à une version précédente du texte éditable.", color = MaterialTheme.colorScheme.tertiary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton({ ingredients = ""; session = OcrSession(); sourceUri = null; preview = null; cropRect = null; cropPreview = null; result = "⚪ En attente d'analyse"; diagnostics = null }, Modifier.weight(1f)) { Text(ui(R.string.clear)) }
                OutlinedButton({ shareOcrExport(context, OcrExportReport.build(session, appVersionName(context)), appVersionName(context)) }, enabled = session.analyses.isNotEmpty() || session.rawOcrText != null, modifier = Modifier.weight(1f)) { Text(ui(R.string.export)) }
            }
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(ui(R.string.result), style = MaterialTheme.typography.titleLarge); HorizontalDivider(); SelectionContainer { Text(result, style = MaterialTheme.typography.bodyLarge) } } }
            Text(ui(R.string.language), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UiLanguage.entries.forEach { language ->
                    val label = when (language) {
                        UiLanguage.FR -> ui(R.string.language_french)
                        UiLanguage.EN -> ui(R.string.language_english)
                        UiLanguage.NL -> ui(R.string.language_dutch)
                    }
                    FilterChip(
                        selected = uiLanguage == language,
                        onClick = {
                            uiLanguage = language
                            UiLanguagePreferences.write(context, language)
                            diagnostics = null
                        },
                        label = { Text(label) }
                    )
                }
            }
            Text("Version ${appVersionName(context)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))
        }
    }
    if (cropDialogVisible) {
        preview?.let { original ->
            OcrCropDialog(
                bitmap = original,
                initialRect = cropRect ?: OcrCropRect.full(),
                onCancel = { cropDialogVisible = false },
                onConfirm = { rect ->
                    cropDialogVisible = false
                    if (sourceUri == null) return@OcrCropDialog
                    preparingCrop = true
                    ocrMessage = "Préparation de la zone recadrée…"
                    coroutineScope.launch {
                        val preparedPreview = try {
                            OcrThreading.cpu {
                                OcrBitmapCropper.crop(original, rect).let { croppedPreview ->
                                    if (croppedPreview === original) {
                                        original.copy(original.config ?: Bitmap.Config.ARGB_8888, false)
                                    } else {
                                        croppedPreview
                                    }
                                }
                            }
                        } catch (_: Exception) {
                            null
                        } catch (_: OutOfMemoryError) {
                            null
                        }
                        preparingCrop = false
                        if (preparedPreview == null) {
                            ocrMessage = "Impossible de préparer ce recadrage. L’image entière reste disponible."
                        } else {
                            cropRect = rect
                            cropPreview = preparedPreview
                            session = OcrSession()
                            diagnostics = null
                            result = "⚪ En attente d'analyse"
                            ocrMessage = "Cadre validé. Appuyez sur EXTRAIRE LE TEXTE."
                        }
                    }
                }
            )
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
            a.verdict == AnalysisVerdict.NON_VEGETARIAN -> "❌ NON VEGAN\n\nIngrédient détecté :\n" + a.matched.filter { it.status == VeganStatus.NON_VEGAN }.joinToString("\n\n") { "${it.eNumber ?: it.name} — ${it.name}\n${it.reason}" }
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
