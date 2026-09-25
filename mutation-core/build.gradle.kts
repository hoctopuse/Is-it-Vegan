plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    id("info.solidsoft.pitest") version "1.19.0"
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    testImplementation(libs.junit)
}

pitest {
    targetClasses.set(setOf(
        "com.example.isitvegan.VerdictEngine",
        "com.example.isitvegan.IngredientMatcher",
        "com.example.isitvegan.IngredientTreeParser",
        "com.example.isitvegan.FunctionalClassLexicon",
        "com.example.isitvegan.OriginQualifierRuleSet",
        "com.example.isitvegan.TextNormalizer",
        "com.example.isitvegan.LabelSectionExtractor",
        "com.example.isitvegan.LabelLexicon",
        "com.example.isitvegan.LabelLanguageSegmenter",
        "com.example.isitvegan.OcrBlockLanguageClassifier"
    ))
    targetTests.set(setOf("com.example.isitvegan.*"))
    outputFormats.set(setOf("HTML", "XML"))
    timestampedReports.set(false)
    threads.set(2)
}
