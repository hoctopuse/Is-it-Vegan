package com.example.isitvegan

internal object OcrTextCleaner {
    fun clean(text: String): String {
        // Compile on demand: a platform-specific regex failure must remain catchable by OcrProcessor.
        val uppercaseHyphenatedLineBreak = Regex("""(\p{Lu}{2,})-\r?\n(\p{Lu}{2,})""")
        val hyphenatedLineBreak = Regex("""(\p{L}{2,})-\r?\n(\p{Ll}{2,})""")
        // Frequent ML Kit confusion on small French labels: the narrow "i" is read as
        // a lowercase "l". Restrict the correction to a delimited section heading so
        // ordinary ingredient text is never guessed or rewritten.
        val frenchIngredientHeadingWithOcrL = Regex(
            """(?<![\p{L}\d])ingr[éeè]dlents?(?=\s*(?:[:;,.]|\r?$))""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE)
        )
        val heading = Regex(
            """\b(ingrédients|ingredients)\s*[:;,.]?\s*(\S)""",
            RegexOption.IGNORE_CASE
        )
        val mayContain = Regex(
            """\b(peut\s+contenir)\s*[:;,.]?\s*(\S)""",
            RegexOption.IGNORE_CASE
        )
        val repeatedSpaces = Regex("""[\t ]+""")
        val spaceBeforePunctuation = Regex("""\s+([,.])""")
        val spaceBeforeClosingParenthesis = Regex("""\s+\)""")
        val commaWithoutSpace = Regex(""",([\p{L}*])""")
        val spacesAroundColon = Regex("""[\t ]*:[\t ]*""")
        val spaceAfterOpeningParenthesis = Regex("""\([\t ]+""")
        val repeatedSeparators = Regex("""([,:;])\1+""")

        return text
            .replace("\r\n", "\n")
            .replace(uppercaseHyphenatedLineBreak, "$1-$2")
            .replace(hyphenatedLineBreak, "$1$2")
            .replace(frenchIngredientHeadingWithOcrL, "ingrédients")
            .split('\n')
            .joinToString("\n") { line ->
                if (line.isBlank()) return@joinToString ""
                line.trim()
                    .replace(repeatedSpaces, " ")
                    .replace(spaceBeforePunctuation, "$1")
                    .replace(spaceBeforeClosingParenthesis, ")")
                    .replace(commaWithoutSpace, ", $1")
                    .replace(spacesAroundColon, ": ")
                    .replace(spaceAfterOpeningParenthesis, "(")
                    .replace(repeatedSeparators, "$1")
                    .replace(heading, "$1 : $2")
                    .replace(mayContain, "$1 : $2")
                    .trimEnd()
            }
    }

    fun cleanSelectedBlock(text: String): String {
        val cleaned = clean(text)
        val parasite = Regex(
            "(?im)(?:^|\\n)[\\t ]*(?:open\\s+here|ouvrir\\s+ici|importateur|imported\\s+by|importer|certification|certified)\\b"
        ).find(cleaned)
        return cleaned.substring(0, parasite?.range?.first ?: cleaned.length).trim()
    }
}
