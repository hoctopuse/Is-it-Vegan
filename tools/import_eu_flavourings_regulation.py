#!/usr/bin/env python3
"""Merge reviewed Article 3 CELEX 02008R1334 flavouring categories.

Only terms verified in their own local FR/NL/EN/DE consolidation are written.
The regulation identifies categories, not vegan suitability of a formulation.
"""
from __future__ import annotations

import argparse
import json
import re
import unicodedata
from pathlib import Path

from pypdf import PdfReader

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "reference-input" / "eu-food-labelling" / "01-core-labelling-terms"
PDFS = {language: BASE / f"CELEX_02008R1334-20260216_{language}_TXT.pdf" for language in ("FR", "NL", "EN", "DE")}
INGREDIENTS = ROOT / "knowledge" / "ingredients.json"
LEXICON = ROOT / "knowledge" / "ingredient_aliases_multilingual.json"
SOURCES = ROOT / "knowledge" / "sources.json"

SOURCE = {
    "id": "eu-flavourings-regulation-1334-2008-20260216",
    "title": "Regulation (EC) No 1334/2008 — flavourings and certain food ingredients with flavouring properties",
    "celex": "02008R1334",
    "url": "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02008R1334-20260216",
    "language": "FR, NL, EN, DE",
    "institution": "European Parliament and Council of the European Union",
    "consolidationDate": "2026-02-16",
    "localPdfPaths": [str(path.relative_to(ROOT)).replace("\\", "/") for path in PDFS.values()],
    "role": "Primary local regulatory source for Article 3 flavouring category names and definitions.",
    "limitations": "The regulation defines categories and possible source materials; it does not establish vegan suitability or prove that a named food is present in an aroma.",
    "use": "Evidence for reviewed CELEX 02008R1334 category aliases only; no translation or formulation-specific origin is inferred.",
}

# Article 3(2), checked verbatim after Unicode/punctuation normalisation in the
# PDF of the corresponding language.  German uses Aromaextrakt for Article 3(d).
ALIASES = {
    "flavouring": {
        "FR": ["arôme", "arômes"], "NL": ["aroma", "aroma's"],
        "EN": ["flavouring", "flavourings"], "DE": ["Aroma", "Aromen"],
    },
    "flavouring_substance": {
        "FR": ["substance aromatisante"], "NL": ["aromastof"],
        "EN": ["flavouring substance"], "DE": ["Aromastoff"],
    },
    "natural_flavouring": {
        "FR": ["substance aromatisante naturelle"], "NL": ["natuurlijke aromastof"],
        "EN": ["natural flavouring substance"], "DE": ["natürlicher Aromastoff"],
    },
    "flavouring_preparation": {
        "FR": ["préparation aromatisante"], "NL": ["aromatiserend preparaat"],
        "EN": ["flavouring preparation"], "DE": ["Aromaextrakt"],
    },
    "thermal_process_flavouring": {
        "FR": ["arôme obtenu par traitement thermique"],
        "NL": ["via een thermisch procedé verkregen aroma"],
        "EN": ["thermal process flavouring"], "DE": ["thermisch gewonnenes Reaktionsaroma"],
    },
    "smoke_flavouring": {
        "FR": ["arôme de fumée"], "NL": ["rookaroma"],
        "EN": ["smoke flavouring"], "DE": ["Raucharoma"],
    },
    "flavour_precursor": {
        "FR": ["précurseur d'arôme"], "NL": ["aromaprecursor"],
        "EN": ["flavour precursor"], "DE": ["Aromavorstufe"],
    },
    "other_flavouring": {
        "FR": ["autre arôme"], "NL": ["overig aroma"],
        "EN": ["other flavouring"], "DE": ["sonstiges Aroma"],
    },
    "food_ingredient_with_flavouring_properties": {
        "FR": ["ingrédient alimentaire possédant des propriétés aromatisantes"],
        "NL": ["voedselingrediënt met aromatiserende eigenschappen"],
        "EN": ["food ingredient with flavouring properties"],
        "DE": ["Lebensmittelzutat mit Aromaeigenschaften"],
    },
}

NEW = {
    "flavouring": "Arôme",
    "flavouring_substance": "Substance aromatisante",
    "flavouring_preparation": "Préparation aromatisante",
    "thermal_process_flavouring": "Arôme obtenu par traitement thermique",
    "smoke_flavouring": "Arôme de fumée",
    "flavour_precursor": "Précurseur d’arôme",
    "other_flavouring": "Autre arôme",
    "food_ingredient_with_flavouring_properties": "Ingrédient alimentaire aux propriétés aromatisantes",
}
NOTE = {
    "origins": ["PLANT", "ANIMAL", "MICROBIAL"],
    "variability": "RAW_MATERIAL_AND_PROCESS",
    "sourceIds": [SOURCE["id"]],
    "confidence": "REGULATORY_CATEGORY",
}


def normalise(value: str) -> str:
    decomposed = unicodedata.normalize("NFKD", value.casefold())
    plain = "".join(char for char in decomposed if not unicodedata.combining(char))
    return re.sub(r"[^a-z0-9]+", " ", plain).strip()


def compact(value: str) -> str:
    return normalise(value).replace(" ", "")


