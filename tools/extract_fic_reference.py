#!/usr/bin/env python3
"""Extract the local CELEX 02011R1169 FIC consolidations reproducibly.

The PDFs remain the primary evidence.  This utility only creates UTF-8 review
copies and page-based thematic extracts; it never writes application knowledge.
"""
from __future__ import annotations

import argparse
import hashlib
import re
from collections import defaultdict
from pathlib import Path

from pypdf import PdfReader

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / "reference-input" / "eu-food-labelling" / "00-general-food-labelling"
EXTRACTED = BASE / "extracted"
SECTIONS = BASE / "sections"
MANIFEST = BASE / "SOURCE_MANIFEST.md"
CELEX = "02011R1169"
CONSOLIDATION = "2025-04-01"
# Fixed with this documented import run so --check is deterministic.
EXTRACTION_DATE = "2026-09-30"
LANGUAGES = ("FR", "NL", "EN", "DE")
PDFS = {language: BASE / f"CELEX_{CELEX}-{CONSOLIDATION.replace('-', '')}_{language}_TXT.pdf" for language in LANGUAGES}

THEMES = {
    "allergens": ["annex ii", "annexe ii", "bijlage ii", "anhang ii", "allerg", "allergenen"],
    "ingredient-names": ["article 17", "article 18", "article 19", "article 20", "article 21", "article 22"],
    "compound-ingredients": ["compound ingredient", "ingrédient composé", "samengesteld ingrediënt", "zusammengesetzte zutat"],
    "oils-and-fats": ["vegetable oil", "vegetable fat", "huile végétale", "matière grasse végétale", "plantaardige olie", "plantaardig vet", "pflanzliches öl", "pflanzliches fett"],
    "proteins-and-starches": ["protein", "starch", "protéine", "amidon", "eiwit", "zetmeel", "protein", "stärke"],
    "origin-and-provenance": ["country of origin", "place of provenance", "pays d’origine", "lieu de provenance", "land van oorsprong", "plaats van herkomst", "ursprungsland", "herkunftsort"],
    "flavourings-and-extracts": ["flavouring", "arôme", "aroma"],
    "additives-and-processing-aids": ["additive", "processing aid", "additif", "auxiliaire technologique", "levensmiddelenadditief", "verwerkingshulpstof", "zusatzstoff", "verarbeitungshilfsstoff"],
    "traces-and-contamination": ["may contain", "peut contenir", "kan bevatten", "kann spuren", "cross-contamination", "contamination croisée", "kruisbesmetting", "kreuzkontamination"],
    "product-categories": ["annex vi", "annexe vi", "bijlage vi", "anhang vi", "name of the food", "dénomination de la denrée", "benaming van het levensmiddel", "bezeichnung des lebensmittels"],
    "labelling-rules-for-analysis": ["article 9", "article 13", "article 14", "article 15", "article 16", "article 21"],
}


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def extract_pages(path: Path) -> list[str]:
    if not path.is_file():
        raise ValueError(f"missing local PDF: {path.relative_to(ROOT)}")
    pages = [page.extract_text() or "" for page in PdfReader(path).pages]
    if not pages or any("\ufffd" in page for page in pages):
        raise ValueError(f"lost characters while extracting: {path.relative_to(ROOT)}")
    return pages


def raw_text(language: str, pages: list[str]) -> str:
    blocks = [
        f"CELEX {CELEX} — FIC — {language} — consolidation {CONSOLIDATION}",
        "Derived UTF-8 extraction. The local PDF remains the primary source.",
        "",
    ]
    for index, page in enumerate(pages, start=1):
        blocks.extend((f"===== PAGE {index} =====", page.rstrip(), ""))
    return "\n".join(blocks).rstrip() + "\n"


def matching_pages(pages: list[str], patterns: list[str]) -> list[tuple[int, str]]:
    selected = []
    for number, page in enumerate(pages, start=1):
        folded = page.casefold()
        if any(pattern in folded for pattern in patterns):
            selected.append((number, page))
    return selected


