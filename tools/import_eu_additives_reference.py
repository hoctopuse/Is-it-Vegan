#!/usr/bin/env python3
"""Synchronize the EU additive reference into editorial knowledge sources."""

import argparse
import csv
import json
import tempfile
import unicodedata
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = ROOT / "reference-input" / "eu-food-labelling" / "03-food-additives" / "EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv"
DATABASE = ROOT / "knowledge" / "ingredients.json"
RUNTIME_DATABASE = ROOT / "app" / "src" / "main" / "assets" / "ingredients.json"
LEXICON = ROOT / "knowledge" / "ingredient_aliases_multilingual.json"
RUNTIME_LEXICON = ROOT / "app" / "src" / "main" / "assets" / "ingredient_aliases_multilingual.json"
LANGUAGE_COLUMNS = (
    "official_name_fr",
    "official_name_nl",
    "official_name_en",
    "official_name_de",
)
IMPORTABLE_STATUSES = {"COMPLETE_4_LANGUAGES", "PARTIAL_OFFICIAL"}
AMBIGUOUS_STATUS = "AMBIGUOUS_GROUP_OR_RANGE"
UNCERTAIN_REASON = (
    "Additif autorisé dans l’Union européenne ; son autorisation ne documente "
    "pas son origine ni son procédé de fabrication."
)


def read_inputs():
    with REFERENCE.open(encoding="utf-8-sig", newline="") as handle:
        reference = list(csv.DictReader(handle))
    with DATABASE.open(encoding="utf-8") as handle:
        database = json.load(handle)
    with RUNTIME_DATABASE.open(encoding="utf-8") as handle:
        runtime_database = json.load(handle)
    lexicon_path = LEXICON if LEXICON.exists() else RUNTIME_LEXICON
    with lexicon_path.open(encoding="utf-8") as handle:
        lexicon = json.load(handle)
    return reference, database, runtime_database, lexicon


def validate_reference(reference):
    numbers = [row["e_number"] for row in reference]
    statuses = [row["verification_status"] for row in reference]
    if len(reference) != 340 or len(set(numbers)) != 340:
        raise ValueError("expected 340 unique regulatory E-numbers")
    if statuses.count("COMPLETE_4_LANGUAGES") != 337:
        raise ValueError("expected 337 complete multilingual rows")
    if statuses.count("PARTIAL_OFFICIAL") != 1:
        raise ValueError("expected one partial official row")
    if statuses.count(AMBIGUOUS_STATUS) != 2:
        raise ValueError("expected two ambiguous rows")
    partial = [row for row in reference if row["verification_status"] == "PARTIAL_OFFICIAL"]
    if not (
        partial[0]["e_number"] == "E322a"
        and all(partial[0][column] for column in LANGUAGE_COLUMNS[:3])
        and not partial[0]["official_name_de"]
    ):
        raise ValueError("E322a must be the sole partial row and have no German name")
    ambiguous = {row["e_number"] for row in reference if row["verification_status"] == AMBIGUOUS_STATUS}
    if ambiguous != {"E345", "E345(i)"}:
        raise ValueError("unexpected ambiguous reference rows")


def unique(values):
    result = []
    for value in values:
        if value and value not in result:
            result.append(value)
    return result


def normalized(value):
    decomposed = "".join(
        character for character in unicodedata.normalize("NFKD", value.casefold().strip())
        if not unicodedata.combining(character)
    )
    compact = re.sub(r"[^a-z0-9]+", " ", decomposed).strip()
    return re.sub(r"^e\s+(?=\d)", "e", compact)


def source_entry(runtime_item):
    """Convert the prior generated representation without losing historical data."""
    return {
        "id": runtime_item["id"],
        "name": runtime_item["name"],
        "eNumber": runtime_item.get("eNumber", ""),
        "aliases": runtime_item["aliases"],
        "status": runtime_item["status"],
        "reason": runtime_item["reason"],
        "sources": [item.strip() for item in runtime_item.get("source", "").split(",") if item.strip()],
    }


