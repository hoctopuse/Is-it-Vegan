#!/usr/bin/env python3
"""Audit the canonical multilingual lexicon and render its review document."""

from __future__ import annotations

import argparse
import json
import re
import unicodedata
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LEXICON = ROOT / "knowledge" / "ingredient_aliases_multilingual.json"
ASSET = ROOT / "app" / "src" / "main" / "assets" / "ingredient_aliases_multilingual.json"
INGREDIENTS = ROOT / "knowledge" / "ingredients.json"
OUTPUT = ROOT / "docs" / "generated" / "multilingual-ingredient-mapping.md"
TARGET_LANGUAGES = ("FR", "NL", "EN", "DE")
SUPPORTED_LANGUAGES = {*TARGET_LANGUAGES, "IT", "ES", "PL"}
EXCLUDED_CONTEXTS = {
    "honey": ["arome de miel", "gout de miel", "honey flavour", "honey flavor"],
    "milk": ["arome de lait", "gout de lait", "milk flavour", "milk flavor", "plant milk", "lait vegetal"],
}


def normalized(value: str) -> str:
    value = unicodedata.normalize("NFKD", value.casefold())
    value = "".join(char for char in value if not unicodedata.combining(char))
    return re.sub(r"[^a-z0-9]+", " ", value).strip()


def mapping_group(concept: dict) -> str:
    identifier = concept["id"]
    if identifier == "honey":
        return "honey-regulatory"
    if identifier == "milk":
        return "preserved-milk"
    if identifier == "cream":
        return "preserved-milk-cream"
    if "eu-jams-directive-2001-113-20260614" in concept.get("sources", []):
        return "fruit-jams-directive"
    if "eu-agricultural-products-regulation-1308-2013-20260818" in concept.get("sources", []):
        return "agricultural-products-regulation"
    if "eu-cocoa-chocolate-directive-2000-36-20131118" in concept.get("sources", []):
        return "cocoa-chocolate-directive"
    if concept.get("eNumber") and "eu-additives" in concept.get("sources", []):
        return "eu-additives"
    if "possibleOriginNote" in concept:
        return "possible-origin-notes"
    return identifier


def relation(concept: dict, ocr: bool) -> str:
    if ocr:
        return "OCR_VARIANT"
    if concept["id"] in {"honey", "milk", "cream"} or (
        concept.get("eNumber") and "eu-additives" in concept.get("sources", [])
    ) or "eu-jams-directive-2001-113-20260614" in concept.get("sources", []) or "eu-agricultural-products-regulation-1308-2013-20260818" in concept.get("sources", []) or "eu-cocoa-chocolate-directive-2000-36-20131118" in concept.get("sources", []):
        return "REGULATORY_ALIAS"
    return "COMMON_LABEL_NAME"


def confidence(kind: str) -> str:
    return "OCR_REVIEWED" if kind == "OCR_VARIANT" else "REVIEWED"


