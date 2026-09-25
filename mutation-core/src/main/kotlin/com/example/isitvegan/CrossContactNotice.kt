package com.example.isitvegan

object CrossContactNotice {
    fun format(warnings: List<String>): String {
        if (warnings.isEmpty()) return ""
        return "\n\n⚠️ TRACES SIGNALÉES\n\n" +
            "Le fabricant signale un risque de contamination croisée :\n" +
            warnings.joinToString("\n") { "• $it" } +
            "\n\nCette information n’est pas utilisée pour calculer le verdict."
    }
}
