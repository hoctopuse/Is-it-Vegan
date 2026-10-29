package com.example.isitvegan

import android.content.res.Resources
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun MultilingualAlignmentReview(
    evaluation: MultilingualAlignmentEvaluation,
    editableText: String,
    dismissedProposalKeys: Set<String>,
    resources: Resources,
    onAccept: (IngredientAlignmentResult, String) -> Unit,
    onDismiss: (IngredientAlignmentResult) -> Unit,
    applicableProposalKeys: Set<String> = emptySet()
) {
    val corroborations = evaluation.links.filter { it.kind == AlignmentKind.CORROBORATION }
    val proposals = evaluation.links.filter {
        it.kind == AlignmentKind.CORRECTION_PROPOSAL &&
            it.proposalKey !in dismissedProposalKeys &&
            it.proposalKey in applicableProposalKeys
    }
    val ambiguousCount = evaluation.links.count { it.kind == AlignmentKind.AMBIGUOUS }
    val conflicts = evaluation.links.filter { it.kind == AlignmentKind.CONFLICT }
    if (corroborations.isEmpty() && proposals.isEmpty() && ambiguousCount == 0 && conflicts.isEmpty()) return

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(resources.getString(R.string.multilingual_review_title), style = MaterialTheme.typography.titleMedium)
            corroborations.forEach { link ->
                Text(
                    resources.getString(
                        R.string.multilingual_confirmed_item,
                        link.first.rawText,
                        link.first.language.displayName,
                        link.second.rawText,
                        link.second.language.displayName
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            proposals.forEach { proposal ->
                CorrectionProposalEditor(proposal, resources, onAccept, onDismiss)
            }
            if (ambiguousCount > 0) {
                Text(resources.getString(R.string.multilingual_ambiguous))
                evaluation.links.filter { it.kind == AlignmentKind.AMBIGUOUS }.forEach { link ->
                    Text(resources.getString(R.string.multilingual_confirmed_item,
                        link.first.rawText, link.first.language.displayName,
                        link.second.rawText, link.second.language.displayName),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            if (conflicts.isNotEmpty()) {
                Text(resources.getString(R.string.multilingual_conflict))
                conflicts.forEach { link ->
                    Text(resources.getString(R.string.multilingual_confirmed_item,
                        link.first.rawText, link.first.language.displayName,
                        link.second.rawText, link.second.language.displayName))
                }
            }
        }
    }
}

@Composable
private fun CorrectionProposalEditor(
    proposal: IngredientAlignmentResult,
    resources: Resources,
    onAccept: (IngredientAlignmentResult, String) -> Unit,
    onDismiss: (IngredientAlignmentResult) -> Unit
) {
    var replacement by remember(proposal.proposalKey) { mutableStateOf(proposal.first.rawText) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(resources.getString(R.string.multilingual_proposal_title), style = MaterialTheme.typography.bodyMedium)
        Text(resources.getString(R.string.multilingual_source_text, proposal.first.rawText))
        Text(
            resources.getString(
                R.string.multilingual_evidence_text,
                proposal.second.rawText,
                proposal.second.language.displayName,
                proposal.proposedConceptName ?: proposal.proposedCanonicalId.orEmpty()
            ),
            style = MaterialTheme.typography.bodySmall
        )
        OutlinedTextField(
            value = replacement,
            onValueChange = { replacement = it },
            modifier = Modifier.fillMaxWidth().testTag("multilingual-correction-text"),
            label = { Text(resources.getString(R.string.multilingual_edit_label)) }
        )
        Text(resources.getString(R.string.multilingual_accept_note), style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onAccept(proposal, replacement) },
                enabled = replacement.isNotBlank() && replacement != proposal.first.rawText,
                modifier = Modifier.testTag("multilingual-correction-accept")
            ) { Text(resources.getString(R.string.multilingual_accept)) }
            OutlinedButton(
                onClick = { onDismiss(proposal) },
                modifier = Modifier.testTag("multilingual-correction-dismiss")
            ) { Text(resources.getString(R.string.multilingual_dismiss)) }
        }
    }
}
