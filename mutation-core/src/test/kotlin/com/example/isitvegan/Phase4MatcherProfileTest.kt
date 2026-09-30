package com.example.isitvegan

import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.roundToLong
import org.junit.Assert.assertEquals
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
        val legacyBases = aliases.filter { alias ->
            aliases.any { longer ->
                longer.length > alias.length &&
                    longer.startsWith("$alias ") &&
                    Regex("^(?:de|d|du|des|a|au)(?:\\s|$).*")
                        .matches(longer.removePrefix("$alias "))
            }
        }.toSet()

        assertEquals(legacyBases, IngredientMatcher.longerLinkedAliasBases(aliases))
    }

    private fun corpus() = listOf(
        Case("water", "eau"), Case("sugar", "sucre"), Case("milk", "lait"),
        Case("gelatin", "gélatine"), Case("honey", "miel"), Case("coffee", "café"),
        Case("e471", "E471"), Case("e471-spaced", "E 471"), Case("e471-bare", "471"),
        Case("flavour", "arôme chocolat"), Case("flavour-de", "arôme de chocolat"),
        Case("extract", "extrait de café"), Case("taste", "goût fraise"),
        Case("plant-milk", "lait végétal"), Case("german", "Schokolade"),
        Case("collision-e470b", "E470b"), Case("collision-e572", "E572"),
        Case("unknown", "ingrédient inconnu"),
        Case("nested-child", "chocolat", depth = 2), Case("repeat-one", "eau"), Case("repeat-two", "eau")
    )

    private fun longText() = listOf(
        "eau", "sucre", "sel", "farine de blé", "lait végétal", "arôme chocolat",
        "extrait de café", "E471", "E470b", "E572", "chocolade", "Schokolade",
        "ingredient totalement inconnu", "miel", "gélatine", "fraise", "café"
    ).joinToString(", ")

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
