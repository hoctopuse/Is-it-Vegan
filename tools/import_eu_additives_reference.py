#!/usr/bin/env python3
"""Import the locally acquired EU additive reference into the runtime database."""

import argparse
import csv
import json
import tempfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = ROOT / "reference-input" / "eu-food-labelling" / "03-food-additives" / "EU_ADDITIVES_OFFICIAL_MULTILINGUAL_REFERENCE.csv"
DATABASE = ROOT / "app" / "src" / "main" / "assets" / "ingredients.json"
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
    return reference, database


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


def import_reference(reference, database):
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
            aliases = existing.setdefault("aliases", [])
            for official_name in official_names:
                if official_name not in aliases and official_name != existing["name"]:
                    aliases.append(official_name)
                    aliases_added_to_existing += 1
            continue

        identifier = number.lower()
        if identifier in ids:
            raise ValueError(f"generated id already exists: {identifier}")
        entry = {
            "id": identifier,
            "name": row["official_name_fr"],
            "eNumber": number,
            "aliases": official_names,
            "status": "UNCERTAIN",
            "reason": UNCERTAIN_REASON,
            "source": "eu-additives",
        }
        database.append(entry)
        by_number[number.upper()] = entry
        ids.add(identifier)
        imported.append(number)

    if any(item["status"] != before_statuses[item["id"]] for item in database if item["id"] in before_statuses):
        raise ValueError("an existing classification changed")
    if any(
        {key: value for key, value in item.items() if key != "aliases"} != before_fields[item["id"]]
        for item in database
        if item["id"] in before_fields
    ):
        raise ValueError("an existing non-alias field changed")
    if any(item["status"] != "UNCERTAIN" for item in database if item["eNumber"] in imported):
        raise ValueError("a newly imported additive is not UNCERTAIN")

    return {
        "database": database,
        "imported": imported,
        "already_present": already_present,
        "excluded": excluded,
        "aliases_added_to_existing": aliases_added_to_existing,
    }


def write_database(database):
    content = json.dumps(database, ensure_ascii=False, indent=2) + "\n"
    with tempfile.NamedTemporaryFile(
        "w", encoding="utf-8", newline="\n", dir=DATABASE.parent, delete=False
    ) as handle:
        handle.write(content)
        temporary = Path(handle.name)
    temporary.replace(DATABASE)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--write", action="store_true", help="replace the runtime database atomically")
    args = parser.parse_args()
    reference, database = read_inputs()
    validate_reference(reference)
    before_entries = len(database)
    before_aliases = sum(len(item.get("aliases", [])) for item in database)
    result = import_reference(reference, database)
    if args.write:
        write_database(result["database"])
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
