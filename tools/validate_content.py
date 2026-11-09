#!/usr/bin/env python3
"""Checks course content before a build: run `python3 tools/validate_content.py` from the project root."""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONTENT = ROOT / "app/src/main/assets/content"
MODEL = ROOT / "core/model/src/main/kotlin/com/vocab/core/model/Content.kt"
COLORS = {"periwinkle", "mint", "butter", "pink", "sky", "lilac"}
MIN_WORDS = 4  # distractors come from the same unit


def main() -> int:
    illustrations = set(re.findall(r'[A-Z_]+\("([a-z-]+)"\)', MODEL.read_text()))
    course = json.loads((CONTENT / "course.json").read_text())
    seen, problems = set(), []
    for level in course["levels"]:
        for path in level["units"]:
            unit = json.loads((CONTENT / path).read_text())
            where = path
            words = unit["words"]
            for item_id in [unit["id"]] + [w["id"] for w in words] + [g["id"] for g in unit.get("gapQuestions", [])]:
                if item_id in seen:
                    problems.append(f"{where}: duplicate id {item_id}")
                seen.add(item_id)
            if unit["illustration"] not in illustrations:
                problems.append(f"{where}: unknown illustration {unit['illustration']}")
            if unit["color"] not in COLORS:
                problems.append(f"{where}: unknown color {unit['color']}")
            if len(words) < MIN_WORDS:
                problems.append(f"{where}: needs at least {MIN_WORDS} words")
            meanings = [w["meaningFa"] for w in words]
            for m in {m for m in meanings if meanings.count(m) > 1}:
                problems.append(f"{where}: duplicate meaning {m}")
            for w in words:
                if not re.search(r"\b" + re.escape(w["text"]) + r"\b", w["example"], re.IGNORECASE):
                    problems.append(f"{where}: example for '{w['text']}' doesn't contain the word")
                if w.get("illustration") and w["illustration"] not in illustrations:
                    problems.append(f"{where}: unknown illustration on '{w['text']}'")
            texts = {w["text"] for w in words}
            for g in unit.get("gapQuestions", []):
                if g["sentence"].count("___") != 1:
                    problems.append(f"{where}: {g['id']} needs exactly one ___")
                if g["answer"] not in texts:
                    problems.append(f"{where}: {g['id']} answer '{g['answer']}' is not a word of the unit")
            print(f"{path}: {len(words)} words, {len(unit.get('gapQuestions', []))} gap questions")
    for p in problems:
        print("✗", p)
    print("OK" if not problems else f"{len(problems)} problem(s)")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
