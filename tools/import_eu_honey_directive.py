#!/usr/bin/env python3
"""Import the reviewed multilingual honey names from local CELEX 02001L0110 PDFs."""

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
PDFS = {lang: BASE / f"CELEX_02001L0110-20260614_{lang}_TXT.pdf" for lang in ("FR", "NL", "EN", "DE")}

OFFICIAL_ALIASES = {
    "FR": ["miel", "miel de fleurs", "miel de nectars", "miel de miellat", "miel en rayons", "miel avec morceaux de rayons", "miel égoutté", "miel centrifugé", "miel pressé", "miel destiné à l’industrie"],
    "NL": ["honing", "honig", "bloemenhoning", "bloemenhonig", "nectarhoning", "nectarhonig", "honingdauwhoning", "honingdauwhonig", "raathoning", "raathonig", "brokhoning", "brokhonig", "raatbrokken in honing/honig", "lekhoning", "lekhonig", "slingerhoning", "slingerhonig", "pershoning", "pershonig", "bakkershoning"],
    "EN": ["honey", "blossom honey", "nectar honey", "honeydew honey", "comb honey", "chunk honey", "cut comb in honey", "drained honey", "extracted honey", "pressed honey", "Baker's honey"],
    "DE": ["Honig", "Blütenhonig", "Nektarhonig", "Honigtauhonig", "Wabenhonig", "Scheibenhonig", "Honig mit Wabenteilen", "Wabenstücke in Honig", "Tropfhonig", "Schleuderhonig", "Presshonig", "Backhonig"],
}
SOURCE = {
    "id": "eu-honey-directive-2001-110-20260614",
    "title": "Council Directive 2001/110/EC — relating to honey",
    "celex": "02001L0110",
    "url": "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02001L0110-20260614",
    "language": "FR, NL, EN, DE",
    "institution": "Council of the European Union",
    "consolidationDate": "2026-06-14",
    "localPdfPaths": [str(path.relative_to(ROOT)).replace("\\", "/") for path in PDFS.values()],
    "role": "Primary local regulatory source for honey definitions and multilingual product names in Annex I.",
    "limitations": "Defines the regulated honey product and names; it does not classify composite products or replace their declared ingredient lists.",
    "use": "Evidence for the honey concept and Annex I multilingual aliases."
}


def normalize(value: str) -> str:
    value = unicodedata.normalize("NFKC", value).replace("\u00ad", "")
    value = re.sub(r"\s+", " ", value).strip().casefold()
    return "".join(char for char in unicodedata.normalize("NFD", value) if not unicodedata.combining(char))


def pdf_text(path: Path) -> str:
    if not path.is_file():
        raise ValueError(f"missing local PDF: {path.relative_to(ROOT)}")
    return "\n".join(page.extract_text() or "" for page in PdfReader(path).pages)


def compact(text: str) -> str:
    return re.sub(r"\s+", "", normalize(text))


def verify_extraction() -> dict[str, list[str]]:
    texts = {lang: compact(pdf_text(path)) for lang, path in PDFS.items()}
    missing = {lang: [alias for alias in aliases if compact(alias) not in texts[lang]] for lang, aliases in OFFICIAL_ALIASES.items()}
    missing = {lang: aliases for lang, aliases in missing.items() if aliases}
    if missing:
        raise ValueError(f"unjoined or ambiguous official aliases: {missing}")
    return {lang: aliases[:] for lang, aliases in OFFICIAL_ALIASES.items()}


def merge_aliases(lexicon: dict, aliases_by_language: dict[str, list[str]]) -> bool:
    changed = False
    entries = lexicon.setdefault("aliases", [])
    owners = {}
    for entry in entries:
        for alias in entry.get("aliases", []) + entry.get("ocrVariants", []):
            owners[(entry["language"], normalize(alias))] = entry["canonicalId"]
    for language, aliases in aliases_by_language.items():
        entry = next((item for item in entries if item["canonicalId"] == "honey" and item["language"] == language), None)
        if entry is None:
            entry = {"canonicalId": "honey", "language": language, "aliases": [], "ocrVariants": []}
            entries.append(entry)
            changed = True
        for alias in aliases:
            owner = owners.get((language, normalize(alias)))
            if owner and owner != "honey":
                raise ValueError(f"alias collision: {language} {alias!r} belongs to {owner}")
            if alias not in entry["aliases"]:
                entry["aliases"].append(alias)
                changed = True
    return changed


def merge_canonical_aliases(database: list[dict], honey: dict, aliases_by_language: dict[str, list[str]]) -> bool:
    owners = {}
    for entry in database:
        for alias in entry.get("aliases", []):
            owners.setdefault(normalize(alias), entry["id"])
    changed = False
    for alias in [alias for values in aliases_by_language.values() for alias in values]:
        owner = owners.get(normalize(alias))
        if owner and owner != "honey":
            raise ValueError(f"canonical alias collision: {alias!r} belongs to {owner}")
        if alias not in honey["aliases"]:
            honey["aliases"].append(alias)
            changed = True
    return changed


def render(value: object) -> str:
    return json.dumps(value, ensure_ascii=False, indent=2) + "\n"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--write", action="store_true")
    args = parser.parse_args()
    if args.dry_run == args.write:
        parser.error("choose exactly one of --dry-run or --write")
    aliases_by_language = verify_extraction()
    database = json.loads(DATABASE.read_text(encoding="utf-8"))
    lexicon = json.loads(LEXICON.read_text(encoding="utf-8"))
    sources = json.loads(SOURCES.read_text(encoding="utf-8"))
    honey = next((item for item in database if item["id"] == "honey"), None)
    if honey is None:
        raise ValueError("canonical honey concept is missing")
    if honey["status"] != "VEGETARIAN":
        raise ValueError("honey must remain VEGETARIAN")
    changed = False
    if SOURCE["id"] not in honey["sources"]:
        honey["sources"].append(SOURCE["id"])
        changed = True
    required_reason = "Produit et transformé par les abeilles ; origine animale au sens du projet, végétarien mais non vegan."
    if honey["reason"] != required_reason:
        honey["reason"] = required_reason
        changed = True
    changed = merge_canonical_aliases(database, honey, aliases_by_language) or changed
    changed = merge_aliases(lexicon, aliases_by_language) or changed
    existing_source = next((item for item in sources if item["id"] == SOURCE["id"]), None)
    if existing_source is None:
        sources.append(SOURCE)
        changed = True
    elif existing_source != SOURCE:
        raise ValueError(f"conflicting source definition: {SOURCE['id']}")
    print("CELEX 02001L0110 Annex I: 4 languages, 53 joined aliases, 0 ambiguous aliases")
    print(f"changes={'1' if changed else '0'}")
    if args.write and changed:
        DATABASE.write_text(render(database), encoding="utf-8")
        LEXICON.write_text(render(lexicon), encoding="utf-8")
        SOURCES.write_text(render(sources), encoding="utf-8")


if __name__ == "__main__":
    main()
