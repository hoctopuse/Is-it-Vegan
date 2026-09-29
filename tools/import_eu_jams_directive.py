#!/usr/bin/env python3
"""Import CELEX 02001L0113 Annex I product names from the four local PDFs.

The importer deliberately models the four regulatory product families as
separate concepts.  It does not turn a fruit, fruit juice, fruit purée, aroma
or generic commercial preparation into one of those concepts.
"""

from __future__ import annotations

import argparse
import json
import re
import unicodedata
from pathlib import Path

from pypdf import PdfReader

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "reference-input" / "eu-food-labelling" / "02-sector-product-standards"
DATABASE = ROOT / "knowledge" / "ingredients.json"
LEXICON = ROOT / "knowledge" / "ingredient_aliases_multilingual.json"
SOURCES = ROOT / "knowledge" / "sources.json"
LANGUAGES = ("FR", "NL", "EN", "DE")
PDFS = {language: BASE / f"CELEX_02001L0113-20260614_{language}_TXT.pdf" for language in LANGUAGES}

# Annex I names verified verbatim in their matching local PDF.  The shorter
# national-use terms “marmelade” / “Marmelade” are intentionally not added:
# the Directive permits them for jam in some territories while also defining
# a distinct citrus-marmalade product, so a bare term would be ambiguous.
OFFICIAL_ALIASES = {
    "fruit_jam": {
        "FR": ["confiture", "confiture extra"],
        "NL": ["jam", "confituur", "extra jam", "extra confituur"],
        "EN": ["jam", "extra jam"],
        "DE": ["Konfitüre", "Konfitüre extra"],
    },
    "fruit_jelly": {
        "FR": ["gelée", "gelée extra"],
        "NL": ["gelei", "extra gelei"],
        "EN": ["jelly", "extra jelly"],
        "DE": ["Gelee", "Gelee extra"],
    },
    "citrus_marmalade": {
        "FR": ["marmelade d’agrumes", "marmelade-gelée"],
        "NL": ["citrusmarmelade", "geleimarmelade"],
        "EN": ["citrus marmalade", "jelly marmalade"],
        "DE": ["Zitrusmarmelade", "Gelee-Marmelade"],
    },
    "sweetened_chestnut_puree": {
        "FR": ["crème de marrons"],
        "NL": ["kastanjepasta"],
        "EN": ["sweetened chestnut purée"],
        "DE": ["Maronenkrem"],
    },
}

CONCEPTS = {
    "fruit_jam": ("Confiture de fruits", "Catégorie réglementaire composée de sucres, de pulpe et/ou de purée de fruits et d’eau : vegan selon cette définition."),
    "fruit_jelly": ("Gelée de fruits", "Catégorie réglementaire composée de sucres, de jus et/ou d’extraits aqueux de fruits : vegan selon cette définition."),
    "citrus_marmalade": ("Marmelade d’agrumes", "Catégorie réglementaire composée d’eau, de sucres et de produits issus d’agrumes : vegan selon cette définition."),
    "sweetened_chestnut_puree": ("Crème de marrons", "Catégorie réglementaire composée d’eau, de sucre et de purée de châtaignes : vegan selon cette définition."),
}

SOURCE = {
    "id": "eu-jams-directive-2001-113-20260614",
    "title": "Council Directive 2001/113/EC — fruit jams, jellies, marmalades and sweetened chestnut purée",
    "celex": "02001L0113",
    "url": "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02001L0113-20260614",
    "language": "FR, NL, EN, DE",
    "institution": "Council of the European Union",
    "consolidationDate": "2026-06-14",
    "localPdfPaths": [str(path.relative_to(ROOT)).replace("\\", "/") for path in PDFS.values()],
    "role": "Primary local regulatory source for Annex I multilingual names and definitions of jams, jellies, citrus marmalade and sweetened chestnut purée.",
    "limitations": "The directive defines regulated categories. It does not classify a product merely marketed with a fruit-related name, nor replace its declared ingredient list when it is outside those definitions.",
    "use": "Evidence for the CELEX 02001L0113 aliases and their narrowly defined product concepts.",
}


def normalize(value: str) -> str:
    value = unicodedata.normalize("NFKD", value.casefold())
    value = "".join(character for character in value if not unicodedata.combining(character))
    return re.sub(r"[^a-z0-9]+", " ", value).strip()


def compact(value: str) -> str:
    return re.sub(r"\s+", "", normalize(value).replace(" ", ""))


def pdf_text(path: Path) -> str:
    if not path.is_file():
        raise ValueError(f"missing local PDF: {path.relative_to(ROOT)}")
    return "\n".join(page.extract_text() or "" for page in PdfReader(path).pages)


def verify_extraction() -> None:
    texts = {language: compact(pdf_text(path)) for language, path in PDFS.items()}
    missing = {
        f"{concept}/{language}": [alias for alias in aliases if compact(alias) not in texts[language]]
        for concept, by_language in OFFICIAL_ALIASES.items()
        for language, aliases in by_language.items()
    }
    missing = {key: value for key, value in missing.items() if value}
    if missing:
        raise ValueError(f"unjoined or unverified local-PDF aliases: {missing}")


