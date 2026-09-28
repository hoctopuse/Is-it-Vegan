#!/usr/bin/env python3
"""Import reviewed CELEX 02001L0114 product names from the local PDFs only."""

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
PDFS = {language: BASE / f"CELEX_02001L0114-20260614_{language}_TXT.pdf" for language in ("FR", "NL", "EN", "DE")}

# Each string is a designation or wording present in Annex I or II of the
# corresponding local PDF.  The two product concepts stay deliberately broad:
# preparation and fat-content variants are aliases, not duplicate concepts.
OFFICIAL_ALIASES = {
    "milk": {
        "FR": [
            "lait", "lait partiellement déshydraté", "lait concentré riche en matières grasses",
            "lait concentré", "lait concentré partiellement écrémé", "lait concentré écrémé",
            "lait concentré sucré", "lait concentré sucré partiellement écrémé",
            "lait concentré sucré écrémé", "lait totalement déshydraté",
            "lait en poudre riche en matières grasses", "poudre de lait riche en matières grasses",
            "lait en poudre entier", "poudre de lait entier", "lait en poudre partiellement écrémé",
            "poudre de lait partiellement écrémé", "lait en poudre écrémé", "poudre de lait écrémé",
            "lait demi-écrémé concentré", "lait de mi-écrémé concentré non sucré",
            "lait demi-écrémé concentré sucré", "lait demi-écrémé en poudre",
        ],
        "NL": [
            "melk", "gedeeltelijk gedehydrateerde melk", "geëvaporeerde melk met hoog vetgehalte",
            "geëvaporeerde volle melk", "geëvaporeerde gedeeltelijk afgeroomde melk",
            "geëvaporeerde magere melk", "gecondenseerde volle melk met suiker",
            "gecondenseerde gedeeltelijk afgeroomde melk met suiker",
            "gecondenseerde magere melk met suiker", "geheel gedehydrateerde melk",
            "melkpoeder", "melkpoeder met hoog vetgehalte", "volle melkpoeder",
            "melkpoeder van gedeeltelijk afgeroomde melk", "magere melkpoeder",
            "geëvaporeerde halfvolle melk", "halfvolle koffiemelk",
            "halfvolle melkpoeder", "koffiemelk",
        ],
        "EN": [
            "milk", "partly dehydrated milk", "condensed high-fat milk", "condensed milk",
            "condensed, partly skimmed milk", "condensed skimmed milk", "sweetened condensed milk",
            "sweetened condensed, partly skimmed milk", "sweetened condensed skimmed milk",
            "totally dehydrated milk", "milk powder", "dried high-fat milk", "high-fat milk powder",
            "dried whole milk", "whole milk powder", "dried partly skimmed milk",
            "partly skimmed-milk powder", "dried skimmed milk", "skimmed-milk powder",
            "evaporated milk", "evaporated semi-skimmed milk", "semi-skimmed milk powder",
            "dried semi-skimmed milk",
        ],
        "DE": [
            "Milch", "Eingedickte Milch", "Kondensmilch mit hohem Fettgehalt", "Kondensmilch",
            "kondensierte Vollmilch", "Teilentrahmte Kondensmilch", "Kondensmagermilch",
            "kondensierte Magermilch", "Gezuckerte Kondensmilch", "gezuckerte kondensierte Vollmilch",
            "Gezuckerte teilentrahmte Kondensmilch", "gezuckerte teilentrahmte kondensierte Milch",
            "Gezuckerte Kondensmagermilch", "gezuckerte kondensierte Magermilch", "Trockenmilch",
            "Milchpulver", "Milchpulver mit hohem Fettgehalt", "Vollmilchpulver",
            "Teilentrahmtes Milchpulver", "Magermilchpulver", "kondensierte Kaffeesahne",
        ],
    },
    # Annex II explicitly says these are alternative designations for Annex I
    # (2)(a).  They enrich the existing dairy-cream concept rather than creating
    # a duplicate "cream powder" concept.
    "cream": {
        "FR": ["crème en poudre"],
        "NL": ["roompoeder"],
        "EN": [],
        "DE": ["Rahmpulver", "Sahnepulver"],
    },
}

SOURCE = {
    "id": "eu-preserved-milk-directive-2001-114-20260614",
    "title": "Council Directive 2001/114/EC — relating to certain preserved partly or wholly dehydrated milks for human consumption",
    "celex": "02001L0114",
    "url": "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02001L0114-20260614",
    "language": "FR, NL, EN, DE",
    "institution": "Council of the European Union",
    "consolidationDate": "2026-06-14",
    "localPdfPaths": [str(path.relative_to(ROOT)).replace("\\", "/") for path in PDFS.values()],
    "role": "Primary local regulatory source for preserved partly and wholly dehydrated milk definitions and multilingual product names in Annexes I and II.",
    "limitations": "Defines regulated dairy product names and composition only; it does not classify composite products, flavour wording, plant drinks or an individual label beyond its declared ingredients.",
    "use": "Evidence for CELEX 02001L0114 multilingual aliases attached to the existing milk and cream concepts.",
}


