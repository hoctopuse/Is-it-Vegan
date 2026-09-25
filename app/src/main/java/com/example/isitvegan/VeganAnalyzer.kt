package com.example.isitvegan

/** Android-facing facade. All analysis behavior lives in the JVM core service. */
object VeganAnalyzer {
    @Volatile
    private var service: IngredientAnalysisService? = null

    fun isDatabaseLoaded(): Boolean = service != null

    @Synchronized
    fun loadDatabase(context: android.content.Context) {
        if (service != null) return
        service = IngredientAnalysisService(AndroidIngredientKnowledgeLoader.load(context))
    }

    fun analyze(
        text: String,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST
    ): AnalysisResult = requireService().analyze(text, inputMode)

    fun analyzeWithDiagnostics(
        text: String,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST,
        preferredLanguage: UiLanguage = UiLanguage.FR,
        selectedBlockId: String? = null
    ): AnalysisDiagnostics = requireService().analyzeWithDiagnostics(
        text, inputMode, preferredLanguage, selectedBlockId
    )

    internal fun analyze(
        text: String,
        database: List<Ingredient>,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST,
        rules: OriginQualifierRuleSet = OriginQualifierRuleSet.empty(),
        ruleErrors: List<String> = emptyList(),
        multilingualLexicon: MultilingualIngredientLexicon = MultilingualIngredientLexicon.empty()
    ): AnalysisResult = IngredientAnalysisService(
        IngredientKnowledge(database, rules, ruleErrors, multilingualLexicon)
    ).analyze(text, inputMode)

    internal fun analyzeWithDiagnostics(
        text: String,
        database: List<Ingredient>,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST,
        rules: OriginQualifierRuleSet = OriginQualifierRuleSet.empty(),
        ruleErrors: List<String> = emptyList(),
        preferredLanguage: UiLanguage = UiLanguage.FR,
        selectedBlockId: String? = null,
        multilingualLexicon: MultilingualIngredientLexicon = MultilingualIngredientLexicon.empty()
    ): AnalysisDiagnostics = IngredientAnalysisService(
        IngredientKnowledge(database, rules, ruleErrors, multilingualLexicon)
    ).analyzeWithDiagnostics(text, inputMode, preferredLanguage, selectedBlockId)

    private fun requireService(): IngredientAnalysisService = checkNotNull(service) {
        "La base d’ingrédients doit être chargée avant l’analyse."
    }
}
