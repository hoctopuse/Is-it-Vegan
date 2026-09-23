#!/usr/bin/env python3
"""Render the data-driven sections of the knowledge-base documentation."""

from __future__ import annotations

import json
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INGREDIENTS = ROOT / "knowledge" / "ingredients.json"
ALIASES = ROOT / "app" / "src" / "main" / "assets" / "ingredient_aliases_multilingual.json"
ORIGIN_RULES = ROOT / "knowledge" / "origin_qualifier_rules.json"
OUTPUT = ROOT / "docs" / "generated" / "base-connaissances-data.md"

LANGUAGES = ("FR", "NL", "DE", "EN", "IT", "ES", "PL")
OCR_CONCEPTS = {
    "zonnebloemole": "sunflower_oil",
    "havervokken": "oats",
    "haerioken": "oats",
    "volkoren haerioken": "oats",
    "gevriesdroogde bosteen": "blueberry",
    "Kakaopulvert": "cocoa",
    "Olpalme": "palm_oil",
    "écithines": "e322",
    "qlucose": "glucose_syrup",
}


def load(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def cell(value: str) -> str:
    return value.replace("|", "\\|").replace("\n", " ") or "—"


def markdown_table(headers: list[str], rows: list[list[str]]) -> str:
    header = "| " + " | ".join(headers) + " |"
    separator = "| " + " | ".join("---" for _ in headers) + " |"
    body = ["| " + " | ".join(cell(value) for value in row) + " |" for row in rows]
    return "\n".join([header, separator, *body])


def main() -> None:
    ingredients = load(INGREDIENTS)
    aliases_data = load(ALIASES)
    origin_data = load(ORIGIN_RULES)
    origin_by_id: dict[str, list[str]] = defaultdict(list)
    for rule in origin_data["rules"]:
        for identifier in rule.get("target", {}).get("ingredientIds", []):
            origin_by_id[identifier].append(rule["id"])

    canonical_rows = [
        [
            entry["id"],
            entry["name"],
            entry["status"],
            entry.get("eNumber", "") or "—",
            ", ".join(origin_by_id.get(entry["id"], [])) or "—",
            ", ".join(entry["sources"]),
        ]
        for entry in ingredients
    ]

    aliases_by_concept: dict[str, dict[str, list[str]]] = defaultdict(lambda: defaultdict(list))
    for entry in aliases_data["aliases"]:
        aliases_by_concept[entry["canonicalId"]][entry["language"]].extend(entry["aliases"])
        aliases_by_concept[entry["canonicalId"]][entry["language"]].extend(entry["ocrVariants"])
    alias_rows = [
        [concept, *[", ".join(dict.fromkeys(aliases_by_concept[concept].get(language, []))) or "—" for language in LANGUAGES]]
        for concept in sorted(aliases_by_concept)
    ]

    canonical_ids = {entry["id"] for entry in ingredients}
    absent_rows = []
    for concept in sorted(set(aliases_by_concept) - canonical_ids):
        observed = sorted({alias for values in aliases_by_concept[concept].values() for alias in values})
        languages = ", ".join(language for language in LANGUAGES if aliases_by_concept[concept].get(language))
        decision = "Concept générique : conserver non classifiable" if concept == "cereals" else "Créer le concept canonique après revue"
        absent_rows.append([concept, ", ".join(observed), languages, "Non classifiable", decision])

    correction_rows = [
        [
            correction["language"],
            correction["from"],
            correction["to"],
            OCR_CONCEPTS.get(correction["from"], "À préciser"),
            "Expression complète dans le bloc linguistique sélectionné",
            "Actif",
        ]
        for correction in aliases_data["ocrCorrections"]
    ]
    correction_rows.extend(
        [
            entry["language"],
            variant,
            entry["aliases"][0],
            entry["canonicalId"],
            "Variante OCR complète dans le bloc linguistique sélectionné",
            "Actif",
        ]
        for entry in aliases_data["aliases"]
        for variant in entry["ocrVariants"]
    )

    rendered = "\n".join([
        "<!-- Généré par tools/build_knowledge_docs.py : ne pas modifier à la main. -->",
        "",
        "### Concepts canoniques existants",
        "",
        markdown_table(["ID", "Nom français", "Statut", "E-number", "Règle d’origine", "Source"], canonical_rows),
        "",
        "### Alias multilingues",
        "",
        markdown_table(["Concept", *LANGUAGES], alias_rows),
        "",
        "### Concepts reconnus mais absents",
        "",
        markdown_table(["Concept proposé", "Alias observés", "Langues", "Statut actuel", "Décision nécessaire"], absent_rows),
        "",
        "Le lexique reconnaît le terme, mais l’application ne lui attribue aucun statut tant que le concept canonique n’existe pas dans `ingredients.json`.",
        "",
        "### Corrections OCR actives",
        "",
        markdown_table(["Langue", "OCR observé", "Correction proposée", "Concept lié", "Contexte", "Décision"], correction_rows),
        "",
    ])
    if "--check" in sys.argv:
        if not OUTPUT.exists() or OUTPUT.read_text(encoding="utf-8") != rendered:
            raise SystemExit("Knowledge documentation is stale; run: python tools/build_knowledge_docs.py")
        print(f"Knowledge documentation is current: {OUTPUT.relative_to(ROOT)}")
        return
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(rendered, encoding="utf-8")
    print(f"Generated {OUTPUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
