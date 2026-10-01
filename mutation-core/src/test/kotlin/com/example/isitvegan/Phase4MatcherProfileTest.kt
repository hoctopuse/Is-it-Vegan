package com.example.isitvegan

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.roundToLong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase4MatcherProfileTest {
    private val knowledge by lazy { loadKnowledge() }

    @Test
    fun profilesMatcherStagesAndKeepsInstrumentedResultsIdentical() {
        val profiled = MatcherProfileCollector()
        val matcher = IngredientMatcher(knowledge.ingredients, profiled)
        val reference = IngredientMatcher(knowledge.ingredients)
        val corpus = corpus()

        corpus.forEachIndexed { order, case ->
            val token = IngredientToken(case.text, depth = case.depth, order = order)
            assertEquals(case.name, reference.match(token), matcher.match(token))
        }

        val construction = measure { IngredientMatcher(knowledge.ingredients, MatcherProfileCollector()) }
        val simple = measure { matcher.match(IngredientToken("eau", 0, 0)) }
        val nestedLeaf = measure { matcher.match(IngredientToken("arôme de chocolat", 1, 2)) }
        val multilingual = measure { matcher.match(IngredientToken("Schokolade", 0, 0)) }
        val traceAlias = measure { matcher.match(IngredientToken("lait", 0, 0)) }
        val longText = measure { matcher.match(IngredientToken(longText(), 0, 0)) }
        val noMatch = measure { matcher.match(IngredientToken("ingrédient entièrement inconnu sans correspondance", 0, 0)) }
        val service = IngredientAnalysisService(knowledge)
        val parsing = measure {
            IngredientTreeParser.parse(
                "parent (enfant (eau, chocolat), lait) 12,5 %", knowledge.originRules
            )
        }
        val preprocessing = measure {
            LabelPreprocessor.preprocess("Ingrédients : eau, sucre. Peut contenir : lait, œuf.")
        }
        val lexical = measure {
            knowledge.multilingualLexicon.resolve("Schokolade", LabelLanguage.GERMAN, knowledge.ingredients)
        }
        val originRules = measure {
            knowledge.originRules.extractAttached("gélatine (origine bovine)")
        }
        val analysisSimple = measure {
            service.analyzeWithDiagnostics("eau, sucre", InputMode.MANUAL_INGREDIENT_LIST)
        }
        val analysisNested = measure {
            service.analyzeWithDiagnostics(
                "parent (enfant (eau, chocolat), lait) 12,5 %", InputMode.MANUAL_INGREDIENT_LIST
            )
        }
        val analysisMultilingual = measure {
            service.analyzeWithDiagnostics(
                "Ingredients: water, chocolate flavour, coffee extract", InputMode.FULL_LABEL
            )
        }
        val analysisTrace = measure {
            service.analyzeWithDiagnostics(
                "Ingrédients : eau, sucre. Peut contenir : lait, œuf.", InputMode.FULL_LABEL
            )
        }
        val analysisLong = measure {
            service.analyzeWithDiagnostics(longText(), InputMode.MANUAL_INGREDIENT_LIST)
        }
        val analysisUnknown = measure {
            service.analyzeWithDiagnostics(
                "ingrédient entièrement inconnu sans correspondance", InputMode.MANUAL_INGREDIENT_LIST
            )
        }
        val diagnostics = measure {
            service.analyzeWithDiagnostics(
                "sauce (eau, lait, ingrédient inconnu). Peut contenir : œuf.", InputMode.FULL_LABEL
            ).decision
        }
        val verdict = measure {
            service.analyze("eau, sucre", InputMode.MANUAL_INGREDIENT_LIST).verdict
        }

        val profile = profiled.snapshot()
        assertTrue(profile.aliasEntries > 0)
        assertTrue(profile.regexTests >= profile.aliasEntries.toLong())
        assertTrue(profile.matchCalls >= corpus.size)
        println("PHASE4_MATCHER_PROFILE|aliases=${profile.aliasEntries}|normalizedAliases=${profile.normalizedAliasCount}|matchCalls=${profile.matchCalls}|regexTests=${profile.regexTests}|candidates=${profile.candidatesFound}|blocked=${profile.candidatesBlocked}|selected=${profile.candidatesSelected}")
        println("PHASE4_MATCHER_STAGES|construction=${micros(profile.matcherConstructionNanos)}us|normalizationPreparation=${micros(profile.normalizationPreparationNanos)}us|aliasPreparation=${micros(profile.aliasPreparationNanos)}us|regexPreparation=${micros(profile.regexPreparationNanos)}us|linkedAliasChecks=${micros(profile.longerLinkedAliasCheckNanos)}us|matchNormalization=${micros(profile.matchNormalizationNanos)}us|candidateSearch=${micros(profile.candidateSearchNanos)}us|contextFiltering=${micros(profile.contextFilteringNanos)}us|selectionResolution=${micros(profile.selectionAndResolutionNanos)}us")
        listOf(
            "construct-matcher-cold" to construction,
            "match-exact" to simple,
            "match-protected-context" to nestedLeaf,
            "match-multilingual-form" to multilingual,
            "match-trace-alias" to traceAlias,
            "match-long-many-aliases" to longText,
            "match-no-correspondence" to noMatch,
            "preprocess" to preprocessing,
            "parse-nested" to parsing,
            "lexical-resolution" to lexical,
            "origin-rule-extraction" to originRules,
            "analysis-simple" to analysisSimple,
            "analysis-nested" to analysisNested,
            "analysis-multilingual" to analysisMultilingual,
            "analysis-traces" to analysisTrace,
            "analysis-long" to analysisLong,
            "analysis-no-correspondence" to analysisUnknown,
            "diagnostics" to diagnostics,
            "verdict" to verdict
        ).forEach { (name, timing) -> println("PHASE4_TIMING|$name|${timing.render()}") }
    }

    @Test
    fun linkedAliasPrecomputationIsEquivalentToTheFormerPairwisePredicate() {
        val aliases = knowledge.ingredients.flatMap { ingredient ->
            listOf(ingredient.name) + ingredient.aliases + listOfNotNull(ingredient.eNumber)
        }.map(TextNormalizer::normalize).filter(String::isNotBlank).distinct()
        assertEquals(2485, aliases.size)
        val legacyBases = historicalLinkedAliasBases(aliases)

        assertEquals(legacyBases, longerLinkedAliasBases(aliases))

        val legacyTiming = measure { historicalLinkedAliasBases(aliases) }
        val optimizedTiming = measure { longerLinkedAliasBases(aliases) }
        println("PHASE4_LINKED_ALIAS_BASELINE|historical-pairwise|${legacyTiming.render()}")
        println("PHASE4_LINKED_ALIAS_AFTER|prepared-prefixes|${optimizedTiming.render()}")
    }

    @Test
    fun linkedAliasPrecomputationKeepsHistoricalConnectorsAndRejectsFalsePrefixes() {
        val historicalSuffix = Regex("^(?:de|d|du|des|a|au)(?:\\s|$).*")
        val aliases = listOf(
            "huile", "huile de colza", "beurre", "beurre de cacao", "lait", "lait en poudre",
            "arome", "arome naturel", "extrait", "extrait de vanille", "gout", "gout au citron",
            "milk", "milk of oat", "melk", "melk van haver", "milch", "milch aus hafer",
            "cacao", "cacaoter", "huile colza", "lait vegetal", "arome chocolat", "gout citron"
        )
        val legacyBases = historicalLinkedAliasBases(aliases, historicalSuffix)

        assertEquals(legacyBases, longerLinkedAliasBases(aliases))
        assertEquals(
            setOf("huile", "beurre", "extrait", "gout"),
            longerLinkedAliasBases(aliases)
        )
        assertTrue("false prefixes must remain absent", "cacao" !in longerLinkedAliasBases(aliases))
        assertTrue("non-linked suffixes must remain absent", "lait" !in longerLinkedAliasBases(aliases))
        assertTrue("historical English preposition must remain absent", "milk" !in longerLinkedAliasBases(aliases))
    }

    @Test
    fun everyHistoricalLinkedConnectorHasPositiveBoundaryAndNegativePrefixCases() {
        data class ConnectorCase(val connector: String, val suffix: String)
        val cases = listOf(
            ConnectorCase("de", "colza"), ConnectorCase("d", "olive"),
            ConnectorCase("du", "cacao"), ConnectorCase("des", "amandes"),
            ConnectorCase("a", "la vanille"), ConnectorCase("au", "citron")
        )
        val historicalSuffix = Regex("^(?:de|d|du|des|a|au)(?:\\s|$).*")

        cases.forEach { (connector, suffix) ->
            val short = "base-$connector"
            val positive = "$short $connector $suffix"
            val invalidSuffix = "$short naturel"
            val falsePrefix = "base-$connector-plus $connector $suffix"
            val normalized = listOf(short, positive, invalidSuffix, falsePrefix)

            assertTrue("$connector positive", short in historicalLinkedAliasBases(normalized, historicalSuffix))
            assertTrue("$connector optimized positive", short in longerLinkedAliasBases(normalized))
            assertTrue("$connector false prefix", "base-$connector-plus" !in longerLinkedAliasBases(normalized))
            assertTrue("$connector invalid suffix", "base-$connector" !in longerLinkedAliasBases(listOf(short, invalidSuffix)))
        }
    }

    @Test
    fun normalizationKeepsCaseSpacesNumbersPunctuationAndProtectedContextsOutsideThePredicate() {
        val normalized = listOf(
            "E471", "e 471", "e-471", "(E471)", "arôme chocolat", "goût chocolat",
            "extrait de café", "lait végétal", "CHOCOLAT", "chocolat  de cacao"
        ).map(TextNormalizer::normalize).filter(String::isNotBlank).distinct()

        assertTrue(normalized.contains("e471"))
        assertTrue(normalized.any { it.replace(" ", "") == "e471" })
        assertTrue(normalized.any { it.contains("arome chocolat") })
        assertTrue(normalized.any { it.contains("extrait de cafe") })
        assertEquals(
            historicalLinkedAliasBases(normalized),
            longerLinkedAliasBases(normalized)
        )
    }

    @Test
    fun linkedAliasConnectorsRequireTheirExactBoundaryAndARealShortAlias() {
        listOf("de", "d", "du", "des", "a", "au").forEach { connector ->
            assertTrue(
                "positive connector: $connector",
                "base" in longerLinkedAliasBases(listOf("base", "base $connector ingredient"))
            )
            assertTrue(
                "connector at end boundary: $connector",
                "base" in longerLinkedAliasBases(listOf("base", "base $connector"))
            )
            assertFalse(
                "invalid connector suffix: $connector",
                "base" in longerLinkedAliasBases(listOf("base", "base ${connector}x ingredient"))
            )
            assertFalse(
                "false prefix: $connector",
                "base" in longerLinkedAliasBases(listOf("base $connector ingredient"))
            )
            assertFalse(
                "short alias must be complete: $connector",
                "ba" in longerLinkedAliasBases(listOf("ba", "base $connector ingredient"))
            )
        }
    }

    @Test
    fun matcherParityCoversFrenchDutchEnglishGermanCaseAndNormalizedSpaces() {
        val matcher = IngredientMatcher(knowledge.ingredients)
        listOf(
            "eau" to "water",
            "melk" to "milk",
            "milk" to "milk",
            "Schokolade" to "chocolate",
            "  E 471  " to "e471"
        ).forEach { (text, expectedId) ->
            val result = matcher.match(IngredientToken(text, depth = 0, order = 0))
            assertTrue("$text should match $expectedId", result.ingredients.any { it.id == expectedId })
        }
    }

    private fun corpus() = listOf(
        Case("water", "eau"), Case("water-en", "water"), Case("water-nl", "water"),
        Case("sugar", "sucre"), Case("milk", "lait"),
        Case("gelatin", "gélatine"), Case("honey", "miel"), Case("coffee", "café"),
        Case("e471", "E471"), Case("e471-lower", "e471"), Case("e471-spaced", "E 471"),
        Case("e471-hyphen", "E-471"), Case("e471-parenthesized", "(E 471)"), Case("e471-bare", "471"),
        Case("flavour", "arôme chocolat"), Case("flavour-de", "arôme de chocolat"),
        Case("extract", "extrait de café"), Case("taste", "goût fraise"),
        Case("plant-milk", "lait végétal"), Case("dutch", "chocolade"),
        Case("english", "chocolate"), Case("german", "Schokolade"),
        Case("collision-e470b", "E470b"), Case("collision-e572", "E572"),
        Case("unknown", "ingrédient inconnu"),
        Case("nested-child", "chocolat", depth = 2), Case("repeat-one", "eau"), Case("repeat-two", "eau")
    )

    private fun longText() = listOf(
        "eau", "sucre", "sel", "farine de blé", "lait végétal", "arôme chocolat",
        "extrait de café", "E471", "E470b", "E572", "chocolade", "Schokolade",
        "ingredient totalement inconnu", "miel", "gélatine", "fraise", "café"
    ).joinToString(", ")

    private fun historicalLinkedAliasBases(
        aliases: Collection<String>,
        suffix: Regex = Regex("^(?:de|d|du|des|a|au)(?:\\s|$).*")
    ): Set<String> = aliases.filter { alias ->
        aliases.any { longer ->
            longer.length > alias.length && longer.startsWith("$alias ") &&
                suffix.matches(longer.removePrefix("$alias "))
        }
    }.toSet()

    private fun loadKnowledge(): IngredientKnowledge {
        val root = generateSequence(Path.of(System.getProperty("user.dir"))) { it.parent }
            .first { Files.exists(it.resolve("app/src/main/assets/ingredients.json")) }
        return IngredientKnowledge.fromJson(
            Files.readString(root.resolve("app/src/main/assets/ingredients.json")),
            Files.readString(root.resolve("app/src/main/assets/ingredient_aliases_multilingual.json")),
            Files.readString(root.resolve("app/src/main/assets/origin_qualifier_rules.json"))
        )
    }

    private fun measure(block: () -> Any?): Timing {
        repeat(5) { block() }
        val samples = (1..20).map {
            val started = System.nanoTime()
            block()
            System.nanoTime() - started
        }.sorted()
        return Timing(samples.first(), samples[samples.lastIndex / 2], samples.last())
    }

    private data class Case(val name: String, val text: String, val depth: Int = 0)
    private data class Timing(val minimum: Long, val median: Long, val maximum: Long) {
        fun render() = "min=${micros(minimum)}us|median=${micros(median)}us|max=${micros(maximum)}us|n=20|warmup=5"
    }
}

private fun micros(nanos: Long): Long = (nanos / 1_000.0).roundToLong()
