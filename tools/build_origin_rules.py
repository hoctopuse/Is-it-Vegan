#!/usr/bin/env python3
"""Validate the editable origin-rule table and generate its Android asset."""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "knowledge" / "origin_qualifier_rules.json"
OUTPUT = ROOT / "app" / "src" / "main" / "assets" / "origin_qualifier_rules.json"
ACTIVATIONS = {"ACTIVE", "REVIEW_BEFORE_ENGINE_USE"}
VEGAN_STATUSES = {"VEGAN", "NOT_VEGAN", "UNCERTAIN"}
VEGETARIAN_STATUSES = {"VEGETARIAN_OR_VEGAN", "VEGETARIAN", "NON_VEGETARIAN", "UNCERTAIN"}
ATTACHMENT_FORMS = {"PARENTHETICAL", "DIRECT_SUFFIX"}


def fail(message: str) -> None:
    print(f"ERROR: {message}", file=sys.stderr)
    raise SystemExit(1)


def non_empty_strings(value: object, path: str) -> list[str]:
    if not isinstance(value, list) or any(not isinstance(item, str) or not item for item in value):
        fail(f"{path} must be an array of non-empty strings")
    return value


def main() -> None:
    data = json.loads(SOURCE.read_text(encoding="utf-8"))
    if not isinstance(data, dict) or data.get("schemaVersion") != 2:
        fail("schemaVersion must be 2")

    sources = {source.get("id") for source in data.get("sources", []) if isinstance(source, dict)}
    rule_ids: set[str] = set()
    signatures: dict[tuple[str, str, str], tuple[str, str]] = {}
    for index, rule in enumerate(data.get("rules", [])):
        path = f"rules[{index}]"
        if not isinstance(rule, dict) or not isinstance(rule.get("id"), str):
            fail(f"{path} must have an id")
        if rule["id"] in rule_ids:
            fail(f"duplicate rule id: {rule['id']}")
        rule_ids.add(rule["id"])
        if rule.get("activation") not in ACTIVATIONS:
            fail(f"{path}.activation is unknown")
        unknown_sources = set(non_empty_strings(rule.get("sourceIds"), f"{path}.sourceIds")) - sources
        if unknown_sources:
            fail(f"{path} references unknown sources: {sorted(unknown_sources)}")
        if rule["activation"] != "ACTIVE":
            continue
        target = rule.get("target")
        if not isinstance(target, dict):
            fail(f"{path}.target is required")
        ingredient_ids = non_empty_strings(target.get("ingredientIds"), f"{path}.target.ingredientIds")
        aliases = non_empty_strings(target.get("aliases"), f"{path}.target.aliases")
        e_numbers = non_empty_strings(target.get("eNumbers"), f"{path}.target.eNumbers")
        if not ingredient_ids or not aliases + e_numbers:
            fail(f"{path} needs ingredientIds and textual targets")
        outcomes = rule.get("qualifierOutcomes")
        if not isinstance(outcomes, list) or not outcomes:
            fail(f"{path}.qualifierOutcomes must not be empty")
        for outcome_index, outcome in enumerate(outcomes):
            outcome_path = f"{path}.qualifierOutcomes[{outcome_index}]"
            if not isinstance(outcome, dict) or not outcome.get("id") or not outcome.get("reason"):
                fail(f"{outcome_path} needs id and reason")
            qualifiers = non_empty_strings(outcome.get("qualifiers"), f"{outcome_path}.qualifiers")
            forms = set(non_empty_strings(outcome.get("attachmentForms"), f"{outcome_path}.attachmentForms"))
            if not forms <= ATTACHMENT_FORMS:
                fail(f"{outcome_path} has an unknown attachment form")
            result = (outcome.get("veganStatus"), outcome.get("vegetarianStatus"))
            if result[0] not in VEGAN_STATUSES or result[1] not in VEGETARIAN_STATUSES:
                fail(f"{outcome_path} has an unknown result")
            if result[0] == "VEGAN" and result[1] != "VEGETARIAN_OR_VEGAN":
                fail(f"{outcome_path} has contradictory results")
            for target_text in aliases + e_numbers:
                for qualifier in qualifiers:
                    for form in forms:
                        signature = (target_text.casefold(), qualifier.casefold(), form)
                        previous = signatures.setdefault(signature, result)
                        if previous != result:
                            fail(f"contradictory qualifier: {target_text} / {qualifier} / {form}")

    protected_ids: set[str] = set()
    for index, expression in enumerate(data.get("protectedExpressions", [])):
        path = f"protectedExpressions[{index}]"
        if not isinstance(expression, dict) or not expression.get("id"):
            fail(f"{path} must have an id")
        if expression["id"] in protected_ids:
            fail(f"duplicate protected expression id: {expression['id']}")
        protected_ids.add(expression["id"])
        if expression.get("activation") not in ACTIVATIONS:
            fail(f"{path}.activation is unknown")
        non_empty_strings(expression.get("aliases"), f"{path}.aliases")
        if expression.get("parenthesisPolicy") != "IGNORE_CONTENT_FOR_VERDICT":
            fail(f"{path}.parenthesisPolicy is unknown")

    rendered = SOURCE.read_bytes()
    if "--check" in sys.argv:
        if not OUTPUT.exists() or OUTPUT.read_bytes() != rendered:
            fail("Android origin-rule asset is stale; run: python tools/build_origin_rules.py")
        print(f"Origin rules are valid and {OUTPUT.relative_to(ROOT)} is current.")
        return
    OUTPUT.write_bytes(rendered)
    print(f"Generated {OUTPUT.relative_to(ROOT)} with {len(rule_ids)} rules.")


if __name__ == "__main__":
    main()
