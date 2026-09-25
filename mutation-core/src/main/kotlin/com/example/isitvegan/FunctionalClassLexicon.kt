package com.example.isitvegan

enum class FunctionalClass {
    ACID, ACIDITY_REGULATOR, ANTI_CAKING_AGENT, ANTI_FOAMING_AGENT, ANTIOXIDANT,
    BULKING_AGENT, COLOUR, EMULSIFIER, EMULSIFYING_SALTS, FIRMING_AGENT,
    FLAVOUR_ENHANCER, FLOUR_TREATMENT_AGENT, FOAMING_AGENT, GELLING_AGENT,
    GLAZING_AGENT, HUMECTANT, MODIFIED_STARCH, PRESERVATIVE, PROPELLANT_GAS,
    RAISING_AGENT, SEQUESTRANT, STABILISER, SWEETENER, THICKENER
}

data class FunctionalClassMatch(
    val canonical: FunctionalClass,
    val originalText: String,
    val designation: String = ""
)

/** Regulatory functional classes. They provide structure and never a vegan status. */
object FunctionalClassLexicon {
    private data class Entry(val canonical: FunctionalClass, val aliases: Set<String>)

    private fun entry(canonical: FunctionalClass, vararg aliases: String) = Entry(
        canonical,
        aliases.mapTo(linkedSetOf(), TextNormalizer::normalize)
    )