def verify_preservation(database: list[dict], lexicon: dict) -> None:
    ids = [entry.get("id") for entry in database]
    if len(ids) != len(set(ids)):
        raise ValueError("duplicate canonical ingredient id")
    if len(database) < 455:
        raise ValueError("pre-existing concepts are missing; refusing destructive import")
    if len(lexicon.get("mappings", [])) < 1834:
        raise ValueError("pre-existing multilingual mappings are missing; refusing destructive import")
    mandatory = {"fruit_juice", "fruit_puree", "fruit_nectar", "honey", "milk", "cream", "whey", "casein", "lactose"}
    missing = mandatory - set(ids)
    if missing:
        raise ValueError(f"historical concepts are missing: {sorted(missing)}")


def merge(database: list[dict], lexicon: dict, sources: list[dict]) -> bool:
    changed = False
    ids = {entry["id"] for entry in database}
    canonical_owners = {}
    for entry in database:
        for alias in entry.get("aliases", []):
            canonical_owners.setdefault(normalize(alias), entry["id"])
    lexicon_owners = {}
    for entry in lexicon["aliases"]:
        for alias in entry.get("aliases", []) + entry.get("ocrVariants", []):
            lexicon_owners.setdefault((entry["language"], normalize(alias)), entry["canonicalId"])
    mapping_keys = {(item["conceptId"], item["language"], item["normalizedForm"], item["relation"]) for item in lexicon.get("mappings", [])}

    for concept, by_language in OFFICIAL_ALIASES.items():
        if concept not in ids:
            name, reason = CONCEPTS[concept]
            database.append({"id": concept, "name": name, "aliases": [], "status": "VEGAN", "reason": reason, "sources": [SOURCE["id"]]})
            ids.add(concept)
            changed = True
        entry = next(item for item in database if item["id"] == concept)
        if entry["status"] != "VEGAN":
            raise ValueError(f"{concept} has an incompatible pre-existing status")
        if SOURCE["id"] not in entry["sources"]:
            entry["sources"].append(SOURCE["id"])
            changed = True
        for language, aliases in by_language.items():
            lex_entry = next((item for item in lexicon["aliases"] if item["canonicalId"] == concept and item["language"] == language), None)
            if lex_entry is None:
                lex_entry = {"canonicalId": concept, "language": language, "aliases": [], "ocrVariants": []}
                lexicon["aliases"].append(lex_entry)
                changed = True
            for alias in aliases:
                owner = canonical_owners.get(normalize(alias))
                if owner and owner != concept:
                    raise ValueError(f"canonical alias collision: {alias!r} belongs to {owner}")
                multilingual_owner = lexicon_owners.get((language, normalize(alias)))
                if multilingual_owner and multilingual_owner != concept:
                    raise ValueError(f"multilingual alias collision: {language}/{alias!r} belongs to {multilingual_owner}")
                if alias not in entry["aliases"]:
                    entry["aliases"].append(alias)
                    changed = True
                if alias not in lex_entry["aliases"]:
                    lex_entry["aliases"].append(alias)
                    changed = True
                record = {
                    "surfaceForm": alias,
                    "language": language,
                    "conceptId": concept,
                    "mappingGroup": "fruit-jams-directive",
                    "relation": "REGULATORY_ALIAS",
                    "normalizedForm": normalize(alias),
                    "source": SOURCE["id"],
                    "confidence": "REVIEWED",
                }
                key = (concept, language, record["normalizedForm"], record["relation"])
                if key not in mapping_keys:
                    lexicon.setdefault("mappings", []).append(record)
                    mapping_keys.add(key)
                    changed = True
    current_source = next((item for item in sources if item["id"] == SOURCE["id"]), None)
    if current_source is None:
        sources.append(SOURCE)
        changed = True
    elif current_source != SOURCE:
        raise ValueError(f"conflicting source definition: {SOURCE['id']}")
    return changed


def render(value: object) -> str:
    return json.dumps(value, ensure_ascii=False, indent=2) + "\n"


def main() -> None:
    parser = argparse.ArgumentParser()
    modes = parser.add_mutually_exclusive_group(required=True)
    modes.add_argument("--check", action="store_true")
    modes.add_argument("--dry-run", action="store_true")
    modes.add_argument("--write", action="store_true")
    args = parser.parse_args()
    verify_extraction()
    database = json.loads(DATABASE.read_text(encoding="utf-8"))
    lexicon = json.loads(LEXICON.read_text(encoding="utf-8"))
    sources = json.loads(SOURCES.read_text(encoding="utf-8"))
    verify_preservation(database, lexicon)
    changed = merge(database, lexicon, sources)
    aliases = sum(len(values) for languages in OFFICIAL_ALIASES.values() for values in languages.values())
    print(f"CELEX 02001L0113 Annex I: {len(CONCEPTS)} concepts, {aliases} aliases, 4 joined languages, changes={int(changed)}")
    if args.check and changed:
        raise SystemExit("jams directive import is stale; run: python tools/import_eu_jams_directive.py --write")
    if args.write and changed:
        DATABASE.write_text(render(database), encoding="utf-8")
        LEXICON.write_text(render(lexicon), encoding="utf-8")
        SOURCES.write_text(render(sources), encoding="utf-8")


if __name__ == "__main__":
    main()
