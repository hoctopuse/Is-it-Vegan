package com.example.isitvegan

/** Test-only adapter preserving the old test call shape while exercising the core service. */
object CoreTestAnalyzer {
    fun analyze(
        text: String,
        database: List<Ingredient>,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST,
        rules: OriginQualifierRuleSet = OriginQualifierRuleSet.empty(),
        ruleErrors: List<String> = emptyList(),
        multilingualLexicon: MultilingualIngredientLexicon = MultilingualIngredientLexicon.empty()
    ): AnalysisResult = service(database, rules, ruleErrors, multilingualLexicon)
        .analyze(text, inputMode)

    fun analyzeWithDiagnostics(
        text: String,
        database: List<Ingredient>,
        inputMode: InputMode = InputMode.MANUAL_INGREDIENT_LIST,
        rules: OriginQualifierRuleSet = OriginQualifierRuleSet.empty(),
        ruleErrors: List<String> = emptyList(),
        preferredLanguage: UiLanguage = UiLanguage.FR,
        selectedBlockId: String? = null,
        multilingualLexicon: MultilingualIngredientLexicon = MultilingualIngredientLexicon.empty()
    ): AnalysisDiagnostics = service(database, rules, ruleErrors, multilingualLexicon)
        .analyzeWithDiagnostics(text, inputMode, preferredLanguage, selectedBlockId)

    private fun service(
        database: List<Ingredient>,
        rules: OriginQualifierRuleSet,
        ruleErrors: List<String>,
        multilingualLexicon: MultilingualIngredientLexicon
    ) = IngredientAnalysisService(
        IngredientKnowledge(database, rules, ruleErrors, multilingualLexicon)
    )
}
