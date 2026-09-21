package com.example.isitvegan

internal object OcrTextCleaner {
    fun clean(text: String): String {
        // Compile on demand: a platform-specific regex failure must remain catchable by OcrProcessor.
        val uppercaseHyphenatedLineBreak = Regex("""(\p{Lu}{2,})-\r?\n(\p{Lu}{2,})""")
        val hyphenatedLineBreak = Regex("""(\p{L}{2,})-\r?\n(\p{Ll}{2,})""")
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

        return text
            .replace("\r\n", "\n")
            .replace(uppercaseHyphenatedLineBreak, "$1-$2")
            .replace(hyphenatedLineBreak, "$1$2")
            .split('\n')
            .joinToString("\n") { line ->
                if (line.isBlank()) return@joinToString ""
                line.trim()
                    .replace(repeatedSpaces, " ")
                    .replace(spaceBeforePunctuation, "$1")
                    .replace(spaceBeforeClosingParenthesis, ")")
                    .replace(commaWithoutSpace, ", $1")
                    .replace(heading, "$1 : $2")
                    .replace(mayContain, "$1 : $2")
                    .trimEnd()
            }
    }
}
