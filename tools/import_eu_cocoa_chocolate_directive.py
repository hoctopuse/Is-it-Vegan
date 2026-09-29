#!/usr/bin/env python3
"""Import reviewed CELEX 02000L0036 cocoa/chocolate sales names from local PDFs.

Only names whose exact form occurs in a local FR, NL, EN or DE consolidation
are merged.  The importer never derives one language from another.
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
PDFS = {language: BASE / f"CELEX_02000L0036-20131118_{language}_TXT.pdf" for language in ("FR", "NL", "EN", "DE")}
INGREDIENTS = ROOT / "knowledge" / "ingredients.json"
LEXICON = ROOT / "knowledge" / "ingredient_aliases_multilingual.json"
SOURCES = ROOT / "knowledge" / "sources.json"

SOURCE = {
    "id": "eu-cocoa-chocolate-directive-2000-36-20131118",
    "title": "Directive 2000/36/EC — cocoa and chocolate products intended for human consumption",
    "celex": "02000L0036",
    "url": "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=CELEX:02000L0036-20131118",
    "language": "FR, NL, EN, DE",
    "institution": "European Parliament and Council of the European Union",
    "consolidationDate": "2013-11-18",
    "localPdfPaths": [str(path.relative_to(ROOT)).replace("\\", "/") for path in PDFS.values()],
    "role": "Primary local regulatory source for Annex I cocoa and chocolate sales names and definitions.",
    "limitations": "The directive defines categories and does not make generic chocolate or a filled product vegan; recipes and fillings can contain milk or other animal ingredients.",
    "use": "Evidence for reviewed CELEX 02000L0036 aliases only; no translation is inferred.",
}

# Each spelling below is checked verbatim in the corresponding local PDF.
ALIASES = {
    "cocoa": {
        "FR": ["cacao en poudre", "cacao", "cacao maigre en poudre"],
        "NL": ["cacaopoeder", "cacao", "mager cacaopoeder"],
        "EN": ["cocoa powder", "cocoa", "fat-reduced cocoa powder"],
        "DE": ["Kakaopulver", "Kakao", "mageres Kakaopulver"],
    },
    "cocoa_butter": {
        "FR": ["beurre de cacao"], "NL": ["cacaoboter"], "EN": ["cocoa butter"], "DE": ["Kakaobutter"],
    },
    "powdered_chocolate": {
        "FR": ["chocolat en poudre", "cacao sucré"],
        "NL": ["chocoladepoeder", "gesuikerd cacaopoeder"],
        "EN": ["powdered chocolate", "sweetened cocoa powder"],
        "DE": ["Schokoladenpulver", "gezuckerter Kakao"],
    },
    "chocolate": {
        "FR": ["chocolat", "chocolat de couverture"],
        "NL": ["chocolade", "chocoladecouverture"],
        "EN": ["chocolate", "couverture chocolate"],
        "DE": ["Schokolade", "Schokoladenkuvertüre"],
    },
    "milk_chocolate": {
        "FR": ["chocolat au lait", "chocolat de ménage au lait"],
        "NL": ["melkchocolade", "huishoudmelkchocolade"],
        "EN": ["milk chocolate", "family milk chocolate"],
        "DE": ["Milchschokolade", "Haushaltsmilchschokolade"],
    },
    "white_chocolate": {
        "FR": ["chocolat blanc"], "NL": ["witte chocolade"], "EN": ["white chocolate"], "DE": ["Weiße Schokolade"],
    },
    "filled_chocolate": {
        "FR": ["chocolat fourré"], "NL": ["gevulde chocolade"], "EN": ["filled chocolate"], "DE": ["Gefüllte Schokolade"],
    },
    "chocolate_confection": {
        "FR": ["bonbon de chocolat", "praline"],
        "NL": ["chocoladebonbon", "praline"],
        "EN": ["praline"], "DE": ["Praline", "Schokoladebonbon"],
    },
}

NEW = {
    "cocoa_butter": ("Beurre de cacao", "VEGAN", "Matière grasse obtenue à partir de fèves de cacao : d’origine végétale."),
    "powdered_chocolate": ("Chocolat en poudre", "VEGAN", "Définition réglementaire : mélange de cacao en poudre et de sucres."),
    "chocolate": ("Chocolat", "UNCERTAIN", "La dénomination réglementaire ne garantit pas l’absence de lait ni d’autres ingrédients animaux dans une recette commerciale."),
    "milk_chocolate": ("Chocolat au lait", "VEGETARIAN", "La définition réglementaire exige du lait ou des produits laitiers : végétarien mais non vegan."),
    "white_chocolate": ("Chocolat blanc", "VEGETARIAN", "La définition réglementaire exige du lait ou des produits laitiers : végétarien mais non vegan."),
    "filled_chocolate": ("Chocolat fourré", "UNCERTAIN", "La garniture peut contenir des produits laitiers, des œufs ou du miel : lire la liste complète des ingrédients."),
    "chocolate_confection": ("Bonbon de chocolat ou praline", "UNCERTAIN", "La composition et la garniture ne sont pas déterminées par la seule dénomination."),
}

# These two historical multilingual forms were attached to the broad `cocoa`
# concept.  Annex I(1) identifies them specifically as cocoa butter.  Their
# surface forms are retained; only the canonical target is corrected.
TRANSFERRED_ALIASES = {
    ("NL", "cacaoboter"): "cocoa_butter",
    ("DE", "Kakaobutter"): "cocoa_butter",
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
    return "\n".join(page.extract_text() or "" for page in PdfReader(path).pages)


def verify_pdfs() -> None:
    text = {language: compact(extract(path)) for language, path in PDFS.items()}
    missing = [f"{concept}/{language}/{alias}" for concept, languages in ALIASES.items()
               for language, aliases in languages.items() for alias in aliases
               if compact(alias) not in text[language]]
    if missing:
        raise ValueError(f"unverified local-PDF aliases: {missing}")


def render(value: object) -> str:
    return json.dumps(value, ensure_ascii=False, indent=2) + "\n"


def merge(database: list[dict], lexicon: dict, sources: list[dict]) -> bool:
    if len(database) < 464 or len(lexicon.get("mappings", [])) < 1906:
        raise ValueError("historical knowledge is missing; refusing a destructive import")
    ids = {entry["id"] for entry in database}
    if len(ids) != len(database):
        raise ValueError("duplicate canonical ingredient id")
    changed = False
    for (language, alias), target in TRANSFERRED_ALIASES.items():
        old_entry = next((item for item in lexicon["aliases"] if item["canonicalId"] == "cocoa" and item["language"] == language), None)
        if old_entry and alias in old_entry["aliases"]:
            old_entry["aliases"].remove(alias)
            target_entry = next((item for item in lexicon["aliases"] if item["canonicalId"] == target and item["language"] == language), None)
            if target_entry is None:
                target_entry = {"canonicalId": target, "language": language, "aliases": [], "ocrVariants": []}
                lexicon["aliases"].append(target_entry)
            if alias not in target_entry["aliases"]:
                target_entry["aliases"].append(alias)
            changed = True
        for mapping in lexicon.get("mappings", []):
            if mapping["conceptId"] == "cocoa" and mapping["language"] == language and mapping["normalizedForm"] == normalise(alias):
                mapping.update({"conceptId": target, "mappingGroup": "cocoa-chocolate-directive", "relation": "REGULATORY_ALIAS", "source": SOURCE["id"]})
                changed = True
    owners = {normalise(alias): entry["id"] for entry in database for alias in entry.get("aliases", [])}
    multilingual = {(entry["language"], normalise(alias)): entry["canonicalId"] for entry in lexicon["aliases"]
                    for alias in entry.get("aliases", []) + entry.get("ocrVariants", [])}
    mapping_keys = {(item["conceptId"], item["language"], item["normalizedForm"]) for item in lexicon.get("mappings", [])}
    for concept, languages in ALIASES.items():
        if concept not in ids:
            name, status, reason = NEW[concept]
            entry = {"id": concept, "name": name, "aliases": [], "status": status, "reason": reason, "sources": [SOURCE["id"]]}
            if status == "UNCERTAIN":
                entry["possibleOriginNote"] = {"origins": ["PLANT", "ANIMAL", "EGG"], "variability": "RAW_MATERIAL_AND_PROCESS", "sourceIds": [SOURCE["id"]], "confidence": "REGULATORY_CATEGORY"}
            database.append(entry); ids.add(concept); changed = True
        entry = next(item for item in database if item["id"] == concept)
        expected_status = "VEGAN" if concept == "cocoa" else NEW[concept][1]
        if entry["status"] != expected_status:
            raise ValueError(f"incompatible historical status for {concept}: {entry['status']}")
        if SOURCE["id"] not in entry["sources"]:
            entry["sources"].append(SOURCE["id"]); changed = True
        for language, aliases in languages.items():
            lex_entry = next((item for item in lexicon["aliases"] if item["canonicalId"] == concept and item["language"] == language), None)
            if lex_entry is None:
                lex_entry = {"canonicalId": concept, "language": language, "aliases": [], "ocrVariants": []}
                lexicon["aliases"].append(lex_entry); changed = True
            for alias in aliases:
                if owners.get(normalise(alias), concept) != concept or multilingual.get((language, normalise(alias)), concept) != concept:
                    raise ValueError(f"alias collision: {language}/{alias}")
                if alias not in entry["aliases"]: entry["aliases"].append(alias); changed = True
                if alias not in lex_entry["aliases"]: lex_entry["aliases"].append(alias); changed = True
                mapping = {"surfaceForm": alias, "language": language, "conceptId": concept, "mappingGroup": "cocoa-chocolate-directive", "relation": "REGULATORY_ALIAS", "normalizedForm": normalise(alias), "source": SOURCE["id"], "confidence": "REVIEWED"}
                key = (concept, language, mapping["normalizedForm"])
                if key not in mapping_keys:
                    lexicon.setdefault("mappings", []).append(mapping); mapping_keys.add(key); changed = True
    old_source = next((item for item in sources if item["id"] == SOURCE["id"]), None)
    if old_source is None: sources.append(SOURCE); changed = True
    elif old_source != SOURCE: raise ValueError(f"conflicting source definition: {SOURCE['id']}")
    return changed


def main() -> None:
    parser = argparse.ArgumentParser()
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--check", action="store_true"); mode.add_argument("--dry-run", action="store_true"); mode.add_argument("--write", action="store_true")
    args = parser.parse_args(); verify_pdfs()
    database = json.loads(INGREDIENTS.read_text(encoding="utf-8")); lexicon = json.loads(LEXICON.read_text(encoding="utf-8")); sources = json.loads(SOURCES.read_text(encoding="utf-8"))
    changed = merge(database, lexicon, sources)
    total = sum(len(aliases) for languages in ALIASES.values() for aliases in languages.values())
    print(f"CELEX 02000L0036 Annex I: {len(NEW)} new concepts, {total} aliases, 4 joined languages, changes={int(changed)}")
    if args.check and changed: raise SystemExit("cocoa/chocolate directive import is stale; run: python tools/import_eu_cocoa_chocolate_directive.py --write")
    if args.write and changed:
        INGREDIENTS.write_text(render(database), encoding="utf-8"); LEXICON.write_text(render(lexicon), encoding="utf-8"); SOURCES.write_text(render(sources), encoding="utf-8")


if __name__ == "__main__": main()
