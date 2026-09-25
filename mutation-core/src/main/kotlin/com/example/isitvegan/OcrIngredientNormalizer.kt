package com.example.isitvegan

/** Reviewed OCR and language bridge used only for matching; raw OCR remains untouched. */
object OcrIngredientNormalizer {
    private val exactReplacements = mapOf(
        "powron" to "poivron", "olves nores" to "olives noires",
        "huile ove" to "huile d olive", "huie dove" to "huile d olive",
        "doutble oncentre" to "double concentre", "ognon" to "oignon", "toumesol" to "tournesol",
        "zucker" to "sucre", "weizenmehl" to "farine de ble", "palmfett" to "graisse de palme",
        "palmkern" to "palmiste", "kokosnuss" to "noix de coco", "rapsol" to "huile de colza",
        "weizenstarke" to "amidon de ble", "glukosesirup" to "sirop de glucose",
        "speisesalz" to "sel", "knoblauch" to "ail", "zwiebel" to "oignon", "essig" to "vinaigre",
        "ammoniumcarbonate" to "carbonate d ammonium", "natriumcarbonate" to "carbonate de sodium"
    )

    fun forMatching(value: String): String = exactReplacements[TextNormalizer.normalize(value)] ?: value
}