def normalize(value: str) -> str:
    value = unicodedata.normalize("NFKC", value).replace("\u00ad", "")
    value = re.sub(r"\s+", " ", value).strip().casefold()
    return "".join(character for character in unicodedata.normalize("NFD", value) if not unicodedata.combining(character))


def compact(value: str) -> str:
    # PDF extraction inserts spaces within words and soft hyphens at line wraps.
    # Removing only whitespace and soft hyphens makes the check robust without
    # rewriting any editorial spelling.
    return re.sub(r"\s+", "", normalize(value))


def pdf_text(path: Path) -> str:
    if not path.is_file():
        raise ValueError(f"missing local PDF: {path.relative_to(ROOT)}")
    return "\n".join(page.extract_text() or "" for page in PdfReader(path).pages)


def verify_extraction() -> None:
    texts = {language: compact(pdf_text(path)) for language, path in PDFS.items()}
    missing = {
        f"{concept}/{language}": [alias for alias in aliases if compact(alias) not in texts[language]]
        for concept, languages in OFFICIAL_ALIASES.items()
        for language, aliases in languages.items()
    }
    missing = {key: aliases for key, aliases in missing.items() if aliases}
    if missing:
        raise ValueError(f"unjoined or ambiguous official aliases: {missing}")
    missing_languages = [
        language for language in PDFS if not OFFICIAL_ALIASES["milk"].get(language)
    ]
    if missing_languages:
        raise ValueError(f"missing required milk language aliases: {missing_languages}")


def merge_canonical_aliases(database: list[dict]) -> bool:
    owners: dict[str, str] = {}
    for entry in database:
        for alias in entry.get("aliases", []):
            owners.setdefault(normalize(alias), entry["id"])
    changed = False
    for concept, languages in OFFICIAL_ALIASES.items():
        entry = next((item for item in database if item["id"] == concept), None)
        if entry is None:
            raise ValueError(f"canonical {concept} concept is missing")
        if entry["status"] != "VEGETARIAN":
            raise ValueError(f"{concept} must remain VEGETARIAN")
        for alias in (alias for values in languages.values() for alias in values):
            owner = owners.get(normalize(alias))
            if owner and owner != concept:
                raise ValueError(f"canonical alias collision: {alias!r} belongs to {owner}")
            if alias not in entry["aliases"]:
                entry["aliases"].append(alias)
                owners[normalize(alias)] = concept
                changed = True
        if SOURCE["id"] not in entry["sources"]:
            entry["sources"].append(SOURCE["id"])
            changed = True
    milk = next(item for item in database if item["id"] == "milk")
    reason = "Produit laitier d’origine animale : végétarien mais non vegan."
    if milk["reason"] != reason:
        milk["reason"] = reason
        changed = True
    return changed


def merge_lexicon(lexicon: dict) -> bool:
    changed = False
    entries = lexicon.setdefault("aliases", [])
    owners = {
        (entry["language"], normalize(alias)): entry["canonicalId"]
        for entry in entries
        for alias in entry.get("aliases", []) + entry.get("ocrVariants", [])
    }
    for concept, languages in OFFICIAL_ALIASES.items():
        for language, aliases in languages.items():
            if not aliases:
                continue
            entry = next((item for item in entries if item["canonicalId"] == concept and item["language"] == language), None)
            if entry is None:
                entry = {"canonicalId": concept, "language": language, "aliases": [], "ocrVariants": []}
                entries.append(entry)
                changed = True
            for alias in aliases:
                owner = owners.get((language, normalize(alias)))
                if owner and owner != concept:
                    raise ValueError(f"lexicon alias collision: {language} {alias!r} belongs to {owner}")
                if alias not in entry["aliases"]:
                    entry["aliases"].append(alias)
                    owners[(language, normalize(alias))] = concept
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
    verify_extraction()
    database = json.loads(DATABASE.read_text(encoding="utf-8"))
    lexicon = json.loads(LEXICON.read_text(encoding="utf-8"))
    sources = json.loads(SOURCES.read_text(encoding="utf-8"))
    changed = merge_canonical_aliases(database)
    changed = merge_lexicon(lexicon) or changed
    existing_source = next((item for item in sources if item["id"] == SOURCE["id"]), None)
    if existing_source is None:
        sources.append(SOURCE)
        changed = True
    elif existing_source != SOURCE:
        raise ValueError(f"conflicting source definition: {SOURCE['id']}")
    totals = {language: sum(len(aliases.get(language, [])) for aliases in OFFICIAL_ALIASES.values()) for language in PDFS}
    print(f"CELEX 02001L0114 Annexes I-II: {totals}, 0 ambiguous aliases")
    print(f"changes={'1' if changed else '0'}")
    if args.write and changed:
        DATABASE.write_text(render(database), encoding="utf-8")
        LEXICON.write_text(render(lexicon), encoding="utf-8")
        SOURCES.write_text(render(sources), encoding="utf-8")


if __name__ == "__main__":
    main()
