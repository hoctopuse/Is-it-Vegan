#!/usr/bin/env python3
"""Validate the editable knowledge base and generate the Android asset."""

from __future__ import annotations

import json
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "knowledge" / "ingredients.json"
OUTPUT = ROOT / "app" / "src" / "main" / "assets" / "ingredients.json"
VALID_STATUSES = {"VEGAN", "NON_VEGAN", "UNCERTAIN"}


def fail(message: str) -> None:
    print(f"ERROR: {message}", file=sys.stderr)
    raise SystemExit(1)


def main() -> None:
    entries = json.loads(SOURCE.read_text(encoding="utf-8"))
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

        entry_aliases = entry.get("aliases")
        if not isinstance(entry_aliases, list) or not entry_aliases:
            fail(f"{identifier}: aliases are required")
        for alias in entry_aliases:
            key = alias.casefold().strip()
            previous = aliases.get(key)
            if previous and previous != identifier:
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
        })

    rendered = json.dumps(exported, ensure_ascii=False, indent=2) + "\n"
    if "--check" in sys.argv:
        if not OUTPUT.exists() or OUTPUT.read_text(encoding="utf-8") != rendered:
            fail("Android asset is stale; run: python3 tools/build_ingredients.py")
        print(f"Knowledge base is valid and {OUTPUT.relative_to(ROOT)} is current.")
        return

    OUTPUT.write_text(rendered, encoding="utf-8")
    print(f"Generated {OUTPUT.relative_to(ROOT)}: {len(exported)} entries; {dict(Counter(x['status'] for x in exported))}")


if __name__ == "__main__":
    main()
