package com.example.isitvegan

import java.io.File

/** Test-only JSON transport to the read-only Python guards; no production serializer. */
internal object KnowledgeValidationTestSupport {
    fun root(): File = generateSequence(File(requireNotNull(System.getProperty("user.dir"))).canonicalFile) {
        it.parentFile
    }.first { File(it, "settings.gradle.kts").isFile }

    fun json(value: Any?): String = when (value) {
        null -> "null"
        is String -> buildString {
            append('"')
            value.forEach { c ->
                when (c) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> if (c.code < 32) append("\\u%04x".format(c.code)) else append(c)
                }
            }
            append('"')
        }
        is Map<*, *> -> value.entries.joinToString(",", "{", "}") { json(it.key as String) + ":" + json(it.value) }
        is List<*> -> value.joinToString(",", "[", "]") { json(it) }
        is Boolean, is Number -> value.toString()
        else -> error("Unsupported test JSON type: ${value.javaClass}")
    }

    fun historicalCheck(root: File, ingredientsJson: String, lexiconJson: String) {
        val process = ProcessBuilder("python", "-B", "-X", "utf8", "tools/validate_knowledge_history.py", "--stdin")
            .directory(root).redirectErrorStream(true).start()
        process.outputStream.bufferedWriter(Charsets.UTF_8).use {
            it.write("{\"ingredients\":$ingredientsJson,\"lexicon\":$lexiconJson}")
        }
        val output = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
        require(process.waitFor() == 0) { output }
    }
}