def themed_text(slug: str, all_pages: dict[str, list[str]]) -> str:
    title = slug.replace("-", " ")
    lines = [f"# FIC 1169/2011 — {title}", "", "Extrait automatique par pages. Les PDF locaux restent la source primaire.", ""]
    for language in LANGUAGES:
        pages = matching_pages(all_pages[language], THEMES[slug])
        lines.extend((f"## {language}", ""))
        if not pages:
            lines.extend(("Aucune page sélectionnée automatiquement ; consulter l’extraction complète.", ""))
            continue
        for number, page in pages:
            lines.extend((f"### Page {number}", "", "```text", page.rstrip(), "```", ""))
    return "\n".join(lines).rstrip() + "\n"


def manifest_text(raw_by_language: dict[str, str], pages_by_language: dict[str, list[str]]) -> str:
    lines = [
        "# Manifest — règlement FIC 1169/2011", "",
        "Les quatre PDF locaux sont les sources officielles. Les TXT et extraits ci-dessous sont dérivés, reproductibles et ne remplacent pas les PDF.", "",
        f"- CELEX : `{CELEX}`", f"- Consolidation : `{CONSOLIDATION}`", f"- Date d’extraction : `{EXTRACTION_DATE}`", "- Outil : `pypdf` via `tools/extract_fic_reference.py`", "- Limites : extraction textuelle page par page ; la disposition des tableaux, notes et césures est conservée autant que le permet le PDF mais n’est pas une restitution typographique certifiée.", "",
        "| Langue | PDF | SHA-256 PDF | TXT dérivé | SHA-256 TXT | Pages | Caractères | Statut |", "| --- | --- | --- | --- | --- | ---: | ---: | --- |",
    ]
    for language in LANGUAGES:
        pdf = PDFS[language]
        txt = EXTRACTED / f"CELEX_{CELEX}-{CONSOLIDATION.replace('-', '')}_{language}.txt"
        lines.append(f"| {language} | `{pdf.name}` | `{sha256(pdf)}` | `{txt.relative_to(BASE).as_posix()}` | `{hashlib.sha256(raw_by_language[language].encode('utf-8')).hexdigest()}` | {len(pages_by_language[language])} | {len(raw_by_language[language])} | généré et vérifié |")
    lines.extend(("", "## Extraits thématiques", "", "Les fichiers de `sections/` regroupent les pages candidates par mots-clés, sans interprétation ni import automatique : allergènes, dénominations d’ingrédients, ingrédients composés, huiles et graisses, protéines et amidons, origine et provenance, arômes et extraits, additifs et auxiliaires, traces et contamination, catégories de produits et règles d’étiquetage utiles à l’analyse.", ""))
    return "\n".join(lines)


def expected_files() -> dict[Path, str]:
    pages = {language: extract_pages(path) for language, path in PDFS.items()}
    raw = {language: raw_text(language, language_pages) for language, language_pages in pages.items()}
    output = {EXTRACTED / f"CELEX_{CELEX}-{CONSOLIDATION.replace('-', '')}_{language}.txt": text for language, text in raw.items()}
    output.update({SECTIONS / f"{slug}.md": themed_text(slug, pages) for slug in THEMES})
    output[MANIFEST] = manifest_text(raw, pages)
    return output


def main() -> None:
    parser = argparse.ArgumentParser()
    mode = parser.add_mutually_exclusive_group(required=True)
    mode.add_argument("--check", action="store_true")
    mode.add_argument("--write", action="store_true")
    args = parser.parse_args()
    expected = expected_files()
    stale = [path for path, text in expected.items() if not path.is_file() or path.read_text(encoding="utf-8") != text]
    print(f"CELEX {CELEX}: {len(LANGUAGES)} PDFs, {sum(len(extract_pages(path)) for path in PDFS.values())} pages, {len(THEMES)} thematic extracts, stale={len(stale)}")
    if args.write:
        for path, text in expected.items():
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding="utf-8", newline="\n")
    elif stale:
        raise SystemExit("FIC extracts are stale; run: python tools/extract_fic_reference.py --write")


if __name__ == "__main__":
    main()