def merge_runtime_history(database, runtime_database):
    """Recover entries previously written directly to the generated asset exactly once."""
    by_id = {item["id"]: item for item in database}
    for runtime_item in runtime_database:
        if runtime_item["id"] not in by_id:
            editorial = source_entry(runtime_item)
            database.append(editorial)
            by_id[editorial["id"]] = editorial


def remove_injected_conflicting_aliases(reference, database):
    """Keep historical aliases, but move direct-import copies into the language lexicon.

    E470b/E572 is a documented deliberate ambiguity and remains in the canonical
    matcher.  Other collisions came from the old asset-only import and would make
    the editorial validator choose one concept implicitly.
    """
    by_number = {row["e_number"]: row for row in reference}
    owners = {}
    for entry in database:
        for alias in entry.get("aliases", []):
            owners.setdefault(alias.casefold().strip(), set()).add(entry["id"])
    for entry in database:
        number = entry.get("eNumber")
        row = by_number.get(number)
        if not row or number == "E470b":
            continue
        if number == "E901":
            # The direct asset import copied the older generic beeswax aliases
            # onto E901 although its Annex II-B name is more specific.  Keep the
            # historical beeswax concept and recognize the official forms only
            # through the per-language lexicon.
            entry["aliases"] = [
                alias for alias in entry["aliases"]
                if len(owners.get(alias.casefold().strip(), set())) == 1
            ] or [number, number.replace("E", "E ", 1)]
        official = {row[column].casefold().strip() for column in LANGUAGE_COLUMNS if row[column].strip()}
        conflicting = {alias for alias in official if len(owners.get(alias, set())) > 1}
        if conflicting:
            entry["aliases"] = [
                alias for alias in entry["aliases"] if alias.casefold().strip() not in conflicting
            ]
            if not entry["aliases"]:
                entry["aliases"] = [number, number.replace("E", "E ", 1)]


def add_official_aliases(lexicon, row, identifier):
    entries = lexicon.setdefault("aliases", [])
    seen = {}
    for entry in entries:
        for alias in entry.get("aliases", []) + entry.get("ocrVariants", []):
            seen[(entry["language"], normalized(alias))] = entry["canonicalId"]
    for language, column in zip(("FR", "NL", "EN", "DE"), LANGUAGE_COLUMNS):
        alias = row[column].strip()
        if not alias:
            continue
        key = (language, normalized(alias))
        owner = seen.get(key)
        if owner and owner != identifier:
            raise ValueError(f"multilingual alias collision: {language} {alias!r} belongs to {owner}")
        matching_entry = next((entry for entry in entries if entry["canonicalId"] == identifier and entry["language"] == language), None)
        if matching_entry is None:
            matching_entry = {"canonicalId": identifier, "language": language, "aliases": [], "ocrVariants": []}
            entries.append(matching_entry)
        existing_terms = matching_entry["aliases"] + matching_entry["ocrVariants"]
        equivalent_alias_index = next(
            (index for index, term in enumerate(matching_entry["aliases"]) if normalized(term) == normalized(alias)),
            None
        )
        if equivalent_alias_index is not None:
            # Retain the official source spelling/case while avoiding a second
            # normalized surface in the runtime lexicon.
            matching_entry["aliases"][equivalent_alias_index] = alias
        elif normalized(alias) not in {normalized(term) for term in existing_terms}:
            matching_entry["aliases"].append(alias)
        seen[key] = identifier


def consolidate_lexicon(lexicon):
    """The pre-existing lexicon may have several blocks for one concept/language."""
    merged = {}
    for entry in lexicon.get("aliases", []):
        key = (entry["canonicalId"], entry["language"])
        target = merged.setdefault(key, {
            "canonicalId": entry["canonicalId"], "language": entry["language"],
            "aliases": [], "ocrVariants": []
        })
        seen = {normalized(term) for term in target["aliases"] + target["ocrVariants"]}
        for field in ("aliases", "ocrVariants"):
            for term in entry.get(field, []):
                if normalized(term) not in seen:
                    target[field].append(term)
                    seen.add(normalized(term))
    lexicon["aliases"] = list(merged.values())


