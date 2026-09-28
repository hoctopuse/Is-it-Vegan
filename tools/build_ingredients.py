#!/usr/bin/env python3
"""Validate the editable knowledge base and generate the Android asset."""

from __future__ import annotations

import json
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "knowledge" / "ingredients.json"
SOURCES = ROOT / "knowledge" / "sources.json"
OUTPUT = ROOT / "app" / "src" / "main" / "assets" / "ingredients.json"
ALIASES_SOURCE = ROOT / "knowledge" / "ingredient_aliases_multilingual.json"
ALIASES_OUTPUT = ROOT / "app" / "src" / "main" / "assets" / "ingredient_aliases_multilingual.json"
VALID_STATUSES = {"VEGAN", "VEGETARIAN", "NON_VEGAN", "UNCERTAIN"}
VALID_POSSIBLE_ORIGINS = {"PLANT", "ANIMAL", "EGG", "SYNTHETIC", "MICROBIAL", "MARINE"}
VALID_ORIGIN_VARIABILITY = {"RAW_MATERIAL_AND_PROCESS", "PRODUCTION_METHOD", "MANUFACTURER"}


def fail(message: str) -> None:
    print(f"ERROR: {message}", file=sys.stderr)
    raise SystemExit(1)


def main() -> None:
    entries = json.loads(SOURCE.read_text(encoding="utf-8"))
    source_ids = {item.get("id") for item in json.loads(SOURCES.read_text(encoding="utf-8")) if item.get("id")}
    ids: set[str] = set()
    aliases: dict[str, str] = {}
    exported: list[dict[str, object]] = []

    for entry in entries:
        identifier = entry.get("id")
        if not isinstance(identifier, str) or not identifier:
            fail("an entry has no id")
        if identifier in ids:
            fail(f"duplicate id: {identifier}")
        ids.add(identifier)

        if entry.get("status") not in VALID_STATUSES:
            fail(f"{identifier}: invalid status")
        if not entry.get("reason") or not entry.get("sources"):
            fail(f"{identifier}: reason and sources are required")
        note = entry.get("possibleOriginNote")
        if note is not None:
            if entry["status"] != "UNCERTAIN":
                fail(f"{identifier}: possibleOriginNote is only valid for UNCERTAIN entries")
            if not isinstance(note, dict):
                fail(f"{identifier}: possibleOriginNote must be an object")
            origins = note.get("origins")
            if not isinstance(origins, list) or not origins or set(origins) - VALID_POSSIBLE_ORIGINS:
                fail(f"{identifier}: possibleOriginNote.origins is invalid")
            if note.get("variability") not in VALID_ORIGIN_VARIABILITY:
                fail(f"{identifier}: possibleOriginNote.variability is invalid")
            note_sources = note.get("sourceIds")
            if not isinstance(note_sources, list) or not note_sources or set(note_sources) - source_ids:
                fail(f"{identifier}: possibleOriginNote.sourceIds is invalid")
            if not isinstance(note.get("confidence"), str) or not note["confidence"].strip():
                fail(f"{identifier}: possibleOriginNote.confidence is required")

        entry_aliases = entry.get("aliases")
        if not isinstance(entry_aliases, list) or not entry_aliases:
            fail(f"{identifier}: aliases are required")
        for alias in entry_aliases:
            key = alias.casefold().strip()
            previous = aliases.get(key)
            # E470b and E572 share the same official names in Annex II-B.  Both
            # concepts are explicitly uncertain; retain this documented ambiguity
            # instead of silently reassigning a historical alias.
            permitted_collision = {previous, identifier} == {"e470b", "e572"}
            if previous and previous != identifier and not permitted_collision:
                fail(f"alias '{alias}' belongs to both {previous} and {identifier}")
            aliases[key] = identifier

        exported.append({
            "id": identifier,
            "name": entry["name"],
            "eNumber": entry.get("eNumber", ""),
            "aliases": entry_aliases,
            "status": entry["status"],
            "reason": entry["reason"],
            "source": ", ".join(entry["sources"]),
            **({"possibleOriginNote": entry["possibleOriginNote"]} if note is not None else {}),
        })

    rendered = json.dumps(exported, ensure_ascii=False, indent=2) + "\n"
    if not ALIASES_SOURCE.exists():
        fail(f"missing multilingual alias source: {ALIASES_SOURCE.relative_to(ROOT)}")
    aliases_rendered = ALIASES_SOURCE.read_bytes()
    if "--check" in sys.argv:
        if not OUTPUT.exists() or OUTPUT.read_text(encoding="utf-8") != rendered:
            fail("Android asset is stale; run: python3 tools/build_ingredients.py")
        if not ALIASES_OUTPUT.exists() or ALIASES_OUTPUT.read_bytes() != aliases_rendered:
            fail("Android multilingual alias asset is stale; run: python3 tools/build_ingredients.py")
        print(f"Knowledge base is valid and {OUTPUT.relative_to(ROOT)} is current.")
        return

    OUTPUT.write_text(rendered, encoding="utf-8")
    ALIASES_OUTPUT.write_bytes(aliases_rendered)
    print(f"Generated {OUTPUT.relative_to(ROOT)}: {len(exported)} entries; {dict(Counter(x['status'] for x in exported))}")


if __name__ == "__main__":
    main()
