package com.example.isitvegan

internal data class PreprocessedLabel(
    val compositionText: String,
    val crossContactWarnings: List<String>,
    val excludedNotes: List<String>,
    val ocrCorrections: List<String> = emptyList()
)

internal object LabelPreprocessor {
    private val crossContactMarker = Regex(
        "(?i)\\b(?:p(?:eu|e)t\\s+cont[eé]nir|p(?:eu|e)t\\s+conterir|p(?:eu|e)t\\s+conteir|p(?:eu|e)t\\s+conteuir|traces?\\s*(?:éventuelles?\\s*)?(?:de|d['’]|:)|" +
            "may\\s+contain(?:\\s+traces?\\s+of)?|" +
            "kan(?:\\s+\\p{L}+){0,5}\\s+bevatten|" +
            "kann(?:\\s+\\p{L}+){0,7}\\s+enthalten|" +
            "puede\\s+contener(?:\\s+trazas\\s+de)?|" +
            "fabriqu[ée]\\s+dans\\s+un\\s+atelier)"
    )
    private val ingredientHeading = Regex("(?i)^\\s*(?:ingr[ée]dients?|sngredients?|ingr[ée]cients?|ingredi[ëè]nten?|ingredienten?|zutaten|ingredientes?)\\s*:\\s*")
    private val simpleOcrCorrections = listOf(
        // These spellings are only processed after section extraction, never in raw label text.
        Regex("(?i)\\bTOZijnen\\b") to "rozijnen",
        Regex("(?i)\\bqerousterde\\b") to "geroosterde",
        Regex("(?i)\\bpisaehenoten\\b") to "pistachenoten",
        Regex("(?i)\\bcranbery's\\b") to "cranberry's",
        Regex("(?i)\\bsujke\\b") to "suiker",
        Regex("(?i)\\bp\u00e5te\\b") to "p\u00e2te",
        Regex("\\bSUCre\\b") to "sucre",
        Regex("(?i)\\bl\u00e9cith\u00ednes\\b") to "l\u00e9cithines",
        Regex("(?i)\\bsirop\\s+de\\s+qlucose\\b") to "sirop de glucose",
        Regex("(?i)\\bhulle\\s+de\\s+tournesol\\b") to "huile de tournesol",
        Regex("(?i)\\bhulle\\s+de\\s*\\r?\\n\\s*tournesol\\b") to "huile de tournesol",
        Regex("(?i)\\bhuile\\s+de\\s*\\r?\\n\\s*tournesol\\b") to "huile de tournesol",
        Regex("(?i)\\bfarine\\s+de\\s*\\r?\\n\\s*riz\\b") to "farine de riz",
        Regex("(?i)\\bextrait\\s+riche\\s+en\\s*\\r?\\n\\s*tocophérols\\b") to "extrait riche en tocophérols",
        Regex("(?i)\\b(\\d{1,2},\\d)h(?=\\s*(?:huile|hulle)\\b)") to "$1 %",
        Regex("(?i)\\bformage(?=\\s+grana\\b)") to "fromage",
        Regex("(?i)\\bpowron\\b") to "poivron",
        Regex("(?i)\\bolves\\s+nores\\b") to "olives noires",
        Regex("(?i)\\bhuie\\s+[đd]ove\\b") to "huile d’olive",
        Regex("(?i)\\bdoutble\\s+oncentré\\b") to "double concentré",
        Regex("(?i)\\bognon\\b") to "oignon",
        Regex("(?i)\\btoumesol\\b") to "tournesol",
        Regex("(?i)\\btourmesol\\b") to "tournesol",
        Regex("(?i)\\bbé\\s+entier\\b") to "blé entier",
        Regex("(?i)\\bestait\\s+de\\s+male\\s+d['’]orge\\b") to "extrait de malt d’orge",
        Regex("(?i)\\bsiop\\b") to "sirop",
        Regex("(?i)\\bblé\\s+entie\\b") to "blé entier",
        Regex("(?i)\\bflocons\\s+d['’]?avoine\\b") to "flocons d’avoine",
        Regex("(?i)\\bflocons\\s+de\\s+seigle\\s+entie\\b") to "flocons de seigle entier",
        Regex("(?i)\\bmüre\\b") to "mûre",
        Regex("(?i)\\bmytile\\b") to "myrtille",
        Regex("(?i)\\byophilisée\\b") to "lyophilisée",
        Regex("(?i)\\blyophlisée\\b") to "lyophilisée",
        Regex("(?i)\\baröớme\\b") to "arôme",
        Regex("(?i)\\baröme\\b") to "arôme",
        Regex("(?i)\\bfrase\\b") to "fraise",
        Regex("(?i)\\bemuisifiant\\b") to "émulsifiant",
        Regex("(?i)\\blecthines\\b") to "lécithines",
        Regex("(?i)(?<!\\p{L})écithines\\b") to "lécithines",
        Regex("(?i)\\bSáurerequlator\\b") to "Säureregulator",
        Regex("(?i)\\bCitronensåure\\b") to "Citronensäure",
        Regex("(?i)\\bpoudrel\\b") to "poudre",
        Regex("(?i)\\bextait\\b") to "extrait",
        Regex("(?i)\\blquide\\b") to "liquide",
        Regex("(?i)\\bdéshydrate\\b") to "déshydraté",
        Regex("(?i)\\bgelfiant\\b") to "gélifiant",
        Regex("(?i)\\barôme\\s+naturel\\s+de\\s+fraise\\s+sel\\b") to "arôme naturel de fraise, sel",
        Regex("(?i)(émulsifiant)\\s*\\(\\s*(lécithines\\s*\\([^)]*\\))\\s*,\\s*(?=correcteur\\s+d['’]acidité)") to "$1 : $2, "
    )