def import_reference(reference, database, runtime_database, lexicon):
    merge_runtime_history(database, runtime_database)
    remove_injected_conflicting_aliases(reference, database)
    by_number = {
        item["eNumber"].upper(): item
        for item in database
        if item.get("eNumber")
    }
    if len(by_number) != sum(bool(item.get("eNumber")) for item in database):
        raise ValueError("runtime database contains duplicate E-numbers")
    ids = {item["id"] for item in database}
    before_statuses = {item["id"]: item["status"] for item in database}
    before_fields = {
        item["id"]: {
            key: value
            for key, value in item.items()
            if key != "aliases"
        }
        for item in database
    }
    imported = []
    already_present = []
    excluded = []
    aliases_added_to_existing = 0

    for row in reference:
        number = row["e_number"]
        status = row["verification_status"]
        if status == AMBIGUOUS_STATUS:
            excluded.append(number)
            continue
        if status not in IMPORTABLE_STATUSES:
            raise ValueError(f"unsupported verification status for {number}: {status}")
        official_names = unique(row[column] for column in LANGUAGE_COLUMNS)
        if not official_names:
            raise ValueError(f"no official name available for {number}")

        existing = by_number.get(number.upper())
        if existing is not None:
            already_present.append(number)
            before_aliases = sum(len(entry.get("aliases", [])) for entry in lexicon.get("aliases", []))
            add_official_aliases(lexicon, row, existing["id"])
            aliases_added_to_existing += sum(len(entry.get("aliases", [])) for entry in lexicon.get("aliases", [])) - before_aliases
            continue

        identifier = number.lower()
        if identifier in ids:
            raise ValueError(f"generated id already exists: {identifier}")
        entry = {
            "id": identifier,
            "name": row["official_name_fr"],
            "eNumber": number,
            "aliases": [number, number.replace("E", "E ", 1)],
            "status": "UNCERTAIN",
            "reason": UNCERTAIN_REASON,
            "sources": ["eu-additives"],
        }
        database.append(entry)
        by_number[number.upper()] = entry
        ids.add(identifier)
        imported.append(number)
        add_official_aliases(lexicon, row, identifier)

    consolidate_lexicon(lexicon)

    if any(item["status"] != before_statuses[item["id"]] for item in database if item["id"] in before_statuses):
        raise ValueError("an existing classification changed")
    if any(
        {key: value for key, value in item.items() if key != "aliases"} != before_fields[item["id"]]
        for item in database
        if item["id"] in before_fields
    ):
        raise ValueError("an existing non-alias field changed")
    if any(item["status"] != "UNCERTAIN" for item in database if item.get("eNumber") in imported):
        raise ValueError("a newly imported additive is not UNCERTAIN")

    return {
        "database": database,
        "lexicon": lexicon,
        "imported": imported,
        "already_present": already_present,
        "excluded": excluded,
        "aliases_added_to_existing": aliases_added_to_existing,
    }


def write_json(path, data):
    content = json.dumps(data, ensure_ascii=False, indent=2) + "\n"
    with tempfile.NamedTemporaryFile(
        "w", encoding="utf-8", newline="\n", dir=path.parent, delete=False
    ) as handle:
        handle.write(content)
        temporary = Path(handle.name)
    temporary.replace(path)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--write", action="store_true", help="replace the runtime database atomically")
    args = parser.parse_args()
    reference, database, runtime_database, lexicon = read_inputs()
    validate_reference(reference)
    before_entries = len(database)
    before_aliases = sum(len(item.get("aliases", [])) for item in database)
    result = import_reference(reference, database, runtime_database, lexicon)
    if args.write:
        write_json(DATABASE, result["database"])
        write_json(LEXICON, result["lexicon"])
    summary = {
        "mode": "write" if args.write else "dry-run",
        "entries_before": before_entries,
        "entries_after": len(result["database"]),
        "aliases_before": before_aliases,
        "aliases_after": sum(len(item.get("aliases", [])) for item in result["database"]),
        "imported": len(result["imported"]),
        "already_present": len(result["already_present"]),
        "excluded": result["excluded"],
        "aliases_added_to_existing": result["aliases_added_to_existing"],
    }
    print(json.dumps(summary, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