def load(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


def build() -> tuple[str, dict]:
    lexicon = load(LEXICON)
    ingredients = {entry["id"]: entry for entry in load(INGREDIENTS)}
    declared_mappings = lexicon.get("mappings", [])
    records: dict[str, dict[str, list[dict]]] = defaultdict(lambda: defaultdict(list))
    alerts: dict[str, list[str]] = defaultdict(list)
    absent_records: list[tuple[str, str, str]] = []
    owners: dict[tuple[str, str], set[str]] = defaultdict(set)
    placeholders: list[str] = []
    invalid_languages: list[str] = []

    for entry in lexicon["aliases"]:
        concept_id, language = entry.get("canonicalId", ""), entry.get("language", "")
        concept = ingredients.get(concept_id)
        if concept is None:
            alerts[concept_id].append("concept absent de ingredients.json")
            absent_records.extend((concept_id, language, surface) for surface in entry.get("aliases", []) + entry.get("ocrVariants", []))
            continue
        if language not in SUPPORTED_LANGUAGES:
            invalid_languages.append(f"{concept_id}/{language or 'missing'}")
            continue
        for field, is_ocr in (("aliases", False), ("ocrVariants", True)):
            for surface in entry.get(field, []):
                kind = relation(concept, is_ocr)
                record = {
                    "surfaceForm": surface,
                    "language": language,
                    "conceptId": concept_id,
                    "mappingGroup": mapping_group(concept),
                    "relation": kind,
                    "normalizedForm": normalized(surface),
                    "source": ", ".join(concept.get("sources", [])) or "—",
                    "confidence": confidence(kind),
                }
                records[concept_id][language].append(record)
                owners[(language, record["normalizedForm"])].add(concept_id)
                if "?" in surface:
                    placeholders.append(f"{concept_id}/{language}: {surface}")

    for concept_id, concept in ingredients.items():
        if concept_id not in records and (concept_id in {"honey", "milk", "cream"} or "possibleOriginNote" in concept):
            alerts[concept_id].append("aucun alias multilingue")
        if concept_id in records and not concept.get("sources"):
            alerts[concept_id].append("source du concept absente")
    for (language, form), concepts in owners.items():
        if len(concepts) > 1:
            for concept_id in concepts:
                alerts[concept_id].append(f"collision {language}: {form} -> {', '.join(sorted(concepts))}")

    audit_ids = sorted(set(records) | {"honey", "milk", "cream"} | {x["id"] for x in ingredients.values() if "possibleOriginNote" in x})
    synthesis = []
    details = []
    for concept_id in audit_ids:
        concept = ingredients.get(concept_id)
        if concept is None:
            continue
        languages = records.get(concept_id, {})
        cells = []
        for language in TARGET_LANGUAGES:
            values = [record["surfaceForm"] for record in languages.get(language, [])]
            cells.append("; ".join(dict.fromkeys(values)) or "—")
        synthesis.append([concept_id, concept["status"], *cells, ", ".join(concept.get("sources", [])) or "—", "; ".join(alerts[concept_id]) or "—"])
        details.extend([f"## Concept : {concept_id}", "", f"Statut : `{concept['status']}`<br>", f"Sources : {', '.join(concept.get('sources', [])) or '—'}<br>", f"Mapping group : `{mapping_group(concept)}`", ""])
        for language in TARGET_LANGUAGES:
            details.append(f"### {language}")
            values = languages.get(language, [])
            if not values:
                details.append("- —")
            for record in values:
                details.append(f"- `{record['surfaceForm']}` — {record['relation']}; normalise: `{record['normalizedForm']}`; confiance: `{record['confidence']}`; source: {record['source']}")
            details.append("")
        extra = [(language, values) for language, values in languages.items() if language not in TARGET_LANGUAGES]
        if extra:
            details.append("### Alias supplementaires")
            for language, values in extra:
                details.append(f"- {language}: " + "; ".join(record["surfaceForm"] for record in values))
            details.append("")
        if EXCLUDED_CONTEXTS.get(concept_id):
            details.extend(["### Contextes exclus", "- " + "; ".join(EXCLUDED_CONTEXTS[concept_id]), ""])
        if alerts[concept_id]:
            details.extend(["### Alertes", *[f"- {alert}" for alert in alerts[concept_id]], ""])

    header = [
        "<!-- Generated by tools/build_multilingual_ingredient_mapping.py; do not edit manually. -->", "",
        "# Mapping multilingue des ingredients", "",
        "Les sources d'alias restent dans `knowledge/ingredient_aliases_multilingual.json`. Les sources affichees sont celles du concept canonique dans `knowledge/ingredients.json`; aucun JSON parallele n'est cree.", "",
        "## Tableau synthetique", "",
        "| Concept | Statut | FR | NL | EN | DE | Sources | Alertes |", "|---|---|---|---|---|---|---|---|",
    ]
    for row in synthesis:
        header.append("| " + " | ".join(value.replace("|", "\\|") for value in row) + " |")
    header.extend(["", "## Alias non importes", ""])
    if absent_records:
        header.extend([f"- `{concept_id}` / {language} / `{surface}` — concept canonique absent, sans statut ni source." for concept_id, language, surface in absent_records])
    else:
        header.append("- Aucun.")
    header.extend(["", "## Details par concept", ""])
    report = {
        "concepts": len(audit_ids),
        "aliases": Counter(record["language"] for languages in records.values() for values in languages.values() for record in values),
        "missingConcepts": sorted(key for key, value in alerts.items() if "concept absent de ingredients.json" in value),
        "collisions": sum("collision" in alert for values in alerts.values() for alert in values),
        "placeholders": placeholders,
        "assetParity": LEXICON.read_bytes() == ASSET.read_bytes() if ASSET.exists() else False,
        "declaredMappings": len(declared_mappings),
    }
    required_mapping_fields = {"surfaceForm", "language", "conceptId", "mappingGroup", "relation", "normalizedForm", "source", "confidence"}
    mapping_errors = [mapping for mapping in declared_mappings if not required_mapping_fields <= set(mapping)]
    if mapping_errors:
        raise ValueError("mapping metadata has missing required fields")
    if len(declared_mappings) != sum(len(values) for languages in records.values() for values in languages.values()):
        raise ValueError("mapping metadata is not synchronized with aliases and ocrVariants")
    return "\n".join(header + details) + "\n", report


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--write", action="store_true")
    parser.add_argument("--report", action="store_true")
    args = parser.parse_args()
    if args.check and args.write:
        parser.error("choose at most one of --check and --write")
    rendered, report = build()
    if args.report:
        print(json.dumps(report, ensure_ascii=False, indent=2))
    if args.check:
        if not OUTPUT.exists() or OUTPUT.read_text(encoding="utf-8") != rendered:
            raise SystemExit("Multilingual mapping is stale; run: python tools/build_multilingual_ingredient_mapping.py --write")
        if not report["assetParity"]:
            raise SystemExit("Multilingual asset differs from knowledge source; run: python tools/build_ingredients.py")
        print(f"Multilingual mapping is current: {OUTPUT.relative_to(ROOT)}")
        return
    if args.write:
        OUTPUT.parent.mkdir(parents=True, exist_ok=True)
        OUTPUT.write_text(rendered, encoding="utf-8")
        print(f"Generated {OUTPUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