    fun preprocess(text: String): PreprocessedLabel {
        val warnings = linkedSetOf<String>()
        val notes = linkedSetOf<String>()
        val corrections = linkedSetOf<String>()
        var continuingNote = false
        var cleaned = text.replace('\u00A0', ' ')
            .lines()
            .map { line ->
                if (line.isBlank()) {
                    continuingNote = false
                    line
                } else if (continuingNote &&
                    !noteMarker.containsMatchIn(line) &&
                    !crossContactMarker.containsMatchIn(line)
                ) {
                    val previous = notes.last()
                    notes.remove(previous)
                    notes.add("$previous ${line.trim()}")
                    ""
                } else {
                    val noteCount = notes.size
                    extractNotices(line, warnings, notes).also {
                        continuingNote = notes.size > noteCount
                    }
                }
            }
            .joinToString("\n")
            .replace(ingredientHeading, "")

        Regex("(?i)\\b(\\d{1,2},\\d)h(?=\\s*(?:huile|hulle)\\b)").findAll(cleaned).forEach { match ->
            corrections += "${match.value} → ${match.groupValues[1]} %"
        }

        simpleOcrCorrections.forEach { (pattern, replacement) ->
            if ('$' !in replacement) {
                pattern.findAll(cleaned).forEach { match ->
                    corrections += "${match.value} → $replacement"
                }
            }
            cleaned = cleaned.replace(pattern, replacement)
        }
        val referencedMarkers = noteMarker.findAll(text).mapNotNull { match ->
            Regex("^(?:\\*{1,3}|[¹²³]|\\^)").find(match.value.trimStart())?.value
        }.toSet()
        if (referencedMarkers.isNotEmpty()) {
            cleaned = cleaned.replace(attachedReference) { match ->
                if (match.value in referencedMarkers ||
                    (match.value.all { it == '*' } && referencedMarkers.any { marker -> marker.all { it == '*' } })
                ) "" else match.value
            }
        }
        return PreprocessedLabel(cleaned, warnings.toList(), notes.toList(), corrections.toList())
    }

    private val noteMarker = Regex(
        "(?i)(?:(?:\\*{1,3}|[¹²³])?\\s*(?:" +
            "Allerg[èe]nes\\s*:|Rainforest\\s+Alliance\\s+Certified|" +
            "Certifi[ée]\\s+Rainforest\\s+Alliance|zertifiziert|gecertificeerd|Mehr\\s+unter|Agriculture\\s+biologique|" +
            "Issu\\s+de\\s+poules\\s+[ée]lev[ée]es\\s+au\\s+sol)|\\^\\s*concentr[ée])"
    )
    private val attachedReference = Regex(
        "(?<=\\p{L})(\\*{1,3}|[¹²³]|\\^)(?=\\s*(?:[,;.)\\]]|$))"
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
