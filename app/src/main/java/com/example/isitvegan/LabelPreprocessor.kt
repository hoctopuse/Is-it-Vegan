package com.example.isitvegan

internal data class PreprocessedLabel(
    val compositionText: String,
    val crossContactWarnings: List<String>,
    val excludedNotes: List<String>
)

internal object LabelPreprocessor {
    private val crossContactMarker = Regex(
        "(?i)\\b(?:peut\\s+contenir|traces?\\s*(?:éventuelles?\\s*)?(?:de|d['’]|:)|" +
            "fabriqu[ée]\\s+dans\\s+un\\s+atelier)"
    )
    private val ingredientHeading = Regex("(?i)^\\s*ingr[ée]dients?\\s*:\\s*")
    private val simpleOcrCorrections = listOf(
        Regex("(?i)\\bformage(?=\\s+grana\\b)") to "fromage"
    )

    fun preprocess(text: String): PreprocessedLabel {
        val warnings = linkedSetOf<String>()
        val notes = linkedSetOf<String>()
        var cleaned = text.replace('\u00A0', ' ')
            .lines()
            .map { line -> extractNotices(line, warnings, notes) }
            .joinToString("\n")
            .replace(ingredientHeading, "")

        simpleOcrCorrections.forEach { (pattern, replacement) ->
            cleaned = cleaned.replace(pattern, replacement)
        }
        return PreprocessedLabel(cleaned, warnings.toList(), notes.toList())
    }

    private val noteMarker = Regex(
        "(?i)\\bAllerg[èe]nes\\s*:|[¹²³*]*Rainforest\\s+Alliance\\s+Certified|" +
            "(?:\\*\\s*|^\\s*)Agriculture biologique|\\^\\s*concentr[ée]"
    )

    private fun extractNotices(
        line: String,
        warnings: MutableSet<String>,
        notes: MutableSet<String>
    ): String = buildString {
        var cursor = 0
        while (cursor < line.length) {
            val trace = crossContactMarker.find(line, cursor)
            val note = noteMarker.find(line, cursor)
            val marker = listOfNotNull(trace, note).minByOrNull { it.range.first }
            if (marker == null) {
                append(line.substring(cursor))
                break
            }
            append(line.substring(cursor, marker.range.first))
            val isTrace = marker === trace
            var end = marker.range.last + 1
            // Do not mistake the nested "traces de" in "peut contenir des traces de"
            // for a second warning. Sentence/line boundaries delimit the whole notice.
            var nesting = 0
            while (end < line.length) {
                val character = line[end]
                val isBoundary = if (isTrace) character == '.' else character in ".;)]"
                if (nesting == 0 && isBoundary) break
                if (character in "([") nesting++
                if (character in ")]") nesting = (nesting - 1).coerceAtLeast(0)
                end++
            }
            if (end < line.length && line[end] == '.') end++
            if (marker.value.contains("Rainforest", ignoreCase = true)) end = line.length
            // Keep certification text out of warnings, even without a full stop.
            val nextNote = noteMarker.find(line, marker.range.last + 1)
            if (nextNote != null) end = minOf(end, nextNote.range.first)
            val nextTrace = crossContactMarker.findAll(line, marker.range.last + 1)
                .firstOrNull {
                    !(marker.value.startsWith("peut", ignoreCase = true) &&
                        it.value.startsWith("trace", ignoreCase = true) &&
                        line.substring(marker.range.last + 1, it.range.first).trim()
                            .lowercase() in listOf("", "des", "de"))
                }
            if (nextTrace != null) end = minOf(end, nextTrace.range.first)
            val value = line.substring(marker.range.first, end)
                .replace("**", "").trim().trimStart('*', '¹', '²', '³').trim()
                .trimEnd(';').trim()
            if (isTrace) warnings.add(value) else notes.add(value)
            cursor = end
        }
    }
}
