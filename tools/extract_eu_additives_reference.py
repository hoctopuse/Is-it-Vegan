#!/usr/bin/env python3
"""Build a verified multilingual Annex II-B CSV from four EUR-Lex PDFs."""
import argparse
import csv
import json
import os
import re
import sys
import tempfile
from pathlib import Path

from pypdf import PdfReader

# PDF text can contain symbols absent from the Windows legacy console encoding.
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")

HEADERS = ["e_number", "official_name_fr", "official_name_nl", "official_name_en", "official_name_de", "eu_category", "annex_section", "source_celex", "source_consolidation_date", "source_url_fr", "source_url_nl", "source_url_en", "source_url_de", "commission_database_url", "verification_status", "verification_note"]
LANGS = ("FR", "NL", "EN", "DE")
STARTS = {"FR": ("PARTIE B",), "NL": ("DEEL B",), "EN": ("PART B",), "DE": ("TEIL B",)}
ENDS = {"FR": ("PARTIE C",), "NL": ("DEEL C",), "EN": ("PART C",), "DE": ("TEIL C",)}
NUMBER = re.compile(r"^E\s+(\d+[a-z]?(?:\([^)]+\))?)\s+(.+)$")
FR_960B = re.compile(r"^(960b)\s+(.+)$")

def section(text, language):
    starts = [text.find(x) for x in STARTS[language] if text.find(x) >= 0]
    if not starts: raise ValueError(f"Annex II-B start not found: {language}")
    start = min(starts)
    ends = [text.find(x, start + 1) for x in ENDS[language] if text.find(x, start + 1) >= 0]
    if not ends: raise ValueError(f"Annex II-B end not found: {language}")
    return text[start:min(ends)]

def extract(pdf, language):
    text = "\n".join(page.extract_text() or "" for page in PdfReader(pdf).pages)
    rows, key, parts, continuations = {}, None, [], []
    ignored = ("▼", "02008R1333", "E-number", "Numéro E", "Nummer E", "E-nummer", "PART", "PARTIE", "DEEL", "TEIL")
    for raw in section(text, language).splitlines():
        line = raw.strip(); match = NUMBER.match(line); bare = FR_960B.match(line)
        if language == "FR" and bare and key == "E960a":
            continuations.append(bare.group(2)); continue
        if match:
            if key: rows[key] = " ".join(parts)
            key, parts = "E" + match.group(1), [match.group(2)]
        elif key and line and not line.startswith(ignored):
            parts.append(line)
    if key: rows[key] = " ".join(parts)
    return rows, continuations

def build(paths):
    parsed = {lang: extract(paths[lang], lang) for lang in LANGS}
    values = {lang: parsed[lang][0] for lang in LANGS}
    recovery = []
    if parsed["FR"][1] and all("E960b" in values[lang] for lang in ("NL", "EN", "DE")):
        values["FR"]["E960b"] = parsed["FR"][1][0]
        recovery.append("E960b: structural_continuation_recovered")
    keys = sorted(set().union(*(set(values[x]) for x in LANGS)), key=lambda x: (int(re.match(r"E(\d+)", x).group(1)), x))
    records = []
    for key in keys:
        names = [values[lang].get(key, "") for lang in LANGS]
        if key in ("E345", "E345(i)"):
            status, note = "AMBIGUOUS_GROUP_OR_RANGE", "E345 and E345(i) retained as distinct PDF keys; equivalence not demonstrated"
        elif all(names): status, note = "COMPLETE_4_LANGUAGES", ""
        else: status, note = "PARTIAL_OFFICIAL", "Absent from official PDF: " + ",".join(lang for lang, value in zip(LANGS, names) if not value)
        if key == "E960b": note = "structural_continuation_recovered"
        urls = [f"https://eur-lex.europa.eu/legal-content/{lang}/TXT/PDF/?uri=CELEX:02008R1333-20260818" for lang in LANGS]
        records.append(dict(zip(HEADERS, [key, *names, "Annex II Part B", "II-B", "02008R1333", "18.08.2026", *urls, "https://food.ec.europa.eu/food-safety/food-improvement-agents/additives/database_en", status, note])))
    return records, recovery

def validate(records):
    keys = [r["e_number"] for r in records]
    if not records or len(keys) != len(set(keys)): raise ValueError("empty CSV or duplicate E-number")
    return {"records": len(records), "complete": sum(r["verification_status"] == "COMPLETE_4_LANGUAGES" for r in records), "partial_or_ambiguous": sum(r["verification_status"] != "COMPLETE_4_LANGUAGES" for r in records)}

def main():
    base = Path(__file__).resolve().parents[1] / "reference-input" / "eu-food-labelling" / "03-food-additives"
    parser = argparse.ArgumentParser(); parser.add_argument("--fr", type=Path, default=base / "CELEX_02008R1333-20260818_FR_TXT.pdf"); parser.add_argument("--nl", type=Path, default=base / "CELEX_02008R1333-20260818_NL_TXT.pdf"); parser.add_argument("--en", type=Path, default=base / "CELEX_02008R1333-20260818_EN_TXT.pdf"); parser.add_argument("--de", type=Path, default=base / "CELEX_02008R1333-20260818_DE_TXT.pdf"); parser.add_argument("--output", type=Path); parser.add_argument("--dry-run", action="store_true"); args = parser.parse_args()
    if bool(args.output) == bool(args.dry_run): parser.error("choose exactly one of --dry-run or --output")
    os.environ["PYTHONIOENCODING"] = "utf-8"; records, recovery = build({"FR": args.fr, "NL": args.nl, "EN": args.en, "DE": args.de}); summary = validate(records); summary["anomalies"] = recovery + [r["e_number"] + ": " + r["verification_note"] for r in records if r["verification_status"] != "COMPLETE_4_LANGUAGES"]
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        with tempfile.NamedTemporaryFile("w", encoding="utf-8", newline="", dir=args.output.parent, delete=False) as tmp:
            writer = csv.DictWriter(tmp, fieldnames=HEADERS); writer.writeheader(); writer.writerows(records); temp = Path(tmp.name)
        with temp.open(encoding="utf-8", newline="") as source: reread = list(csv.DictReader(source))
        if len(reread) != len(records) or [r["e_number"] for r in reread] != [r["e_number"] for r in records]: temp.unlink(); raise ValueError("temporary CSV validation failed")
        os.replace(temp, args.output); summary["output"] = str(args.output)
    print(json.dumps(summary, ensure_ascii=False))

if __name__ == "__main__":
    try: main()
    except Exception as error: print(f"error: {error}", file=sys.stderr); sys.exit(1)