    private val entries = listOf(
        entry(FunctionalClass.ACID,
            "acidifiant", "acidifiants", "voedingszuur", "voedingszuren", "acid", "acids",
            "Säuerungsmittel", "acidulante", "acidulantes"),
        entry(FunctionalClass.ACIDITY_REGULATOR,
            "correcteur d’acidité", "correcteurs d’acidité", "régulateur d’acidité", "régulateurs d’acidité",
            "zuurteregelaar", "zuurteregelaars", "acidity regulator", "acidity regulators",
            "Säureregulator", "Säureregulatoren", "corrector de acidez", "correctores de acidez"),
        entry(FunctionalClass.ANTI_CAKING_AGENT,
            "antiagglomérant", "antiagglomérants", "antiklontermiddel", "antiklontermiddelen",
            "anti-caking agent", "anti-caking agents", "Trennmittel", "antiaglomerante", "antiaglomerantes"),
        entry(FunctionalClass.ANTI_FOAMING_AGENT,
            "antimoussant", "antimoussants", "antischuimmiddel", "antischuimmiddelen",
            "anti-foaming agent", "anti-foaming agents", "Schaumverhüter", "antiespumante", "antiespumantes"),
        entry(FunctionalClass.ANTIOXIDANT,
            "antioxydant", "antioxydants", "antioxidant", "antioxidanten", "antioxidants",
            "Antioxidationsmittel", "antioxidante", "antioxidantes"),
        entry(FunctionalClass.BULKING_AGENT,
            "agent de charge", "agents de charge", "vulstof", "vulstoffen", "bulking agent", "bulking agents",
            "Füllstoff", "Füllstoffe", "agente de carga", "agentes de carga"),
        entry(FunctionalClass.COLOUR,
            "colorant", "colorants", "kleurstof", "kleurstoffen", "colour", "colours", "color", "colors",
            "Farbstoff", "Farbstoffe", "colorante", "colorantes"),
        entry(FunctionalClass.EMULSIFIER,
            "émulsifiant", "émulsifiants", "emulgator", "emulgatoren", "emulsifier", "emulsifiers",
            "Emulgator", "Emulgatoren", "emulgente", "emulgentes", "emulsionante", "emulsionantes"),
        entry(FunctionalClass.EMULSIFYING_SALTS,
            "sel de fonte", "sels de fonte", "smeltzout", "smeltzouten", "emulsifying salt", "emulsifying salts",
            "Schmelzsalz", "Schmelzsalze", "sal de fundido", "sales de fundido"),
        entry(FunctionalClass.FIRMING_AGENT,
            "affermissant", "affermissants", "verstevigingsmiddel", "verstevigingsmiddelen",
            "firming agent", "firming agents", "Festigungsmittel", "endurecedor", "endurecedores"),
        entry(FunctionalClass.FLAVOUR_ENHANCER,
            "exhausteur de goût", "exhausteurs de goût", "smaakversterker", "smaakversterkers",
            "flavour enhancer", "flavour enhancers", "flavor enhancer", "flavor enhancers",
            "Geschmacksverstärker", "potenciador del sabor", "potenciadores del sabor"),
        entry(FunctionalClass.FLOUR_TREATMENT_AGENT,
            "agent de traitement de la farine", "agents de traitement de la farine",
            "meelverbeteraar", "meelverbeteraars", "flour treatment agent", "flour treatment agents",
            "Mehlbehandlungsmittel", "agente de tratamiento de la harina", "agentes de tratamiento de la harina"),
        entry(FunctionalClass.FOAMING_AGENT,
            "agent moussant", "agents moussants", "schuimmiddel", "schuimmiddelen",
            "foaming agent", "foaming agents", "Schaummittel", "espumante", "espumantes"),
        entry(FunctionalClass.GELLING_AGENT,
            "gélifiant", "gélifiants", "geleermiddel", "geleermiddelen", "gelling agent", "gelling agents",
            "Geliermittel", "gelificante", "gelificantes"),
        entry(FunctionalClass.GLAZING_AGENT,
            "agent d’enrobage", "agents d’enrobage", "glansmiddel", "glansmiddelen",
            "glazing agent", "glazing agents", "Überzugsmittel", "agente de recubrimiento", "agentes de recubrimiento"),
        entry(FunctionalClass.HUMECTANT,
            "humectant", "humectants", "bevochtigingsmiddel", "bevochtigingsmiddelen",
            "humectant", "humectants", "Feuchthaltemittel", "humectante", "humectantes"),
        entry(FunctionalClass.MODIFIED_STARCH,
            "amidon modifié", "amidons modifiés", "gemodificeerd zetmeel", "gemodificeerde zetmelen",
            "modified starch", "modified starches", "modifizierte Stärke", "modifizierte Stärken",
            "almidón modificado", "almidones modificados"),
        entry(FunctionalClass.PRESERVATIVE,
            "conservateur", "conservateurs", "conserveermiddel", "conserveermiddelen",
            "preservative", "preservatives", "Konservierungsstoff", "Konservierungsstoffe",
            "conservador", "conservadores"),
        entry(FunctionalClass.PROPELLANT_GAS,
            "gaz propulseur", "gaz propulseurs", "drijfgas", "drijfgassen", "propellant gas", "propellant gases",
            "Treibgas", "Treibgase", "gas propulsor", "gases propulsores"),
        entry(FunctionalClass.RAISING_AGENT,
            "poudre à lever", "poudres à lever", "agent levant", "agents levants",
            "rijsmiddel", "rijsmiddelen", "raising agent", "raising agents",
            "Backtriebmittel", "gasificante", "gasificantes"),
        entry(FunctionalClass.SEQUESTRANT,
            "séquestrant", "séquestrants", "complexvormer", "complexvormers", "sequestrant", "sequestrants",
            "Komplexbildner", "secuestrante", "secuestrantes"),
        entry(FunctionalClass.STABILISER,
            "stabilisant", "stabilisants", "stabilisator", "stabilisatoren",
            "stabiliser", "stabilisers", "stabilizer", "stabilizers", "Stabilisator", "Stabilisatoren",
            "estabilizador", "estabilizadores"),
        entry(FunctionalClass.SWEETENER,
            "édulcorant", "édulcorants", "zoetstof", "zoetstoffen", "sweetener", "sweeteners",
            "Süßungsmittel", "edulcorante", "edulcorantes"),
        entry(FunctionalClass.THICKENER,
            "épaississant", "épaississants", "verdikkingsmiddel", "verdikkingsmiddelen",
            "thickener", "thickeners", "Verdickungsmittel", "espesante", "espesantes")
    )

    private val aliasIndex = entries.flatMap { entry ->
        entry.aliases.map { alias -> alias to entry.canonical }
    }.toMap()

    fun match(value: String): FunctionalClassMatch? = aliasIndex[TextNormalizer.normalize(value)]?.let {
        FunctionalClassMatch(it, value.trim())
    }

    fun matchPrefix(value: String): FunctionalClassMatch? {
        val trimmed = value.trim()
        val normalized = TextNormalizer.normalize(trimmed)
        val match = aliasIndex.entries
            .filter { (alias, _) -> normalized == alias || normalized.startsWith("$alias ") }
            .maxByOrNull { it.key.length } ?: return null
        val classEnd = (1..trimmed.length).lastOrNull { end ->
            TextNormalizer.normalize(trimmed.substring(0, end)) == match.key
        } ?: return null
        val originalClass = trimmed.substring(0, classEnd).trim()
        val designation = trimmed.substring(classEnd).trim()
        return FunctionalClassMatch(match.value, originalClass, designation)
    }

    fun contains(value: String): Boolean = match(value) != null
}