def extract(path: Path) -> str:
    if not path.is_file():
        raise ValueError(f"missing local PDF: {path.relative_to(ROOT)}")
    reader = PdfReader(path)
    # Article 3 is on the fourth and fifth PDF pages in every supplied local
    # consolidation.  Reading only the cited source pages keeps checks quick
    # while preserving the evidence scope of this importer.
    if len(reader.pages) < 5:
        raise ValueError(f"incomplete local PDF: {path.relative_to(ROOT)}")
    return "\n".join(reader.pages[index].extract_text() or "" for index in (3, 4))


def verify_pdfs() -> None:
    texts = {language: compact(extract(path)) for language, path in PDFS.items()}
    placeholders = [alias for languages in ALIASES.values() for aliases in languages.values() for alias in aliases if "?" in alias]
    if placeholders:
        raise ValueError(f"placeholder aliases: {placeholders}")
    missing = [f"{concept}/{language}/{alias}" for concept, languages in ALIASES.items()
               for language, aliases in languages.items() for alias in aliases if compact(alias) not in texts[language]]
    if missing:
        raise ValueError(f"unverified local-PDF aliases: {missing}")


def render(value: object) -> str:
    return json.dumps(value, ensure_ascii=False, indent=2) + "\n"


def merge(database: list[dict], lexicon: dict, sources: list[dict]) -> bool:
    if len(database) < 471 or len(lexicon.get("mappings", [])) < 1956:
        raise ValueError("historical knowledge is missing; refusing a destructive import")
    ids = {entry["id"] for entry in database}
    if len(ids) != len(database):
        raise ValueError("duplicate canonical ingredient id")
    changed = False
    owners = {normalise(alias): entry["id"] for entry in database for alias in entry.get("aliases", [])}
    multilingual = {(entry["language"], normalise(alias)): entry["canonicalId"] for entry in lexicon["aliases"]
                    for alias in entry.get("aliases", []) + entry.get("ocrVariants", [])}
    mapping_keys = {(item["conceptId"], item["language"], item["normalizedForm"]) for item in lexicon.get("mappings", [])}
    for concept, languages in ALIASES.items():
        if concept not in ids:
            entry = {
                "id": concept, "name": NEW[concept], "aliases": [], "status": "UNCERTAIN",
                "reason": "La catégorie réglementaire peut provenir de matières végétales, animales ou microbiologiques et ne prouve pas la présence de l’aliment dont elle évoque le goût.",
                "sources": [SOURCE["id"]], "possibleOriginNote": NOTE.copy(),
            }
            database.append(entry); ids.add(concept); changed = True
        entry = next(item for item in database if item["id"] == concept)
        if entry["status"] != "UNCERTAIN":
            raise ValueError(f"incompatible historical status for {concept}: {entry['status']}")
        if SOURCE["id"] not in entry["sources"]:
            entry["sources"].append(SOURCE["id"]); changed = True
        if entry.get("possibleOriginNote") != NOTE:
            entry["possibleOriginNote"] = NOTE.copy(); changed = True
        for language, aliases in languages.items():
            lex_entry = next((item for item in lexicon["aliases"] if item["canonicalId"] == concept and item["language"] == language), None)
            if lex_entry is None:
                lex_entry = {"canonicalId": concept, "language": language, "aliases": [], "ocrVariants": []}
                lexicon["aliases"].append(lex_entry); changed = True
            for alias in aliases:
                key = (concept, language, normalise(alias))
                if owners.get(normalise(alias), concept) != concept or multilingual.get((language, normalise(alias)), concept) != concept:
                    raise ValueError(f"alias collision: {language}/{alias}")
                if alias not in entry["aliases"]: entry["aliases"].append(alias); changed = True
                if alias not in lex_entry["aliases"]: lex_entry["aliases"].append(alias); changed = True
                mapping = {"surfaceForm": alias, "language": language, "conceptId": concept,
                           "mappingGroup": "eu-flavourings-regulation", "relation": "REGULATORY_ALIAS",
                           "normalizedForm": normalise(alias), "source": SOURCE["id"], "confidence": "REVIEWED"}
                if key not in mapping_keys:
                    lexicon.setdefault("mappings", []).append(mapping); mapping_keys.add(key); changed = True
    old_source = next((item for item in sources if item["id"] == SOURCE["id"]), None)
    if old_source is None: sources.append(SOURCE); changed = True
    elif old_source != SOURCE: raise ValueError(f"conflicting source definition: {SOURCE['id']}")
    return changed


def main() -> None:
    parser = argparse.ArgumentParser()
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--check", action="store_true")
    mode.add_argument("--dry-run", action="store_true")
    mode.add_argument("--write", action="store_true")
    args = parser.parse_args()
    verify_pdfs()
    database = json.loads(INGREDIENTS.read_text(encoding="utf-8"))
    lexicon = json.loads(LEXICON.read_text(encoding="utf-8"))
    sources = json.loads(SOURCES.read_text(encoding="utf-8"))
    changed = merge(database, lexicon, sources)
    total = sum(len(aliases) for languages in ALIASES.values() for aliases in languages.values())
    print(f"CELEX 02008R1334 Article 3: {len(NEW)} new concepts, {total} aliases, 4 joined languages, changes={int(changed)}")
    if args.check and changed:
        raise SystemExit("flavourings regulation import is stale; run: python tools/import_eu_flavourings_regulation.py --write")
    if args.write and changed:
        INGREDIENTS.write_text(render(database), encoding="utf-8")
        LEXICON.write_text(render(lexicon), encoding="utf-8")
        SOURCES.write_text(render(sources), encoding="utf-8")


if __name__ == "__main__":
    main()
