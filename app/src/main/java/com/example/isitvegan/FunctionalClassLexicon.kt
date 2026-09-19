package com.example.isitvegan

/** Regulatory functional classes. They provide structure and never a vegan status. */
internal object FunctionalClassLexicon {
    private val classes = setOf(
        // French
        "emulsifiant", "emulsifiants", "stabilisant", "stabilisants",
        "correcteur d acidite", "correcteurs d acidite", "regulateur d acidite",
        "regulateurs d acidite", "antioxydant", "antioxydants", "acidifiant",
        "acidifiants", "conservateur", "conservateurs", "epaississant",
        "epaississants", "gelifiant", "gelifiants", "agent levant", "agents levants",
        "poudre a lever", "poudres a lever", "colorant", "colorants",
        // English
        "emulsifier", "emulsifiers", "stabilizer", "stabilizers",
        "acidity regulator", "acidity regulators", "antioxidant", "antioxidants",
        "preservative", "preservatives", "thickener", "thickeners",
        "gelling agent", "gelling agents", "raising agent", "raising agents", "colour", "color",
        // Dutch, German and Spanish
        "emulgator", "emulgatoren", "zuurteregelaar", "zuurteregelaars",
        "verdikkingsmiddel", "verdikkingsmiddelen", "backtriebmittel", "farbstoff",
        "emulsionante", "emulsionantes", "corrector de acidez", "correctores de acidez"
    )

    fun contains(value: String): Boolean = TextNormalizer.normalize(value) in classes
}
