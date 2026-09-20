package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Version0510Test {
    private val database = listOf(
        ingredient("water", "eau", aliases = arrayOf("water", "Wasser", "agua")),
        ingredient("sugar", "sucre", aliases = arrayOf("suiker", "sugar", "Zucker", "azúcar")),
        ingredient("salt", "sel", aliases = arrayOf("zout", "salt", "Salz", "sal")),
        ingredient(
            "milk", "lait", VeganStatus.VEGETARIAN,
            aliases = arrayOf("milk", "melk", "Milch", "leche")
        ),
        ingredient("silicon_dioxide", "dioxyde de silicium"),
        ingredient("sunflower", "tournesol", aliases = arrayOf("sunflower", "zonnebloem", "Sonnenblume", "girasol")),
        ingredient("rapeseed", "colza", aliases = arrayOf("rapeseed", "raapzaad", "Raps", "colza"))
    )

    @Test fun squareAndRoundNanoQualifiersAreMetadataBeforeGroupParsing() {
        listOf(
            "dioxyde de silicium [nano], sel" to "[nano]",
            "dioxyde de silicium [ nano ], sel" to "[ nano ]",
            "dioxyde de silicium (nano), sel" to "(nano)",
            "dioxyde de silicium ( NaNo ), sel" to "( NaNo )"
        ).forEach { (text, syntax) ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(text, database)
            val silicon = diagnostics.ingredientTree.single { it.rawText == "dioxyde de silicium" }
            assertEquals(text, IngredientNodeKind.LEAF, silicon.kind)
            assertTrue(text, silicon.isNano)
            assertEquals(text, syntax, silicon.nanoText)
            assertTrue(text, silicon.children.isEmpty())
            assertFalse(text, diagnostics.result.unknown.any { it.contains("nano", true) })
            assertTrue(text, DiagnosticReport.build(diagnostics, "0.5.10").contains(syntax))
        }

        val sauce = IngredientTreeParser.parse("sauce [eau, sel]").single()
        assertEquals(IngredientNodeKind.COMPOSITE, sauce.kind)
        assertEquals(listOf("eau", "sel"), sauce.children.map { it.rawText })
        assertFalse(sauce.isNano)
    }

    @Test fun boundedTitlesWithoutColonsWorkInEverySupportedLanguage() {
        mapOf(
            "INGRÉDIENTS\neau, sucre, sel" to LabelLanguage.FRENCH,
            "INGREDIËNTEN\nwater, suiker, zout" to LabelLanguage.DUTCH,
            "INGREDIENTS\nwater, sugar, salt" to LabelLanguage.ENGLISH,
            "ZUTATEN\nWasser, Zucker, Salz" to LabelLanguage.GERMAN,
            "INGREDIENTES\nagua, azúcar, sal" to LabelLanguage.SPANISH
        ).forEach { (label, language) ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(label, database, InputMode.FULL_LABEL)
            assertEquals(label, AnalysisAvailability.INGREDIENT_LIST_ANALYZED, diagnostics.result.availability)
            assertEquals(label, language, diagnostics.labelSections.language)
            assertEquals(label, HeadingSeparator.LINE_BREAK, diagnostics.labelSections.ingredientHeadingSeparator)
            assertEquals(label, 3, diagnostics.result.matched.size)
        }

        val extended = VeganAnalyzer.analyzeWithDiagnostics(
            "Ingrédients du bouillon déshydraté\nsel, amidon, sucre",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(AnalysisAvailability.INGREDIENT_LIST_ANALYZED, extended.result.availability)
        assertEquals("Ingrédients du bouillon déshydraté", extended.labelSections.ingredientHeadingText)

        val dashed = LabelSectionExtractor.extract(
            LabelLanguage.FRENCH,
            "INGRÉDIENTS — eau, sucre, sel"
        )
        assertEquals(HeadingSeparator.DASH, dashed.ingredientHeadingSeparator)
        assertEquals("eau, sucre, sel", dashed.ingredientsText)

        listOf(
            "Sans ingrédients artificiels",
            "Nos ingrédients sont soigneusement sélectionnés",
            "Découvrez les ingrédients de notre recette"
        ).forEach { commercial ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(commercial, database, InputMode.FULL_LABEL)
            assertEquals(commercial, AnalysisAvailability.NO_INGREDIENT_LIST, diagnostics.result.availability)
            assertFalse(commercial, diagnostics.labelSections.hasIngredientHeading)
        }
    }

    @Test fun declaredPresenceWithoutColonIsEvidenceButTracePhrasesNeverAre() {
        listOf(
            "Contient : LAIT", "Contient LAIT", "Contient du LAIT",
            "Contains: MILK", "Contains MILK", "Bevat: MELK", "Bevat MELK",
            "Enthält: MILCH", "Enthält MILCH", "Contiene: LECHE", "Contiene LECHE"
        ).forEach { label ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(label, database, InputMode.FULL_LABEL)
            assertEquals(label, AnalysisAvailability.NO_INGREDIENT_LIST, diagnostics.result.availability)
            assertEquals(label, listOf("milk"), diagnostics.result.declaredPresenceIngredientIds)
            assertEquals(label, VeganAssessment.NOT_VEGAN, diagnostics.result.veganAssessment)
            assertNotNull(label, diagnostics.labelSections.declaredContainsSyntax)
            assertNull(label, diagnostics.result.verdict)
        }

        listOf(
            "Peut contenir du LAIT", "Peut contenir : LAIT", "May contain MILK",
            "May contain traces of MILK", "Kan MELK bevatten", "Kan sporen van MELK bevatten",
            "Kann MILCH enthalten", "Kann Spuren von MILCH enthalten",
            "Puede contener LECHE", "Puede contener trazas de LECHE"
        ).forEach { label ->
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(label, database, InputMode.FULL_LABEL)
            assertEquals(label, AnalysisAvailability.NO_INGREDIENT_LIST, diagnostics.result.availability)
            assertTrue(label, diagnostics.result.declaredPresenceIngredientIds.isEmpty())
            assertTrue(label, diagnostics.result.matched.isEmpty())
            assertTrue(label, diagnostics.crossContactWarnings.any { it.contains(label, true) })
        }

        val unknown = VeganAnalyzer.analyzeWithDiagnostics(
            "Contient SUBSTANCE INCONNUE",
            database,
            InputMode.FULL_LABEL
        )
        assertTrue(unknown.result.declaredPresenceIngredientIds.isEmpty())
        assertTrue(unknown.tokens.single().unknown!!.contains("SUBSTANCE INCONNUE"))
    }

    @Test fun allTwentyFourFrenchFunctionalClassesHaveCanonicalIdentities() {
        frenchClasses.forEach { (text, canonical) ->
            val node = if (canonical == FunctionalClass.MODIFIED_STARCH) {
                IngredientTreeParser.parse(text).single()
            } else {
                IngredientTreeParser.parse("$text : E999").single()
            }
            assertEquals(text, canonical, node.functionalClassCanonical)
            if (canonical == FunctionalClass.MODIFIED_STARCH) {
                assertEquals(text, IngredientNodeKind.LEAF, node.kind)
                assertEquals(text, node.rawText)
            } else {
                assertEquals(text, IngredientNodeKind.ADDITIVE, node.kind)
                assertEquals("E999", node.rawText)
            }
        }
        assertEquals(24, frenchClasses.size)
    }

    @Test fun everyFunctionalClassHasDutchEnglishGermanAndSpanishTerms() {
        listOf(dutchClasses, englishClasses, germanClasses, spanishClasses).forEach { languageTerms ->
            assertEquals(24, languageTerms.size)
            languageTerms.forEach { (text, canonical) ->
                assertEquals(text, canonical, FunctionalClassLexicon.match(text)?.canonical)
            }
        }
    }

    @Test fun functionalClassSyntaxesAndMultipleDesignationsStayStructural() {
        mapOf(
            "émulsifiant : E471" to FunctionalClass.EMULSIFIER,
            "émulsifiant E471" to FunctionalClass.EMULSIFIER,
            "émulsifiant (E471)" to FunctionalClass.EMULSIFIER,
            "raising agent E500" to FunctionalClass.RAISING_AGENT,
            "colour E160a" to FunctionalClass.COLOUR,
            "Stabilisator: E407" to FunctionalClass.STABILISER,
            "corrector de acidez: ácido cítrico" to FunctionalClass.ACIDITY_REGULATOR,
            "antiagglomérant : dioxyde de silicium [nano]" to FunctionalClass.ANTI_CAKING_AGENT
        ).forEach { (text, canonical) ->
            val node = IngredientTreeParser.parse(text).single()
            assertEquals(text, canonical, node.functionalClassCanonical)
            assertEquals(text, IngredientNodeKind.ADDITIVE, node.kind)
        }

        val sweeteners = IngredientTreeParser.parse("édulcorants : E950, E955").single()
        assertEquals(IngredientNodeKind.COMPOSITE, sweeteners.kind)
        assertEquals(listOf("E950", "E955"), sweeteners.children.map { it.rawText })
        assertTrue(sweeteners.children.all { it.kind == IngredientNodeKind.ADDITIVE })
        assertFalse(sweeteners.children.any { it.rawText.contains("édulcorant", true) })

        val emulsifiers = IngredientTreeParser.parse("émulsifiants : lécithines, E471").single()
        assertEquals(listOf("lécithines", "E471"), emulsifiers.children.map { it.rawText })
        assertTrue(emulsifiers.children.all { it.kind == IngredientNodeKind.ADDITIVE })

        val report = DiagnosticReport.build(
            VeganAnalyzer.analyzeWithDiagnostics("émulsifiant E471", emptyList()),
            "0.5.10"
        )
        assertTrue(report.contains("classe fonctionnelle originale=émulsifiant"))
        assertTrue(report.contains("classe fonctionnelle canonique=EMULSIFIER"))

        listOf(
            "amidon modifié", "modified starch", "gemodificeerd zetmeel",
            "modifizierte Stärke", "almidón modificado"
        ).forEach { text ->
            val node = IngredientTreeParser.parse(text).single()
            assertEquals(text, IngredientNodeKind.LEAF, node.kind)
            assertEquals(text, text, node.rawText)
            assertEquals(text, FunctionalClass.MODIFIED_STARCH, node.functionalClassCanonical)
        }
    }

    @Test fun multilingualSelectionPrefersRealIngredientListsBeforeLanguage() {
        val dutch = VeganAnalyzer.analyzeWithDiagnostics(
            "FR — Délicieuse préparation familiale.\n\nNL — INGREDIËNTEN\nwater, suiker, zout.",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(LabelLanguage.DUTCH, dutch.labelSections.language)
        assertEquals(setOf("water", "sugar", "salt"), dutch.result.matched.map { it.id }.toSet())
        assertTrue(dutch.languageSegmentation.rejectedUntitledLanguages.contains(LabelLanguage.FRENCH))

        val french = VeganAnalyzer.analyzeWithDiagnostics(
            "NL — Heerlijke familiale bereiding.\n\nFR — INGRÉDIENTS\neau, sucre, sel.",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(LabelLanguage.FRENCH, french.labelSections.language)

        val both = VeganAnalyzer.analyzeWithDiagnostics(
            "NL — INGREDIËNTEN\nwater, suiker, zout.\nFR — INGRÉDIENTS\neau, sucre, sel.",
            database,
            InputMode.FULL_LABEL
        )
        assertEquals(LabelLanguage.FRENCH, both.labelSections.language)
    }

    @Test fun variableProportionsAndExplicitAlternativesAreMultilingualMetadata() {
        listOf(
            "en proportion variable", "en proportions variables", "in varying proportions",
            "in variable proportions", "in wisselende verhoudingen",
            "in veränderlichen Gewichtsanteilen", "en proporción variable",
            "en proporciones variables"
        ).forEach { phrase ->
            val node = IngredientTreeParser.parse("mélange (tournesol, colza) $phrase").single()
            assertTrue(phrase, node.variableProportions)
            assertEquals(phrase, phrase, node.variableProportionsText)
            assertNull(phrase, node.quantityPercent)
            assertEquals(phrase, listOf("tournesol", "colza"), node.children.map { it.rawText })
            assertFalse(phrase, node.rawText.contains("proportion", true))
            assertFalse(phrase, node.rawText.contains("verhouding", true))
            assertFalse(phrase, node.rawText.contains("Gewichtsanteilen", true))
            val diagnostics = VeganAnalyzer.analyzeWithDiagnostics(
                "mélange (tournesol, colza) $phrase",
                database
            )
            assertTrue(phrase, diagnostics.result.unknown.isEmpty())
        }

        mapOf(
            "et/ou" to "tournesol et/ou colza",
            "and/or" to "sunflower and/or rapeseed",
            "en/of" to "zonnebloem en/of raapzaad",
            "und/oder" to "Sonnenblume und/oder Raps",
            "y/o" to "girasol y/o colza"
        ).forEach { (syntax, alternatives) ->
            val node = IngredientTreeParser.parse("mélange ($alternatives)").single()
            assertTrue(syntax, node.hasAlternatives)
            assertEquals(syntax, syntax, node.alternativesText)
            assertEquals(syntax, 2, node.children.size)
        }
    }

    private val frenchClasses = linkedMapOf(
        "acidifiant" to FunctionalClass.ACID,
        "correcteur d’acidité" to FunctionalClass.ACIDITY_REGULATOR,
        "antiagglomérant" to FunctionalClass.ANTI_CAKING_AGENT,
        "antimoussant" to FunctionalClass.ANTI_FOAMING_AGENT,
        "antioxydant" to FunctionalClass.ANTIOXIDANT,
        "agent de charge" to FunctionalClass.BULKING_AGENT,
        "colorant" to FunctionalClass.COLOUR,
        "émulsifiant" to FunctionalClass.EMULSIFIER,
        "sels de fonte" to FunctionalClass.EMULSIFYING_SALTS,
        "affermissant" to FunctionalClass.FIRMING_AGENT,
        "exhausteur de goût" to FunctionalClass.FLAVOUR_ENHANCER,
        "agent de traitement de la farine" to FunctionalClass.FLOUR_TREATMENT_AGENT,
        "agent moussant" to FunctionalClass.FOAMING_AGENT,
        "gélifiant" to FunctionalClass.GELLING_AGENT,
        "agent d’enrobage" to FunctionalClass.GLAZING_AGENT,
        "humectant" to FunctionalClass.HUMECTANT,
        "amidon modifié" to FunctionalClass.MODIFIED_STARCH,
        "conservateur" to FunctionalClass.PRESERVATIVE,
        "gaz propulseur" to FunctionalClass.PROPELLANT_GAS,
        "poudre à lever" to FunctionalClass.RAISING_AGENT,
        "séquestrant" to FunctionalClass.SEQUESTRANT,
        "stabilisant" to FunctionalClass.STABILISER,
        "édulcorant" to FunctionalClass.SWEETENER,
        "épaississant" to FunctionalClass.THICKENER
    )

    private val dutchClasses = terms(
        "voedingszuur", "zuurteregelaar", "antiklontermiddel", "antischuimmiddel", "antioxidant",
        "vulstof", "kleurstof", "emulgator", "smeltzout", "verstevigingsmiddel", "smaakversterker",
        "meelverbeteraar", "schuimmiddel", "geleermiddel", "glansmiddel", "bevochtigingsmiddel",
        "gemodificeerd zetmeel", "conserveermiddel", "drijfgas", "rijsmiddel", "complexvormer",
        "stabilisator", "zoetstof", "verdikkingsmiddel"
    )
    private val englishClasses = terms(
        "acid", "acidity regulator", "anti-caking agent", "anti-foaming agent", "antioxidant",
        "bulking agent", "colour", "emulsifier", "emulsifying salts", "firming agent", "flavour enhancer",
        "flour treatment agent", "foaming agent", "gelling agent", "glazing agent", "humectant",
        "modified starch", "preservative", "propellant gas", "raising agent", "sequestrant",
        "stabiliser", "sweetener", "thickener"
    )
    private val germanClasses = terms(
        "Säuerungsmittel", "Säureregulator", "Trennmittel", "Schaumverhüter", "Antioxidationsmittel",
        "Füllstoff", "Farbstoff", "Emulgator", "Schmelzsalz", "Festigungsmittel", "Geschmacksverstärker",
        "Mehlbehandlungsmittel", "Schaummittel", "Geliermittel", "Überzugsmittel", "Feuchthaltemittel",
        "modifizierte Stärke", "Konservierungsstoff", "Treibgas", "Backtriebmittel", "Komplexbildner",
        "Stabilisator", "Süßungsmittel", "Verdickungsmittel"
    )
    private val spanishClasses = terms(
        "acidulante", "corrector de acidez", "antiaglomerante", "antiespumante", "antioxidante",
        "agente de carga", "colorante", "emulgente", "sales de fundido", "endurecedor",
        "potenciador del sabor", "agente de tratamiento de la harina", "espumante", "gelificante",
        "agente de recubrimiento", "humectante", "almidón modificado", "conservador", "gas propulsor",
        "gasificante", "secuestrante", "estabilizador", "edulcorante", "espesante"
    )

    private fun terms(vararg values: String): Map<String, FunctionalClass> =
        values.zip(FunctionalClass.entries).toMap()

    private fun ingredient(
        id: String,
        alias: String,
        status: VeganStatus = VeganStatus.VEGAN,
        aliases: Array<String> = emptyArray()
    ) = Ingredient(id, alias, listOf(alias) + aliases, null, status, "Test")
}
